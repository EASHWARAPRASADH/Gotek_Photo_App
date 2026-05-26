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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

class StudentSelectionActivity : AppCompatActivity() {
    
    private lateinit var viewModel: StudentSelectionViewModel
    private lateinit var spinnerSchool: AutoCompleteTextView
    private lateinit var spinnerClass: AutoCompleteTextView
    private lateinit var spinnerSection: AutoCompleteTextView
    private lateinit var listViewStudents: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnImportMore: Button
    private lateinit var btnViewGallery: Button
    private lateinit var etAdmissionNumber: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSearch: Button
    
    private var lastSchools: List<String>? = null
    private var lastClasses: List<String>? = null
    private var lastSections: List<String>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_selection)
        
        val database = AppDatabase.getDatabase(this)
        val studentRepository = StudentRepository(
            database.studentDao(),
            com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
        )
        viewModel = StudentSelectionViewModel(studentRepository)
        
        spinnerSchool = findViewById(R.id.spinnerSchool)
        spinnerClass = findViewById(R.id.spinnerClass)
        spinnerSection = findViewById(R.id.spinnerSection)
        listViewStudents = findViewById(R.id.listViewStudents)
        progressBar = findViewById(R.id.progressBar)
        btnImportMore = findViewById(R.id.btnImportMore)
        btnViewGallery = findViewById(R.id.btnViewGallery)
        etAdmissionNumber = findViewById(R.id.etAdmissionNumber)
        btnSearch = findViewById(R.id.btnSearch)
        
        val role = intent.getStringExtra("USER_ROLE")
        val mobile = intent.getStringExtra("USER_ID")?.replace("PARENT_", "") // Extract mobile if parent
        val assignedClass = intent.getStringExtra("ASSIGNED_CLASS")
        val assignedSection = intent.getStringExtra("ASSIGNED_SECTION")
        
        viewModel.initMode(role, mobile, assignedClass, assignedSection)
        
        // Hide UI elements not relevant for parents or locked for teachers
        if (role == "PARENT") {
            findViewById<android.view.View>(R.id.filtersCard).visibility = android.view.View.GONE
        }
        
        if (role == "PARENT" || role == "TEACHER") {
            btnImportMore.visibility = android.view.View.GONE
        }
        
        if (role == "PARENT") {
            btnViewGallery.visibility = android.view.View.GONE
        }
        
        if (role == "TEACHER") {
            spinnerSchool.isEnabled = false
            spinnerClass.isEnabled = false
            spinnerSection.isEnabled = false
        }
        
        setupSpinners()
        setupSearch()
        setupStudentList()
        setupImportMoreButton()
        setupGalleryButton()
        
        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val btnLogout: android.widget.ImageButton = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isTaskRoot) {
                    logout()
                } else {
                    finish()
                }
            }
        })

        observeViewModel()
    }

    private fun logout() {
        val intent = Intent(this, com.studentidphotocapture.app.ui.login.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun setupSearch() {
        btnSearch.setOnClickListener {
            performSearch()
        }

        etAdmissionNumber.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        etAdmissionNumber.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isEmpty()) {
                    viewModel.searchStudents("")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun performSearch() {
        val searchText = etAdmissionNumber.text.toString().trim()
        if (searchText.isNotEmpty() && searchText.matches(Regex("^\\d+$"))) {
            viewModel.searchByAdmissionNumber(searchText)
        } else {
            viewModel.searchStudents(searchText)
        }
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etAdmissionNumber.windowToken, 0)
    }

    private fun setupImportMoreButton() {
        btnImportMore.setOnClickListener {
            val intent = Intent(this, com.studentidphotocapture.app.ui.csvimport.CSVImportActivity::class.java).apply {
                putExtra("USER_ID", intent.getStringExtra("USER_ID"))
                putExtra("USER_ROLE", intent.getStringExtra("USER_ROLE"))
                putExtra("SCHOOL_CODE", intent.getStringExtra("SCHOOL_CODE"))
            }
            startActivity(intent)
        }
    }
    
    private fun setupGalleryButton() {
        btnViewGallery.setOnClickListener {
            val intent = Intent(this, com.studentidphotocapture.app.ui.photogallery.PhotoGalleryActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupSpinners() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // School spinner
                if (lastSchools != state.schools) {
                    lastSchools = state.schools
                    val schoolAdapter = ArrayAdapter(
                        this@StudentSelectionActivity,
                        R.layout.item_dropdown,
                        state.schools
                    )
                    spinnerSchool.setAdapter(schoolAdapter)
                    spinnerSchool.setOnItemClickListener { _, _, position, _ ->
                        viewModel.selectSchool(state.schools[position])
                    }
                }
                
                // Class spinner
                if (lastClasses != state.classes) {
                    lastClasses = state.classes
                    val classAdapter = ArrayAdapter(
                        this@StudentSelectionActivity,
                        R.layout.item_dropdown,
                        state.classes
                    )
                    spinnerClass.setAdapter(classAdapter)
                    spinnerClass.setOnItemClickListener { _, _, position, _ ->
                        viewModel.selectClass(state.classes[position])
                    }
                }
                
                // Section spinner
                if (lastSections != state.sections) {
                    lastSections = state.sections
                    val sectionAdapter = ArrayAdapter(
                        this@StudentSelectionActivity,
                        R.layout.item_dropdown,
                        state.sections
                    )
                    spinnerSection.setAdapter(sectionAdapter)
                    spinnerSection.setOnItemClickListener { _, _, position, _ ->
                        viewModel.selectSection(state.sections[position])
                    }
                }

                // Ensure spinners show current selected values immediately
                state.selectedSchool?.let { if (spinnerSchool.text.toString() != it) spinnerSchool.setText(it, false) }
                state.selectedClass?.let { if (spinnerClass.text.toString() != it) spinnerClass.setText(it, false) }
                state.selectedSection?.let { if (spinnerSection.text.toString() != it) spinnerSection.setText(it, false) }
            }
        }
    }
    
    private fun setupStudentList() {
        listViewStudents.setOnItemClickListener { _, _, position, _ ->
            val state = viewModel.uiState.value
            val student = state.students[position]
            viewModel.selectStudent(student)
            
            if (student.photoStatus == com.studentidphotocapture.app.data.model.PhotoStatus.CAPTURED) {
                val options = arrayOf("View Captured Photo", "Retake Photo")
                android.app.AlertDialog.Builder(this)
                    .setTitle("${student.name}'s Photo")
                    .setItems(options) { _, which ->
                        if (which == 0) {
                            val intent = Intent(this, com.studentidphotocapture.app.ui.photoviewer.PhotoViewerActivity::class.java).apply {
                                putExtra("STUDENT_ID", student.id)
                                putExtra("STUDENT_NAME", student.name)
                                putExtra("ADMISSION_NUMBER", student.admissionNumber)
                                putExtra("CLASS_GRADE", student.classGrade)
                                putExtra("SECTION", student.section)
                                putExtra("ROLL_NUMBER", student.rollNumber)
                                putExtra("SCHOOL_CODE", student.schoolCode)
                            }
                            startActivity(intent)
                        } else {
                            openCamera(student)
                        }
                    }
                    .show()
            } else {
                openCamera(student)
            }
        }
    }

    private fun openCamera(student: com.studentidphotocapture.app.data.model.Student) {
        val intent = Intent(this, CameraActivity::class.java).apply {
            putExtra("STUDENT_ID", student.id)
            putExtra("STUDENT_NAME", student.name)
            putExtra("ROLL_NUMBER", student.rollNumber)
            putExtra("ADMISSION_NUMBER", student.admissionNumber)
            putExtra("CLASS", student.classGrade)
            putExtra("SECTION", student.section)
            putExtra("SCHOOL_CODE", student.schoolCode)
            putExtra("USER_ROLE", intent.getStringExtra("USER_ROLE"))
            putExtra("USER_ID", intent.getStringExtra("USER_ID"))
        }
        startActivity(intent)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
                
                state.errorMessage?.let {
                    Toast.makeText(this@StudentSelectionActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
                
                // Update student list using custom layout
                val studentAdapter = object : ArrayAdapter<com.studentidphotocapture.app.data.model.Student>(
                    this@StudentSelectionActivity,
                    R.layout.item_student,
                    state.students
                ) {
                    override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                        val view = convertView ?: layoutInflater.inflate(R.layout.item_student, parent, false)
                        val student = getItem(position)!!
                        
                        val tvName = view.findViewById<TextView>(R.id.tvStudentName)
                        val tvRoll = view.findViewById<TextView>(R.id.tvRollNumber)
                        val tvAdmission = view.findViewById<TextView>(R.id.tvAdmissionNumber)
                        val tvClassSection = view.findViewById<TextView>(R.id.tvClassSection)
                        val tvStatusBadge = view.findViewById<TextView>(R.id.tvStatusBadge)
                        val btnViewPhoto = view.findViewById<TextView>(R.id.btnViewPhoto)
                        val ivCaptureIcon = view.findViewById<ImageView>(R.id.ivCaptureIcon)
                        
                        tvName.text = student.name
                        tvRoll.text = "Roll No: ${student.rollNumber}"
                        tvAdmission.text = "Admission No: ${student.admissionNumber}"
                        tvClassSection?.text = "Class: ${student.classGrade} - ${student.section}"
                        tvStatusBadge.text = student.photoStatus.name
                        
                        ivCaptureIcon.setOnClickListener {
                            openCamera(student)
                        }
                        
                        // Style the badge based on status
                        if (student.photoStatus == com.studentidphotocapture.app.data.model.PhotoStatus.CAPTURED) {
                            btnViewPhoto.visibility = android.view.View.VISIBLE
                            tvStatusBadge.visibility = android.view.View.GONE
                            btnViewPhoto.setOnClickListener {
                                val intent = Intent(this@StudentSelectionActivity, com.studentidphotocapture.app.ui.photoviewer.PhotoViewerActivity::class.java).apply {
                                    putExtra("STUDENT_ID", student.id)
                                    putExtra("STUDENT_NAME", student.name)
                                    putExtra("ADMISSION_NUMBER", student.admissionNumber)
                                    putExtra("CLASS_GRADE", student.classGrade)
                                    putExtra("SECTION", student.section)
                                    putExtra("ROLL_NUMBER", student.rollNumber)
                                    putExtra("SCHOOL_CODE", student.schoolCode)
                                }
                                this@StudentSelectionActivity.startActivity(intent)
                            }
                        } else {
                            btnViewPhoto.visibility = android.view.View.GONE
                            tvStatusBadge.visibility = android.view.View.VISIBLE
                            tvStatusBadge.text = student.photoStatus.name
                            val badgeColor = ContextCompat.getColor(this@StudentSelectionActivity, R.color.brand_pending)
                            tvStatusBadge.background.setTint(badgeColor)
                        }

                        // Bind row click listener directly to view to bypass ListView block
                        view.setOnClickListener {
                            if (student.photoStatus == com.studentidphotocapture.app.data.model.PhotoStatus.CAPTURED) {
                                val options = arrayOf("View Captured Photo", "Retake Photo")
                                android.app.AlertDialog.Builder(this@StudentSelectionActivity)
                                    .setTitle("${student.name}'s Photo")
                                    .setItems(options) { _, which ->
                                        if (which == 0) {
                                            val intent = Intent(this@StudentSelectionActivity, com.studentidphotocapture.app.ui.photoviewer.PhotoViewerActivity::class.java).apply {
                                                putExtra("STUDENT_ID", student.id)
                                                putExtra("STUDENT_NAME", student.name)
                                                putExtra("ADMISSION_NUMBER", student.admissionNumber)
                                                putExtra("CLASS_GRADE", student.classGrade)
                                                putExtra("SECTION", student.section)
                                                putExtra("ROLL_NUMBER", student.rollNumber)
                                                putExtra("SCHOOL_CODE", student.schoolCode)
                                            }
                                            this@StudentSelectionActivity.startActivity(intent)
                                        } else {
                                            openCamera(student)
                                        }
                                    }
                                    .show()
                            } else {
                                openCamera(student)
                            }
                        }
                        
                        return view
                    }
                }
                listViewStudents.adapter = studentAdapter
            }
        }
    }
}
