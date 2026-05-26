package com.studentidphotocapture.app.util

import android.content.Context
import android.net.Uri
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

data class CSVImportResult(
    val successCount: Int,
    val failureCount: Int,
    val errors: List<CSVImportError>
)

data class CSVImportError(
    val rowNumber: Int,
    val studentId: String?,
    val errorMessage: String
)

data class CSVStudent(
    val studentId: String,
    val name: String,
    val rollNumber: String,
    val admissionNumber: String,
    val classGrade: String,
    val section: String,
    val schoolCode: String,
    val parentMobile: String,
    val photoStatus: String? = null,
    val photoUrl: String? = null
)

class StudentCSVParser(private val context: Context) {

    companion object {
        val REQUIRED_FIELDS = listOf("student_id", "name", "roll_number", "admission_number", "class", "section", "school_code", "parent_mobile")
        private val ALLOWED_SCHOOLS = listOf("Bharathi Vidyalaya HSS", "St. Mary's Matriculation", "Tagore Higher Sec. School")
        private val ALLOWED_CLASSES = listOf("10", "11", "12")
        private val ALLOWED_SECTIONS = listOf("A", "B", "C")
    }

    fun getCSVHeaders(uri: Uri): List<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return emptyList()
            val content = inputStream.use { it.bufferedReader().readText() }
            if (content.isNullOrBlank()) return emptyList()

            // Remove BOM if present
            val cleanContent = if (content.startsWith("\uFEFF")) content.substring(1) else content
            
            // Detect delimiter from first line
            val firstLine = cleanContent.lineSequence().firstOrNull() ?: ""
            val delimiter = when {
                firstLine.contains(";") -> ';'
                firstLine.contains("\t") -> '\t'
                else -> ','
            }

            val format = CSVFormat.DEFAULT
                .withDelimiter(delimiter)
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withIgnoreEmptyLines()

            val parser = org.apache.commons.csv.CSVParser.parse(cleanContent, format)
            parser.headerMap?.keys?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseCSV(uri: Uri): Pair<List<CSVStudent>, CSVImportResult> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Pair(emptyList(), CSVImportResult(0, 0, listOf(CSVImportError(1, null, "Could not open file"))))
            parseCSVStream(inputStream)
        } catch (e: Exception) {
            Pair(emptyList(), CSVImportResult(0, 1, listOf(CSVImportError(0, null, "Critical error: ${e.message}"))))
        }
    }

    fun parseCSVStream(inputStream: java.io.InputStream): Pair<List<CSVStudent>, CSVImportResult> {
        // Default mapping is identity mapping (key maps to itself)
        val defaultMapping = REQUIRED_FIELDS.associateWith { it }
        return parseCSVStreamWithMapping(inputStream, defaultMapping, "Bharathi Vidyalaya HSS")
    }

    fun parseCSVWithMapping(
        uri: Uri,
        mapping: Map<String, String>,
        defaultSchoolCode: String = "Bharathi Vidyalaya HSS"
    ): Pair<List<CSVStudent>, CSVImportResult> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Pair(emptyList(), CSVImportResult(0, 0, listOf(CSVImportError(1, null, "Could not open file"))))
            parseCSVStreamWithMapping(inputStream, mapping, defaultSchoolCode)
        } catch (e: Exception) {
            Pair(emptyList(), CSVImportResult(0, 1, listOf(CSVImportError(0, null, "Critical error: ${e.message}"))))
        }
    }

    fun parseCSVStreamWithMapping(
        inputStream: java.io.InputStream,
        mapping: Map<String, String>,
        defaultSchoolCode: String
    ): Pair<List<CSVStudent>, CSVImportResult> {
        val students = mutableListOf<CSVStudent>()
        val errors = mutableListOf<CSVImportError>()
        
        try {
            val content = inputStream.use { it.bufferedReader().readText() }
            if (content.isNullOrBlank()) {
                return Pair(students, CSVImportResult(0, 0, listOf(CSVImportError(1, null, "File is empty"))))
            }

            // Remove BOM if present
            val cleanContent = if (content.startsWith("\uFEFF")) content.substring(1) else content
            
            // Detect delimiter from first line
            val firstLine = cleanContent.lineSequence().firstOrNull() ?: ""
            val delimiter = when {
                firstLine.contains(";") -> ';'
                firstLine.contains("\t") -> '\t'
                else -> ','
            }

            val format = CSVFormat.DEFAULT
                .withDelimiter(delimiter)
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withIgnoreEmptyLines()

            val parser = org.apache.commons.csv.CSVParser.parse(cleanContent, format)
            val headerMap = parser.headerMap

            if (headerMap == null) {
                return Pair(students, CSVImportResult(0, 0, listOf(CSVImportError(1, null, "Missing header row"))))
            }

            // Validate header for mapped fields
            val missingFields = mutableListOf<String>()
            for (field in REQUIRED_FIELDS) {
                // If school_code is not mapped, we fall back to defaultSchoolCode
                if (field == "school_code" && !mapping.containsKey(field)) {
                    continue
                }
                val mappedHeader = mapping[field]
                if (mappedHeader == null || !headerMap.keys.any { it.equals(mappedHeader, ignoreCase = true) }) {
                    missingFields.add(field)
                }
            }

            if (missingFields.isNotEmpty()) {
                return Pair(students, CSVImportResult(0, 0, listOf(CSVImportError(1, null, "Missing or unmapped fields: ${missingFields.joinToString(", ")}"))))
            }

            val records = parser.records
            var successCount = 0
            var failureCount = 0

            for ((index, record) in records.withIndex()) {
                val rowNumber = index + 2 // 1 for header, 1 for 0-based index
                try {
                    val student = parseStudentRecordWithMapping(record, rowNumber, mapping, defaultSchoolCode)
                    students.add(student)
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    val idCol = mapping["student_id"] ?: "student_id"
                    val studentId = if (record.isMapped(idCol)) record.get(idCol) else "Row $rowNumber"
                    errors.add(CSVImportError(rowNumber, studentId, e.message ?: "Unknown error"))
                }
            }

            return Pair(students, CSVImportResult(successCount, failureCount, errors))

        } catch (e: Exception) {
            return Pair(students, CSVImportResult(0, 1, listOf(CSVImportError(0, null, "Critical error: ${e.message}"))))
        }
    }

    private fun parseStudentRecordWithMapping(
        record: CSVRecord,
        rowNumber: Int,
        mapping: Map<String, String>,
        defaultSchoolCode: String
    ): CSVStudent {
        val studentIdHeader = mapping["student_id"] ?: "student_id"
        val nameHeader = mapping["name"] ?: "name"
        val rollNumberHeader = mapping["roll_number"] ?: "roll_number"
        val admissionNumberHeader = mapping["admission_number"] ?: "admission_number"
        val classHeader = mapping["class"] ?: "class"
        val sectionHeader = mapping["section"] ?: "section"
        val schoolCodeHeader = mapping["school_code"] ?: "school_code"
        val parentMobileHeader = mapping["parent_mobile"] ?: "parent_mobile"

        val studentId = getValueOrThrow(record, studentIdHeader, "Student ID")
        val name = getValueOrThrow(record, nameHeader, "Name")
        val rollNumber = getValueOrThrow(record, rollNumberHeader, "Roll Number")
        val admissionNumber = getValueOrThrow(record, admissionNumberHeader, "Admission Number")
        val classGrade = getValueOrThrow(record, classHeader, "Class")
        val section = getValueOrThrow(record, sectionHeader, "Section")
        
        // Handle optional or default school_code
        val schoolCode = if (mapping.containsKey("school_code")) {
            getValueOrThrow(record, schoolCodeHeader, "School Code")
        } else {
            defaultSchoolCode
        }

        val parentMobile = getValueOrThrow(record, parentMobileHeader, "Parent Mobile")

        // Validate fields
        if (studentId.isBlank()) throw IllegalArgumentException("student_id cannot be empty")
        if (name.isBlank()) throw IllegalArgumentException("name cannot be empty")
        if (rollNumber.isBlank()) throw IllegalArgumentException("roll_number cannot be empty")
        if (admissionNumber.isBlank()) throw IllegalArgumentException("admission_number cannot be empty")
        
        // Clean numeric fields
        val cleanRoll = rollNumber.filter { it.isDigit() }
        if (cleanRoll.isEmpty()) throw IllegalArgumentException("roll_number must contain digits")
        
        val cleanMobile = parentMobile.filter { it.isDigit() }
        if (cleanMobile.length < 10) throw IllegalArgumentException("parent_mobile must be at least 10 digits")
        val finalMobile = cleanMobile.takeLast(10) // Normalize to last 10 digits

        val photoStatus = if (record.isMapped("photo_status")) record.get("photo_status")?.trim() else null
        val photoUrl = if (record.isMapped("photo_url")) record.get("photo_url")?.trim() else null

        return CSVStudent(
            studentId = studentId,
            name = name,
            rollNumber = cleanRoll,
            admissionNumber = admissionNumber,
            classGrade = classGrade,
            section = section,
            schoolCode = schoolCode,
            parentMobile = finalMobile,
            photoStatus = photoStatus,
            photoUrl = photoUrl
        )
    }

    private fun getValueOrThrow(record: CSVRecord, headerName: String, friendlyName: String): String {
        // Look up column case-insensitively
        val actualKey = record.parser.headerMap?.keys?.firstOrNull { it.equals(headerName, ignoreCase = true) }
            ?: throw IllegalArgumentException("$friendlyName ($headerName) column not found")
        return record.get(actualKey)?.trim() ?: throw IllegalArgumentException("$friendlyName is empty")
    }
}
