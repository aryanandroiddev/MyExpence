package dev.app.arya.myexpence.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.app.arya.myexpence.ui.state.AppViewModelFactory
import dev.app.arya.myexpence.ui.state.BudgetsViewModel

@Composable
fun BudgetsScreen(factory: AppViewModelFactory, contentPadding: PaddingValues) {
    val vm: BudgetsViewModel = viewModel(factory = factory)
    val budgets by vm.budgets.collectAsState(initial = emptyList())
    val createError by vm.createError.collectAsState()
    var limit by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(contentPadding).padding(16.dp)) {
        Text("Budgets", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Create overall monthly budget", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Limit amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (createError != null) {
                    Text(createError!!, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = { vm.createOverallMonthlyBudget(limitAmountText = limit, currency = "INR") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Create")
                }
            }
        }

        Text("Existing budgets", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        LazyColumn {
            items(budgets, key = { it.budget.id }) { row ->
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(row.categoryName)
                        Text("Limit: ${formatMinor(row.budget.limitMinor)} ${row.budget.currency}")
                        Text("Thresholds: ${row.budget.thresholdsCsv}")
                    }
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

