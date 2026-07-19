package ec.edu.ug.proyectodesarrollomovil.ui.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.CourseDto

class CourseAdapter(
    private val onCourseClick: (CourseDto) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val courses = mutableListOf<CourseDto>()

    fun submitList(newCourses: List<CourseDto>) {
        courses.clear()
        courses.addAll(newCourses)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position], onCourseClick)
    }

    override fun getItemCount(): Int = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fullNameTextView: TextView = itemView.findViewById(R.id.courseFullNameTextView)
        private val shortNameTextView: TextView = itemView.findViewById(R.id.courseShortNameTextView)
        private val teacherTextView: TextView = itemView.findViewById(R.id.courseTeacherTextView)

        fun bind(course: CourseDto, onCourseClick: (CourseDto) -> Unit) {
            fullNameTextView.text = course.fullname
            shortNameTextView.text = course.shortname
            teacherTextView.text = course.teacherName ?: "Docente no disponible"
            itemView.setOnClickListener { onCourseClick(course) }
        }
    }
}
