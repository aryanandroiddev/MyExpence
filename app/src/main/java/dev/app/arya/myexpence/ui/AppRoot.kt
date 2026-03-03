package dev.app.arya.myexpence.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.app.arya.myexpence.ui.nav.AppScaffold
import dev.app.arya.myexpence.ui.state.AppViewModelFactory
import dev.app.arya.myexpence.ui.state.SettingsViewModel

@Composable
fun AppRoot() {
    val container = LocalAppContainer.current
    val factory = remember(container) { AppViewModelFactory(container) }
    val settingsVm: SettingsViewModel = viewModel(factory = factory)
    val lockEnabled by settingsVm.appLockEnabled.collectAsState(initial = false)

    AppLockGate(enabled = lockEnabled) {
        AppScaffold(factory = factory)
    }
}

