package ec.edu.ug.proyectodesarrollomovil.ui.coursedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.data.remote.dto.ModuleDto
import ec.edu.ug.proyectodesarrollomovil.ui.assignment.AssignmentDetailActivity
import ec.edu.ug.proyectodesarrollomovil.ui.forum.ForumDiscussionsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private val viewModel: CourseDetailViewModel by lazy {
        ViewModelProvider(this)[CourseDetailViewModel::class.java]
    }

    private lateinit var modulesRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val adapter = ModuleAdapter { module -> onModuleClick(module) }
    private var courseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_course_detail)

        modulesRecyclerView = findViewById(R.id.modulesRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView)

        modulesRecyclerView.layoutManager = LinearLayoutManager(this)
        modulesRecyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> render(state) }
        }

        courseId = intent.getIntExtra(EXTRA_COURSE_ID, -1)
        viewModel.loadModules(courseId)
    }

    private fun onModuleClick(module: ModuleDto) {
        when (module.modname) {
            "assign" -> startActivity(
                AssignmentDetailActivity.newIntent(this, courseId = courseId, cmid = module.id)
            )
            "forum" -> startActivity(ForumDiscussionsActivity.newIntent(this, forumId = module.instanceId))
        }
    }

    private fun render(state: CourseDetailUiState) {
        when (state) {
            is CourseDetailUiState.Idle -> Unit
            is CourseDetailUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                modulesRecyclerView.visibility = View.GONE
            }
            is CourseDetailUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                modulesRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.modules)
            }
            is CourseDetailUiState.Failed -> {
                loadingProgressBar.visibility = View.GONE
                modulesRecyclerView.visibility = View.GONE
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = state.message
            }
        }
    }

    companion object {
        private const val EXTRA_COURSE_ID = "extra_course_id"

        fun newIntent(context: Context, courseId: Int): Intent =
            Intent(context, CourseDetailActivity::class.java).apply {
                putExtra(EXTRA_COURSE_ID, courseId)
            }
    }
}
