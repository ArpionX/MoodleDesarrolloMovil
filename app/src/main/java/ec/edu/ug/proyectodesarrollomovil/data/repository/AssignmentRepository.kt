package ec.edu.ug.proyectodesarrollomovil.data.repository

import ec.edu.ug.proyectodesarrollomovil.data.remote.MoodleApiClient
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.AssignmentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssignmentRepository {

    suspend fun getAssignment(wstoken: String, courseId: Int, cmid: Int): AssignmentDto =
        withContext(Dispatchers.IO) {
            MoodleApiClient.getAssignment(wstoken = wstoken, courseId = courseId, cmid = cmid)
        }

    suspend fun submitText(wstoken: String, assignmentId: Int, text: String) =
        withContext(Dispatchers.IO) {
            MoodleApiClient.saveSubmission(wstoken = wstoken, assignmentId = assignmentId, text = text)
        }
}
