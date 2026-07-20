package ec.edu.ug.proyectodesarrollomovil.ui.forum

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ec.edu.ug.proyectodesarrollomovil.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForumPostsActivity : AppCompatActivity() {

    private val viewModel: ForumPostsViewModel by lazy {
        ViewModelProvider(this)[ForumPostsViewModel::class.java]
    }

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var replySubjectEditText: EditText
    private lateinit var replyMessageEditText: EditText
    private lateinit var replyButton: Button
    private val adapter = PostAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forum_posts)

        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView)
        replySubjectEditText = findViewById(R.id.replySubjectEditText)
        replyMessageEditText = findViewById(R.id.replyMessageEditText)
        replyButton = findViewById(R.id.replyButton)

        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = adapter

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        replyButton.setOnClickListener {
            viewModel.replyToDiscussion(
                subject = replySubjectEditText.text.toString(),
                message = replyMessageEditText.text.toString()
            )
        }

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> render(state) }
        }

        lifecycleScope.launch {
            viewModel.replyState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> renderReply(state) }
        }

        val discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, -1)
        viewModel.loadPosts(discussionId)
    }

    private fun render(state: ForumPostsUiState) {
        when (state) {
            is ForumPostsUiState.Idle -> Unit
            is ForumPostsUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                postsRecyclerView.visibility = View.GONE
            }
            is ForumPostsUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                postsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.posts)
            }
            is ForumPostsUiState.Failed -> {
                loadingProgressBar.visibility = View.GONE
                postsRecyclerView.visibility = View.GONE
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = state.message
            }
        }
    }

    private fun renderReply(state: ReplyUiState) {
        when (state) {
            is ReplyUiState.Idle -> Unit
            is ReplyUiState.Sending -> replyButton.isEnabled = false
            is ReplyUiState.Sent -> {
                replyButton.isEnabled = true
                replySubjectEditText.text.clear()
                replyMessageEditText.text.clear()
                Toast.makeText(this, "Respuesta publicada", Toast.LENGTH_SHORT).show()
            }
            is ReplyUiState.Failed -> {
                replyButton.isEnabled = true
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val EXTRA_DISCUSSION_ID = "extra_discussion_id"

        fun newIntent(context: Context, discussionId: Int): Intent =
            Intent(context, ForumPostsActivity::class.java).apply {
                putExtra(EXTRA_DISCUSSION_ID, discussionId)
            }
    }
}
