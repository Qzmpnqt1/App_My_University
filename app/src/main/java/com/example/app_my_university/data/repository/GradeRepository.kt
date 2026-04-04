package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getMyGrades(): Result<List<GradeResponse>> =
        safeApiCall { apiService.getMyGrades() }

    suspend fun getGradesByStudent(studentId: Long): Result<List<GradeResponse>> =
        safeApiCall { apiService.getGradesByStudent(studentId) }

    suspend fun getGradesBySubjectDirection(subjectDirectionId: Long): Result<List<GradeResponse>> =
        safeApiCall { apiService.getGradesBySubjectDirection(subjectDirectionId) }

    suspend fun getTeacherJournal(subjectDirectionId: Long): Result<TeacherJournalResponse> =
        safeApiCall { apiService.getTeacherJournal(subjectDirectionId) }

    suspend fun getMyPracticeGrades(subjectDirectionId: Long? = null): Result<List<PracticeGradeResponse>> =
        safeApiCall { apiService.getMyPracticeGrades(subjectDirectionId) }

    suspend fun getPracticeGradesByPractice(practiceId: Long): Result<List<PracticeGradeResponse>> =
        safeApiCall { apiService.getPracticeGradesByPractice(practiceId) }

    suspend fun createGrade(request: GradeRequest): Result<GradeResponse> =
        safeApiCall { apiService.createGrade(request) }

    suspend fun updateGrade(id: Long, request: GradeRequest): Result<GradeResponse> =
        safeApiCall { apiService.updateGrade(id, request) }

    suspend fun createPracticeGrade(request: PracticeGradeRequest): Result<PracticeGradeResponse> =
        safeApiCall { apiService.createPracticeGrade(request) }

    suspend fun updatePracticeGrade(id: Long, request: PracticeGradeRequest): Result<PracticeGradeResponse> =
        safeApiCall { apiService.updatePracticeGrade(id, request) }

    suspend fun getTeacherGradingInstitutes(): Result<List<TeacherGradingPickResponse>> =
        safeApiCall { apiService.getTeacherGradingInstitutes() }

    suspend fun getTeacherGradingDirections(instituteId: Long): Result<List<TeacherGradingPickResponse>> =
        safeApiCall { apiService.getTeacherGradingDirections(instituteId) }

    suspend fun getTeacherGradingSubjectDirections(directionId: Long): Result<List<SubjectInDirectionResponse>> =
        safeApiCall { apiService.getTeacherGradingSubjectDirections(directionId) }

    suspend fun getTeacherGradingGroups(subjectDirectionId: Long): Result<List<TeacherGradingPickResponse>> =
        safeApiCall { apiService.getTeacherGradingGroups(subjectDirectionId) }

    suspend fun getTeacherGradingStudents(subjectDirectionId: Long, groupId: Long): Result<List<TeacherGradingPickResponse>> =
        safeApiCall { apiService.getTeacherGradingStudents(subjectDirectionId, groupId) }

    suspend fun getTeacherStudentAssessment(
        subjectDirectionId: Long,
        groupId: Long,
        studentUserId: Long,
    ): Result<TeacherStudentAssessmentResponse> =
        safeApiCall {
            apiService.getTeacherStudentAssessment(subjectDirectionId, groupId, studentUserId)
        }
}
