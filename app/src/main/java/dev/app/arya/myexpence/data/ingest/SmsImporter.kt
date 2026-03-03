package dev.app.arya.myexpence.data.ingest

import android.content.Context
import android.net.Uri
import dev.app.arya.myexpence.data.db.IngestionStateDao
import dev.app.arya.myexpence.data.db.IngestionStateEntity
import dev.app.arya.myexpence.data.db.TransactionDao
import dev.app.arya.myexpence.data.db.TransactionEntity
import dev.app.arya.myexpence.data.db.TransactionSourceDb
import dev.app.arya.myexpence.data.db.TransactionTypeDb
import java.security.MessageDigest
import java.util.UUID

class SmsImporter(
    private val context: Context,
    private val stateDao: IngestionStateDao,
    private val transactionDao: TransactionDao,
) {
    suspend fun importNew(defaultCurrency: String = "INR", nowEpochMs: Long = System.currentTimeMillis()): ImportResult {
        val last = stateDao.get(KEY_LAST_SMS_EPOCH_MS)?.value?.toLongOrNull() ?: 0L
        val candidates = readSmsSince(last, defaultCurrency)

        var imported = 0
        var duplicates = 0
        for (c in candidates) {
            try {
                transactionDao.insert(
                    TransactionEntity(
                        id = UUID.randomUUID().toString(),
                        type = c.type,
                        amountMinor = c.amountMinor,
                        currency = c.currency,
                        timestampEpochMs = c.timestampEpochMs,
                        categoryId = null,
                        merchant = c.merchant,
                        notes = c.notes,
                        source = TransactionSourceDb.SMS,
                        sourceKey = c.sourceKey,
                        rawSourceJson = c.rawJson,
                        createdAtEpochMs = nowEpochMs,
                        updatedAtEpochMs = nowEpochMs,
                    )
                )
                imported++
            } catch (_: Exception) {
                duplicates++
            }
        }

        val newLast = candidates.maxOfOrNull { it.timestampEpochMs } ?: last
        stateDao.put(IngestionStateEntity(KEY_LAST_SMS_EPOCH_MS, newLast.toString(), nowEpochMs))

        return ImportResult(parsed = candidates.size, imported = imported, duplicates = duplicates)
    }

    private fun readSmsSince(sinceEpochMs: Long, defaultCurrency: String): List<CandidateTransaction> {
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "date", "address", "body")
        val selection = if (sinceEpochMs > 0) "date > ?" else null
        val selectionArgs = if (sinceEpochMs > 0) arrayOf(sinceEpochMs.toString()) else null
        val sortOrder = "date ASC"

        val out = mutableListOf<CandidateTransaction>()
        val cr = context.contentResolver
        cr.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idxDate = cursor.getColumnIndex("date")
            val idxAddr = cursor.getColumnIndex("address")
            val idxBody = cursor.getColumnIndex("body")
            while (cursor.moveToNext()) {
                val ts = cursor.getLong(idxDate)
                val addr = cursor.getString(idxAddr) ?: ""
                val body = cursor.getString(idxBody) ?: ""
                parseSms(addr, body, ts, defaultCurrency)?.let { out.add(it) }
            }
        }
        return out
    }

    private fun parseSms(sender: String, body: String, ts: Long, defaultCurrency: String): CandidateTransaction? {
        val lower = body.lowercase()
        val type = when {
            listOf("credited", "received", "deposit").any { it in lower } -> TransactionTypeDb.INCOME
            listOf("debited", "spent", "purchase", "paid").any { it in lower } -> TransactionTypeDb.EXPENSE
            else -> return null
        }

        val amount = extractAmountMinor(body) ?: return null
        val currency = extractCurrency(body) ?: defaultCurrency
        val merchant = extractMerchant(body)

        val raw = """{"sender":${json(sender)},"body":${json(body)},"timestamp":$ts}"""
        val sourceKey = sha256Hex("$sender|$ts|$amount|$currency|${body.take(80)}")
        return CandidateTransaction(
            type = type,
            amountMinor = amount,
            currency = currency,
            timestampEpochMs = ts,
            merchant = merchant,
            notes = null,
            sourceKey = sourceKey,
            rawJson = raw,
        )
    }

    data class ImportResult(val parsed: Int, val imported: Int, val duplicates: Int)

    companion object {
        private const val KEY_LAST_SMS_EPOCH_MS = "sms_last_epoch_ms"
    }
}

private fun extractCurrency(text: String): String? {
    val lower = text.lowercase()
    return when {
        "inr" in lower || "rs" in lower || "₹" in text -> "INR"
        "usd" in lower || "$" in text -> "USD"
        "eur" in lower || "€" in text -> "EUR"
        else -> null
    }
}

private fun extractAmountMinor(text: String): Long? {
    // Very simple heuristic: find first number with optional decimals after currency tokens.
    val regex = Regex("""(?i)(?:inr|rs\.?|₹|\$|usd|eur|€)\s*([0-9]{1,3}(?:,[0-9]{3})*|[0-9]+)(?:\.([0-9]{1,2}))?""")
    val match = regex.find(text) ?: return null
    val majorStr = match.groupValues[1].replace(",", "")
    val minorStr = match.groupValues.getOrNull(2).orEmpty().padEnd(2, '0').take(2)
    val major = majorStr.toLongOrNull() ?: return null
    val minor = minorStr.toLongOrNull() ?: 0L
    return major * 100 + minor
}

private fun extractMerchant(text: String): String? {
    val regex = Regex("""(?i)(?:at|to)\s+([A-Za-z0-9 &._-]{3,40})""")
    return regex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
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

