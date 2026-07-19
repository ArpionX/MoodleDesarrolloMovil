package ec.edu.ug.proyectodesarrollomovil.data.repository

import android.content.Context
import android.util.Log
import ec.edu.ug.proyectodesarrollomovil.BuildConfig
import ec.edu.ug.proyectodesarrollomovil.auth.GoogleAuthManager
import ec.edu.ug.proyectodesarrollomovil.auth.GoogleIdTokenParser
import ec.edu.ug.proyectodesarrollomovil.data.local.SecureTokenStorage
import ec.edu.ug.proyectodesarrollomovil.data.remote.MoodleApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val email: String, val fullName: String) : AuthResult()
    data class EmailMismatch(val googleEmail: String, val moodleEmail: String) : AuthResult()
    data class Error(val throwable: Throwable) : AuthResult()
}

class AuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val googleAuthManager = GoogleAuthManager(appContext)
    private val tokenStorage = SecureTokenStorage(appContext)

    /** userId del único usuario Moodle de prueba habilitado (administrador). */
    private val moodleAdminUserId = 2

    /** [activityContext] debe ser la Activity que dispara el login (Credential Manager necesita mostrar UI sobre ella). */
    suspend fun authenticate(activityContext: Context): AuthResult = withContext(Dispatchers.IO) {
        try {
            val credential = googleAuthManager.signIn(activityContext)
            val googleEmail = GoogleIdTokenParser.extractEmail(credential.idToken)

            val moodleUser = MoodleApiClient.getUserById(
                wstoken = BuildConfig.MOODLE_ADMIN_TOKEN,
                userId = moodleAdminUserId
            )

            if (googleEmail.equals(moodleUser.email, ignoreCase = true)) {
                tokenStorage.saveToken(BuildConfig.MOODLE_ADMIN_TOKEN)
                tokenStorage.saveUserId(moodleUser.id)
                AuthResult.Success(email = googleEmail, fullName = moodleUser.fullname)
            } else {
                AuthResult.EmailMismatch(googleEmail = googleEmail, moodleEmail = moodleUser.email)
            }
        } catch (e: Exception) {
            Log.e(TAG, "authenticate() failed: ${e::class.simpleName} - ${e.message}", e)
            AuthResult.Error(e)
        }
    }

    fun getStoredToken(): String? = tokenStorage.getToken()

    fun getStoredUserId(): Int? = tokenStorage.getUserId()

    fun signOut() = tokenStorage.clear()

    private companion object {
        const val TAG = "AuthRepository"
    }
}
