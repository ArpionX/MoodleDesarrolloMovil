package ec.edu.ug.proyectodesarrollomovil.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_MOODLE_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_MOODLE_TOKEN, null)

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_MOODLE_USER_ID, userId).apply()
    }

    fun getUserId(): Int? {
        val userId = prefs.getInt(KEY_MOODLE_USER_ID, -1)
        return if (userId == -1) null else userId
    }

    fun clear() {
        prefs.edit().remove(KEY_MOODLE_TOKEN).remove(KEY_MOODLE_USER_ID).apply()
    }

    private companion object {
        const val PREFS_FILE_NAME = "secure_auth_prefs"
        const val KEY_MOODLE_TOKEN = "moodle_wstoken"
        const val KEY_MOODLE_USER_ID = "moodle_user_id"
    }
}
