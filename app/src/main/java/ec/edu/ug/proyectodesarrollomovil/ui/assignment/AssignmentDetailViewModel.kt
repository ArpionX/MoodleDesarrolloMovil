package ec.edu.ug.proyectodesarrollomovil.ui.assignment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.AssignmentDto
import ec.edu.ug.proyectodesarrollomovil.data.repository.AssignmentRepository
import ec.edu.ug.proyectodesarrollomovil.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AssignmentDetailUiState {
    data object Idle : AssignmentDetailUiState()
    data object Loading : AssignmentDetailUiState()
    data class Success(val assignment: AssignmentDto) : AssignmentDetailUiState()
    data class Failed(val message: String) : AssignmentDetailUiState()
}

sealed class SubmissionUiState {
    data object Idle : SubmissionUiState()
    data object Sending : SubmissionUiState()
    data object Sent : SubmissionUiState()
    data class Failed(val message: String) : SubmissionUiState()
}

class AssignmentDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val assignmentRepository = AssignmentRepository()

    private val _uiState = MutableStateFlow<AssignmentDetailUiState>(AssignmentDetailUiState.Idle)
    val uiState: StateFlow<AssignmentDetailUiState> = _uiState.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionUiState>(SubmissionUiState.Idle)
    val submissionState: StateFlow<SubmissionUiState> = _submissionState.asStateFlow()

    fun loadAssignment(courseId: Int, cmid: Int) {
        val token = authRepository.getStoredToken()
        if (token == null) {
            _uiState.value = AssignmentDetailUiState.Failed("Sesión no encontrada, volvé a iniciar sesión")
            return
        }

        _uiState.value = AssignmentDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                AssignmentDetailUiState.Success(
                    assignmentRepository.getAssignment(wstoken = token, courseId = courseId, cmid = cmid)
                )
            } catch (e: Exception) {
                AssignmentDetailUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }

    fun submitText(text: String) {
        val token = authRepository.getStoredToken()
        val assignmentId = (uiState.value as? AssignmentDetailUiState.Success)?.assignment?.id

        if (token == null || assignmentId == null) {
            _submissionState.value = SubmissionUiState.Failed("No se pudo determinar la tarea a enviar")
            return
        }

        _submissionState.value = SubmissionUiState.Sending
        viewModelScope.launch {
            _submissionState.value = try {
                assignmentRepository.submitText(wstoken = token, assignmentId = assignmentId, text = text)
                SubmissionUiState.Sent
            } catch (e: Exception) {
                SubmissionUiState.Failed(e.message ?: "Error desconocido")
            }
        }
    }
}
