package com.example.app_my_university.data.api.model

data class ScheduleResponse(
    val id: Long,
    val subjectTypeId: Long?,
    val subjectName: String?,
    val lessonType: String?,
    val teacherId: Long?,
    val teacherName: String?,
    val groupId: Long?,
    val groupName: String?,
    val classroomId: Long?,
    val classroomInfo: String?,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekNumber: Int?
)

data class ScheduleRequest(
    val subjectTypeId: Long,
    val teacherId: Long,
    val groupId: Long,
    val classroomId: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekNumber: Int
)
