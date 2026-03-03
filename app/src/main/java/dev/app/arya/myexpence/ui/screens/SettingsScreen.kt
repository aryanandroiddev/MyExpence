package dev.app.arya.myexpence.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.app.arya.myexpence.ui.LocalAppContainer
import dev.app.arya.myexpence.ui.state.AppViewModelFactory
import dev.app.arya.myexpence.ui.state.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(factory: AppViewModelFactory, contentPadding: PaddingValues) {
    val vm: SettingsViewModel = viewModel(factory = factory)
    val appLock by vm.appLockEnabled.collectAsState(initial = false)
    val smsImport by vm.smsImportEnabled.collectAsState(initial = false)
    val defaultCurrency by vm.defaultCurrency.collectAsState(initial = "INR")
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importStatus by remember { mutableStateOf<String?>(null) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) importStatus = "SMS permission denied."
        }
    )

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val csvText = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().readText()
                }
                if (csvText == null) {
                    importStatus = "Unable to read selected file."
                    return@launch
                }
                val result = container.csvImporter.importCsv(csvText, defaultCurrency)
                importStatus = "CSV import: parsed=${result.parsed}, imported=${result.imported}, dup=${result.duplicates}"
            }
        }
    )

    Column(modifier = Modifier.padding(contentPadding).padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        SettingToggleRow(
            title = "App lock",
            checked = appLock,
            onCheckedChange = vm::setAppLockEnabled,
        )
        SettingToggleRow(
            title = "SMS import (READ_SMS)",
            checked = smsImport,
            onCheckedChange = vm::setSmsImportEnabled,
        )
        Text(
            "Note: SMS import requires permission and device policy support.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                } else {
                    scope.launch {
                        val result = container.smsImporter.importNew(defaultCurrency)
                        importStatus = "SMS import: parsed=${result.parsed}, imported=${result.imported}, dup=${result.duplicates}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            enabled = smsImport,
        ) {
            Text("Import SMS now")
        }

        Button(
            onClick = { csvPickerLauncher.launch(arrayOf("text/*", "text/csv", "application/csv")) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("Import CSV…")
        }

        if (importStatus != null) {
            Text(importStatus!!, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

