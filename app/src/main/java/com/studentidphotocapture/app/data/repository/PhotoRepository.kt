package com.studentidphotocapture.app.data.repository

import android.content.Context
import com.studentidphotocapture.app.data.dao.PhotoMetadataDao
import com.studentidphotocapture.app.data.model.PhotoMetadata
import com.studentidphotocapture.app.data.model.UploadStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

class PhotoRepository(
    private val photoMetadataDao: PhotoMetadataDao,
    private val context: Context
) {
    
    fun getPendingUploads(): Flow<List<PhotoMetadata>> {
        return photoMetadataDao.getPendingUploads()
    }
    
    suspend fun getPendingUploadsSync(): List<PhotoMetadata> {
        return photoMetadataDao.getPendingUploadsSync()
    }
    
    suspend fun savePhotoMetadata(metadata: PhotoMetadata) {
        photoMetadataDao.insertPhotoMetadata(metadata)
    }
    
    suspend fun updatePhotoMetadata(metadata: PhotoMetadata) {
        photoMetadataDao.updatePhotoMetadata(metadata)
    }
    
    suspend fun deletePhotoMetadata(id: String) {
        photoMetadataDao.deletePhotoMetadata(id)
    }
    
    suspend fun getPhotoByStudentId(studentId: String): PhotoMetadata? {
        return photoMetadataDao.getPhotoByStudentId(studentId)
    }
    
        
    fun generatePhotoFileName(
        schoolCode: String,
        classGrade: String,
        rollNumber: String
    ): String {
        return "${schoolCode}-${classGrade}-${rollNumber}.jpg"
    }
    
    fun getLocalPhotoDir(): File {
        val dir = File(context.filesDir, "student_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    fun getPhotoFile(fileName: String): File {
        return File(getLocalPhotoDir(), fileName)
    }
}
