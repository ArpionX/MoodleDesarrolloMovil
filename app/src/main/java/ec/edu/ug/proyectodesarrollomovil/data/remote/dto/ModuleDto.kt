package ec.edu.ug.proyectodesarrollomovil.data.remote.dto

data class ModuleDto(
    val id: Int,
    /** id de la instancia (assignment/forum), a usar en las funciones mod_* de Moodle — distinto del [id] (course module id). */
    val instanceId: Int,
    val name: String,
    val modname: String,
    val sectionName: String,
    val dueDate: Long?
)
