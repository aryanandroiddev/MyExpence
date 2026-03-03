package dev.app.arya.myexpence.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.app.arya.myexpence.data.repo.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {
    val appLockEnabled = settings.appLockEnabled
    val smsImportEnabled = settings.smsImportEnabled
    val defaultCurrency = settings.defaultCurrency

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setAppLockEnabled(enabled) }
    }

    fun setSmsImportEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setSmsImportEnabled(enabled) }
    }
}

