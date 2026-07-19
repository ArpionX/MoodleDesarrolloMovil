package ec.edu.ug.proyectodesarrollomovil.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import ec.edu.ug.proyectodesarrollomovil.BuildConfig

class GoogleAuthManager(applicationContext: Context) {

    private val credentialManager = CredentialManager.create(applicationContext)

    /**
     * [activityContext] tiene que ser un Context de Activity real (no el de aplicación):
     * Credential Manager necesita poder lanzar la UI del selector de cuentas sobre esa Activity.
     */
    suspend fun signIn(activityContext: Context): GoogleIdTokenCredential {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = try {
            credentialManager.getCredential(request = request, context = activityContext)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException type=${e.type} message=${e.message}", e)
            throw GoogleSignInFailedException("No se pudo completar el inicio de sesión con Google: ${e.type}", e)
        }

        val credential = response.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw GoogleSignInFailedException("La credencial recibida no es un ID de Google válido")
        }

        return try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw GoogleSignInFailedException("No se pudo interpretar el token de Google", e)
        }
    }

    private companion object {
        const val TAG = "GoogleAuthManager"
    }
}

class GoogleSignInFailedException(message: String, cause: Throwable? = null) : Exception(message, cause)
