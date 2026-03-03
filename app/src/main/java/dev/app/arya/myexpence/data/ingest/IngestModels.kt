package dev.app.arya.myexpence.data.ingest

import dev.app.arya.myexpence.data.db.TransactionTypeDb

data class CandidateTransaction(
    val type: TransactionTypeDb,
    val amountMinor: Long,
    val currency: String,
    val timestampEpochMs: Long,
    val merchant: String?,
    val notes: String?,
    val sourceKey: String,
    val rawJson: String,
)

