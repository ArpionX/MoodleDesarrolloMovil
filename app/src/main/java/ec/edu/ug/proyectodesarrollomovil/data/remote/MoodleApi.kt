package ec.edu.ug.proyectodesarrollomovil.data.remote

import okhttp3.ResponseBody
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Field
import retrofit2.http.POST
import retrofit2.http.Query

interface MoodleApi {

    /**
     * Devuelve el body crudo en vez de un DTO tipado porque Moodle, ante un error
     * (token inválido, usuario inexistente), responde con un objeto JSON de error
     * en lugar del array esperado — parsear manualmente evita que Gson explote.
     */
    @GET("webservice/rest/server.php")
    suspend fun getUserById(
        @Query("wstoken") wstoken: String,
        @Query("field") field: String = "id",
        @Query("values[0]") userId: String,
        @Query("wsfunction") wsFunction: String = "core_user_get_users_by_field",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @GET("webservice/rest/server.php")
    suspend fun getUserCourses(
        @Query("wstoken") wstoken: String,
        @Query("userid") userId: Int,
        @Query("wsfunction") wsFunction: String = "core_enrol_get_users_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @GET("webservice/rest/server.php")
    suspend fun getEnrolledUsers(
        @Query("wstoken") wstoken: String,
        @Query("courseid") courseId: Int,
        @Query("wsfunction") wsFunction: String = "core_enrol_get_enrolled_users",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @GET("webservice/rest/server.php")
    suspend fun getCourseContents(
        @Query("wstoken") wstoken: String,
        @Query("courseid") courseId: Int,
        @Query("wsfunction") wsFunction: String = "core_course_get_contents",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @GET("webservice/rest/server.php")
    suspend fun getForumDiscussions(
        @Query("wstoken") wstoken: String,
        @Query("forumid") forumId: Int,
        @Query("wsfunction") wsFunction: String = "mod_forum_get_forum_discussions",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @GET("webservice/rest/server.php")
    suspend fun getForumDiscussionPosts(
        @Query("wstoken") wstoken: String,
        @Query("discussionid") discussionId: Int,
        @Query("wsfunction") wsFunction: String = "mod_forum_get_discussion_posts",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    /**
     * POST + @FormUrlEncoded en vez de @GET (a diferencia del resto de la API): el mensaje
     * puede ser largo y con caracteres especiales, y Moodle acepta escritura vía POST igual
     * que vía GET, pero evita límites de longitud de URL.
     */
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun addDiscussionPost(
        @Field("wstoken") wstoken: String,
        @Field("postid") postId: Int,
        @Field("subject") subject: String,
        @Field("message") message: String,
        @Field("wsfunction") wsFunction: String = "mod_forum_add_discussion_post",
        @Field("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @GET("webservice/rest/server.php")
    suspend fun getAssignments(
        @Query("wstoken") wstoken: String,
        @Query("courseids[0]") courseId: Int,
        @Query("wsfunction") wsFunction: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): ResponseBody

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun saveSubmission(
        @Field("wstoken") wstoken: String,
        @Field("assignmentid") assignmentId: Int,
        @Field("plugindata[onlinetext_editor][text]") text: String,
        @Field("plugindata[onlinetext_editor][format]") format: Int = 1,
        @Field("plugindata[onlinetext_editor][itemid]") itemId: Int = 0,
        @Field("wsfunction") wsFunction: String = "mod_assign_save_submission",
        @Field("moodlewsrestformat") restFormat: String = "json"
    ): ResponseBody
}

class MoodleApiException(val errorCode: String, message: String) : Exception(message)
