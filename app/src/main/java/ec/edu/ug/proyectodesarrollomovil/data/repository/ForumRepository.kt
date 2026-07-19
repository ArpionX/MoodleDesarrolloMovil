package ec.edu.ug.proyectodesarrollomovil.data.repository

import ec.edu.ug.proyectodesarrollomovil.data.remote.MoodleApiClient
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumDiscussionDto
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumPostDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ForumRepository {

    suspend fun getDiscussions(wstoken: String, forumId: Int): List<ForumDiscussionDto> =
        withContext(Dispatchers.IO) {
            MoodleApiClient.getForumDiscussions(wstoken = wstoken, forumId = forumId)
        }

    suspend fun getPosts(wstoken: String, discussionId: Int): List<ForumPostDto> =
        withContext(Dispatchers.IO) {
            MoodleApiClient.getForumDiscussionPosts(wstoken = wstoken, discussionId = discussionId)
        }

    suspend fun addPost(wstoken: String, postId: Int, subject: String, message: String) =
        withContext(Dispatchers.IO) {
            MoodleApiClient.addDiscussionPost(wstoken = wstoken, postId = postId, subject = subject, message = message)
        }
}
