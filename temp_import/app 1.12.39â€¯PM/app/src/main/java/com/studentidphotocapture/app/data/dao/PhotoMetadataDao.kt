package com.studentidphotocapture.app.data.dao

import androidx.room.*
import com.studentidphotocapture.app.data.model.PhotoMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoMetadataDao {
    @Query("SELECT * FROM photometadata WHERE uploadStatus = 'PENDING'")
    fun getPendingUploads(): Flow<List<PhotoMetadata>>

    @Query("SELECT * FROM photometadata WHERE uploadStatus = 'PENDING'")
    suspend fun getPendingUploadsSync(): List<PhotoMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoMetadata(metadata: PhotoMetadata)

    @Update
    suspend fun updatePhotoMetadata(metadata: PhotoMetadata)

    @Query("DELETE FROM photometadata WHERE id = :id")
    suspend fun deletePhotoMetadata(id: String)

    @Query("SELECT * FROM photometadata WHERE studentId = :studentId LIMIT 1")
    suspend fun getPhotoByStudentId(studentId: String): PhotoMetadata?
}
