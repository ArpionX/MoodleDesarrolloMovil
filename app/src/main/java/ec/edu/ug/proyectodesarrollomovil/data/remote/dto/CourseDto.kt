package ec.edu.ug.proyectodesarrollomovil.data.remote.dto

data class CourseDto(
    val id: Int,
    val fullname: String,
    val shortname: String,
    val teacherName: String?
)
