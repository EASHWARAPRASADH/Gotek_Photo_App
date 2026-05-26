package com.studentidphotocapture.app.data.repository

import com.studentidphotocapture.app.data.dao.StudentDao
import com.studentidphotocapture.app.data.model.Student
import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {
    
    fun getStudentsByClassSection(schoolCode: String, classGrade: String, section: String): Flow<List<Student>> {
        return studentDao.getStudentsByClassSection(schoolCode, classGrade, section)
    }
    
    fun getStudentsBySchool(schoolCode: String): Flow<List<Student>> {
        return studentDao.getStudentsBySchool(schoolCode)
    }
    
    suspend fun getStudentById(id: String): Student? {
        return studentDao.getStudentById(id)
    }
    
    suspend fun insertStudent(student: Student) {
        studentDao.insertStudent(student)
    }
    
    suspend fun insertStudents(students: List<Student>) {
        studentDao.insertStudents(students)
    }
    
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }
    
    fun getPendingStudents(schoolCode: String, classGrade: String, section: String): Flow<List<Student>> {
        return studentDao.getPendingStudents(schoolCode, classGrade, section)
    }
    
    suspend fun getTotalStudentsCount(schoolCode: String, classGrade: String, section: String): Int {
        return studentDao.getTotalStudentsCount(schoolCode, classGrade, section)
    }
    
    suspend fun getCompletedStudentsCount(schoolCode: String, classGrade: String, section: String): Int {
        return studentDao.getCompletedStudentsCount(schoolCode, classGrade, section)
    }
}
