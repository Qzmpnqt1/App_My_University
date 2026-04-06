package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.*
import com.example.app_my_university.util.AlphabeticalSort.withSortedDirectionsRu
import com.example.app_my_university.util.AlphabeticalSort.withSortedGroupsRu
import com.example.app_my_university.util.AlphabeticalSort.withSortedInstitutesRu
import com.example.app_my_university.util.AlphabeticalSort.withSortedPracticeDetailsRu
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(private val api: ApiService) {

    suspend fun getMyStudentPerformance(course: Int? = null, semester: Int? = null): Result<StudentPerformanceSummaryResponse> =
        safeApiCall { api.getMyStudentStatistics(course, semester) }

    suspend fun getSubjectStatistics(subjectDirectionId: Long): Result<SubjectStatisticsResponse> =
        safeApiCall { api.getSubjectStatistics(subjectDirectionId) }

    suspend fun getPracticeStatistics(subjectDirectionId: Long): Result<PracticeStatisticsResponse> =
        safeApiCall { api.getPracticeStatistics(subjectDirectionId) }.map { it.withSortedPracticeDetailsRu() }

    suspend fun getGroupStatistics(groupId: Long): Result<GroupStatisticsResponse> =
        safeApiCall { api.getGroupStatistics(groupId) }

    suspend fun getDirectionStatistics(directionId: Long): Result<DirectionStatisticsResponse> =
        safeApiCall { api.getDirectionStatistics(directionId) }.map { it.withSortedGroupsRu() }

    suspend fun getInstituteStatistics(instituteId: Long): Result<InstituteStatisticsResponse> =
        safeApiCall { api.getInstituteStatistics(instituteId) }.map { it.withSortedDirectionsRu() }

    suspend fun getUniversityStatistics(universityId: Long): Result<UniversityStatisticsResponse> =
        safeApiCall { api.getUniversityStatistics(universityId) }.map { it.withSortedInstitutesRu() }

    suspend fun getTeacherScheduleStatistics(teacherId: Long): Result<ScheduleStatisticsResponse> =
        safeApiCall { api.getTeacherScheduleStatistics(teacherId) }

    suspend fun getGroupScheduleStatistics(groupId: Long): Result<ScheduleStatisticsResponse> =
        safeApiCall { api.getGroupScheduleStatistics(groupId) }

    suspend fun getClassroomScheduleStatistics(classroomId: Long): Result<ScheduleStatisticsResponse> =
        safeApiCall { api.getClassroomScheduleStatistics(classroomId) }
}
