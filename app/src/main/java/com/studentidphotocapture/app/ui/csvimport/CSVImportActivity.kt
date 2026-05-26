package com.studentidphotocapture.app.ui.csvimport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.model.PhotoStatus
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.util.CSVImportError
import com.studentidphotocapture.app.util.StudentCSVParser
import com.studentidphotocapture.app.util.CSVStudent
import kotlinx.coroutines.launch

class CSVImportActivity : AppCompatActivity() {

    private lateinit var btnSelectFile: Button
    private lateinit var btnImport: Button
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvResults: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView
    private lateinit var btnLoadSample: Button

    private var selectedFileUri: Uri? = null
    private val csvParser = StudentCSVParser(this)
    private val studentsToImport = mutableListOf<CSVStudent>()
    private val importErrors = mutableListOf<CSVImportError>()

    companion object {
        private const val PICK_CSV_FILE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_import)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnImport = findViewById(R.id.btnImport)
        btnLoadSample = findViewById(R.id.btnLoadSample)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        tvResults = findViewById(R.id.tvResults)
        progressBar = findViewById(R.id.progressBar)
        tvTitle = findViewById(R.id.tvTitle)
        
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val isInitialImport = intent.getBooleanExtra("IS_INITIAL_IMPORT", false)
        if (isInitialImport) {
            tvTitle.text = "Initial Student Import"
            btnImport.text = "Import & Continue"
        }

        btnSelectFile.setOnClickListener {
            pickCSVFile()
        }

        btnLoadSample.setOnClickListener {
            loadSampleCSV()
        }

        btnImport.setOnClickListener {
            importCSV()
        }

        btnImport.isEnabled = false
    }

    private fun pickCSVFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        startActivityForResult(intent, PICK_CSV_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Take persistent URI permission
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if permission already persisted
                }
                
                selectedFileUri = uri
                tvSelectedFile.text = "Selected: ${getFileName(uri)}"
                
                // Parse CSV to preview with dynamic mapping dialog
                parseCSV(uri)
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex)
                }
            }
        }
        return "Unknown file"
    }

    private fun parseCSV(uri: Uri) {
        lifecycleScope.launch {
            progressBar.visibility = android.view.View.VISIBLE
            tvResults.text = "Reading CSV headers..."
            
            try {
                val headers = csvParser.getCSVHeaders(uri)
                progressBar.visibility = android.view.View.GONE
                
                if (headers.isEmpty()) {
                    tvResults.text = "Failed to read CSV headers. Make sure it's a valid CSV."
                    return@launch
                }
                
                // Show custom mapping dialog
                showMappingDialog(uri, headers)
            } catch (e: Exception) {
                progressBar.visibility = android.view.View.GONE
                tvResults.text = "Error reading CSV: ${e.message}"
            }
        }
    }

    private fun showMappingDialog(uri: Uri, headers: List<String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_csv_mapping, null)
        
        val spinnerStudentId = dialogView.findViewById<Spinner>(R.id.spinnerStudentId)
        val spinnerName = dialogView.findViewById<Spinner>(R.id.spinnerName)
        val spinnerRollNumber = dialogView.findViewById<Spinner>(R.id.spinnerRollNumber)
        val spinnerAdmissionNumber = dialogView.findViewById<Spinner>(R.id.spinnerAdmissionNumber)
        val spinnerClass = dialogView.findViewById<Spinner>(R.id.spinnerClass)
        val spinnerSection = dialogView.findViewById<Spinner>(R.id.spinnerSection)
        val spinnerSchoolCode = dialogView.findViewById<Spinner>(R.id.spinnerSchoolCode)
        val spinnerParentMobile = dialogView.findViewById<Spinner>(R.id.spinnerParentMobile)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        // Add a placeholder/empty option for optional fields (like school_code)
        val optionalHeaders = mutableListOf("[Not in CSV]")
        optionalHeaders.addAll(headers)

        // Adapters
        val requiredAdapter = ArrayAdapter(this, R.layout.item_spinner_selected, headers).apply {
            setDropDownViewResource(R.layout.item_dropdown)
        }
        val optionalAdapter = ArrayAdapter(this, R.layout.item_spinner_selected, optionalHeaders).apply {
            setDropDownViewResource(R.layout.item_dropdown)
        }

        spinnerStudentId.adapter = requiredAdapter
        spinnerName.adapter = requiredAdapter
        spinnerRollNumber.adapter = requiredAdapter
        spinnerAdmissionNumber.adapter = requiredAdapter
        spinnerClass.adapter = requiredAdapter
        spinnerSection.adapter = requiredAdapter
        spinnerSchoolCode.adapter = optionalAdapter
        spinnerParentMobile.adapter = requiredAdapter

        // Pre-select via fuzzy matching
        preselectSpinner(spinnerStudentId, "student_id", headers)
        preselectSpinner(spinnerName, "name", headers)
        preselectSpinner(spinnerRollNumber, "roll_number", headers)
        preselectSpinner(spinnerAdmissionNumber, "admission_number", headers)
        preselectSpinner(spinnerClass, "class", headers)
        preselectSpinner(spinnerSection, "section", headers)
        preselectOptionalSpinner(spinnerSchoolCode, "school_code", headers)
        preselectSpinner(spinnerParentMobile, "parent_mobile", headers)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
        
        val dialog = builder.create()
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
            tvResults.text = "Import cancelled."
        }
        
        btnConfirm.setOnClickListener {
            val mapping = mutableMapOf<String, String>()
            mapping["student_id"] = spinnerStudentId.selectedItem.toString()
            mapping["name"] = spinnerName.selectedItem.toString()
            mapping["roll_number"] = spinnerRollNumber.selectedItem.toString()
            mapping["admission_number"] = spinnerAdmissionNumber.selectedItem.toString()
            mapping["class"] = spinnerClass.selectedItem.toString()
            mapping["section"] = spinnerSection.selectedItem.toString()
            
            val selectedSchoolCode = spinnerSchoolCode.selectedItem.toString()
            if (selectedSchoolCode != "[Not in CSV]") {
                mapping["school_code"] = selectedSchoolCode
            }
            
            mapping["parent_mobile"] = spinnerParentMobile.selectedItem.toString()
            
            dialog.dismiss()
            parseCSVWithMappedColumns(uri, mapping)
        }
        
        dialog.show()
    }

    private fun preselectSpinner(spinner: Spinner, fieldName: String, headers: List<String>) {
        val matchedHeader = fuzzyMatchHeader(fieldName, headers)
        if (matchedHeader != null) {
            val index = headers.indexOf(matchedHeader)
            if (index >= 0) {
                spinner.setSelection(index)
            }
        }
    }

    private fun preselectOptionalSpinner(spinner: Spinner, fieldName: String, headers: List<String>) {
        val matchedHeader = fuzzyMatchHeader(fieldName, headers)
        if (matchedHeader != null) {
            val index = headers.indexOf(matchedHeader)
            if (index >= 0) {
                spinner.setSelection(index + 1) // +1 for the "[Not in CSV]" option
            }
        } else {
            spinner.setSelection(0) // Default to "[Not in CSV]"
        }
    }

    private fun fuzzyMatchHeader(fieldName: String, headers: List<String>): String? {
        val fieldLower = fieldName.lowercase()
        
        // Exact match check
        headers.firstOrNull { it.lowercase() == fieldLower }?.let { return it }
        
        // Dynamic search keywords
        val keywords = when (fieldName) {
            "student_id" -> listOf("student_id", "student id", "uuid", "uid", "std_id", "id", "studentno")
            "name" -> listOf("name", "student name", "student_name", "fullname", "full name", "first name", "firstname")
            "roll_number" -> listOf("roll", "roll_number", "roll number", "roll_no", "roll no", "rollno")
            "admission_number" -> listOf("admission", "admission_number", "admission number", "admission_no", "admission no", "adm", "adm_no", "adm no", "admno")
            "class" -> listOf("class", "class_grade", "class grade", "grade", "standard", "std")
            "section" -> listOf("section", "sec")
            "school_code" -> listOf("school", "school_code", "school code", "school_id", "school id", "schoolcode")
            "parent_mobile" -> listOf("mobile", "phone", "parent_mobile", "parent mobile", "parent_phone", "parent phone", "contact", "mobile_no", "mobile no")
            else -> emptyList()
        }
        
        for (keyword in keywords) {
            val match = headers.firstOrNull { it.lowercase().contains(keyword) }
            if (match != null) return match
        }
        
        return headers.firstOrNull { it.lowercase().startsWith(fieldLower.substring(0, minOf(3, fieldLower.length))) }
    }

    private fun parseCSVWithMappedColumns(uri: Uri, mapping: Map<String, String>) {
        lifecycleScope.launch {
            progressBar.visibility = android.view.View.VISIBLE
            tvResults.text = "Parsing CSV records..."
            
            val activeSchoolCode = intent.getStringExtra("SCHOOL_CODE") ?: "Bharathi Vidyalaya HSS"
            
            try {
                val (students, result) = csvParser.parseCSVWithMapping(uri, mapping, activeSchoolCode)
                studentsToImport.clear()
                studentsToImport.addAll(students)
                importErrors.clear()
                importErrors.addAll(result.errors)

                tvResults.text = "Parsed: ${result.successCount} students, ${result.failureCount} errors"
                
                if (result.successCount > 0) {
                    btnImport.isEnabled = true
                } else {
                    btnImport.isEnabled = false
                }
            } catch (e: Exception) {
                tvResults.text = "Error parsing CSV: ${e.message}"
                btnImport.isEnabled = false
            }

            progressBar.visibility = android.view.View.GONE
        }
    }

    private fun importCSV() {
        lifecycleScope.launch {
            progressBar.visibility = android.view.View.VISIBLE
            btnImport.isEnabled = false
            
            try {
                val database = AppDatabase.getDatabase(this@CSVImportActivity)
                val studentRepository = StudentRepository(
                    database.studentDao(),
                    com.studentidphotocapture.app.data.api.RetrofitClient.studentApiService
                )

                var successCount = 0
                var failureCount = 0

                for (student in studentsToImport) {
                    try {
                        val existingStudent = studentRepository.getStudentById(student.studentId)
                        
                        if (existingStudent != null) {
                            // Update existing student
                            val updatedStudent = existingStudent.copy(
                                name = student.name,
                                rollNumber = student.rollNumber,
                                admissionNumber = student.admissionNumber,
                                classGrade = student.classGrade,
                                section = student.section,
                                schoolCode = student.schoolCode,
                                parentMobile = student.parentMobile,
                                photoStatus = student.photoStatus?.let { PhotoStatus.valueOf(it) } ?: existingStudent.photoStatus,
                                photoUrl = student.photoUrl ?: existingStudent.photoUrl
                            )
                            studentRepository.updateStudent(updatedStudent)
                        } else {
                            // Insert new student
                            val newStudent = com.studentidphotocapture.app.data.model.Student(
                                id = student.studentId,
                                name = student.name,
                                rollNumber = student.rollNumber,
                                admissionNumber = student.admissionNumber,
                                classGrade = student.classGrade,
                                section = student.section,
                                schoolCode = student.schoolCode,
                                parentMobile = student.parentMobile,
                                photoStatus = student.photoStatus?.let { PhotoStatus.valueOf(it) } ?: PhotoStatus.PENDING,
                                photoUrl = student.photoUrl
                            )
                            studentRepository.insertStudent(newStudent)
                        }
                        successCount++
                    } catch (e: Exception) {
                        failureCount++
                    }
                }

                tvResults.text = "Import complete: $successCount students imported, $failureCount failed"
                Toast.makeText(this@CSVImportActivity, "Import completed successfully", Toast.LENGTH_SHORT).show()

                // Check if this was initial import
                val isInitialImport = intent.getBooleanExtra("IS_INITIAL_IMPORT", false)
                if (isInitialImport) {
                    // Navigate to student selection after import
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val studentIntent = android.content.Intent(this@CSVImportActivity, com.studentidphotocapture.app.ui.studentselection.StudentSelectionActivity::class.java).apply {
                            putExtra("USER_ID", intent.getStringExtra("USER_ID"))
                            putExtra("USER_ROLE", intent.getStringExtra("USER_ROLE"))
                            putExtra("SCHOOL_CODE", intent.getStringExtra("SCHOOL_CODE"))
                        }
                        startActivity(studentIntent)
                        finish()
                    }, 2000)
                } else {
                    // Return to dashboard after delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 2000)
                }

            } catch (e: Exception) {
                tvResults.text = "Import failed: ${e.message}"
                btnImport.isEnabled = true
            }

            progressBar.visibility = android.view.View.GONE
        }
    }

    private fun loadSampleCSV() {
        lifecycleScope.launch {
            progressBar.visibility = android.view.View.VISIBLE
            tvResults.text = "Loading sample CSV..."
            
            try {
                val inputStream = resources.openRawResource(R.raw.sample_students)
                val (students, result) = csvParser.parseCSVStream(inputStream)
                studentsToImport.clear()
                studentsToImport.addAll(students)
                importErrors.clear()
                importErrors.addAll(result.errors)

                tvSelectedFile.text = "Selected: Bundled Sample CSV"
                tvResults.text = "Parsed: ${result.successCount} students, ${result.failureCount} errors"
                
                if (result.successCount > 0) {
                    btnImport.isEnabled = true
                }
            } catch (e: Exception) {
                tvResults.text = "Error parsing CSV: ${e.message}"
            }

            progressBar.visibility = android.view.View.GONE
        }
    }
}
