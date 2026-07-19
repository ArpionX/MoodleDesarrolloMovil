package ec.edu.ug.proyectodesarrollomovil.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.ui.courses.CoursesActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    private lateinit var googleSignInButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        googleSignInButton = findViewById(R.id.googleSignInButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView)

        googleSignInButton.setOnClickListener { viewModel.onSignInClicked(this) }

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state -> render(state) }
        }
    }

    private fun render(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> {
                loadingProgressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                googleSignInButton.isEnabled = true
            }
            is LoginUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                googleSignInButton.isEnabled = false
            }
            is LoginUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                startActivity(CoursesActivity.newIntent(this))
                finish()
            }
            is LoginUiState.Failed -> {
                loadingProgressBar.visibility = View.GONE
                googleSignInButton.isEnabled = true
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = state.message
            }
        }
    }
}
