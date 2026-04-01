package com.example.app_my_university.data.api.model

data class GradeResponse(
    val id: Long,
    val studentId: Long,
    val studentName: String?,
    val subjectDirectionId: Long,
    val subjectName: String?,
    val grade: Int?,
    val creditStatus: Boolean?,
    val finalAssessmentType: String? = null
)

data class GradeRequest(
    val studentId: Long,
    val subjectDirectionId: Long,
    val grade: Int?,
    val creditStatus: Boolean?
)

data class PracticeGradeResponse(
    val id: Long,
    val studentId: Long,
    val studentName: String?,
    val practiceId: Long,
    val practiceTitle: String?,
    val practiceNumber: Int?,
    val grade: Int?,
    val creditStatus: Boolean?,
    val maxGrade: Int?,
    val practiceIsCredit: Boolean?
)

data class PracticeGradeRequest(
    val studentId: Long,
    val practiceId: Long,
    val grade: Int?,
    val creditStatus: Boolean?
)

data class TeacherJournalResponse(
    val subjectDirectionId: Long?,
    val subjectName: String?,
    val students: List<TeacherJournalStudentRow>?
)

data class TeacherJournalStudentRow(
    val studentUserId: Long?,
    val studentName: String?,
    val groupName: String?,
    val finalGrade: GradeResponse?,
    val practiceGrades: List<PracticeGradeResponse>?
)
