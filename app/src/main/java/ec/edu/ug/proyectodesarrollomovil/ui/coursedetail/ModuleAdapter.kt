package ec.edu.ug.proyectodesarrollomovil.ui.coursedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ModuleDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ModuleAdapter(
    private val onModuleClick: (ModuleDto) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private val modules = mutableListOf<ModuleDto>()

    fun submitList(newModules: List<ModuleDto>) {
        modules.clear()
        modules.addAll(newModules)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(modules[position], onModuleClick)
    }

    override fun getItemCount(): Int = modules.size

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.moduleIconImageView)
        private val typeTextView: TextView = itemView.findViewById(R.id.moduleTypeTextView)
        private val nameTextView: TextView = itemView.findViewById(R.id.moduleNameTextView)
        private val sectionTextView: TextView = itemView.findViewById(R.id.moduleSectionTextView)
        private val dueDateIconImageView: ImageView = itemView.findViewById(R.id.dueDateIconImageView)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.moduleDueDateTextView)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(module: ModuleDto, onModuleClick: (ModuleDto) -> Unit) {
            iconImageView.setImageResource(
                if (module.modname == "forum") R.drawable.ic_forum else R.drawable.ic_assignment
            )
            typeTextView.text = module.modname.uppercase(Locale.getDefault())
            nameTextView.text = module.name
            sectionTextView.text = module.sectionName
            val dueDate = module.dueDate
            if (dueDate != null) {
                dueDateIconImageView.visibility = View.VISIBLE
                dueDateTextView.visibility = View.VISIBLE
                dueDateTextView.text = "Fecha límite: ${dateFormat.format(Date(dueDate * 1000))}"
            } else {
                dueDateIconImageView.visibility = View.GONE
                dueDateTextView.visibility = View.GONE
            }
            itemView.setOnClickListener { onModuleClick(module) }
        }
    }
}
