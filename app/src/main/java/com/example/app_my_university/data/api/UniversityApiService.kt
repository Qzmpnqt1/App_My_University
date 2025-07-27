package com.example.app_my_university.data.api

import com.example.app_my_university.data.api.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UniversityApiService {

    @GET("/api/universities")
    suspend fun getUniversities(): ApiResponse<List<UniversityDTO>>

    @GET("/api/universities/{universityId}/institutes")
    suspend fun getInstitutes(@Path("universityId") universityId: Int): ApiResponse<List<InstituteDTO>>

    @GET("/api/universities/institutes/{instituteId}/directions")
    suspend fun getDirections(@Path("instituteId") instituteId: Int): ApiResponse<List<StudyDirectionDTO>>

    @GET("/api/universities/directions/{directionId}/groups")
    suspend fun getGroups(@Path("directionId") directionId: Int): ApiResponse<List<AcademicGroupDTO>>

    @GET("/api/universities/directions/{directionId}/course/{course}/groups")
    suspend fun getGroupsByCourse(
        @Path("directionId") directionId: Int,
        @Path("course") course: Int
    ): ApiResponse<List<AcademicGroupDTO>>

    @GET("/api/universities/subjects")
    suspend fun getAllSubjects(): ApiResponse<List<SubjectDTO>>

    @GET("/api/universities/{universityId}/subjects")
    suspend fun getSubjectsByUniversity(@Path("universityId") universityId: Int): ApiResponse<List<SubjectDTO>>

    @GET("/api/universities/subjects/search")
    suspend fun searchSubjects(@Query("query") query: String): ApiResponse<List<SubjectDTO>>
} 