package com.studentidphotocapture.app.ui.photogallery

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.data.repository.PhotoRepository
import com.studentidphotocapture.app.ui.photoviewer.PhotoViewerActivity
import kotlinx.coroutines.launch
import java.io.File

class PhotoGalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearchAdmission: EditText
    private lateinit var btnSearch: Button
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private lateinit var tvNoResults: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var photoAdapter: PhotoGalleryAdapter
    private lateinit var studentRepository: StudentRepository
    private lateinit var photoRepository: PhotoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        initViews()
        setupRepositories()
        setupRecyclerView()
        setupClickListeners()
        loadAllCapturedPhotos()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewPhotos)
        etSearchAdmission = findViewById(R.id.etSearchAdmission)
        btnSearch = findViewById(R.id.btnSearch)
        toolbar = findViewById(R.id.toolbar)
        tvNoResults = findViewById(R.id.tvNoResults)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRepositories() {
        val database = AppDatabase.getDatabase(this)
        studentRepository = StudentRepository(
            database.studentDao(),
            com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
        )
        photoRepository = PhotoRepository(database.photoMetadataDao(), this)
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoGalleryAdapter { student ->
            openPhotoViewer(student)
        }
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@PhotoGalleryActivity, 2)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnSearch.setOnClickListener {
            val admissionNumber = etSearchAdmission.text.toString().trim()
            if (admissionNumber.isNotEmpty()) {
                searchByAdmissionNumber(admissionNumber)
            } else {
                Toast.makeText(this, "Please enter admission number", Toast.LENGTH_SHORT).show()
            }
        }

        etSearchAdmission.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val admissionNumber = etSearchAdmission.text.toString().trim()
                if (admissionNumber.isNotEmpty()) {
                    searchByAdmissionNumber(admissionNumber)
                } else {
                    loadAllCapturedPhotos()
                }
                val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etSearchAdmission.windowToken, 0)
                true
            } else {
                false
            }
        }

        etSearchAdmission.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.isNullOrEmpty()) {
                    loadAllCapturedPhotos()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadAllCapturedPhotos() {
        lifecycleScope.launch {
            progressBar.visibility = ProgressBar.VISIBLE
            try {
                val studentsWithPhotos = studentRepository.getStudentsWithCapturedPhotos()
                photoAdapter.updateStudents(studentsWithPhotos)
                
                tvNoResults.visibility = if (studentsWithPhotos.isEmpty()) {
                    TextView.VISIBLE
                    tvNoResults.text = "No captured photos found. Capture photos first!"
                    TextView.VISIBLE
                } else {
                    TextView.GONE
                }
                
                Toast.makeText(this@PhotoGalleryActivity, "Loaded ${studentsWithPhotos.size} photos", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@PhotoGalleryActivity, "Failed to load photos: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun searchByAdmissionNumber(admissionNumber: String) {
        lifecycleScope.launch {
            progressBar.visibility = ProgressBar.VISIBLE
            try {
                val student = studentRepository.getStudentByAdmissionNumber(admissionNumber)
                val studentsWithPhotos = if (student != null) {
                    val photoMetadata = photoRepository.getPhotoByStudentId(student.id)
                    if (photoMetadata != null) {
                        listOf(student)
                    } else {
                        // Check if photo exists locally even if metadata is missing
                        val photoFile = getPhotoFile(student.id)
                        if (photoFile.exists()) listOf(student) else emptyList()
                    }
                } else {
                    emptyList()
                }
                
                photoAdapter.updateStudents(studentsWithPhotos)
                
                tvNoResults.visibility = if (studentsWithPhotos.isEmpty()) {
                    TextView.VISIBLE
                    tvNoResults.text = "No photos found for admission number: $admissionNumber"
                    TextView.VISIBLE
                } else {
                    TextView.GONE
                }
                
                if (studentsWithPhotos.isEmpty()) {
                    Toast.makeText(this@PhotoGalleryActivity, "No photo found for admission number: $admissionNumber", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PhotoGalleryActivity, "Found photo for admission number: $admissionNumber", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PhotoGalleryActivity, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun openPhotoViewer(student: com.studentidphotocapture.app.data.model.Student) {
        val intent = Intent(this, PhotoViewerActivity::class.java).apply {
            putExtra("STUDENT_ID", student.id)
            putExtra("STUDENT_NAME", student.name)
            putExtra("ADMISSION_NUMBER", student.admissionNumber)
            putExtra("CLASS_GRADE", student.classGrade)
            putExtra("SECTION", student.section)
            putExtra("ROLL_NUMBER", student.rollNumber)
            putExtra("SCHOOL_CODE", student.schoolCode)
        }
        startActivity(intent)
    }
    
    private fun getPhotoFile(studentId: String): File {
        val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_PICTURES
        )
        val studentPhotosDir = java.io.File(picturesDir, "StudentIDPhotos")
        return java.io.File(studentPhotosDir, "${studentId}.jpg")
    }
}
