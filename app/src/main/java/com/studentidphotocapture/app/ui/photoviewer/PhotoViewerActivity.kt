package com.studentidphotocapture.app.ui.photoviewer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.PhotoRepository
import kotlinx.coroutines.launch
import java.io.File

class PhotoViewerActivity : AppCompatActivity() {

    private lateinit var ivPhoto: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvAdmissionNumber: TextView
    private lateinit var tvClassSection: TextView
    private lateinit var tvRollNumber: TextView
    private lateinit var tvSchoolCode: TextView
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private lateinit var btnDownload: Button
    private lateinit var progressBar: ProgressBar

    private var studentId: String = ""
    private var studentName: String = ""
    private var admissionNumber: String = ""
    private var classGrade: String = ""
    private var section: String = ""
    private var rollNumber: String = ""
    private var schoolCode: String = ""
    private var localPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        ivPhoto = findViewById(R.id.ivPhoto)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvAdmissionNumber = findViewById(R.id.tvAdmissionNumber)
        tvClassSection = findViewById(R.id.tvClassSection)
        tvRollNumber = findViewById(R.id.tvRollNumber)
        tvSchoolCode = findViewById(R.id.tvSchoolCode)
        toolbar = findViewById(R.id.toolbar)
        btnDownload = findViewById(R.id.btnDownload)
        progressBar = findViewById(R.id.progressBar)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: ""
        admissionNumber = intent.getStringExtra("ADMISSION_NUMBER") ?: ""
        classGrade = intent.getStringExtra("CLASS_GRADE") ?: ""
        section = intent.getStringExtra("SECTION") ?: ""
        rollNumber = intent.getStringExtra("ROLL_NUMBER") ?: ""
        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: ""
        localPath = intent.getStringExtra("LOCAL_PATH") ?: ""

        // Display student details
        tvStudentName.text = studentName
        tvAdmissionNumber.text = if (admissionNumber.isNotEmpty()) "Admission No: $admissionNumber" else ""
        tvClassSection.text = if (classGrade.isNotEmpty() && section.isNotEmpty()) "Class: $classGrade - Section: $section" else ""
        tvRollNumber.text = if (rollNumber.isNotEmpty()) "Roll No: $rollNumber" else ""
        tvSchoolCode.text = if (schoolCode.isNotEmpty()) "School: $schoolCode" else ""

        loadPhoto()

        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnDownload.setOnClickListener {
            downloadPhoto()
        }
    }

    private fun loadPhoto() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@PhotoViewerActivity)
                val photoRepository = PhotoRepository(database.photoMetadataDao(), this@PhotoViewerActivity)
                
                val photoMetadata = photoRepository.getPhotoByStudentId(studentId)
                var photoPath = photoMetadata?.localPath ?: localPath
                
                if (photoPath.isEmpty()) {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val studentPhotosDir = File(picturesDir, "StudentIDPhotos")
                    val defaultFile = File(studentPhotosDir, "${studentId}.jpg")
                    if (defaultFile.exists()) {
                        photoPath = defaultFile.absolutePath
                    }
                }
                
                if (photoPath.isNotEmpty()) {
                    val photoFile = File(photoPath)
                    if (photoFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        ivPhoto.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(this@PhotoViewerActivity, "Photo file not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PhotoViewerActivity, "No photo path available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PhotoViewerActivity, "Failed to load photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadPhoto() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@PhotoViewerActivity)
                val photoRepository = PhotoRepository(database.photoMetadataDao(), this@PhotoViewerActivity)
                
                val photoMetadata = photoRepository.getPhotoByStudentId(studentId)
                var photoPath = photoMetadata?.localPath ?: localPath
                
                if (photoPath.isEmpty()) {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val studentPhotosDir = File(picturesDir, "StudentIDPhotos")
                    val defaultFile = File(studentPhotosDir, "${studentId}.jpg")
                    if (defaultFile.exists()) {
                        photoPath = defaultFile.absolutePath
                    }
                }
                
                if (photoPath.isNotEmpty()) {
                    val sourceFile = File(photoPath)
                    if (sourceFile.exists()) {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val fileName = photoMetadata?.fileName ?: "${studentId}.jpg"
                        val destFile = File(downloadsDir, fileName)
                        
                        sourceFile.copyTo(destFile, overwrite = true)
                        
                        Toast.makeText(this@PhotoViewerActivity, "Photo downloaded to Downloads", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PhotoViewerActivity, "Photo file not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PhotoViewerActivity, "No photo path available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PhotoViewerActivity, "Failed to download photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
