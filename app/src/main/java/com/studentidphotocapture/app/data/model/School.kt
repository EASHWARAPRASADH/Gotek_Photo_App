package com.studentidphotocapture.app.data.model

data class School(
    val code: String,
    val name: String,
    val classes: List<ClassInfo>
)

data class ClassInfo(
    val grade: String,
    val sections: List<String>
)
