package com.example.app_my_university.data.api.model

import com.google.gson.annotations.SerializedName

data class StudentPerformanceSummaryResponse(
    val courseFilter: Int?,
    val semesterFilter: Int?,
    val plannedSubjects: Int,
    val subjectsWithFinalResult: Int,
    val subjectsCredited: Int,
    val averageNumericGrade: Double?,
    val totalPractices: Int,
    val practicesWithResult: Int,
    val subjectCompletionPercent: Double,
    val practiceCompletionPercent: Double,
    val subjectPracticeProgressByDiscipline: List<SubjectPracticeProgressItem>? = null,
)

data class SubjectPracticeProgressItem(
    val subjectDirectionId: Long?,
    val subjectName: String?,
    val course: Int?,
    val semester: Int?,
    val totalPractices: Int,
    val practicesWithResult: Int,
    val practiceProgressPercent: Double,
    val sumNumericPracticePoints: Int? = null,
)

data class SubjectStatisticsResponse(
    val subjectDirectionId: Long?,
    val directionId: Long? = null,
    val groupIdFilter: Long? = null,
    val samplingScope: String? = null,
    val averagePerformanceScope: String? = null,
    val subjectName: String?,
    val assessmentType: String? = null,
    val averageGrade: Double,
    val medianGrade: Double,
    val creditRate: Double,
    val totalStudents: Int,
    val gradedStudents: Int,
    val missingValues: Int,
    val gradeDistribution: Map<String, Long>?
)

data class PracticeStatisticsResponse(
    val subjectDirectionId: Long?,
    val directionId: Long? = null,
    val groupIdFilter: Long? = null,
    val samplingScope: String? = null,
    val totalRequiredStudents: Int = 0,
    val subjectName: String?,
    val overallProgress: Double,
    val totalScoreAverage: Double,
    val averageNormalizedPercentAcrossNumericPractices: Double? = null,
    val completionPercentage: Double,
    val totalPractices: Int,
    val countedValues: Int,
    val missingValues: Int,
    val practices: List<PracticeStatisticsDetail>?
)

data class PracticeStatisticsDetail(
    val practiceId: Long?,
    val practiceNumber: Int,
    val practiceTitle: String?,
    val totalRecords: Int,
    val totalRequiredStudents: Int = 0,
    val withResult: Int,
    val completionRate: Double,
    val averageGrade: Double,
    val creditRate: Double?,
    val normalizedAverage: Double?,
    val averageNormalizedPercent: Double? = null,
)

data class GroupStatisticsResponse(
    val groupId: Long?,
    val groupName: String?,
    val averagePerformanceScope: String? = null,
    val averagePerformance: Double,
    val debtRate: Double,
    val studentCount: Int,
    val studentsWithDebt: Int,
    val countedValues: Int,
    val missingValues: Long,
    val averageBySubject: Map<String, Double>?,
    val creditPassPercentBySubject: Map<String, Double>? = null
)

data class DirectionStatisticsResponse(
    val directionId: Long?,
    val directionName: String?,
    val averagePerformanceScope: String? = null,
    val averagePerformance: Double,
    val debtRate: Double,
    val totalStudents: Int,
    val studentsWithDebt: Int,
    val groupCount: Int,
    val groups: List<DirectionGroupSummary>?
)

data class DirectionGroupSummary(
    val groupId: Long?,
    val groupName: String?,
    val averagePerformance: Double,
    val debtRate: Double,
    val studentCount: Int
)

data class InstituteStatisticsResponse(
    val instituteId: Long?,
    val instituteName: String?,
    val averagePerformanceScope: String? = null,
    val averagePerformance: Double,
    val debtRate: Double,
    val totalStudents: Int,
    val studentsWithDebt: Int,
    val directionCount: Int,
    val directions: List<InstituteDirectionSummary>?
)

data class InstituteDirectionSummary(
    val directionId: Long?,
    val directionName: String?,
    val averagePerformance: Double,
    val studentCount: Int
)

data class UniversityStatisticsResponse(
    val universityId: Long?,
    val universityName: String?,
    val averagePerformanceScope: String? = null,
    val averagePerformance: Double,
    val debtRate: Double,
    val totalStudents: Int,
    val studentsWithDebt: Int,
    val instituteCount: Int,
    val institutes: List<UniversityInstituteSummary>?
)

data class UniversityInstituteSummary(
    val instituteId: Long?,
    val instituteName: String?,
    val averagePerformance: Double,
    val studentCount: Int
)

data class ScheduleStatisticsResponse(
    val scope: String?,
    val entityId: Long?,
    val weekNumberFilter: Int? = null,
    val totalLessons: Int,
    val totalHours: Double,
    @SerializedName("byDayOfWeek") val byDayOfWeek: Map<String, Long>?,
    @SerializedName("byWeekNumber") val byWeekNumber: Map<String, Long>?
)

data class AuditLogResponse(
    val id: Long,
    val userId: Long?,
    val action: String?,
    val entityType: String?,
    val entityId: Long?,
    val details: String?,
    val createdAt: String?
)
