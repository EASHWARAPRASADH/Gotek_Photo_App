package com.studentidphotocapture.app.data.dao

import androidx.room.*
import com.studentidphotocapture.app.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student WHERE schoolCode = :schoolCode AND classGrade = :classGrade AND section = :section ORDER BY rollNumber ASC")
    fun getStudentsByClassSection(schoolCode: String, classGrade: String, section: String): Flow<List<Student>>

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
}
