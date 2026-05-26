package com.studentidphotocapture.app.ui.preview

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.studentidphotocapture.app.util.TemplateConfigManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PreviewActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var ivBarcode: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentDetails: TextView
    private lateinit var tvCardSchool: TextView
    private lateinit var tvCardRoll: TextView
    private lateinit var tvCardClassSec: TextView
    private lateinit var tvCardAdmNo: TextView
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
        tvCardSchool = findViewById(R.id.tvCardSchool)
        tvCardRoll = findViewById(R.id.tvCardRoll)
        tvCardClassSec = findViewById(R.id.tvCardClassSec)
        tvCardAdmNo = findViewById(R.id.tvCardAdmNo)
        btnRetake = findViewById(R.id.btnRetake)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)
        ivBarcode = findViewById(R.id.ivBarcode)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: ""
        rollNumber = intent.getStringExtra("ROLL_NUMBER") ?: ""
        classGrade = intent.getStringExtra("CLASS") ?: ""
        section = intent.getStringExtra("SECTION") ?: ""
        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: "Bharathi Vidyalaya HSS"
        photoWidth = intent.getIntExtra("PHOTO_WIDTH", 350)
        photoHeight = intent.getIntExtra("PHOTO_HEIGHT", 450)

        tvStudentName.text = studentName
        tvStudentDetails.text = "Class: $classGrade | Section: $section | Roll: $rollNumber"

        val schoolName = when (schoolCode) {
            "Bharathi Vidyalaya HSS" -> "BHARATHI VIDYALAYA HSS"
            "St. Mary's Matriculation" -> "ST. MARY'S MATRICULATION"
            "Tagore Higher Sec. School" -> "TAGORE HIGHER SEC. SCHOOL"
            else -> schoolCode.uppercase().ifEmpty { "STUDENT ID SYSTEM" }
        }

        tvCardSchool.text = schoolName
        tvCardRoll.text = rollNumber
        tvCardClassSec.text = "$classGrade - $section"
        tvCardAdmNo.text = studentId

        // Generate and set dynamic barcode overlay matching studentId
        val barcodeBitmap = com.studentidphotocapture.app.util.BarcodeEncoder.generateCode39(studentId, 400, 80)
        barcodeBitmap?.let {
            ivBarcode.setImageBitmap(it)
        }

        // Load the captured bitmap from camera activity
        loadCapturedPhoto()

        btnRetake.setOnClickListener {
            finish() // Go back to camera
        }

        btnSave.setOnClickListener {
            savePhoto()
        }

        applyTemplateConfiguration()
    }

    private fun applyTemplateConfiguration() {
        val flCardBackground = findViewById<FrameLayout>(R.id.flCardBackground)
        val photoCardView = findViewById<androidx.cardview.widget.CardView>(R.id.photoCardView)
        val llStudentDetails = findViewById<LinearLayout>(R.id.llStudentDetails)

        // 1. Set template background
        val templateFile = TemplateConfigManager.getTemplateFile(this, schoolCode)
        if (templateFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(templateFile.absolutePath)
            if (bitmap != null) {
                flCardBackground.background = BitmapDrawable(resources, bitmap)
            } else {
                flCardBackground.setBackgroundResource(R.drawable.id_card_template_gold)
            }
        } else {
            flCardBackground.setBackgroundResource(R.drawable.id_card_template_gold)
        }

        // 2. Load configs
        val photoShape = TemplateConfigManager.getPhotoShape(this, schoolCode)
        val photoSizeDp = TemplateConfigManager.getPhotoSize(this, schoolCode)
        val photoYDp = TemplateConfigManager.getPhotoY(this, schoolCode)
        val textYDp = TemplateConfigManager.getTextY(this, schoolCode)
        val barcodeYDp = TemplateConfigManager.getBarcodeY(this, schoolCode)
        val textColorHex = TemplateConfigManager.getTextColor(this, schoolCode)

        // Convert dp to px
        val sizePx = TemplateConfigManager.dpToPx(this, photoSizeDp)
        val photoYPx = TemplateConfigManager.dpToPx(this, photoYDp)
        val textYPx = TemplateConfigManager.dpToPx(this, textYDp)
        val barcodeYPx = TemplateConfigManager.dpToPx(this, barcodeYDp)

        // 3. Apply photo frame layout
        val photoParams = photoCardView.layoutParams as RelativeLayout.LayoutParams
        photoParams.width = sizePx
        photoParams.height = sizePx
        photoParams.topMargin = photoYPx
        photoCardView.layoutParams = photoParams

        if (photoShape == "circle") {
            photoCardView.radius = (sizePx / 2).toFloat()
        } else {
            photoCardView.radius = TemplateConfigManager.dpToPx(this, 8).toFloat()
        }

        // 4. Apply text details positioning
        val detailsParams = llStudentDetails.layoutParams as RelativeLayout.LayoutParams
        detailsParams.topMargin = textYPx
        llStudentDetails.layoutParams = detailsParams

        // 5. Apply text details color
        val colorInt = Color.parseColor(textColorHex)
        tvStudentName.setTextColor(colorInt)
        tvCardRoll.setTextColor(colorInt)
        tvCardClassSec.setTextColor(colorInt)
        tvCardAdmNo.setTextColor(colorInt)

        // 6. Apply barcode position
        val barcodeParams = ivBarcode.layoutParams as RelativeLayout.LayoutParams
        barcodeParams.bottomMargin = barcodeYPx
        ivBarcode.layoutParams = barcodeParams
    }

    private fun loadCapturedPhoto() {
        capturedBitmap = BitmapCache.getBitmap()
        BitmapCache.clear() // Clear static cache immediately to prevent memory leaks
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
                val studentRepository = StudentRepository(
                    database.studentDao(),
                    com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
                )

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

                // Trigger real background upload via WorkManager immediately
                val uploadRequest = androidx.work.OneTimeWorkRequestBuilder<com.studentidphotocapture.app.workmanager.PhotoUploadWorker>().build()
                androidx.work.WorkManager.getInstance(this@PreviewActivity).enqueue(uploadRequest)
                
                progressBar.visibility = android.view.View.GONE
                btnSave.isEnabled = true
                btnRetake.isEnabled = true

                Toast.makeText(this@PreviewActivity, "Photo saved! Uploading in background...", Toast.LENGTH_LONG).show()

                // Navigate back to student selection
                BitmapCache.clear()
                val intent = Intent(this@PreviewActivity, com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity::class.java).apply {
                    putExtra("USER_ROLE", this@PreviewActivity.intent.getStringExtra("USER_ROLE"))
                    putExtra("USER_ID", this@PreviewActivity.intent.getStringExtra("USER_ID"))
                    putExtra("SCHOOL_CODE", schoolCode)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
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
