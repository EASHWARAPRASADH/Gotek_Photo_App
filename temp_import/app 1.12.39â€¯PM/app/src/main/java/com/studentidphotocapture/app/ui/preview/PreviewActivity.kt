package com.studentidphotocapture.app.ui.preview

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.model.PhotoMetadata
import com.studentidphotocapture.app.data.model.Student
import com.studentidphotocapture.app.data.model.UploadStatus
import com.studentidphotocapture.app.data.repository.PhotoRepository
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.util.BitmapCache
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PreviewActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentDetails: TextView
    private lateinit var btnRetake: Button
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private var capturedBitmap: Bitmap? = null
    private var studentId: String = ""
    private var studentName: String = ""
    private var rollNumber: String = ""
    private var classGrade: String = ""
    private var section: String = ""
    private var schoolCode: String = ""
    private var photoWidth: Int = 350
    private var photoHeight: Int = 450

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        ivPreview = findViewById(R.id.ivPreview)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvStudentDetails = findViewById(R.id.tvStudentDetails)
        btnRetake = findViewById(R.id.btnRetake)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: ""
        rollNumber = intent.getStringExtra("ROLL_NUMBER") ?: ""
        classGrade = intent.getStringExtra("CLASS") ?: ""
        section = intent.getStringExtra("SECTION") ?: ""
        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: ""
        photoWidth = intent.getIntExtra("PHOTO_WIDTH", 350)
        photoHeight = intent.getIntExtra("PHOTO_HEIGHT", 450)

        tvStudentName.text = studentName
        tvStudentDetails.text = "Class: $classGrade | Section: $section | Roll: $rollNumber"

        // Load the captured bitmap from camera activity
        loadCapturedPhoto()

        btnRetake.setOnClickListener {
            finish() // Go back to camera
        }

        btnSave.setOnClickListener {
            savePhoto()
        }
    }

    private fun loadCapturedPhoto() {
        capturedBitmap = BitmapCache.getBitmap()
        capturedBitmap?.let {
            ivPreview.setImageBitmap(it)
        } ?: run {
            Toast.makeText(this, "Failed to load photo. Please retake.", Toast.LENGTH_SHORT).show()
            finish() // Go back to camera if photo failed to load
        }
    }

    private fun savePhoto() {
        val bitmap = capturedBitmap
        if (bitmap == null) {
            Toast.makeText(this, "No photo to save", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE
        btnSave.isEnabled = false
        btnRetake.isEnabled = false

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@PreviewActivity)
                val photoRepository = PhotoRepository(database.photoMetadataDao(), this@PreviewActivity)
                val studentRepository = StudentRepository(database.studentDao())

                // Generate filename
                val fileName = photoRepository.generatePhotoFileName(schoolCode, classGrade, rollNumber)

                // Save to local storage
                val photoFile = photoRepository.getPhotoFile(fileName)
                val fos = FileOutputStream(photoFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

                // Save metadata
                val metadata = PhotoMetadata(
                    id = UUID.randomUUID().toString(),
                    fileName = fileName,
                    studentId = studentId,
                    studentName = studentName,
                    classGrade = classGrade,
                    section = section,
                    rollNumber = rollNumber,
                    schoolCode = schoolCode,
                    timestamp = System.currentTimeMillis(),
                    localPath = photoFile.absolutePath,
                    uploadStatus = UploadStatus.PENDING
                )
                photoRepository.savePhotoMetadata(metadata)

                // Update student status
                val student = studentRepository.getStudentById(studentId)
                student?.let {
                    val updatedStudent = it.copy(
                        photoStatus = com.studentidphotocapture.app.data.model.PhotoStatus.CAPTURED,
                        photoCapturedAt = System.currentTimeMillis()
                    )
                    studentRepository.updateStudent(updatedStudent)
                }

                progressBar.visibility = android.view.View.GONE
                btnSave.isEnabled = true
                btnRetake.isEnabled = true

                Toast.makeText(this@PreviewActivity, "Photo saved successfully", Toast.LENGTH_SHORT).show()

                // Navigate back to student selection
                BitmapCache.clear()
                val intent = Intent(this@PreviewActivity, com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                progressBar.visibility = android.view.View.GONE
                btnSave.isEnabled = true
                btnRetake.isEnabled = true
                Toast.makeText(this@PreviewActivity, "Failed to save photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
