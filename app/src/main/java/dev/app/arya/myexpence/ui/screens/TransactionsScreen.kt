package dev.app.arya.myexpence.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.app.arya.myexpence.domain.Transaction
import dev.app.arya.myexpence.ui.state.AppViewModelFactory
import dev.app.arya.myexpence.ui.state.TransactionsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(factory: AppViewModelFactory, contentPadding: PaddingValues) {
    val vm: TransactionsViewModel = viewModel(factory = factory)
    val list by vm.transactions.collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(contentPadding)) {
        Text(
            "Transactions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
            items(list, key = { it.id }) { tx ->
                TransactionRow(tx = tx)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    val df = rememberDateFormat()
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${tx.type}: ${formatMinor(tx.amountMinor)} ${tx.currency}", style = MaterialTheme.typography.titleMedium)
            Text(tx.merchant ?: tx.category?.name ?: "—")
            Text(df.format(Date(tx.timestampEpochMs)), style = MaterialTheme.typography.bodySmall)
            if (!tx.notes.isNullOrBlank()) Text(tx.notes!!)
        }
    }
}

@Composable
private fun rememberDateFormat(): SimpleDateFormat {
    return androidx.compose.runtime.remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
}

private fun formatMinor(value: Long): String {
    val abs = kotlin.math.abs(value)
    val major = abs / 100
    val minor = abs % 100
    val sign = if (value < 0) "-" else ""
    return "$sign$major.${minor.toString().padStart(2, '0')}"
}

