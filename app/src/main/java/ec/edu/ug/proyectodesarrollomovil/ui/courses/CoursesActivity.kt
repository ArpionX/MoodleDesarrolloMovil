package ec.edu.ug.proyectodesarrollomovil.ui.courses

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
import com.google.android.material.bottomnavigation.BottomNavigationView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.ui.common.BottomNavTab
import ec.edu.ug.proyectodesarrollomovil.ui.common.setupBottomNav
import ec.edu.ug.proyectodesarrollomovil.ui.coursedetail.CourseDetailActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoursesActivity : AppCompatActivity() {

    private val viewModel: CoursesViewModel by lazy {
        ViewModelProvider(this)[CoursesViewModel::class.java]
    }

    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val adapter = CourseAdapter { course ->
        startActivity(CourseDetailActivity.newIntent(this, courseId = course.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_courses)

        coursesRecyclerView = findViewById(R.id.coursesRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView)

        coursesRecyclerView.layoutManager = LinearLayoutManager(this)
        coursesRecyclerView.adapter = adapter

        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNavigationView), BottomNavTab.COURSES)

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> render(state) }
        }

        viewModel.loadCourses()
    }

    private fun render(state: CoursesUiState) {
        when (state) {
            is CoursesUiState.Idle -> Unit
            is CoursesUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                coursesRecyclerView.visibility = View.GONE
            }
            is CoursesUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                coursesRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.courses)
            }
            is CoursesUiState.Failed -> {
                loadingProgressBar.visibility = View.GONE
                coursesRecyclerView.visibility = View.GONE
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = state.message
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CoursesActivity::class.java)
    }
}
