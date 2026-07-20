package ec.edu.ug.proyectodesarrollomovil.ui.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumPostDto

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<ForumPostDto>()

    fun submitList(newPosts: List<ForumPostDto>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorInitialTextView: TextView = itemView.findViewById(R.id.postAuthorInitialTextView)
        private val subjectTextView: TextView = itemView.findViewById(R.id.postSubjectTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.postAuthorTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.postMessageTextView)

        fun bind(post: ForumPostDto) {
            authorInitialTextView.text = post.userFullname.trim().firstOrNull()?.uppercase() ?: "?"
            subjectTextView.text = post.subject
            authorTextView.text = post.userFullname
            messageTextView.text = post.message
        }
    }
}
