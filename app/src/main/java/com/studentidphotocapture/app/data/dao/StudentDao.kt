package com.studentidphotocapture.app.data.dao

import androidx.room.*
import com.studentidphotocapture.app.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student WHERE schoolCode = :schoolCode AND classGrade = :classGrade AND section = :section ORDER BY rollNumber ASC")
    fun getStudentsByClassSection(schoolCode: String, classGrade: String, section: String): Flow<List<Student>>

    @Query("SELECT * FROM student WHERE id LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR admissionNumber LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT * FROM student WHERE parentMobile = :mobile")
    fun getStudentsByParentMobile(mobile: String): Flow<List<Student>>

    @Query("DELETE FROM student WHERE parentMobile = :mobile AND id != 'SCH01-10-A-1'")
    suspend fun clearDuplicateMockParentStudents(mobile: String)

    @Query("SELECT * FROM student WHERE admissionNumber = :admissionNumber")
    suspend fun getStudentByAdmissionNumber(admissionNumber: String): Student?

    @Query("SELECT * FROM student WHERE id = :id")
    suspend fun getStudentById(id: String): Student?

    @Query("SELECT * FROM student WHERE schoolCode = :schoolCode")
    fun getStudentsBySchool(schoolCode: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM student WHERE schoolCode = :schoolCode AND classGrade = :classGrade AND section = :section AND photoStatus = 'PENDING'")
    fun getPendingStudents(schoolCode: String, classGrade: String, section: String): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM student WHERE schoolCode = :schoolCode AND classGrade = :classGrade AND section = :section")
    suspend fun getTotalStudentsCount(schoolCode: String, classGrade: String, section: String): Int

    @Query("SELECT COUNT(*) FROM student WHERE schoolCode = :schoolCode AND classGrade = :classGrade AND section = :section AND photoStatus != 'PENDING'")
    suspend fun getCompletedStudentsCount(schoolCode: String, classGrade: String, section: String): Int

    @Query("SELECT * FROM student WHERE photoStatus != 'PENDING' ORDER BY photoCapturedAt DESC")
    suspend fun getStudentsWithCapturedPhotos(): List<com.studentidphotocapture.app.data.model.Student>

    @Query("SELECT DISTINCT schoolCode FROM student")
    fun getAllSchoolCodes(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM student WHERE schoolCode = :schoolCode")
    suspend fun getTotalStudentsForSchool(schoolCode: String): Int

    @Query("SELECT COUNT(*) FROM student WHERE schoolCode = :schoolCode AND photoStatus != 'PENDING'")
    suspend fun getCompletedStudentsForSchool(schoolCode: String): Int
}

