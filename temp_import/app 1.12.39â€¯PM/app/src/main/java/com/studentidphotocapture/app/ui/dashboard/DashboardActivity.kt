package com.studentidphotocapture.app.ui.dashboard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalStudents: TextView
    private lateinit var tvCompletedStudents: TextView
    private lateinit var tvPendingStudents: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var listViewStudents: ListView
    private lateinit var spinnerClass: Spinner
    private lateinit var spinnerSection: Spinner
    private lateinit var btnImportCSV: Button

    private var schoolCode = "SCH01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTotalStudents = findViewById(R.id.tvTotalStudents)
        tvCompletedStudents = findViewById(R.id.tvCompletedStudents)
        tvPendingStudents = findViewById(R.id.tvPendingStudents)
        progressBar = findViewById(R.id.progressBar)
        listViewStudents = findViewById(R.id.listViewStudents)
        spinnerClass = findViewById(R.id.spinnerClass)
        spinnerSection = findViewById(R.id.spinnerSection)
        btnImportCSV = findViewById(R.id.btnImportCSV)

        setupSpinners()
        setupStudentListClick()
        setupImportCSVButton()
        loadDashboardData()
    }
    
    private fun setupImportCSVButton() {
        btnImportCSV.setOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.csvimport.CSVImportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSpinners() {
        val classes = listOf("10A", "10B", "11A", "11B", "12A", "12B")
        val sections = listOf("A", "B", "C")

        val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classes)
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClass.adapter = classAdapter

        val sectionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sections)
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSection.adapter = sectionAdapter

        spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadDashboardData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadDashboardData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadDashboardData() {
        val selectedClass = spinnerClass.selectedItem?.toString() ?: "10A"
        val selectedSection = spinnerSection.selectedItem?.toString() ?: "A"

        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@DashboardActivity)
            val studentRepository = StudentRepository(database.studentDao())

            val totalStudents = studentRepository.getTotalStudentsCount(schoolCode, selectedClass, selectedSection)
            val completedStudents = studentRepository.getCompletedStudentsCount(schoolCode, selectedClass, selectedSection)
            val pendingStudents = totalStudents - completedStudents

            tvTotalStudents.text = "Total: $totalStudents"
            tvCompletedStudents.text = "Completed: $completedStudents"
            tvPendingStudents.text = "Pending: $pendingStudents"

            if (totalStudents > 0) {
                val progress = (completedStudents * 100) / totalStudents
                progressBar.progress = progress
            }

            // Load student list
            studentRepository.getStudentsByClassSection(schoolCode, selectedClass, selectedSection)
                .collect { students ->
                    val studentAdapter = ArrayAdapter(
                        this@DashboardActivity,
                        android.R.layout.simple_list_item_1,
                        students.map { "${it.rollNumber} - ${it.name} (${it.photoStatus})" }
                    )
                    listViewStudents.adapter = studentAdapter
                    
                    // Store students for click handler
                    currentStudents = students
                }
        }
    }
    
    private var currentStudents: List<com.studentidphotocapture.app.data.model.Student> = emptyList()
    
    private fun setupStudentListClick() {
        listViewStudents.setOnItemClickListener { _, _, position, _ ->
            val student = currentStudents[position]
            if (student.photoStatus == com.studentidphotocapture.app.data.model.PhotoStatus.CAPTURED || 
                student.photoStatus == com.studentidphotocapture.app.data.model.PhotoStatus.UPLOADED) {
                // Open photo viewer
                val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.photoviewer.PhotoViewerActivity::class.java).apply {
                    putExtra("STUDENT_ID", student.id)
                    putExtra("STUDENT_NAME", student.name)
                    putExtra("LOCAL_PATH", student.photoUrl)
                }
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(this, "No photo captured for this student", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
