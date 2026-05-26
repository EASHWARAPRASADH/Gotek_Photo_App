package com.studentidphotocapture.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val password: String? = null,
    val role: UserRole,
    val schoolCode: String? = null,
    val phoneNumber: String? = null,
    val assignedClass: String? = null,
    val assignedSection: String? = null
)

enum class UserRole {
    TEACHER,
    ADMIN,
    PARENT,
    ULTRA_SUPER_ADMIN,
    SUPER_ADMIN
}

