package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.ScheduleCompareClassroomOptionResponse
import com.example.app_my_university.data.api.model.ScheduleCompareDirectionOptionResponse
import com.example.app_my_university.data.api.model.ScheduleCompareGroupOptionResponse
import com.example.app_my_university.data.api.model.ScheduleCompareInstituteOptionResponse
import com.example.app_my_university.data.api.model.ScheduleCompareRequest
import com.example.app_my_university.data.api.model.ScheduleCompareResultResponse
import com.example.app_my_university.data.api.model.ScheduleCompareTeacherOptionResponse
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.util.AlphabeticalSort.sortedForDisplayRu
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun querySchedule(
        groupId: Long? = null,
        teacherId: Long? = null,
        universityId: Long? = null,
        weekNumber: Int? = null,
        dayOfWeek: Int? = null
    ): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.querySchedule(groupId, teacherId, universityId, weekNumber, dayOfWeek) }

    suspend fun getMySchedule(weekNumber: Int? = null, dayOfWeek: Int? = null): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.getMySchedule(weekNumber, dayOfWeek) }

    suspend fun getLinkedGroupsSchedule(weekNumber: Int? = null, dayOfWeek: Int? = null): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.getLinkedGroupsSchedule(weekNumber, dayOfWeek) }

    suspend fun getGroupSchedule(groupId: Long, weekNumber: Int? = null, dayOfWeek: Int? = null): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.getGroupSchedule(groupId, weekNumber, dayOfWeek) }

    suspend fun getTeacherSchedule(teacherId: Long, weekNumber: Int? = null, dayOfWeek: Int? = null): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.getTeacherSchedule(teacherId, weekNumber, dayOfWeek) }

    suspend fun getScheduleById(id: Long): Result<ScheduleResponse> =
        safeApiCall { apiService.getScheduleById(id) }

    suspend fun getClassroomSchedule(
        classroomId: Long,
        weekNumber: Int? = null,
        dayOfWeek: Int? = null
    ): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.getClassroomSchedule(classroomId, weekNumber, dayOfWeek) }

    suspend fun compareSchedule(body: ScheduleCompareRequest): Result<ScheduleCompareResultResponse> =
        safeApiCall { apiService.compareSchedule(body) }

    suspend fun listCompareInstitutes(universityId: Long? = null): Result<List<ScheduleCompareInstituteOptionResponse>> =
        safeApiCall { apiService.listScheduleCompareInstitutes(universityId) }.map { it.sortedForDisplayRu() }

    suspend fun listCompareDirections(universityId: Long?, instituteId: Long): Result<List<ScheduleCompareDirectionOptionResponse>> =
        safeApiCall { apiService.listScheduleCompareDirections(universityId, instituteId) }.map { it.sortedForDisplayRu() }

    suspend fun listCompareGroups(
        universityId: Long? = null,
        instituteId: Long? = null,
        directionId: Long? = null,
        q: String? = null
    ): Result<List<ScheduleCompareGroupOptionResponse>> =
        safeApiCall { apiService.listScheduleCompareGroups(universityId, instituteId, directionId, q) }.map { it.sortedForDisplayRu() }

    suspend fun listCompareTeachers(universityId: Long? = null, q: String? = null): Result<List<ScheduleCompareTeacherOptionResponse>> =
        safeApiCall { apiService.listScheduleCompareTeachers(universityId, q) }.map { it.sortedForDisplayRu() }

    suspend fun listCompareClassrooms(universityId: Long? = null, q: String? = null): Result<List<ScheduleCompareClassroomOptionResponse>> =
        safeApiCall { apiService.listScheduleCompareClassrooms(universityId, q) }.map { it.sortedForDisplayRu() }
}
