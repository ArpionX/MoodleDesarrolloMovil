package ec.edu.ug.proyectodesarrollomovil.data.repository

import ec.edu.ug.proyectodesarrollomovil.data.remote.MoodleApiClient
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ModuleDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseContentRepository {

    suspend fun getModules(wstoken: String, courseId: Int): List<ModuleDto> =
        withContext(Dispatchers.IO) {
            MoodleApiClient.getCourseContents(wstoken = wstoken, courseId = courseId)
        }
}
