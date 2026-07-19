package ec.edu.ug.proyectodesarrollomovil.ui.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthRepository
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val email: String, val fullName: String) : LoginUiState()
    data class Failed(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** [activityContext] es la Activity que invoca el login; Credential Manager necesita mostrar UI sobre ella. */
    fun onSignInClicked(activityContext: Context) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = authRepository.authenticate(activityContext)) {
                is AuthResult.Success -> LoginUiState.Success(result.email, result.fullName)
                is AuthResult.EmailMismatch -> LoginUiState.Failed(
                    "El correo ${result.googleEmail} no coincide con el usuario Moodle autorizado (${result.moodleEmail})"
                )
                is AuthResult.Error -> LoginUiState.Failed(
                    result.throwable.message ?: "Error desconocido"
                )
            }
        }
    }
}
