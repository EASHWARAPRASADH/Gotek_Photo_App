package com.studentidphotocapture.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val password: String,
    val role: UserRole,
    val schoolCode: String? = null
)

enum class UserRole {
    TEACHER,
    ADMIN
}
