package ec.edu.ug.proyectodesarrollomovil.data.remote.dto

data class AssignmentDto(
    val id: Int,
    val name: String,
    val intro: String,
    val dueDate: Long?
)
