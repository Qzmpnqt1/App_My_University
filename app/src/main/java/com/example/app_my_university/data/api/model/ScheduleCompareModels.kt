package com.example.app_my_university.data.api.model

data class ScheduleCompareRequest(
    val mode: String,
    val leftKind: String? = null,
    val leftId: Long? = null,
    val rightKind: String,
    val rightId: Long,
    val weekNumber: Int,
    val dayOfWeek: Int? = null,
)

data class ScheduleCompareResultResponse(
    val weekNumber: Int?,
    val leftLabel: String?,
    val rightLabel: String?,
    val segmentsBothSidesBusy: Int,
    val segmentsOnlyLeft: Int,
    val segmentsOnlyRight: Int,
    val days: List<ScheduleCompareDayResponse>?,
)

data class ScheduleCompareDayResponse(
    val dayOfWeek: Int?,
    val segments: List<ScheduleCompareSegmentResponse>?,
)

data class ScheduleCompareSegmentResponse(
    val segmentStart: String?,
    val segmentEnd: String?,
    val segmentType: String?,
    val leftEntries: List<ScheduleResponse>?,
    val rightEntries: List<ScheduleResponse>?,
)

data class ScheduleCompareInstituteOptionResponse(
    val id: Long?,
    val name: String?,
    val shortName: String?,
)

data class ScheduleCompareDirectionOptionResponse(
    val id: Long?,
    val name: String?,
    val code: String?,
)

data class ScheduleCompareGroupOptionResponse(
    val id: Long?,
    val name: String?,
    val directionName: String?,
    val instituteName: String?,
)

data class ScheduleCompareTeacherOptionResponse(
    val userId: Long?,
    val displayName: String?,
    val instituteName: String?,
    val position: String?,
)

data class ScheduleCompareClassroomOptionResponse(
    val id: Long?,
    val building: String?,
    val roomNumber: String?,
    val capacity: Int?,
    val label: String?,
)
