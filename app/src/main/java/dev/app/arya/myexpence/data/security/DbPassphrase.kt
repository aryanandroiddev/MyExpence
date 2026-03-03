package dev.app.arya.myexpence.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object DbPassphrase {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase_b64"

    fun getOrCreate(context: Context): ByteArray {
        val prefs = securePrefs(context)
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return android.util.Base64.decode(existing, android.util.Base64.DEFAULT)
        }

        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP))
            .apply()
        return bytes
    }

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

