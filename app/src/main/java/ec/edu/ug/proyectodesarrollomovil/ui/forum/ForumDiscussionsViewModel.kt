package ec.edu.ug.proyectodesarrollomovil.ui.forum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumDiscussionDto
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthRepository
import ec.edu.ug.proyectodesarrollomovil.data.repository.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForumDiscussionsUiState {
    data object Idle : ForumDiscussionsUiState()
    data object Loading : ForumDiscussionsUiState()
    data class Success(val discussions: List<ForumDiscussionDto>) : ForumDiscussionsUiState()
    data class Failed(val message: String) : ForumDiscussionsUiState()
}

class ForumDiscussionsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val forumRepository = ForumRepository()

    private val _uiState = MutableStateFlow<ForumDiscussionsUiState>(ForumDiscussionsUiState.Idle)
    val uiState: StateFlow<ForumDiscussionsUiState> = _uiState.asStateFlow()

    fun loadDiscussions(forumId: Int) {
        val token = authRepository.getStoredToken()
        if (token == null) {
            _uiState.value = ForumDiscussionsUiState.Failed("Sesión no encontrada, volvé a iniciar sesión")
            return
        }

        _uiState.value = ForumDiscussionsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ForumDiscussionsUiState.Success(
                    forumRepository.getDiscussions(wstoken = token, forumId = forumId)
                )
            } catch (e: Exception) {
                ForumDiscussionsUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }
}
