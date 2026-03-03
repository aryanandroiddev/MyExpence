package dev.app.arya.myexpence.ui.state

import androidx.lifecycle.ViewModel
import dev.app.arya.myexpence.data.repo.TransactionRepository

class TransactionsViewModel(
    private val repo: TransactionRepository,
) : ViewModel() {
    val transactions = repo.observeAll()
}

