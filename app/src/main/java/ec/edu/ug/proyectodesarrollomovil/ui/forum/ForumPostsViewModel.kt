package ec.edu.ug.proyectodesarrollomovil.ui.forum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumPostDto
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthRepository
import ec.edu.ug.proyectodesarrollomovil.data.repository.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForumPostsUiState {
    data object Idle : ForumPostsUiState()
    data object Loading : ForumPostsUiState()
    data class Success(val posts: List<ForumPostDto>) : ForumPostsUiState()
    data class Failed(val message: String) : ForumPostsUiState()
}

sealed class ReplyUiState {
    data object Idle : ReplyUiState()
    data object Sending : ReplyUiState()
    data object Sent : ReplyUiState()
    data class Failed(val message: String) : ReplyUiState()
}

class ForumPostsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val forumRepository = ForumRepository()

    private val _uiState = MutableStateFlow<ForumPostsUiState>(ForumPostsUiState.Idle)
    val uiState: StateFlow<ForumPostsUiState> = _uiState.asStateFlow()

    private val _replyState = MutableStateFlow<ReplyUiState>(ReplyUiState.Idle)
    val replyState: StateFlow<ReplyUiState> = _replyState.asStateFlow()

    private var discussionId: Int = -1

    fun loadPosts(discussionId: Int) {
        this.discussionId = discussionId
        val token = authRepository.getStoredToken()
        if (token == null) {
            _uiState.value = ForumPostsUiState.Failed("Sesión no encontrada, volvé a iniciar sesión")
            return
        }

        _uiState.value = ForumPostsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ForumPostsUiState.Success(
                    forumRepository.getPosts(wstoken = token, discussionId = discussionId)
                )
            } catch (e: Exception) {
                ForumPostsUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }

    /** Responde al primer mensaje (raíz) de la discusión ya cargada. */
    fun replyToDiscussion(subject: String, message: String) {
        val token = authRepository.getStoredToken()
        val rootPostId = (uiState.value as? ForumPostsUiState.Success)?.posts?.firstOrNull()?.id

        if (token == null || rootPostId == null) {
            _replyState.value = ReplyUiState.Failed("No se pudo determinar el mensaje raíz de la discusión")
            return
        }

        _replyState.value = ReplyUiState.Sending
        viewModelScope.launch {
            _replyState.value = try {
                forumRepository.addPost(wstoken = token, postId = rootPostId, subject = subject, message = message)
                loadPosts(discussionId)
                ReplyUiState.Sent
            } catch (e: Exception) {
                ReplyUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }
}
