package com.studentidphotocapture.app.ui.template

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.util.TemplateConfigManager
import java.io.File

class TemplateCustomizationActivity : AppCompatActivity() {

    private lateinit var flCardBackground: FrameLayout
    private lateinit var photoCardView: androidx.cardview.widget.CardView
    private lateinit var llStudentDetails: LinearLayout
    private lateinit var tvStudentName: TextView
    private lateinit var tvClassRoll: TextView
    private lateinit var tvAdmNo: TextView
    private lateinit var ivBarcode: ImageView

    private lateinit var btnUploadTemplate: Button
    private lateinit var rgPhotoShape: RadioGroup
    private lateinit var rbCircle: RadioButton
    private lateinit var rbRectangle: RadioButton

    private lateinit var sbPhotoSize: SeekBar
    private lateinit var tvPhotoSizeVal: TextView
    private lateinit var sbPhotoY: SeekBar
    private lateinit var tvPhotoYVal: TextView

    private lateinit var rgTextColor: RadioGroup
    private lateinit var rbWhiteText: RadioButton
    private lateinit var rbBlackText: RadioButton

    private lateinit var sbTextY: SeekBar
    private lateinit var tvTextYVal: TextView
    private lateinit var sbBarcodeY: SeekBar
    private lateinit var tvBarcodeYVal: TextView
    private lateinit var btnSaveConfig: Button

    private var schoolCode = "Bharathi Vidyalaya HSS"

    // Configuration values (default values match the default layout XML spec)
    private var photoShape = "circle"
    private var photoSizeDp = 156
    private var photoYDp = 120
    private var textColorHex = "#FFFFFF"
    private var textYDp = 284
    private var barcodeYDp = 38

    private val scale = 220f / 300f

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            copyTemplateImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template_customization)

        schoolCode = intent.getStringExtra("SCHOOL_CODE") ?: "Bharathi Vidyalaya HSS"

        // Initialize Toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Initialize Mockup Views
        flCardBackground = findViewById(R.id.flCardBackground)
        photoCardView = findViewById(R.id.photoCardView)
        llStudentDetails = findViewById(R.id.llStudentDetails)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvClassRoll = findViewById(R.id.tvClassRoll)
        tvAdmNo = findViewById(R.id.tvAdmNo)
        ivBarcode = findViewById(R.id.ivBarcode)

        // Initialize Control Views
        btnUploadTemplate = findViewById(R.id.btnUploadTemplate)
        rgPhotoShape = findViewById(R.id.rgPhotoShape)
        rbCircle = findViewById(R.id.rbCircle)
        rbRectangle = findViewById(R.id.rbRectangle)

        sbPhotoSize = findViewById(R.id.sbPhotoSize)
        tvPhotoSizeVal = findViewById(R.id.tvPhotoSizeVal)
        sbPhotoY = findViewById(R.id.sbPhotoY)
        tvPhotoYVal = findViewById(R.id.tvPhotoYVal)

        rgTextColor = findViewById(R.id.rgTextColor)
        rbWhiteText = findViewById(R.id.rbWhiteText)
        rbBlackText = findViewById(R.id.rbBlackText)

        sbTextY = findViewById(R.id.sbTextY)
        tvTextYVal = findViewById(R.id.tvTextYVal)
        sbBarcodeY = findViewById(R.id.sbBarcodeY)
        tvBarcodeYVal = findViewById(R.id.tvBarcodeYVal)
        btnSaveConfig = findViewById(R.id.btnSaveConfig)

        // Load existing settings
        loadSavedConfig()

        // Bind control actions
        btnUploadTemplate.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        rgPhotoShape.setOnCheckedChangeListener { _, checkedId ->
            photoShape = if (checkedId == R.id.rbCircle) "circle" else "rectangle"
            updatePreview()
        }

        sbPhotoSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                photoSizeDp = progress + 100 // Min size 100dp
                tvPhotoSizeVal.text = "$photoSizeDp dp"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbPhotoY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                photoYDp = progress + 20 // Min vertical offset 20dp
                tvPhotoYVal.text = "$photoYDp dp"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rgTextColor.setOnCheckedChangeListener { _, checkedId ->
            textColorHex = if (checkedId == R.id.rbWhiteText) "#FFFFFF" else "#000000"
            updatePreview()
        }

        sbTextY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textYDp = progress + 100 // Min vertical offset 100dp
                tvTextYVal.text = "$textYDp dp"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbBarcodeY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                barcodeYDp = progress + 10 // Min bottom offset 10dp
                tvBarcodeYVal.text = "$barcodeYDp dp"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSaveConfig.setOnClickListener {
            saveConfig()
        }

        // Generate dynamic barcode image for the mockup card preview
        val barcodeBitmap = com.studentidphotocapture.app.util.BarcodeEncoder.generateCode39("1234567890", 400, 80)
        barcodeBitmap?.let {
            ivBarcode.setImageBitmap(it)
        }

        // Apply settings initially
        loadBackgroundTemplate()
        updateUIControls()
        updatePreview()
    }

    private fun loadSavedConfig() {
        photoShape = TemplateConfigManager.getPhotoShape(this, schoolCode)
        photoSizeDp = TemplateConfigManager.getPhotoSize(this, schoolCode)
        photoYDp = TemplateConfigManager.getPhotoY(this, schoolCode)
        textColorHex = TemplateConfigManager.getTextColor(this, schoolCode)
        textYDp = TemplateConfigManager.getTextY(this, schoolCode)
        barcodeYDp = TemplateConfigManager.getBarcodeY(this, schoolCode)
    }

    private fun updateUIControls() {
        if (photoShape == "circle") rbCircle.isChecked = true else rbRectangle.isChecked = true
        sbPhotoSize.progress = photoSizeDp - 100
        tvPhotoSizeVal.text = "$photoSizeDp dp"
        sbPhotoY.progress = photoYDp - 20
        tvPhotoYVal.text = "$photoYDp dp"

        if (textColorHex == "#FFFFFF") rbWhiteText.isChecked = true else rbBlackText.isChecked = true
        sbTextY.progress = textYDp - 100
        tvTextYVal.text = "$textYDp dp"
        sbBarcodeY.progress = barcodeYDp - 10
        tvBarcodeYVal.text = "$barcodeYDp dp"
    }

    private fun updatePreview() {
        val sizePx = TemplateConfigManager.dpToPx(this, (photoSizeDp * scale).toInt())
        val photoYPx = TemplateConfigManager.dpToPx(this, (photoYDp * scale).toInt())
        val textYPx = TemplateConfigManager.dpToPx(this, (textYDp * scale).toInt())
        val barcodeYPx = TemplateConfigManager.dpToPx(this, (barcodeYDp * scale).toInt())

        // Photo slot layout positioning
        val photoParams = photoCardView.layoutParams as RelativeLayout.LayoutParams
        photoParams.width = sizePx
        photoParams.height = sizePx
        photoParams.topMargin = photoYPx
        photoCardView.layoutParams = photoParams

        if (photoShape == "circle") {
            photoCardView.radius = (sizePx / 2).toFloat()
        } else {
            photoCardView.radius = TemplateConfigManager.dpToPx(this, 6).toFloat()
        }

        // Student Details text position
        val detailsParams = llStudentDetails.layoutParams as RelativeLayout.LayoutParams
        detailsParams.topMargin = textYPx
        llStudentDetails.layoutParams = detailsParams

        // Text details Color
        val colorInt = Color.parseColor(textColorHex)
        tvStudentName.setTextColor(colorInt)
        tvClassRoll.setTextColor(colorInt)
        tvAdmNo.setTextColor(colorInt)

        // Barcode position
        val barcodeParams = ivBarcode.layoutParams as RelativeLayout.LayoutParams
        barcodeParams.bottomMargin = barcodeYPx
        ivBarcode.layoutParams = barcodeParams
    }

    private fun loadBackgroundTemplate() {
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
    }

    private fun copyTemplateImage(uri: Uri) {
        try {
            val destinationFile = TemplateConfigManager.getTemplateFile(this, schoolCode)
            contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Custom template image uploaded successfully!", Toast.LENGTH_SHORT).show()
            loadBackgroundTemplate()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load custom template: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveConfig() {
        TemplateConfigManager.setPhotoShape(this, schoolCode, photoShape)
        TemplateConfigManager.setPhotoSize(this, schoolCode, photoSizeDp)
        TemplateConfigManager.setPhotoY(this, schoolCode, photoYDp)
        TemplateConfigManager.setTextColor(this, schoolCode, textColorHex)
        TemplateConfigManager.setTextY(this, schoolCode, textYDp)
        TemplateConfigManager.setBarcodeY(this, schoolCode, barcodeYDp)

        Toast.makeText(this, "Template configuration saved successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
