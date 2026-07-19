package ec.edu.ug.proyectodesarrollomovil.ui.assignment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ec.edu.ug.proyectodesarrollomovil.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentDetailActivity : AppCompatActivity() {

    private val viewModel: AssignmentDetailViewModel by lazy {
        ViewModelProvider(this)[AssignmentDetailViewModel::class.java]
    }

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var contentScrollView: ScrollView
    private lateinit var assignmentNameTextView: TextView
    private lateinit var assignmentDueDateTextView: TextView
    private lateinit var assignmentIntroTextView: TextView
    private lateinit var submissionTextEditText: EditText
    private lateinit var submitButton: Button
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assignment_detail)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView)
        contentScrollView = findViewById(R.id.contentScrollView)
        assignmentNameTextView = findViewById(R.id.assignmentNameTextView)
        assignmentDueDateTextView = findViewById(R.id.assignmentDueDateTextView)
        assignmentIntroTextView = findViewById(R.id.assignmentIntroTextView)
        submissionTextEditText = findViewById(R.id.submissionTextEditText)
        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            viewModel.submitText(submissionTextEditText.text.toString())
        }

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> render(state) }
        }

        lifecycleScope.launch {
            viewModel.submissionState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> renderSubmission(state) }
        }

        val courseId = intent.getIntExtra(EXTRA_COURSE_ID, -1)
        val cmid = intent.getIntExtra(EXTRA_CMID, -1)
        viewModel.loadAssignment(courseId = courseId, cmid = cmid)
    }

    private fun render(state: AssignmentDetailUiState) {
        when (state) {
            is AssignmentDetailUiState.Idle -> Unit
            is AssignmentDetailUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                contentScrollView.visibility = View.GONE
            }
            is AssignmentDetailUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                contentScrollView.visibility = View.VISIBLE
                assignmentNameTextView.text = state.assignment.name
                assignmentDueDateTextView.text = state.assignment.dueDate?.let {
                    "Fecha límite: ${dateFormat.format(Date(it * 1000))}"
                } ?: ""
                assignmentIntroTextView.text = state.assignment.intro
            }
            is AssignmentDetailUiState.Failed -> {
                loadingProgressBar.visibility = View.GONE
                contentScrollView.visibility = View.GONE
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = state.message
            }
        }
    }

    private fun renderSubmission(state: SubmissionUiState) {
        when (state) {
            is SubmissionUiState.Idle -> Unit
            is SubmissionUiState.Sending -> submitButton.isEnabled = false
            is SubmissionUiState.Sent -> {
                submitButton.isEnabled = true
                Toast.makeText(this, "Entrega enviada correctamente", Toast.LENGTH_SHORT).show()
            }
            is SubmissionUiState.Failed -> {
                submitButton.isEnabled = true
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val EXTRA_COURSE_ID = "extra_course_id"
        private const val EXTRA_CMID = "extra_cmid"

        fun newIntent(context: Context, courseId: Int, cmid: Int): Intent =
            Intent(context, AssignmentDetailActivity::class.java).apply {
                putExtra(EXTRA_COURSE_ID, courseId)
                putExtra(EXTRA_CMID, cmid)
            }
    }
}
