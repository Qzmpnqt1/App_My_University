package com.example.app_my_university.util

import com.example.app_my_university.data.api.model.*
import java.text.Collator
import java.util.Locale
import kotlin.jvm.JvmName

/**
 * Единая защитная сортировка для списков сущностей на клиенте (ru-RU, без учёта регистра).
 */
object AlphabeticalSort {

    private val collator: Collator = Collator.getInstance(Locale("ru", "RU")).apply {
        strength = Collator.PRIMARY
    }

    fun compareText(a: String?, b: String?): Int = collator.compare(a ?: "", b ?: "")

    private fun compareNullableIntNullsLast(a: Int?, b: Int?): Int = when {
        a == null && b == null -> 0
        a == null -> 1
        b == null -> -1
        else -> a.compareTo(b)
    }

    fun chatContactSortKey(c: ChatContactResponse): String {
        val parts = listOfNotNull(c.lastName, c.firstName, c.middleName).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(" ") else c.email
    }

    fun userProfileSortKey(u: UserProfileResponse): String {
        val parts = listOfNotNull(u.lastName, u.firstName, u.middleName).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(" ") else u.email
    }

    @JvmName("sortedUniversitiesForDisplayRu")
    fun List<UniversityResponse>.sortedForDisplayRu(): List<UniversityResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else a.id.compareTo(b.id)
        }

    @JvmName("sortedInstitutesForDisplayRu")
    fun List<InstituteResponse>.sortedForDisplayRu(): List<InstituteResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else a.id.compareTo(b.id)
        }

    @JvmName("sortedStudyDirectionsForDisplayRu")
    fun List<StudyDirectionResponse>.sortedForDisplayRu(): List<StudyDirectionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else {
                val c2 = compareText(a.code, b.code)
                if (c2 != 0) c2 else a.id.compareTo(b.id)
            }
        }

    @JvmName("sortedAcademicGroupsForDisplayRu")
    fun List<AcademicGroupResponse>.sortedForDisplayRu(): List<AcademicGroupResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else a.id.compareTo(b.id)
        }

    @JvmName("sortedSubjectsForDisplayRu")
    fun List<SubjectResponse>.sortedForDisplayRu(): List<SubjectResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else a.id.compareTo(b.id)
        }

    @JvmName("sortedSubjectInDirectionsForDisplayRu")
    fun List<SubjectInDirectionResponse>.sortedForDisplayRu(): List<SubjectInDirectionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.subjectName, b.subjectName)
            if (c != 0) return@sortedWith c
            val c1 = compareValues(a.course, b.course)
            if (c1 != 0) return@sortedWith c1
            val c2 = compareValues(a.semester, b.semester)
            if (c2 != 0) return@sortedWith c2
            val c3 = compareText(a.directionName, b.directionName)
            if (c3 != 0) return@sortedWith c3
            a.id.compareTo(b.id)
        }

    @JvmName("sortedSubjectLessonTypesForDisplayRu")
    fun List<SubjectLessonTypeResponse>.sortedForDisplayRu(): List<SubjectLessonTypeResponse> =
        sortedWith { a, b ->
            val c = compareText(a.lessonType, b.lessonType)
            if (c != 0) c else a.id.compareTo(b.id)
        }

    /** Учебный порядок практик: номер, затем название. */
    @JvmName("sortedSubjectPracticesBySequenceRu")
    fun List<SubjectPracticeResponse>.sortedByPracticeSequenceRu(): List<SubjectPracticeResponse> =
        sortedWith { a, b ->
            val n = compareNullableIntNullsLast(a.practiceNumber, b.practiceNumber)
            if (n != 0) return@sortedWith n
            val t = compareText(a.practiceTitle, b.practiceTitle)
            if (t != 0) return@sortedWith t
            a.id.compareTo(b.id)
        }

    @JvmName("sortedTeacherSubjectsForDisplayRu")
    fun List<TeacherSubjectResponse>.sortedForDisplayRu(): List<TeacherSubjectResponse> =
        sortedWith { a, b ->
            val c = compareText(a.subjectName, b.subjectName)
            if (c != 0) return@sortedWith c
            val c2 = compareText(a.directionName, b.directionName)
            if (c2 != 0) return@sortedWith c2
            val c3 = compareText(a.teacherName, b.teacherName)
            if (c3 != 0) return@sortedWith c3
            a.id.compareTo(b.id)
        }

    @JvmName("sortedClassroomsForDisplayRu")
    fun List<ClassroomResponse>.sortedForDisplayRu(): List<ClassroomResponse> =
        sortedWith { a, b ->
            val c = compareText(a.building, b.building)
            if (c != 0) return@sortedWith c
            val c2 = compareText(a.roomNumber, b.roomNumber)
            if (c2 != 0) return@sortedWith c2
            a.id.compareTo(b.id)
        }

    @JvmName("sortedUserProfilesForDisplayRu")
    fun List<UserProfileResponse>.sortedForDisplayRu(): List<UserProfileResponse> =
        sortedWith { a, b ->
            val c = compareText(userProfileSortKey(a), userProfileSortKey(b))
            if (c != 0) c else a.id.compareTo(b.id)
        }

    @JvmName("sortedChatContactsForDisplayRu")
    fun List<ChatContactResponse>.sortedForDisplayRu(): List<ChatContactResponse> =
        sortedWith { a, b ->
            val c = compareText(chatContactSortKey(a), chatContactSortKey(b))
            if (c != 0) c else a.id.compareTo(b.id)
        }

    @JvmName("sortedTeacherGradingPicksForDisplayRu")
    fun List<TeacherGradingPickResponse>.sortedForDisplayRu(): List<TeacherGradingPickResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else a.id.compareTo(b.id)
        }

    fun List<GradeResponse>.sortedBySubjectPlanRu(): List<GradeResponse> =
        sortedWith { a, b ->
            val c = compareText(a.subjectName, b.subjectName)
            if (c != 0) return@sortedWith c
            val c1 = compareValues(a.course, b.course)
            if (c1 != 0) return@sortedWith c1
            val c2 = compareValues(a.semester, b.semester)
            if (c2 != 0) return@sortedWith c2
            a.id.compareTo(b.id)
        }

    @JvmName("sortedGradesByStudentNameRu")
    fun List<GradeResponse>.sortedByStudentNameRu(): List<GradeResponse> =
        sortedWith { a, b ->
            val c = compareText(a.studentName, b.studentName)
            if (c != 0) c else a.studentId.compareTo(b.studentId)
        }

    @JvmName("sortedPracticeGradesByStudentNameRu")
    fun List<PracticeGradeResponse>.sortedByStudentNameRu(): List<PracticeGradeResponse> =
        sortedWith { a, b ->
            val c = compareText(a.studentName, b.studentName)
            if (c != 0) c else a.studentId.compareTo(b.studentId)
        }

    @JvmName("sortedPracticeGradesByPracticeSequenceRu")
    fun List<PracticeGradeResponse>.sortedByPracticeSequenceRu(): List<PracticeGradeResponse> =
        sortedWith { a, b ->
            val n = compareNullableIntNullsLast(a.practiceNumber, b.practiceNumber)
            if (n != 0) return@sortedWith n
            val t = compareText(a.practiceTitle, b.practiceTitle)
            if (t != 0) return@sortedWith t
            a.id.compareTo(b.id)
        }

    @JvmName("sortedStudentPracticeSlotsBySequenceRu")
    fun List<StudentPracticeSlotResponse>.sortedByPracticeSequenceRu(): List<StudentPracticeSlotResponse> =
        sortedWith { a, b ->
            val n = compareNullableIntNullsLast(a.practiceNumber, b.practiceNumber)
            if (n != 0) return@sortedWith n
            a.practiceId.compareTo(b.practiceId)
        }

    @JvmName("sortedScheduleCompareInstitutesForDisplayRu")
    fun List<ScheduleCompareInstituteOptionResponse>.sortedForDisplayRu(): List<ScheduleCompareInstituteOptionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) c else compareValues(a.id, b.id)
        }

    @JvmName("sortedScheduleCompareDirectionsForDisplayRu")
    fun List<ScheduleCompareDirectionOptionResponse>.sortedForDisplayRu(): List<ScheduleCompareDirectionOptionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) return@sortedWith c
            val c2 = compareText(a.code, b.code)
            if (c2 != 0) return@sortedWith c2
            compareValues(a.id, b.id)
        }

    @JvmName("sortedScheduleCompareGroupsForDisplayRu")
    fun List<ScheduleCompareGroupOptionResponse>.sortedForDisplayRu(): List<ScheduleCompareGroupOptionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.name, b.name)
            if (c != 0) return@sortedWith c
            val c2 = compareText(a.directionName, b.directionName)
            if (c2 != 0) return@sortedWith c2
            compareValues(a.id, b.id)
        }

    @JvmName("sortedScheduleCompareTeachersForDisplayRu")
    fun List<ScheduleCompareTeacherOptionResponse>.sortedForDisplayRu(): List<ScheduleCompareTeacherOptionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.displayName, b.displayName)
            if (c != 0) c else compareValues(a.userId, b.userId)
        }

    @JvmName("sortedScheduleCompareClassroomsForDisplayRu")
    fun List<ScheduleCompareClassroomOptionResponse>.sortedForDisplayRu(): List<ScheduleCompareClassroomOptionResponse> =
        sortedWith { a, b ->
            val c = compareText(a.building, b.building)
            if (c != 0) return@sortedWith c
            val c2 = compareText(a.roomNumber, b.roomNumber)
            if (c2 != 0) return@sortedWith c2
            compareValues(a.id, b.id)
        }

    fun PracticeStatisticsResponse.withSortedPracticeDetailsRu(): PracticeStatisticsResponse =
        copy(practices = practices?.sortedWith { a, b ->
            val n = a.practiceNumber.compareTo(b.practiceNumber)
            if (n != 0) return@sortedWith n
            val t = compareText(a.practiceTitle, b.practiceTitle)
            if (t != 0) return@sortedWith t
            compareValues(a.practiceId, b.practiceId)
        })

    fun DirectionStatisticsResponse.withSortedGroupsRu(): DirectionStatisticsResponse =
        copy(groups = groups?.sortedWith { a, b ->
            val c = compareText(a.groupName, b.groupName)
            if (c != 0) c else compareValues(a.groupId, b.groupId)
        })

    fun InstituteStatisticsResponse.withSortedDirectionsRu(): InstituteStatisticsResponse =
        copy(directions = directions?.sortedWith { a, b ->
            val c = compareText(a.directionName, b.directionName)
            if (c != 0) c else compareValues(a.directionId, b.directionId)
        })

    fun UniversityStatisticsResponse.withSortedInstitutesRu(): UniversityStatisticsResponse =
        copy(institutes = institutes?.sortedWith { a, b ->
            val c = compareText(a.instituteName, b.instituteName)
            if (c != 0) c else compareValues(a.instituteId, b.instituteId)
        })

    fun TeacherJournalResponse.normalizedForDisplayRu(): TeacherJournalResponse =
        copy(
            students = students
                ?.map { row ->
                    row.copy(
                        practiceGrades = row.practiceGrades?.sortedWith { a, b ->
                            val n = compareNullableIntNullsLast(a.practiceNumber, b.practiceNumber)
                            if (n != 0) return@sortedWith n
                            a.practiceId.compareTo(b.practiceId)
                        }
                    )
                }
                ?.sortedWith { a, b ->
                    val c = compareText(a.studentName, b.studentName)
                    if (c != 0) c else compareValues(a.studentUserId, b.studentUserId)
                }
        )

    fun TeacherStudentAssessmentResponse.normalizedForDisplayRu(): TeacherStudentAssessmentResponse =
        copy(
            practices = practices?.sortedWith { a, b ->
                val n = compareNullableIntNullsLast(a.practiceNumber, b.practiceNumber)
                if (n != 0) return@sortedWith n
                a.practiceId.compareTo(b.practiceId)
            }
        )
}
