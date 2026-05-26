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
    val classGrade: String,
    val section: String,
    val schoolCode: String,
    val photoStatus: String? = null,
    val photoUrl: String? = null
)

class CSVParser(private val context: Context) {

    companion object {
        private val REQUIRED_FIELDS = listOf("student_id", "name", "roll_number", "class", "section", "school_code")
        private val ALLOWED_SCHOOLS = listOf("SCH01", "SCH02", "SCH03")
        private val ALLOWED_CLASSES = listOf("10A", "10B", "11A", "11B", "12A", "12B")
        private val ALLOWED_SECTIONS = listOf("A", "B", "C")
    }

    fun parseCSV(uri: Uri): Pair<List<CSVStudent>, CSVImportResult> {
        val students = mutableListOf<CSVStudent>()
        val errors = mutableListOf<CSVImportError>()
        var successCount = 0
        var failureCount = 0

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))

            // Detect delimiter and format
            val format = detectCSVFormat(reader)
            
            // Reset reader
            val newInputStream = context.contentResolver.openInputStream(uri)
            val newReader = BufferedReader(InputStreamReader(newInputStream, Charset.forName("UTF-8")))

            val parser = CSVParser.parse(newReader, format)
            val records = parser.records

            if (records.isEmpty()) {
                return Pair(students, CSVImportResult(0, 0, listOf(CSVImportError(0, null, "CSV file is empty"))))
            }

            // Validate header
            val header = records[0]
            val missingFields = REQUIRED_FIELDS.filter { !header.toMap().containsKey(it) }
            if (missingFields.isNotEmpty()) {
                return Pair(students, CSVImportResult(0, 0, listOf(CSVImportError(1, null, "Missing required fields: ${missingFields.joinToString(", ")}"))))
            }

            // Parse data rows
            for (i in 1 until records.size) {
                val record = records[i]
                val rowNumber = i + 1

                try {
                    val student = parseStudentRecord(record, rowNumber)
                    students.add(student)
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    errors.add(CSVImportError(rowNumber, record.get("student_id"), e.message ?: "Unknown error"))
                }
            }

            parser.close()

        } catch (e: Exception) {
            return Pair(students, CSVImportResult(0, 1, listOf(CSVImportError(0, null, "Failed to parse CSV: ${e.message}"))))
        }

        return Pair(students, CSVImportResult(successCount, failureCount, errors))
    }

    private fun detectCSVFormat(reader: BufferedReader): CSVFormat {
        val firstLine = reader.readLine()
        reader.close()

        // Detect delimiter
        val delimiter = when {
            firstLine.contains(";") -> ';'
            firstLine.contains("\t") -> '\t'
            else -> ','
        }

        return CSVFormat.DEFAULT
            .withDelimiter(delimiter)
            .withHeader()
            .withIgnoreHeaderCase()
            .withTrim()
            .withIgnoreEmptyLines()
    }

    private fun parseStudentRecord(record: CSVRecord, rowNumber: Int): CSVStudent {
        val studentId = record.get("student_id")?.trim() ?: throw IllegalArgumentException("student_id is required")
        val name = record.get("name")?.trim() ?: throw IllegalArgumentException("name is required")
        val rollNumber = record.get("roll_number")?.trim() ?: throw IllegalArgumentException("roll_number is required")
        val classGrade = record.get("class")?.trim() ?: throw IllegalArgumentException("class is required")
        val section = record.get("section")?.trim() ?: throw IllegalArgumentException("section is required")
        val schoolCode = record.get("school_code")?.trim() ?: throw IllegalArgumentException("school_code is required")

        // Validate fields
        if (studentId.isBlank()) throw IllegalArgumentException("student_id cannot be empty")
        if (name.isBlank()) throw IllegalArgumentException("name cannot be empty")
        if (rollNumber.isBlank()) throw IllegalArgumentException("roll_number cannot be empty")
        if (!rollNumber.matches(Regex("\\d+"))) throw IllegalArgumentException("roll_number must be numeric")
        if (!ALLOWED_SCHOOLS.contains(schoolCode)) throw IllegalArgumentException("Invalid school code: $schoolCode")
        if (!ALLOWED_CLASSES.contains(classGrade)) throw IllegalArgumentException("Invalid class: $classGrade")
        if (!ALLOWED_SECTIONS.contains(section)) throw IllegalArgumentException("Invalid section: $section")

        val photoStatus = record.get("photo_status")?.trim()
        val photoUrl = record.get("photo_url")?.trim()

        return CSVStudent(
            studentId = studentId,
            name = name,
            rollNumber = rollNumber,
            classGrade = classGrade,
            section = section,
            schoolCode = schoolCode,
            photoStatus = photoStatus,
            photoUrl = photoUrl
        )
    }
}
