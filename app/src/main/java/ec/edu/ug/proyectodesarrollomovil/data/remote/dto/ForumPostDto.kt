package ec.edu.ug.proyectodesarrollomovil.data.remote.dto

data class ForumPostDto(
    val id: Int,
    val subject: String,
    val message: String,
    val userFullname: String
)
