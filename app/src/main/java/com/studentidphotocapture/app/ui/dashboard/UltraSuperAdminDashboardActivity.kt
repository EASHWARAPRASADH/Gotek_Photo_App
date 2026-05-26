package com.studentidphotocapture.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UltraSuperAdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvGlobalPercent: TextView
    private lateinit var tvTotalInstitutions: TextView
    private lateinit var tvTotalStudentsText: TextView
    private lateinit var globalProgressBar: LinearProgressIndicator
    private lateinit var rvInstitutions: RecyclerView
    private lateinit var adapter: InstitutionAdapter
    
    private lateinit var studentRepository: StudentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ultra_dashboard)

        tvGlobalPercent = findViewById(R.id.tvGlobalPercent)
        tvTotalInstitutions = findViewById(R.id.tvTotalInstitutions)
        tvTotalStudentsText = findViewById(R.id.tvTotalStudents)
        globalProgressBar = findViewById(R.id.globalProgressBar)
        rvInstitutions = findViewById(R.id.rvInstitutions)

        val database = AppDatabase.getDatabase(this)
        studentRepository = StudentRepository(
            database.studentDao(),
            com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
        )

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.login.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnLogout: android.widget.ImageButton = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.login.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupRecyclerView()
        loadGlobalData()
    }

    private fun setupRecyclerView() {
        adapter = InstitutionAdapter(emptyList()) { schoolCode ->
            // Open the regular dashboard for this specific school
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("SCHOOL_CODE", schoolCode)
            startActivity(intent)
        }
        rvInstitutions.layoutManager = LinearLayoutManager(this)
        rvInstitutions.adapter = adapter
    }

    private fun loadGlobalData() {
        lifecycleScope.launch {
            studentRepository.getAllSchoolCodes().collect { schoolCodes ->
                val summaries = mutableListOf<InstitutionSummary>()
                var totalGlobalStudents = 0
                var totalGlobalCompleted = 0

                for (code in schoolCodes) {
                    val total = studentRepository.getTotalStudentsForSchool(code)
                    val completed = studentRepository.getCompletedStudentsForSchool(code)
                    
                    summaries.add(
                        InstitutionSummary(
                            code = code,
                            name = "Institute $code", // In a real app, you'd fetch names from an Institution table
                            totalStudents = total,
                            completedStudents = completed
                        )
                    )
                    
                    totalGlobalStudents += total
                    totalGlobalCompleted += completed
                }

                adapter.updateData(summaries)
                
                tvTotalInstitutions.text = "Institutions: ${schoolCodes.size}"
                tvTotalStudentsText.text = "Total Students: $totalGlobalStudents"
                
                if (totalGlobalStudents > 0) {
                    val progress = (totalGlobalCompleted * 100) / totalGlobalStudents
                    tvGlobalPercent.text = "$progress%"
                    globalProgressBar.progress = progress
                } else {
                    tvGlobalPercent.text = "0%"
                    globalProgressBar.progress = 0
                }
            }
        }
    }
}
