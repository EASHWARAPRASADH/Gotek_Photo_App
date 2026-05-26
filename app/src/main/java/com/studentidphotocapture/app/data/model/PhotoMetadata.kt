package com.studentidphotocapture.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photometadata")
data class PhotoMetadata(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val studentId: String,
    val studentName: String,
    val classGrade: String,
    val section: String,
    val rollNumber: String,
    val schoolCode: String,
    val timestamp: Long,
    val localPath: String,
    val uploadStatus: UploadStatus = UploadStatus.PENDING
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED
}
