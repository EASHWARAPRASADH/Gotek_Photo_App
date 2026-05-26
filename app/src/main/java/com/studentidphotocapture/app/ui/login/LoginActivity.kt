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
import com.studentidphotocapture.app.data.model.Student
import com.studentidphotocapture.app.data.model.User
import com.studentidphotocapture.app.data.model.UserRole
import com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity
import com.studentidphotocapture.app.workmanager.PhotoUploadWorker
import com.studentidphotocapture.app.workmanager.StudentDataSyncWorker
import kotlinx.coroutines.launch
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etMobile: EditText
    private lateinit var etOtp: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGetOtp: Button
    private lateinit var institutionFields: android.view.View
    private lateinit var parentFields: android.view.View
    private lateinit var otpLayout: android.view.View
    private lateinit var tabLayout: com.google.android.material.tabs.TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvOtpTimer: android.widget.TextView
    private lateinit var viewModel: LoginViewModel
    
    private var otpTimer: android.os.CountDownTimer? = null
    
    private var isParentMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(this)
        val authRepository = AuthRepository(database.userDao())
        viewModel = LoginViewModel(authRepository)
        
        try {
            setupViews()
            setupTabs()
            setupBackgroundSync()
            initializeData(authRepository)
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "Startup failed", e)
            Toast.makeText(this, "Startup issue: ${e.message}", Toast.LENGTH_LONG).show()
        }
        
        observeViewModel()
    }

    private fun setupViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etMobile = findViewById(R.id.etMobile)
        etOtp = findViewById(R.id.etOtp)
        btnLogin = findViewById(R.id.btnLogin)
        btnGetOtp = findViewById(R.id.btnGetOtp)
        institutionFields = findViewById(R.id.institutionFields)
        parentFields = findViewById(R.id.parentFields)
        otpLayout = findViewById(R.id.otpLayout)
        tabLayout = findViewById(R.id.loginTabLayout)
        progressBar = findViewById(R.id.progressBar)
        tvOtpTimer = findViewById(R.id.tvOtpTimer)

        btnGetOtp.setOnClickListener {
            val mobile = etMobile.text.toString().trim()
            if (mobile.length == 10) {
                startOtpTimer()
                otpLayout.visibility = android.view.View.VISIBLE
                btnGetOtp.isEnabled = false
                Toast.makeText(this, "OTP sent to your mobile", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            }
        }

        // Pre-fill admin credentials for easier testing
        etUsername.setText("admin")
        etPassword.setText("admin123")

        btnLogin.setOnClickListener {
            if (isParentMode) {
                handleParentLogin()
            } else {
                handleInstitutionLogin()
            }
        }
    }

    private fun initializeData(authRepository: AuthRepository) {
        lifecycleScope.launch {
            try {
                android.util.Log.d("LoginActivity", "Creating default admin user...")
                createDefaultUser(authRepository)
                android.util.Log.d("LoginActivity", "Default user created/verified.")
                btnLogin.isEnabled = true
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Database init failed", e)
            }
        }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                isParentMode = tab?.position == 1
                institutionFields.visibility = if (isParentMode) android.view.View.GONE else android.view.View.VISIBLE
                parentFields.visibility = if (isParentMode) android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun handleInstitutionLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.login(username, password)
    }

    private fun handleParentLogin() {
        val mobile = etMobile.text.toString().trim()
        val otp = etOtp.text.toString().trim()
        
        if (mobile.isEmpty() || otp.isEmpty()) {
            Toast.makeText(this, "Please enter mobile number and OTP", Toast.LENGTH_SHORT).show()
            return
        }
        
        // For professional version, we skip password for parents and use phone/OTP
        viewModel.loginWithOtp(mobile, otp)
    }
    
    private suspend fun createDefaultUser(authRepository: AuthRepository) {
            // Default Ultra Super Admin
            authRepository.registerUser(
                User(
                    id = "ultra_admin",
                    username = "ultra_admin",
                    password = "ultra123",
                    role = UserRole.ULTRA_SUPER_ADMIN
                )
            )

            // Default Super Admin for School 01
            authRepository.registerUser(
                User(
                    id = "super_admin_sch01",
                    username = "super_admin_sch01",
                    password = "super123",
                    role = UserRole.SUPER_ADMIN,
                    schoolCode = "Bharathi Vidyalaya HSS"
                )
            )

            // Default Admin (Old role, keeping for backward compatibility)
            authRepository.registerUser(
                User(
                    id = "admin",
                    username = "admin",
                    password = "admin123",
                    role = UserRole.ADMIN,
                    schoolCode = "Bharathi Vidyalaya HSS"
                )
            )
            
            // Sample Teacher 10A
            authRepository.registerUser(
                User(
                    id = "teacher10a",
                    username = "teacher10a",
                    password = "pass123",
                    role = UserRole.TEACHER,
                    schoolCode = "Bharathi Vidyalaya HSS",
                    assignedClass = "10",
                    assignedSection = "A"
                )
            )
            
            // Sample Teacher 11B
            authRepository.registerUser(
                User(
                    id = "teacher11b",
                    username = "teacher11b",
                    password = "pass123",
                    role = UserRole.TEACHER,
                    schoolCode = "Bharathi Vidyalaya HSS",
                    assignedClass = "11",
                    assignedSection = "B"
                )
            )

            // Sample Parent for Testing
            val testParentMobile = "9876543210"
            authRepository.registerUser(
                User(
                    id = "PARENT_$testParentMobile",
                    username = "parent_test",
                    role = UserRole.PARENT,
                    phoneNumber = testParentMobile
                )
            )
        }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
                btnLogin.isEnabled = !state.isLoading
                
                state.errorMessage?.let { msg ->
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
                
                if (state.isLoggedIn) {
                    val userRole = state.user?.role
                    val intent = when (userRole) {
                        UserRole.ULTRA_SUPER_ADMIN -> {
                            Intent(this@LoginActivity, com.studentidphotocapture.app.ui.dashboard.UltraSuperAdminDashboardActivity::class.java)
                        }
                        UserRole.SUPER_ADMIN, UserRole.ADMIN -> {
                            Intent(this@LoginActivity, com.studentidphotocapture.app.ui.dashboard.DashboardActivity::class.java)
                        }
                        else -> {
                            Intent(this@LoginActivity, com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity::class.java)
                        }
                    }
                    
                    intent.apply {
                        putExtra("USER_ID", state.user?.id)
                        putExtra("USER_ROLE", state.user?.role?.name)
                        putExtra("SCHOOL_CODE", state.user?.schoolCode)
                        putExtra("ASSIGNED_CLASS", state.user?.assignedClass)
                        putExtra("ASSIGNED_SECTION", state.user?.assignedSection)
                        putExtra("IS_INITIAL_IMPORT", false) // Simplified for general flow
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun startOtpTimer() {
        otpTimer?.cancel()
        tvOtpTimer.visibility = android.view.View.VISIBLE
        
        otpTimer = object : android.os.CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvOtpTimer.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                tvOtpTimer.visibility = android.view.View.GONE
                btnGetOtp.isEnabled = true
                btnGetOtp.text = "Resend OTP"
            }
        }.start()
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Photo Upload Sync
        val uploadRequest = PeriodicWorkRequestBuilder<PhotoUploadWorker>(
            4, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PhotoUploadSync",
            ExistingPeriodicWorkPolicy.KEEP,
            uploadRequest
        )

        // 2. Student Data Sync
        val dataSyncRequest = PeriodicWorkRequestBuilder<StudentDataSyncWorker>(
            4, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "StudentDataSync",
            ExistingPeriodicWorkPolicy.KEEP,
            dataSyncRequest
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        otpTimer?.cancel()
    }
}
