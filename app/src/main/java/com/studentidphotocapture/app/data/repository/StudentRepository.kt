package com.studentidphotocapture.app.data.repository

import com.studentidphotocapture.app.data.api.StudentApiService
import com.studentidphotocapture.app.data.dao.StudentDao
import com.studentidphotocapture.app.data.model.Student
import kotlinx.coroutines.flow.Flow

class StudentRepository(
    private val studentDao: StudentDao,
    private val apiService: StudentApiService
) {
    
    fun getStudentsByClassSection(schoolCode: String, classGrade: String, section: String): Flow<List<Student>> {
        return studentDao.getStudentsByClassSection(schoolCode, classGrade, section)
    }

    suspend fun syncStudents(schoolCode: String, classGrade: String, section: String): Result<Unit> {
        return try {
            val response = apiService.getStudents(schoolCode, classGrade, section)
            if (response.isSuccessful) {
                response.body()?.let { students ->
                    studentDao.insertStudents(students)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchStudents(query: String): Flow<List<Student>> {
        return studentDao.searchStudents(query)
    }

    fun getStudentsByParentMobile(mobile: String): Flow<List<Student>> {
        return studentDao.getStudentsByParentMobile(mobile)
    }

    suspend fun clearDuplicateMockParentStudents(mobile: String) {
        studentDao.clearDuplicateMockParentStudents(mobile)
    }
    
    fun getStudentsBySchool(schoolCode: String): Flow<List<Student>> {
        return studentDao.getStudentsBySchool(schoolCode)
    }
    
    suspend fun getStudentById(id: String): Student? {
        return studentDao.getStudentById(id)
    }
    
    suspend fun getStudentByAdmissionNumber(admissionNumber: String): Student? {
        return studentDao.getStudentByAdmissionNumber(admissionNumber)
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
    
    suspend fun getStudentsWithCapturedPhotos(): List<com.studentidphotocapture.app.data.model.Student> {
        return studentDao.getStudentsWithCapturedPhotos()
    }

    fun getAllSchoolCodes(): Flow<List<String>> {
        return studentDao.getAllSchoolCodes()
    }

    suspend fun getTotalStudentsForSchool(schoolCode: String): Int {
        return studentDao.getTotalStudentsForSchool(schoolCode)
    }

    suspend fun getCompletedStudentsForSchool(schoolCode: String): Int {
        return studentDao.getCompletedStudentsForSchool(schoolCode)
    }
}

