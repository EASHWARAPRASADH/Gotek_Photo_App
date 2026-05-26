package com.studentidphotocapture.app.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.util.BitmapCache
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var tvInstructions: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var faceGuideOverlay: android.view.View

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
        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: ""

        viewFinder = findViewById(R.id.viewFinder)
        btnCapture = findViewById(R.id.btnCapture)
        tvInstructions = findViewById(R.id.tvInstructions)
        progressBar = findViewById(R.id.progressBar)
        faceGuideOverlay = findViewById(R.id.faceGuideOverlay)

        cameraExecutor = Executors.newSingleThreadExecutor()

        tvInstructions.text = "Position $studentName within the face guide"

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        btnCapture.setOnClickListener {
            takePhoto()
        }

        setupFaceGuide()
    }

    private fun setupFaceGuide() {
        viewFinder.post {
            val width = viewFinder.width
            val height = viewFinder.height
            
            // Calculate face guide dimensions (7:9 aspect ratio for ID card)
            val guideWidth = (width * 0.6).toInt()
            val guideHeight = (guideWidth * 9 / 7).toInt()
            
            // Center the guide
            val params = faceGuideOverlay.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.width = guideWidth
            params.height = guideHeight
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            faceGuideOverlay.layoutParams = params
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

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Photo capture failed", Toast.LENGTH_SHORT).show()
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
                    
                    // Validate photo quality
                    validateAndCropPhoto(rotatedBitmap, imageProxy)
                }
            }
        )
    }

    private fun validateAndCropPhoto(bitmap: Bitmap, imageProxy: ImageProxy) {
        // Perform face detection
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
        
        val detector = FaceDetection.getClient(options)
        
        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    imageProxy.close()
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, "No face detected. Please retake.", Toast.LENGTH_SHORT).show()
                    }
                    return@addOnSuccessListener
                }
                
                // Check blur using variance of Laplacian
                if (isImageBlurry(bitmap)) {
                    imageProxy.close()
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, "Image is blurry. Please retake.", Toast.LENGTH_SHORT).show()
                    }
                    return@addOnSuccessListener
                }
                
                // Crop to ID card size
                val croppedBitmap = cropToIDCardSize(bitmap)
                capturedBitmap = croppedBitmap
                imageProxy.close()
                
                // Navigate to preview
                runOnUiThread {
                    navigateToPreview()
                }
            }
            .addOnFailureListener { e ->
                imageProxy.close()
                runOnUiThread {
                    Toast.makeText(this@CameraActivity, "Face detection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isImageBlurry(bitmap: Bitmap): Boolean {
        // Simple blur detection using edge detection
        val width = bitmap.width
        val height = bitmap.height
        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(grayBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Calculate variance (simplified)
        var sum = 0
        var sumSquared = 0
        val pixels = IntArray(width * height)
        grayBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (pixel in pixels) {
            val gray = Color.red(pixel)
            sum += gray
            sumSquared += gray * gray
        }
        
        val mean = sum / (width * height).toFloat()
        val variance = (sumSquared / (width * height).toFloat()) - (mean * mean)
        
        // Threshold for blur detection (adjust as needed)
        return variance < 100f
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropToIDCardSize(bitmap: Bitmap): Bitmap {
        // ID card aspect ratio is 7:9 (35mm x 45mm)
        val targetRatio = 7f / 9f
        val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        var cropWidth: Int
        var cropHeight: Int
        var x: Int
        var y: Int

        if (bitmapRatio > targetRatio) {
            // Image is wider than ID card ratio, crop width
            cropHeight = bitmap.height
            cropWidth = (cropHeight * targetRatio).toInt()
            x = (bitmap.width - cropWidth) / 2
            y = 0
        } else {
            // Image is taller than ID card ratio, crop height
            cropWidth = bitmap.width
            cropHeight = (cropWidth / targetRatio).toInt()
            x = 0
            y = (bitmap.height - cropHeight) / 2
        }

        // Crop the image
        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight)

        // Scale to ID card size (350x450 pixels for good quality)
        val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, PHOTO_WIDTH, PHOTO_HEIGHT, true)

        return scaledBitmap
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
            putExtra("PHOTO_WIDTH", PHOTO_WIDTH)
            putExtra("PHOTO_HEIGHT", PHOTO_HEIGHT)
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
