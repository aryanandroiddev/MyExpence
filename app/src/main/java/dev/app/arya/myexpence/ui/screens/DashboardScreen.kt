package dev.app.arya.myexpence.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.app.arya.myexpence.ui.state.AppViewModelFactory
import dev.app.arya.myexpence.ui.state.DashboardViewModel

@Composable
fun DashboardScreen(factory: AppViewModelFactory, contentPadding: PaddingValues) {
    val vm: DashboardViewModel = viewModel(factory = factory)
    val summary by vm.summary.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadForCurrentMonth()
    }

    Column(modifier = Modifier.padding(contentPadding).padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
        if (summary == null) {
            Text("Loading…")
            return@Column
        }
        val s = summary!!
        Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("This month", style = MaterialTheme.typography.titleMedium)
                Text("Income: ${formatMinor(s.incomeTotalMinor)} ${s.currency}")
                Text("Expense: ${formatMinor(s.expenseTotalMinor)} ${s.currency}")
                Text("Net: ${formatMinor(s.netMinor)} ${s.currency}")
            }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Top categories", style = MaterialTheme.typography.titleMedium)
                s.expenseByCategory.take(6).forEach { row ->
                    Text("${row.category?.name ?: "Uncategorized"}: ${formatMinor(row.totalMinor)} ${s.currency}")
                }
            }
        }
    }
}

private fun formatMinor(value: Long): String {
    val abs = kotlin.math.abs(value)
    val major = abs / 100
    val minor = abs % 100
    val sign = if (value < 0) "-" else ""
    return "$sign$major.${minor.toString().padStart(2, '0')}"
}

