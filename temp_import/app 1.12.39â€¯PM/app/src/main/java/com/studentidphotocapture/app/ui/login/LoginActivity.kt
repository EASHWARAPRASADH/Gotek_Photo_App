package com.studentidphotocapture.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.AuthRepository
import com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var viewModel: LoginViewModel
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(this)
        val authRepository = AuthRepository(database.userDao())
        viewModel = LoginViewModel(authRepository)
        
        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        
        // Create default admin user if not exists before enabling login
        lifecycleScope.launch {
            createDefaultUser(authRepository)
            btnLogin.isEnabled = true
        }
        
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.login(username, password)
        }
        
        observeViewModel()
    }
    
    private suspend fun createDefaultUser(authRepository: AuthRepository) {
        val existingUser = authRepository.getUserById("admin")
        if (existingUser == null) {
            authRepository.registerUser(
                com.studentidphotocapture.app.data.model.User(
                    id = "admin",
                    username = "admin",
                    password = "admin123",
                    role = com.studentidphotocapture.app.data.model.UserRole.ADMIN,
                    schoolCode = "SCH01"
                )
            )
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
                btnLogin.isEnabled = !state.isLoading
                
                state.errorMessage?.let {
                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
                
                if (state.isLoggedIn) {
                    val intent = Intent(this@LoginActivity, com.studentidphotocapture.app.ui.csvimport.CSVImportActivity::class.java).apply {
                        putExtra("USER_ID", state.user?.id)
                        putExtra("USER_ROLE", state.user?.role?.name)
                        putExtra("SCHOOL_CODE", state.user?.schoolCode)
                        putExtra("IS_INITIAL_IMPORT", true)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
