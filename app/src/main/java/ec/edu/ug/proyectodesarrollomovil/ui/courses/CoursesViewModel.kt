package ec.edu.ug.proyectodesarrollomovil.ui.courses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.CourseDto
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthRepository
import ec.edu.ug.proyectodesarrollomovil.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CoursesUiState {
    data object Idle : CoursesUiState()
    data object Loading : CoursesUiState()
    data class Success(val courses: List<CourseDto>) : CoursesUiState()
    data class Failed(val message: String) : CoursesUiState()
}

class CoursesViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val courseRepository = CourseRepository()

    private val _uiState = MutableStateFlow<CoursesUiState>(CoursesUiState.Idle)
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    fun loadCourses() {
        val token = authRepository.getStoredToken()
        val userId = authRepository.getStoredUserId()

        if (token == null || userId == null) {
            _uiState.value = CoursesUiState.Failed("Sesión no encontrada, volvé a iniciar sesión")
            return
        }

        _uiState.value = CoursesUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                CoursesUiState.Success(courseRepository.getCourses(wstoken = token, userId = userId))
            } catch (e: Exception) {
                CoursesUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }
}
