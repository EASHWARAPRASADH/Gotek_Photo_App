package com.studentidphotocapture.app.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.studentidphotocapture.app.data.model.Student
import com.studentidphotocapture.app.data.repository.PhotoRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PDFExportUtil {

    fun exportStudentsToPDF(
        context: Context,
        students: List<Student>,
        schoolCode: String,
        photoRepository: PhotoRepository
    ): Result<String> {
        val pdfDocument = PdfDocument()
        
        // Page dimensions (A4 size at 72 DPI is approx 595x842 points)
        val pageHeight = 842
        val pageWidth = 595
        
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        
        val headerPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.DKGRAY
        }
        
        val contentPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.BLACK
        }

        var studentIndex = 0
        val studentsPerPage = 4
        
        while (studentIndex < students.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, (studentIndex / studentsPerPage) + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw Header
            canvas.drawText("Student ID Photo Export - $schoolCode", 40f, 60f, titlePaint)
            canvas.drawLine(40f, 75f, pageWidth - 40f, 75f, paint)
            
            // Draw Students (2x2 grid)
            for (i in 0 until studentsPerPage) {
                if (studentIndex >= students.size) break
                
                val student = students[studentIndex]
                val col = i % 2
                val row = i / 2
                
                val startX = 40f + (col * (pageWidth / 2 - 20f))
                val startY = 100f + (row * (pageHeight / 2 - 60f))
                
                drawStudentCard(canvas, student, startX, startY, contentPaint, headerPaint, photoRepository)
                
                studentIndex++
            }
            
            pdfDocument.finishPage(page)
        }

        val fileName = "StudentExport_${schoolCode}_${System.currentTimeMillis()}.pdf"
        
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StudentIDExport")
                }
                
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                } else {
                    throw IOException("Failed to create MediaStore record.")
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appFolder = File(downloadsDir, "StudentIDExport")
                if (!appFolder.exists()) {
                    appFolder.mkdirs()
                }
                val file = File(appFolder, fileName)
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            }
            
            pdfDocument.close()
            Result.success(fileName)
        } catch (e: Exception) {
            pdfDocument.close()
            Result.failure(e)
        }
    }

    private fun drawStudentCard(
        canvas: Canvas,
        student: Student,
        x: Float,
        y: Float,
        paint: Paint,
        headerPaint: Paint,
        photoRepository: PhotoRepository
    ) {
        // Draw Student Photo
        val photoMetadata = try {
            // This is a bit tricky since we are in a non-suspend function
            // But we can just check if file exists based on naming convention or a helper
            null 
        } catch (e: Exception) { null }
        
        // Let's assume we can get the file path directly if we know the naming convention
        val fileName = photoRepository.generatePhotoFileName(student.schoolCode, student.classGrade, student.rollNumber)
        val photoFile = photoRepository.getPhotoFile(fileName)
        
        val photoX = x
        val photoY = y + 10f
        val photoWidth = 120f
        val photoHeight = 154f // 120 * (9/7) approx
        
        if (photoFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            if (bitmap != null) {
                val rect = RectF(photoX, photoY, photoX + photoWidth, photoY + photoHeight)
                canvas.drawBitmap(bitmap, null, rect, null)
                bitmap.recycle()
            } else {
                drawPlaceholder(canvas, photoX, photoY, photoWidth, photoHeight)
            }
        } else {
            drawPlaceholder(canvas, photoX, photoY, photoWidth, photoHeight)
        }
        
        // Draw Student Details
        val textX = x + photoWidth + 20f
        var textY = y + 25f
        
        canvas.drawText("Name:", textX, textY, headerPaint)
        textY += 15f
        canvas.drawText(student.name, textX, textY, paint)
        textY += 25f
        
        canvas.drawText("Roll Number:", textX, textY, headerPaint)
        textY += 15f
        canvas.drawText(student.rollNumber, textX, textY, paint)
        textY += 25f
        
        canvas.drawText("Class/Section:", textX, textY, headerPaint)
        textY += 15f
        canvas.drawText("${student.classGrade} - ${student.section}", textX, textY, paint)
        textY += 25f
        
        canvas.drawText("Admission ID:", textX, textY, headerPaint)
        textY += 15f
        canvas.drawText(student.admissionNumber, textX, textY, paint)
    }

    private fun drawPlaceholder(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + w, y + h, paint)
        
        val textPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("No Photo", x + w/2, y + h/2, textPaint)
    }
}
