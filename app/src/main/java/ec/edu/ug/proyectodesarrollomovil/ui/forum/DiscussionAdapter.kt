package ec.edu.ug.proyectodesarrollomovil.ui.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ForumDiscussionDto

class DiscussionAdapter(
    private val onDiscussionClick: (ForumDiscussionDto) -> Unit
) : RecyclerView.Adapter<DiscussionAdapter.DiscussionViewHolder>() {

    private val discussions = mutableListOf<ForumDiscussionDto>()

    fun submitList(newDiscussions: List<ForumDiscussionDto>) {
        discussions.clear()
        discussions.addAll(newDiscussions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion, parent, false)
        return DiscussionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscussionViewHolder, position: Int) {
        holder.bind(discussions[position], onDiscussionClick)
    }

    override fun getItemCount(): Int = discussions.size

    class DiscussionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectTextView: TextView = itemView.findViewById(R.id.discussionSubjectTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.discussionAuthorTextView)

        fun bind(discussion: ForumDiscussionDto, onDiscussionClick: (ForumDiscussionDto) -> Unit) {
            subjectTextView.text = discussion.subject
            authorTextView.text = discussion.userFullname
            itemView.setOnClickListener { onDiscussionClick(discussion) }
        }
    }
}
