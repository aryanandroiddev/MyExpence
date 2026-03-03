package dev.app.arya.myexpence.data.repo

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context,
) {
    private object Keys {
        val appLockEnabled = booleanPreferencesKey("app_lock_enabled")
        val appLockTimeoutMs = longPreferencesKey("app_lock_timeout_ms")
        val smsImportEnabled = booleanPreferencesKey("sms_import_enabled")
        val defaultCurrency = stringPreferencesKey("default_currency")
    }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.appLockEnabled] ?: false }
    val appLockTimeoutMs: Flow<Long> = context.dataStore.data.map { it[Keys.appLockTimeoutMs] ?: 60_000L }
    val smsImportEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.smsImportEnabled] ?: false }
    val defaultCurrency: Flow<String> = context.dataStore.data.map { it[Keys.defaultCurrency] ?: "INR" }

    suspend fun setAppLockEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.appLockEnabled] = enabled }
    suspend fun setAppLockTimeoutMs(value: Long) = context.dataStore.edit { it[Keys.appLockTimeoutMs] = value }
    suspend fun setSmsImportEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.smsImportEnabled] = enabled }
    suspend fun setDefaultCurrency(code: String) = context.dataStore.edit { it[Keys.defaultCurrency] = code }
}

