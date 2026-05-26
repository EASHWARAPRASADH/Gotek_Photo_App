package com.studentidphotocapture.app.data.api

import com.studentidphotocapture.app.data.model.Student
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface StudentApiService {

    @GET("students/search")
    suspend fun searchStudent(
        @Query("admission_no") admissionNo: String
    ): Response<Student>

    @GET("students")
    suspend fun getStudents(
        @Query("school_code") schoolCode: String,
        @Query("class") classGrade: String,
        @Query("section") section: String
    ): Response<List<Student>>

    @Multipart
    @POST("students/{id}/photo")
    suspend fun uploadPhoto(
        @Path("id") studentId: String,
        @Part photo: MultipartBody.Part
    ): Response<Unit>

    companion object {
        const val BASE_URL = "https://api.student-portal.com/v1/"
    }
}
