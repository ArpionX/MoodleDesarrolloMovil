package ec.edu.ug.proyectodesarrollomovil.ui.coursedetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ModuleDto
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthRepository
import ec.edu.ug.proyectodesarrollomovil.data.repository.CourseContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CourseDetailUiState {
    data object Idle : CourseDetailUiState()
    data object Loading : CourseDetailUiState()
    data class Success(val modules: List<ModuleDto>) : CourseDetailUiState()
    data class Failed(val message: String) : CourseDetailUiState()
}

class CourseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val courseContentRepository = CourseContentRepository()

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Idle)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    fun loadModules(courseId: Int) {
        val token = authRepository.getStoredToken()
        if (token == null) {
            _uiState.value = CourseDetailUiState.Failed("Sesión no encontrada, volvé a iniciar sesión")
            return
        }

        _uiState.value = CourseDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                CourseDetailUiState.Success(
                    courseContentRepository.getModules(wstoken = token, courseId = courseId)
                )
            } catch (e: Exception) {
                CourseDetailUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }
}
