package com.studentidphotocapture.app.ui.csvimport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.model.PhotoStatus
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.util.CSVImportError
import com.studentidphotocapture.app.util.CSVParser
import com.studentidphotocapture.app.util.CSVStudent
import kotlinx.coroutines.launch

class CSVImportActivity : AppCompatActivity() {

    private lateinit var btnSelectFile: Button
    private lateinit var btnImport: Button
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvResults: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerViewErrors: RecyclerView
    private lateinit var tvTitle: TextView

    private var selectedFileUri: Uri? = null
    private val csvParser = CSVParser(this)
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
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        tvResults = findViewById(R.id.tvResults)
        progressBar = findViewById(R.id.progressBar)
        recyclerViewErrors = findViewById(R.id.recyclerViewErrors)
        tvTitle = findViewById(R.id.tvTitle)
        
        val isInitialImport = intent.getBooleanExtra("IS_INITIAL_IMPORT", false)
        if (isInitialImport) {
            tvTitle.text = "Initial Student Import"
            btnImport.text = "Import & Continue"
        }

        btnSelectFile.setOnClickListener {
            pickCSVFile()
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
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                selectedFileUri = uri
                tvSelectedFile.text = "Selected: ${getFileName(uri)}"
                
                // Parse CSV to preview
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
            tvResults.text = "Parsing CSV..."
            
            try {
                val (students, result) = csvParser.parseCSV(uri)
                studentsToImport.clear()
                studentsToImport.addAll(students)
                importErrors.clear()
                importErrors.addAll(result.errors)

                tvResults.text = "Parsed: ${result.successCount} students, ${result.failureCount} errors"
                
                if (result.successCount > 0) {
                    btnImport.isEnabled = true
                }

                // Show errors
                if (importErrors.isNotEmpty()) {
                    showErrorList()
                }

            } catch (e: Exception) {
                tvResults.text = "Error parsing CSV: ${e.message}"
            }

            progressBar.visibility = android.view.View.GONE
        }
    }

    private fun showErrorList() {
        recyclerViewErrors.layoutManager = LinearLayoutManager(this)
        val adapter = ImportErrorAdapter(importErrors)
        recyclerViewErrors.adapter = adapter
    }

    private fun importCSV() {
        lifecycleScope.launch {
            progressBar.visibility = android.view.View.VISIBLE
            btnImport.isEnabled = false
            
            try {
                val database = AppDatabase.getDatabase(this@CSVImportActivity)
                val studentRepository = StudentRepository(database.studentDao())

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
                                classGrade = student.classGrade,
                                section = student.section,
                                schoolCode = student.schoolCode,
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
                                classGrade = student.classGrade,
                                section = student.section,
                                schoolCode = student.schoolCode,
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
}
