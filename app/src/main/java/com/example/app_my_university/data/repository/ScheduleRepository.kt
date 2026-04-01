package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.ScheduleResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun querySchedule(
        groupId: Long? = null,
        teacherId: Long? = null,
        weekNumber: Int? = null,
        dayOfWeek: Int? = null
    ): Result<List<ScheduleResponse>> =
        safeApiCall { apiService.querySchedule(groupId, teacherId, weekNumber, dayOfWeek) }

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
}
