package com.studentidphotocapture.app.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studentidphotocapture.app.data.database.AppDatabase
import com.studentidphotocapture.app.data.repository.StudentRepository
import com.studentidphotocapture.app.data.api.RetrofitClient

class StudentDataSyncWorker(
    private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("StudentDataSyncWorker", "Starting background data sync")
        
        return try {
            val database = AppDatabase.getDatabase(context)
            val studentRepository = StudentRepository(database.studentDao(), RetrofitClient.studentApiService)
            
            // For professional demo: Sync a few priority classes/sections
            val syncLists = listOf(
                Triple("Bharathi Vidyalaya HSS", "10", "A"),
                Triple("Bharathi Vidyalaya HSS", "11", "B"),
                Triple("Bharathi Vidyalaya HSS", "12", "C")
            )
            
            var success = true
            for (syncItem in syncLists) {
                val result = studentRepository.syncStudents(syncItem.first, syncItem.second, syncItem.third)
                if (result.isFailure) {
                    Log.e("StudentDataSyncWorker", "Failed to sync ${syncItem.second}-${syncItem.third}: ${result.exceptionOrNull()?.message}")
                    success = false
                }
            }
            
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("StudentDataSyncWorker", "Worker execution failed", e)
            Result.failure()
        }
    }
}
