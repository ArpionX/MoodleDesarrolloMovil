package ec.edu.ug.proyectodesarrollomovil.data.repository

import ec.edu.ug.proyectodesarrollomovil.data.remote.MoodleApiClient
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.CourseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository {

    suspend fun getCourses(wstoken: String, userId: Int): List<CourseDto> =
        withContext(Dispatchers.IO) {
            MoodleApiClient.getUserCourses(wstoken = wstoken, userId = userId)
        }
}
