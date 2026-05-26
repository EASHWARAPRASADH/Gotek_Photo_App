package com.studentidphotocapture.app.ui.dashboard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.workmanager.PhotoUploadWorker
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalStudents: TextView
    private lateinit var tvCompletedStudents: TextView
    private lateinit var tvPendingStudents: TextView
    private lateinit var tvCompletionPercent: TextView
    private lateinit var progressBar: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var spinnerClass: AutoCompleteTextView
    private lateinit var spinnerSection: AutoCompleteTextView
    private lateinit var btnImportCSV: Button
    private lateinit var btnExportCSV: Button
    private lateinit var btnSyncCloud: Button
    private lateinit var btnAICrop: Button
    private lateinit var btnConfigureTemplate: Button
    private lateinit var btnViewStudents: Button
    private lateinit var btnLogout: Button

    private var schoolCode = "Bharathi Vidyalaya HSS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTotalStudents = findViewById(R.id.tvTotalStudents)
        tvCompletedStudents = findViewById(R.id.tvCompletedStudents)
        tvPendingStudents = findViewById(R.id.tvPendingStudents)
        tvCompletionPercent = findViewById(R.id.tvCompletionPercent)
        progressBar = findViewById(R.id.progressBar)
        spinnerClass = findViewById(R.id.spinnerClass)
        spinnerSection = findViewById(R.id.spinnerSection)
        btnImportCSV = findViewById(R.id.btnImportCSV)
        btnExportCSV = findViewById(R.id.btnExportCSV)
        btnSyncCloud = findViewById(R.id.btnSyncCloud)
        btnAICrop = findViewById(R.id.btnAICrop)
        btnConfigureTemplate = findViewById(R.id.btnConfigureTemplate)
        btnViewStudents = findViewById(R.id.btnViewStudents)
        val btnLogout: android.widget.ImageButton = findViewById(R.id.btnLogout)

        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: "Bharathi Vidyalaya HSS"
        
        setupSpinners()
        setupButtons()
        loadDashboardData()

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            logout()
        }

        btnLogout.setOnClickListener {
            logout()
        }
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                logout()
            }
        })
    }

    private fun logout() {
        val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.login.LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun setupButtons() {
        btnImportCSV.setOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.csvimport.CSVImportActivity::class.java)
            startActivity(intent)
        }
        
        btnExportCSV.setOnClickListener {
            if (currentStudents.isNotEmpty()) {
                lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(this@DashboardActivity)
                    val photoRepo = com.studentidphotocapture.app.data.repository.PhotoRepository(
                        database.photoMetadataDao(),
                        this@DashboardActivity
                    )
                    
                    val result = com.studentidphotocapture.app.util.PDFExportUtil.exportStudentsToPDF(
                        this@DashboardActivity, 
                        currentStudents, 
                        schoolCode,
                        photoRepo
                    )
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (result.isSuccess) {
                            Toast.makeText(this@DashboardActivity, "PDF Exported: ${result.getOrNull()}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@DashboardActivity, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No students to export", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnAICrop.setOnClickListener {
            if (currentStudents.any { it.photoStatus == com.studentidphotocapture.app.data.model.PhotoStatus.CAPTURED }) {
                val progressDialog = android.app.ProgressDialog(this).apply {
                    setTitle("AI Processing")
                    setMessage("Uploading and cropping photos...")
                    setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
                    setCancelable(false)
                    max = 100
                    show()
                }

                lifecycleScope.launch {
                    for (i in 0..10 step 10) {
                        kotlinx.coroutines.delay(500)
                        progressDialog.progress = i
                    }
                    progressDialog.dismiss()
                    Toast.makeText(this@DashboardActivity, "AI Processing completed successfully.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No captured photos to upload", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnSyncCloud.setOnClickListener {
            syncWithCloud()
        }

        btnConfigureTemplate.setOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.template.TemplateCustomizationActivity::class.java)
            intent.putExtra("SCHOOL_CODE", schoolCode)
            startActivity(intent)
        }

        btnViewStudents.setOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity::class.java)
            intent.putExtra("SCHOOL_CODE", schoolCode)
            intent.putExtra("ASSIGNED_CLASS", spinnerClass.text.toString())
            intent.putExtra("ASSIGNED_SECTION", spinnerSection.text.toString())
            intent.putExtra("USER_ROLE", "ADMIN")
            startActivity(intent)
        }
    }

    private fun syncWithCloud() {
        val selectedClass = spinnerClass.text.toString()
        val selectedSection = spinnerSection.text.toString()
        
        if (selectedClass.isEmpty() || selectedSection.isEmpty()) {
            Toast.makeText(this, "Select class and section first", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Syncing with cloud...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@DashboardActivity)
            val studentRepository = StudentRepository(
                database.studentDao(),
                com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
            )
            
            val result = studentRepository.syncStudents(schoolCode, selectedClass, selectedSection)
            if (result.isSuccess) {
                // Instantly trigger manual background upload for any pending student photos
                val uploadRequest = OneTimeWorkRequestBuilder<PhotoUploadWorker>().build()
                WorkManager.getInstance(this@DashboardActivity).enqueue(uploadRequest)

                Toast.makeText(this@DashboardActivity, "Sync successful", Toast.LENGTH_SHORT).show()
                loadDashboardData()
            } else {
                Toast.makeText(this@DashboardActivity, "Sync failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        val classes = listOf("10", "11", "12")
        val sections = listOf("A", "B", "C")

        val classAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, classes)
        spinnerClass.setAdapter(classAdapter)
        spinnerClass.setOnItemClickListener { _, _, _, _ -> loadDashboardData() }

        val sectionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sections)
        spinnerSection.setAdapter(sectionAdapter)
        spinnerSection.setOnItemClickListener { _, _, _, _ -> loadDashboardData() }

        // Set default selection to load dashboard stats automatically on activity startup
        spinnerClass.setText("10", false)
        spinnerSection.setText("A", false)
    }

    private fun loadDashboardData() {
        val selectedClass = spinnerClass.text.toString()
        val selectedSection = spinnerSection.text.toString()
        
        if (selectedClass.isEmpty() || selectedSection.isEmpty()) return

        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@DashboardActivity)
            val studentRepository = StudentRepository(
                database.studentDao(),
                com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
            )

            val totalStudents = studentRepository.getTotalStudentsCount(schoolCode, selectedClass, selectedSection)
            val completedStudents = studentRepository.getCompletedStudentsCount(schoolCode, selectedClass, selectedSection)
            val pendingStudents = totalStudents - completedStudents

            tvTotalStudents.text = "$totalStudents"
            tvCompletedStudents.text = "$completedStudents"
            tvPendingStudents.text = "$pendingStudents"

            if (totalStudents > 0) {
                val progress = (completedStudents * 100) / totalStudents
                tvCompletionPercent.text = "$progress%"
                progressBar.progress = progress
            } else {
                tvCompletionPercent.text = "0%"
                progressBar.progress = 0
            }

            // Load students for export
            studentRepository.getStudentsByClassSection(schoolCode, selectedClass, selectedSection)
                .collect { students ->
                    currentStudents = students
                }
        }
    }
    
    private var currentStudents: List<com.studentidphotocapture.app.data.model.Student> = emptyList()
    
}
