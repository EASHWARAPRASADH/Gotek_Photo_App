package com.studentidphotocapture.app.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.util.BitmapCache
import com.studentidphotocapture.app.util.TemplateConfigManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var tvInstructions: TextView
    private lateinit var tvCameraStudentName: TextView
    private lateinit var tvCameraAdmissionNo: TextView
    private lateinit var tvCameraClass: TextView
    private lateinit var progressBar: ProgressBar

    private var imageCapture: ImageCapture? = null
    private var capturedBitmap: Bitmap? = null
    private lateinit var cameraExecutor: ExecutorService

    private var studentId: String = ""
    private var studentName: String = ""
    private var rollNumber: String = ""
    private var classGrade: String = ""
    private var section: String = ""
    private var schoolCode: String = ""

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        
        // ID card photo size: 35mm x 45mm (aspect ratio 7:9)
        private const val PHOTO_WIDTH = 350
        private const val PHOTO_HEIGHT = 450
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Get student data
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: ""
        rollNumber = intent.getStringExtra("ROLL_NUMBER") ?: ""
        classGrade = intent.getStringExtra("CLASS") ?: ""
        section = intent.getStringExtra("SECTION") ?: ""
        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: "Bharathi Vidyalaya HSS"

        viewFinder = findViewById(R.id.viewFinder)
        btnCapture = findViewById(R.id.btnCapture)
        tvInstructions = findViewById(R.id.tvInstructions)
        tvCameraStudentName = findViewById(R.id.tvCameraStudentName)
        tvCameraAdmissionNo = findViewById(R.id.tvCameraAdmissionNo)
        tvCameraClass = findViewById(R.id.tvCameraClass)
        progressBar = findViewById(R.id.progressBar)
        
        // Bind student info to integrated UI
        tvCameraStudentName.text = studentName
        val displayId = intent.getStringExtra("ADMISSION_NUMBER") ?: studentId
        tvCameraAdmissionNo.text = "ID: $displayId"
        tvCameraClass.text = "Class: $classGrade - $section"

        cameraExecutor = Executors.newSingleThreadExecutor()

        tvInstructions.text = "Position $studentName within the photo frame"

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCapture.setOnClickListener {
            takePhoto()
        }

        applyTemplateConfiguration()
    }

    private fun applyTemplateConfiguration() {
        val flCardBackground = findViewById<FrameLayout>(R.id.flCardBackground)
        val photoCardView = findViewById<androidx.cardview.widget.CardView>(R.id.photoCardView)
        val llStudentDetails = findViewById<LinearLayout>(R.id.llStudentDetails)
        val ivCameraBarcode = findViewById<ImageView>(R.id.ivCameraBarcode)

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

        // 2. Load layout configs
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

        // 3. Apply photo container layout parameters
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

        // 5. Apply text color
        val colorInt = Color.parseColor(textColorHex)
        tvCameraStudentName.setTextColor(colorInt)
        tvCameraAdmissionNo.setTextColor(colorInt)
        tvCameraClass.setTextColor(colorInt)

        // 6. Apply barcode positioning and draw code
        val barcodeParams = ivCameraBarcode.layoutParams as RelativeLayout.LayoutParams
        barcodeParams.bottomMargin = barcodeYPx
        ivCameraBarcode.layoutParams = barcodeParams

        val barcodeBitmap = com.studentidphotocapture.app.util.BarcodeEncoder.generateCode39(studentId, 400, 80)
        barcodeBitmap?.let {
            ivCameraBarcode.setImageBitmap(it)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        progressBar.visibility = android.view.View.VISIBLE
        btnCapture.isEnabled = false

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    runOnUiThread {
                        progressBar.visibility = android.view.View.GONE
                        btnCapture.isEnabled = true
                        Toast.makeText(this@CameraActivity, "Photo capture failed", Toast.LENGTH_SHORT).show()
                    }
                }

                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val buffer = imageProxy.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    
                    // Convert to Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    
                    // Rotate bitmap if needed
                    val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
                    
                    // Crop matching current template aspect ratio
                    val croppedBitmap = cropToIDCardSize(rotatedBitmap)
                    capturedBitmap = croppedBitmap
                    imageProxy.close()
                    
                    runOnUiThread {
                        progressBar.visibility = android.view.View.GONE
                        btnCapture.isEnabled = true
                        navigateToPreview()
                    }
                }
            }
        )
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropToIDCardSize(bitmap: Bitmap): Bitmap {
        val photoShape = TemplateConfigManager.getPhotoShape(this, schoolCode)
        val targetRatio = if (photoShape == "circle") 1.0f else 7f / 9f
        val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        var cropWidth: Int
        var cropHeight: Int
        var x: Int
        var y: Int

        if (bitmapRatio > targetRatio) {
            // Image is wider, crop width
            cropHeight = bitmap.height
            cropWidth = (cropHeight * targetRatio).toInt()
            x = (bitmap.width - cropWidth) / 2
            y = 0
        } else {
            // Image is taller, crop height
            cropWidth = bitmap.width
            cropHeight = (cropWidth / targetRatio).toInt()
            x = 0
            y = (bitmap.height - cropHeight) / 2
        }

        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight)
        
        // Scale to a standard size (450x450 for circle, or 350x450 for rectangle)
        val scaledWidth = if (photoShape == "circle") 450 else PHOTO_WIDTH
        val scaledHeight = PHOTO_HEIGHT
        
        return Bitmap.createScaledBitmap(croppedBitmap, scaledWidth, scaledHeight, true)
    }

    private fun navigateToPreview() {
        capturedBitmap?.let { BitmapCache.setBitmap(it) }
        
        val intent = android.content.Intent(this, com.studentidphotocapture.app.ui.preview.PreviewActivity::class.java).apply {
            putExtra("STUDENT_ID", studentId)
            putExtra("STUDENT_NAME", studentName)
            putExtra("ROLL_NUMBER", rollNumber)
            putExtra("CLASS", classGrade)
            putExtra("SECTION", section)
            putExtra("SCHOOL_CODE", schoolCode)
            
            // Pass correct crop size to preview
            val photoShape = TemplateConfigManager.getPhotoShape(this@CameraActivity, schoolCode)
            if (photoShape == "circle") {
                putExtra("PHOTO_WIDTH", 450)
                putExtra("PHOTO_HEIGHT", 450)
            } else {
                putExtra("PHOTO_WIDTH", PHOTO_WIDTH)
                putExtra("PHOTO_HEIGHT", PHOTO_HEIGHT)
            }
        }
        startActivity(intent)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
