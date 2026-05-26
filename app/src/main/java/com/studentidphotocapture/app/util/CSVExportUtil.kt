package com.studentidphotocapture.app.util

import android.content.Context
import android.os.Environment
import com.studentidphotocapture.app.data.model.Student
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CSVExportUtil {

    /**
     * Exports a list of students to a CSV file in the Downloads directory.
     * The CSV will contain Admission Number, Name, Class, Section, and the local photo filename.
     */
    fun exportStudentsToCSV(context: Context, students: List<Student>, schoolCode: String): Result<File> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            
            // Create a dedicated folder for the exports
            val appFolder = File(downloadsDir, "StudentIDExport")
            if (!appFolder.exists()) {
                appFolder.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "Export_${schoolCode}_${timestamp}.csv"
            val file = File(appFolder, fileName)

            val fileWriter = FileWriter(file)
            
            // Write CSV Header
            fileWriter.append("Admission Number,Student Name,Class,Section,Photo Filename\n")

            // Write Data Rows
            for (student in students) {
                // Only include the filename, the external company can match it with the exported photo folder
                val extractedFileName = student.photoUrl?.substringAfterLast("/") ?: "NO_PHOTO"
                
                // Escape commas in names to prevent CSV corruption
                val safeName = if (student.name.contains(",")) "\"${student.name}\"" else student.name
                
                fileWriter.append("${student.id},${safeName},${student.classGrade},${student.section},${extractedFileName}\n")
            }

            fileWriter.flush()
            fileWriter.close()

            Result.success(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
