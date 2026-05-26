package com.studentidphotocapture.app.ui.photoviewer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
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
    private lateinit var tvPhotoPath: TextView
    private lateinit var btnBack: Button
    private lateinit var btnDownload: Button

    private var studentId: String = ""
    private var studentName: String = ""
    private var localPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        ivPhoto = findViewById(R.id.ivPhoto)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvPhotoPath = findViewById(R.id.tvPhotoPath)
        btnBack = findViewById(R.id.btnBack)
        btnDownload = findViewById(R.id.btnDownload)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: ""
        localPath = intent.getStringExtra("LOCAL_PATH") ?: ""

        tvStudentName.text = studentName
        tvPhotoPath.text = localPath

        loadPhoto()

        btnBack.setOnClickListener {
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
                val photoPath = photoMetadata?.localPath ?: localPath
                
                if (photoPath.isNotEmpty()) {
                    val photoFile = File(photoPath)
                    if (photoFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        ivPhoto.setImageBitmap(bitmap)
                        tvPhotoPath.text = photoPath
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
                val photoPath = photoMetadata?.localPath ?: localPath
                
                if (photoPath.isNotEmpty()) {
                    val sourceFile = File(photoPath)
                    if (sourceFile.exists()) {
                        // Download to Downloads folder
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val destFile = File(downloadsDir, photoMetadata?.fileName ?: "student_photo.jpg")
                        
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
