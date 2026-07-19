package ec.edu.ug.proyectodesarrollomovil.data.remote

import ec.edu.ug.proyectodesarrollomovil.BuildConfig
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.AssignmentDto
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.CourseDto
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumDiscussionDto
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumPostDto
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ModuleDto
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.MoodleUserDto
import java.net.InetAddress

object MoodleApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * `MOODLE_HOST_IP` es la IP LAN del host donde corre la VM de Moodle. El hostname
     * (`labdigital.com`) solo está mapeado en el archivo hosts de la PC de desarrollo,
     * así que en el celular resolvería por DNS público a otra IP — se fuerza acá para que
     * la request siga mandando el Host header correcto (Apache usa vhosts por nombre).
     */
    private val moodleDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            val overrideIp = BuildConfig.MOODLE_HOST_IP
            return if (hostname.equals("labdigital.com", ignoreCase = true) && overrideIp.isNotBlank()) {
                listOf(InetAddress.getByName(overrideIp))
            } else {
                Dns.SYSTEM.lookup(hostname)
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .dns(moodleDns)
        .addInterceptor(loggingInterceptor)
        .build()

    private val baseUrl = BuildConfig.MOODLE_BASE_URL.let {
        if (it.endsWith("/")) it else "$it/"
    }

    val api: MoodleApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MoodleApi::class.java)

    /**
     * Llama a core_user_get_users_by_field(field=id) y parsea la respuesta a mano:
     * Moodle devuelve un array JSON en éxito, u objeto {exception, errorcode, message} en error.
     */
    suspend fun getUserById(wstoken: String, userId: Int): MoodleUserDto {
        val raw = api.getUserById(wstoken = wstoken, userId = userId.toString()).string().trim()

        if (raw.startsWith("{")) {
            val error = JSONObject(raw)
            throw MoodleApiException(
                errorCode = error.optString("errorcode", "unknown_error"),
                message = error.optString("message", "Error desconocido de Moodle")
            )
        }

        val users = JSONArray(raw)
        if (users.length() == 0) {
            throw MoodleApiException("no_user_found", "No se encontró el usuario Moodle con id=$userId")
        }

        val user = users.getJSONObject(0)
        return MoodleUserDto(
            id = user.optInt("id"),
            email = user.optString("email"),
            fullname = user.optString("fullname")
        )
    }

    /**
     * Llama a core_enrol_get_users_courses y, por cada curso, a core_enrol_get_enrolled_users
     * para resolver el nombre del docente (Moodle no lo devuelve en la primera llamada).
     */
    suspend fun getUserCourses(wstoken: String, userId: Int): List<CourseDto> {
        val raw = api.getUserCourses(wstoken = wstoken, userId = userId).string().trim()

        if (raw.startsWith("{")) {
            val error = JSONObject(raw)
            throw MoodleApiException(
                errorCode = error.optString("errorcode", "unknown_error"),
                message = error.optString("message", "Error desconocido de Moodle")
            )
        }

        val courses = JSONArray(raw)
        return (0 until courses.length()).map { index ->
            val course = courses.getJSONObject(index)
            val courseId = course.optInt("id")
            CourseDto(
                id = courseId,
                fullname = course.optString("fullname"),
                shortname = course.optString("shortname"),
                teacherName = getTeacherName(wstoken, courseId)
            )
        }
    }

    private suspend fun getTeacherName(wstoken: String, courseId: Int): String? {
        val raw = api.getEnrolledUsers(wstoken = wstoken, courseId = courseId).string().trim()

        if (raw.startsWith("{")) {
            return null
        }

        val users = JSONArray(raw)
        for (index in 0 until users.length()) {
            val user = users.getJSONObject(index)
            val roles = user.optJSONArray("roles") ?: continue
            val isTeacher = (0 until roles.length()).any { roleIndex ->
                val shortname = roles.getJSONObject(roleIndex).optString("shortname")
                shortname == "editingteacher" || shortname == "teacher"
            }
            if (isTeacher) {
                return user.optString("fullname")
            }
        }
        return null
    }

    /**
     * Llama a core_course_get_contents y aplana secciones+módulos en una sola lista,
     * denormalizando el nombre de sección en cada módulo (evita un adapter con headers).
     */
    suspend fun getCourseContents(wstoken: String, courseId: Int): List<ModuleDto> {
        val raw = api.getCourseContents(wstoken = wstoken, courseId = courseId).string().trim()

        if (raw.startsWith("{")) {
            val error = JSONObject(raw)
            throw MoodleApiException(
                errorCode = error.optString("errorcode", "unknown_error"),
                message = error.optString("message", "Error desconocido de Moodle")
            )
        }

        val sections = JSONArray(raw)
        val modules = mutableListOf<ModuleDto>()
        for (sectionIndex in 0 until sections.length()) {
            val section = sections.getJSONObject(sectionIndex)
            val sectionName = section.optString("name")
            val sectionModules = section.optJSONArray("modules") ?: continue

            for (moduleIndex in 0 until sectionModules.length()) {
                val module = sectionModules.getJSONObject(moduleIndex)
                modules.add(
                    ModuleDto(
                        id = module.optInt("id"),
                        instanceId = module.optInt("instance"),
                        name = module.optString("name"),
                        modname = module.optString("modname"),
                        sectionName = sectionName,
                        dueDate = module.optJSONArray("dates")?.let { dates ->
                            if (dates.length() > 0) dates.getJSONObject(0).optLong("timestamp") else null
                        }
                    )
                )
            }
        }
        return modules
    }

    /**
     * mod_forum_get_forum_discussions devuelve un objeto {discussions: [...], warnings: [...]}
     * en éxito (a diferencia de core_enrol_get_users_courses, que devuelve el array directo).
     */
    suspend fun getForumDiscussions(wstoken: String, forumId: Int): List<ForumDiscussionDto> {
        val raw = api.getForumDiscussions(wstoken = wstoken, forumId = forumId).string().trim()
        val response = JSONObject(raw)

        if (response.has("exception")) {
            throw MoodleApiException(
                errorCode = response.optString("errorcode", "unknown_error"),
                message = response.optString("message", "Error desconocido de Moodle")
            )
        }

        val discussions = response.optJSONArray("discussions") ?: JSONArray()
        return (0 until discussions.length()).map { index ->
            val discussion = discussions.getJSONObject(index)
            ForumDiscussionDto(
                id = discussion.optInt("id"),
                subject = discussion.optString("subject"),
                userFullname = discussion.optString("userfullname")
            )
        }
    }

    suspend fun getForumDiscussionPosts(wstoken: String, discussionId: Int): List<ForumPostDto> {
        val raw = api.getForumDiscussionPosts(wstoken = wstoken, discussionId = discussionId).string().trim()
        val response = JSONObject(raw)

        if (response.has("exception")) {
            throw MoodleApiException(
                errorCode = response.optString("errorcode", "unknown_error"),
                message = response.optString("message", "Error desconocido de Moodle")
            )
        }

        val posts = response.optJSONArray("posts") ?: JSONArray()
        return (0 until posts.length()).map { index ->
            val post = posts.getJSONObject(index)
            ForumPostDto(
                id = post.optInt("id"),
                subject = post.optString("subject"),
                message = post.optString("message"),
                userFullname = post.optString("userfullname")
            )
        }
    }

    suspend fun addDiscussionPost(wstoken: String, postId: Int, subject: String, message: String) {
        val raw = api.addDiscussionPost(
            wstoken = wstoken,
            postId = postId,
            subject = subject,
            message = message
        ).string().trim()

        if (raw.startsWith("{")) {
            val response = JSONObject(raw)
            if (response.has("exception")) {
                throw MoodleApiException(
                    errorCode = response.optString("errorcode", "unknown_error"),
                    message = response.optString("message", "Error desconocido de Moodle")
                )
            }
        }
    }

    /**
     * mod_assign_get_assignments devuelve {courses: [{id, assignments: [...]}], warnings: []}.
     * Busca dentro de assignments la que coincide con [cmid] (course module id), ya que
     * Moodle no permite pedir una tarea puntual por su cmid directamente.
     */
    suspend fun getAssignment(wstoken: String, courseId: Int, cmid: Int): AssignmentDto {
        val raw = api.getAssignments(wstoken = wstoken, courseId = courseId).string().trim()
        val response = JSONObject(raw)

        if (response.has("exception")) {
            throw MoodleApiException(
                errorCode = response.optString("errorcode", "unknown_error"),
                message = response.optString("message", "Error desconocido de Moodle")
            )
        }

        val courses = response.optJSONArray("courses") ?: JSONArray()
        for (courseIndex in 0 until courses.length()) {
            val assignments = courses.getJSONObject(courseIndex).optJSONArray("assignments") ?: continue
            for (assignmentIndex in 0 until assignments.length()) {
                val assignment = assignments.getJSONObject(assignmentIndex)
                if (assignment.optInt("cmid") == cmid) {
                    return AssignmentDto(
                        id = assignment.optInt("id"),
                        name = assignment.optString("name"),
                        intro = assignment.optString("intro"),
                        dueDate = assignment.optLong("duedate").takeIf { it > 0 }
                    )
                }
            }
        }
        throw MoodleApiException("assignment_not_found", "No se encontró la tarea solicitada")
    }

    suspend fun saveSubmission(wstoken: String, assignmentId: Int, text: String) {
        val raw = api.saveSubmission(wstoken = wstoken, assignmentId = assignmentId, text = text)
            .string().trim()

        if (raw.startsWith("{")) {
            val response = JSONObject(raw)
            if (response.has("exception")) {
                throw MoodleApiException(
                    errorCode = response.optString("errorcode", "unknown_error"),
                    message = response.optString("message", "Error desconocido de Moodle")
                )
            }
        }
    }
}
