package com.studentidphotocapture.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student")
data class Student(
    @PrimaryKey
    val id: String,
    val name: String,
    val rollNumber: String,
    val admissionNumber: String,
    val classGrade: String,
    val section: String,
    val schoolCode: String,
    val parentMobile: String? = null,
    val photoStatus: PhotoStatus = PhotoStatus.PENDING,
    val photoUrl: String? = null,
    val photoCapturedAt: Long? = null
)

enum class PhotoStatus {
    PENDING,
    CAPTURED,
    UPLOADED,
    FAILED
}
