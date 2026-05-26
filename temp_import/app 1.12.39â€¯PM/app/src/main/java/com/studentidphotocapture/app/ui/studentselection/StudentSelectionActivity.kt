package com.studentidphotocapture.app.ui.studentselection

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.ui.camera.CameraActivity
import kotlinx.coroutines.launch

class StudentSelectionActivity : AppCompatActivity() {
    
    private lateinit var viewModel: StudentSelectionViewModel
    private lateinit var spinnerSchool: Spinner
    private lateinit var spinnerClass: Spinner
    private lateinit var spinnerSection: Spinner
    private lateinit var listViewStudents: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnImportMore: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_selection)
        
        val database = AppDatabase.getDatabase(this)
        val studentRepository = StudentRepository(database.studentDao())
        viewModel = StudentSelectionViewModel(studentRepository)
        
        spinnerSchool = findViewById(R.id.spinnerSchool)
        spinnerClass = findViewById(R.id.spinnerClass)
        spinnerSection = findViewById(R.id.spinnerSection)
        listViewStudents = findViewById(R.id.listViewStudents)
        progressBar = findViewById(R.id.progressBar)
        btnImportMore = findViewById(R.id.btnImportMore)
        
        setupSpinners()
        setupStudentList()
        setupImportMoreButton()
        observeViewModel()
    }
    
    private fun setupImportMoreButton() {
        btnImportMore.setOnClickListener {
            val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.csvimport.CSVImportActivity::class.java).apply {
                putExtra("USER_ID", intent.getStringExtra("USER_ID"))
                putExtra("USER_ROLE", intent.getStringExtra("USER_ROLE"))
                putExtra("SCHOOL_CODE", intent.getStringExtra("SCHOOL_CODE"))
            }
            startActivity(intent)
        }
    }
    
    private fun setupSpinners() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // School spinner
                val schoolAdapter = ArrayAdapter(
                    this@StudentSelectionActivity,
                    android.R.layout.simple_spinner_item,
                    state.schools
                )
                schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSchool.adapter = schoolAdapter
                
                spinnerSchool.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        viewModel.selectSchool(state.schools[position])
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                
                // Class spinner
                val classAdapter = ArrayAdapter(
                    this@StudentSelectionActivity,
                    android.R.layout.simple_spinner_item,
                    state.classes
                )
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerClass.adapter = classAdapter
                
                spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        viewModel.selectClass(state.classes[position])
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                
                // Section spinner
                val sectionAdapter = ArrayAdapter(
                    this@StudentSelectionActivity,
                    android.R.layout.simple_spinner_item,
                    state.sections
                )
                sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSection.adapter = sectionAdapter
                
                spinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        viewModel.selectSection(state.sections[position])
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }
    
    private fun setupStudentList() {
        listViewStudents.setOnItemClickListener { _, _, position, _ ->
            val state = viewModel.uiState.value
            val student = state.students[position]
            viewModel.selectStudent(student)
            
            val intent = Intent(this, CameraActivity::class.java).apply {
                putExtra("STUDENT_ID", student.id)
                putExtra("STUDENT_NAME", student.name)
                putExtra("ROLL_NUMBER", student.rollNumber)
                putExtra("CLASS", student.classGrade)
                putExtra("SECTION", student.section)
                putExtra("SCHOOL_CODE", student.schoolCode)
            }
            startActivity(intent)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
                
                state.errorMessage?.let {
                    Toast.makeText(this@StudentSelectionActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
                
                // Update student list
                val studentAdapter = ArrayAdapter(
                    this@StudentSelectionActivity,
                    android.R.layout.simple_list_item_1,
                    state.students.map { "${it.rollNumber} - ${it.name} (${it.photoStatus})" }
                )
                listViewStudents.adapter = studentAdapter
            }
        }
    }
}
