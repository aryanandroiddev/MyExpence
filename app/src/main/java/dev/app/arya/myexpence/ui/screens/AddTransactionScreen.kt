package dev.app.arya.myexpence.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.app.arya.myexpence.domain.TransactionType
import dev.app.arya.myexpence.ui.state.AddTransactionViewModel
import dev.app.arya.myexpence.ui.state.AppViewModelFactory

@Composable
fun AddTransactionScreen(
    factory: AppViewModelFactory,
    contentPadding: PaddingValues,
    onDone: () -> Unit,
) {
    val vm: AddTransactionViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.padding(contentPadding).padding(16.dp)) {
        Text("Add transaction", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))
        Row {
            RadioButton(selected = state.type == TransactionType.EXPENSE, onClick = { vm.setType(TransactionType.EXPENSE) })
            Text("Expense", modifier = Modifier.padding(top = 12.dp, end = 16.dp))
            RadioButton(selected = state.type == TransactionType.INCOME, onClick = { vm.setType(TransactionType.INCOME) })
            Text("Income", modifier = Modifier.padding(top = 12.dp))
        }

        OutlinedTextField(
            value = state.amount,
            onValueChange = vm::setAmount,
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.merchant,
            onValueChange = vm::setMerchant,
            label = { Text("Merchant/Payee (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = vm::setNotes,
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.save(onDone = onDone) }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
}

