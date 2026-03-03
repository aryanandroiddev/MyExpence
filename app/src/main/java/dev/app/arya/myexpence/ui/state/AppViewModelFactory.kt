package dev.app.arya.myexpence.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.app.arya.myexpence.data.AppContainer

class AppViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.settingsRepository) as T
            modelClass.isAssignableFrom(TransactionsViewModel::class.java) ->
                TransactionsViewModel(container.transactionRepository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(container.analyticsRepository, container.settingsRepository) as T
            modelClass.isAssignableFrom(BudgetsViewModel::class.java) ->
                BudgetsViewModel(container.budgetRepository, container.db.categoryDao()) as T
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) ->
                AddTransactionViewModel(
                    container.transactionRepository,
                    container.db.categoryDao(),
                    container.settingsRepository,
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}

