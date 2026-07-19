package ec.edu.ug.proyectodesarrollomovil.ui.forum

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForumDiscussionsActivity : AppCompatActivity() {

    private val viewModel: ForumDiscussionsViewModel by lazy {
        ViewModelProvider(this)[ForumDiscussionsViewModel::class.java]
    }

    private lateinit var discussionsRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val adapter = DiscussionAdapter { discussion ->
        startActivity(ForumPostsActivity.newIntent(this, discussionId = discussion.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forum_discussions)

        discussionsRecyclerView = findViewById(R.id.discussionsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView)

        discussionsRecyclerView.layoutManager = LinearLayoutManager(this)
        discussionsRecyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> render(state) }
        }

        val forumId = intent.getIntExtra(EXTRA_FORUM_ID, -1)
        viewModel.loadDiscussions(forumId)
    }

    private fun render(state: ForumDiscussionsUiState) {
        when (state) {
            is ForumDiscussionsUiState.Idle -> Unit
            is ForumDiscussionsUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                discussionsRecyclerView.visibility = View.GONE
            }
            is ForumDiscussionsUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                discussionsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.discussions)
            }
            is ForumDiscussionsUiState.Failed -> {
                loadingProgressBar.visibility = View.GONE
                discussionsRecyclerView.visibility = View.GONE
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = state.message
            }
        }
    }

    companion object {
        private const val EXTRA_FORUM_ID = "extra_forum_id"

        fun newIntent(context: Context, forumId: Int): Intent =
            Intent(context, ForumDiscussionsActivity::class.java).apply {
                putExtra(EXTRA_FORUM_ID, forumId)
            }
    }
}
