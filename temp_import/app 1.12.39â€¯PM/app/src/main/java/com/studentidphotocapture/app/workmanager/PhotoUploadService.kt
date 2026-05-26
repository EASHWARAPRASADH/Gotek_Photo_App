package com.studentidphotocapture.app.workmanager

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.studentidphotocapture.app.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PhotoUploadService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val photoMetadataDao = database.photoMetadataDao()
                val pendingPhotos = photoMetadataDao.getPendingUploadsSync()
                
                for (metadata in pendingPhotos) {
                    try {
                        // Simulate upload
                        delay(2000)
                        
                        // Update status to uploaded
                        val updatedMetadata = metadata.copy(uploadStatus = com.studentidphotocapture.app.data.model.UploadStatus.UPLOADED)
                        photoMetadataDao.updatePhotoMetadata(updatedMetadata)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        
                        // Update status to failed
                        val failedMetadata = metadata.copy(uploadStatus = com.studentidphotocapture.app.data.model.UploadStatus.FAILED)
                        photoMetadataDao.updatePhotoMetadata(failedMetadata)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stopSelf()
        }
        return START_STICKY
    }
}
