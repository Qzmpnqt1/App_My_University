package com.example.app_my_university.data.api.model

// Responses

data class UniversityResponse(
    val id: Long,
    val name: String,
    val shortName: String?,
    val city: String?
)

data class InstituteResponse(
    val id: Long,
    val name: String,
    val shortName: String?,
    val universityId: Long,
    val universityName: String?
)

data class StudyDirectionResponse(
    val id: Long,
    val name: String,
    val code: String?,
    val instituteId: Long,
    val instituteName: String?
)

data class AcademicGroupResponse(
    val id: Long,
    val name: String,
    val course: Int?,
    val yearOfAdmission: Int?,
    val directionId: Long,
    val directionName: String?
)

data class SubjectResponse(
    val id: Long,
    val name: String
)

data class SubjectInDirectionResponse(
    val id: Long,
    val subjectId: Long,
    val subjectName: String?,
    val directionId: Long,
    val directionName: String?,
    val course: Int?,
    val semester: Int?,
    /** EXAM — итог 2–5; CREDIT — зачёт/незачёт */
    val finalAssessmentType: String? = null
)

data class SubjectPracticeResponse(
    val id: Long,
    val subjectDirectionId: Long,
    val practiceNumber: Int?,
    val practiceTitle: String?,
    val maxGrade: Int?,
    val isCredit: Boolean?
)

data class SubjectLessonTypeResponse(
    val id: Long,
    val subjectDirectionId: Long,
    val lessonType: String?
)

data class TeacherSubjectResponse(
    val id: Long,
    val teacherId: Long,
    val teacherName: String?,
    val subjectId: Long,
    val subjectName: String?
)

data class ClassroomResponse(
    val id: Long,
    val building: String?,
    val roomNumber: String?,
    val capacity: Int?,
    val universityId: Long
)

// Requests

data class UniversityRequest(
    val name: String,
    val shortName: String?,
    val city: String?
)

data class InstituteRequest(
    val name: String,
    val shortName: String?,
    val universityId: Long
)

data class StudyDirectionRequest(
    val name: String,
    val code: String?,
    val instituteId: Long
)

data class AcademicGroupRequest(
    val name: String,
    val course: Int,
    val yearOfAdmission: Int,
    val directionId: Long
)

data class SubjectRequest(
    val name: String
)

data class ClassroomRequest(
    val building: String,
    val roomNumber: String,
    val capacity: Int?,
    val universityId: Long
)

data class SubjectInDirectionRequest(
    val subjectId: Long,
    val directionId: Long,
    val course: Int,
    val semester: Int,
    val finalAssessmentType: String? = null
)

data class SubjectLessonTypeRequest(
    val subjectDirectionId: Long,
    val lessonType: String
)

data class SubjectPracticeRequest(
    val subjectDirectionId: Long,
    val practiceNumber: Int,
    val practiceTitle: String,
    val maxGrade: Int?,
    val isCredit: Boolean?
)

data class TeacherSubjectRequest(
    val teacherId: Long,
    val subjectId: Long
)
