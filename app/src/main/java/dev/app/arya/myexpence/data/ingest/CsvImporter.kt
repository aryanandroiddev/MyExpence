package dev.app.arya.myexpence.data.ingest

import dev.app.arya.myexpence.data.db.IngestionStateDao
import dev.app.arya.myexpence.data.db.IngestionStateEntity
import dev.app.arya.myexpence.data.db.TransactionDao
import dev.app.arya.myexpence.data.db.TransactionEntity
import dev.app.arya.myexpence.data.db.TransactionSourceDb
import dev.app.arya.myexpence.data.db.TransactionTypeDb
import java.security.MessageDigest
import java.util.UUID

class CsvImporter(
    private val stateDao: IngestionStateDao,
    private val transactionDao: TransactionDao,
) {
    /**
     * Minimal Phase 1 importer:
     * - assumes CSV has headers and at least: date, description/narration, amount (or debit/credit)
     * - date must be epoch ms or ISO-8601 instant; otherwise row is skipped
     */
    suspend fun importCsv(
        csvText: String,
        defaultCurrency: String = "INR",
        nowEpochMs: Long = System.currentTimeMillis(),
    ): ImportResult {
        val rows = CsvParser.parse(csvText)
        if (rows.isEmpty()) return ImportResult(0, 0, 0)

        val header = rows.first()
        val dataRows = rows.drop(1)
        val idxDate = header.indexOfFirst { it.equals("date", true) }
        val idxDesc = header.indexOfFirst { it.equals("description", true) || it.equals("narration", true) }
        val idxAmount = header.indexOfFirst { it.equals("amount", true) }
        val idxDebit = header.indexOfFirst { it.equals("debit", true) }
        val idxCredit = header.indexOfFirst { it.equals("credit", true) }

        var parsed = 0
        var imported = 0
        var duplicates = 0

        for (row in dataRows) {
            val candidate = parseRow(row, idxDate, idxDesc, idxAmount, idxDebit, idxCredit, defaultCurrency)
                ?: continue
            parsed++
            try {
                transactionDao.insert(
                    TransactionEntity(
                        id = UUID.randomUUID().toString(),
                        type = candidate.type,
                        amountMinor = candidate.amountMinor,
                        currency = candidate.currency,
                        timestampEpochMs = candidate.timestampEpochMs,
                        categoryId = null,
                        merchant = candidate.merchant,
                        notes = candidate.notes,
                        source = TransactionSourceDb.CSV,
                        sourceKey = candidate.sourceKey,
                        rawSourceJson = candidate.rawJson,
                        createdAtEpochMs = nowEpochMs,
                        updatedAtEpochMs = nowEpochMs,
                    )
                )
                imported++
            } catch (_: Exception) {
                duplicates++
            }
        }

        stateDao.put(IngestionStateEntity(KEY_LAST_CSV_IMPORT_EPOCH_MS, nowEpochMs.toString(), nowEpochMs))
        return ImportResult(parsed = parsed, imported = imported, duplicates = duplicates)
    }

    private fun parseRow(
        row: List<String>,
        idxDate: Int,
        idxDesc: Int,
        idxAmount: Int,
        idxDebit: Int,
        idxCredit: Int,
        defaultCurrency: String,
    ): CandidateTransaction? {
        if (idxDate < 0) return null
        val ts = parseDateToEpochMs(row.getOrNull(idxDate) ?: return null) ?: return null
        val desc = row.getOrNull(idxDesc).orEmpty().trim().ifBlank { null }

        val amountMinor: Long
        val type: TransactionTypeDb
        if (idxAmount >= 0) {
            val parsed = parseSignedAmountToMinor(row.getOrNull(idxAmount).orEmpty()) ?: return null
            if (parsed == 0L) return null
            type = if (parsed < 0) TransactionTypeDb.EXPENSE else TransactionTypeDb.INCOME
            amountMinor = kotlin.math.abs(parsed)
        } else {
            val debit = if (idxDebit >= 0) parseUnsignedAmountToMinor(row.getOrNull(idxDebit).orEmpty()) else null
            val credit = if (idxCredit >= 0) parseUnsignedAmountToMinor(row.getOrNull(idxCredit).orEmpty()) else null
            when {
                debit != null && debit > 0 -> {
                    type = TransactionTypeDb.EXPENSE
                    amountMinor = debit
                }
                credit != null && credit > 0 -> {
                    type = TransactionTypeDb.INCOME
                    amountMinor = credit
                }
                else -> return null
            }
        }

        val raw =
            """{"date":${json(row.getOrNull(idxDate).orEmpty())},"description":${json(desc ?: "")},"amountMinor":$amountMinor,"type":"$type"}"""
        val sourceKey = sha256Hex("$ts|$amountMinor|${desc ?: ""}")
        return CandidateTransaction(
            type = type,
            amountMinor = amountMinor,
            currency = defaultCurrency,
            timestampEpochMs = ts,
            merchant = desc,
            notes = null,
            sourceKey = sourceKey,
            rawJson = raw,
        )
    }

    data class ImportResult(val parsed: Int, val imported: Int, val duplicates: Int)

    companion object {
        private const val KEY_LAST_CSV_IMPORT_EPOCH_MS = "csv_last_import_epoch_ms"
    }
}

private object CsvParser {
    fun parse(text: String): List<List<String>> {
        val lines = text.split("\n")
            .map { it.trimEnd('\r') }
            .filter { it.isNotBlank() }
        return lines.map { parseLine(it) }
    }

    private fun parseLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when (c) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',' -> {
                    if (inQuotes) sb.append(c) else {
                        out.add(sb.toString())
                        sb.clear()
                    }
                }
                else -> sb.append(c)
            }
            i++
        }
        out.add(sb.toString())
        return out.map { it.trim() }
    }
}

private fun parseUnsignedAmountToMinor(text: String): Long? {
    val cleaned = text.trim().replace(",", "")
    if (cleaned.isBlank()) return null
    val value = cleaned.toBigDecimalOrNull() ?: return null
    val minor = value.movePointRight(2)
    return try {
        minor.toLong()
    } catch (_: ArithmeticException) {
        null
    }
}

private fun parseSignedAmountToMinor(text: String): Long? {
    val cleaned = text.trim().replace(",", "")
    if (cleaned.isBlank()) return null
    val value = cleaned.toBigDecimalOrNull() ?: return null
    val minor = value.movePointRight(2)
    return try {
        minor.toLong()
    } catch (_: ArithmeticException) {
        null
    }
}

private fun parseDateToEpochMs(text: String): Long? {
    val t = text.trim()
    if (t.isBlank()) return null
    t.toLongOrNull()?.let { return it }
    return try {
        java.time.Instant.parse(t).toEpochMilli()
    } catch (_: Exception) {
        null
    }
}

private fun sha256Hex(text: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(text.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun json(value: String): String =
    "\"" + value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r") + "\""

