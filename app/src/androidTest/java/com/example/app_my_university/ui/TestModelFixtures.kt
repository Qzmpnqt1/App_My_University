package com.example.app_my_university.ui

import com.example.app_my_university.data.api.model.AcademicGroupResponse
import com.example.app_my_university.data.api.model.AdminProfileInfo
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.data.api.model.ScheduleStatisticsResponse
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import com.example.app_my_university.data.api.model.StudentProfileInfo
import com.example.app_my_university.data.api.model.SubjectStatisticsResponse
import com.example.app_my_university.data.api.model.TeacherProfileInfo
import com.example.app_my_university.data.api.model.TeacherStudentAssessmentResponse
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.api.model.UserProfileResponse

object TestModelFixtures {

    fun adminUserProfile(universityId: Long = 1L): UserProfileResponse =
        UserProfileResponse(
            id = 1L,
            email = "admin@test.ru",
            firstName = "Админ",
            lastName = "Тестов",
            middleName = null,
            userType = "ADMIN",
            isActive = true,
            createdAt = null,
            studentProfile = null,
            teacherProfile = null,
            adminProfile = AdminProfileInfo(
                universityId = universityId,
                universityName = "Тестовый вуз",
                role = "ADMIN",
            ),
        )

    fun superAdminUserProfile(): UserProfileResponse =
        UserProfileResponse(
            id = 2L,
            email = "super@test.ru",
            firstName = "Супер",
            lastName = "Админ",
            middleName = null,
            userType = "SUPER_ADMIN",
            isActive = true,
            createdAt = null,
            studentProfile = null,
            teacherProfile = null,
            adminProfile = AdminProfileInfo(
                universityId = null,
                universityName = null,
                role = "SUPER_ADMIN",
            ),
        )

    fun studentUserProfile(): UserProfileResponse =
        UserProfileResponse(
            id = 10L,
            email = "student@test.ru",
            firstName = "Студент",
            lastName = "Тестов",
            middleName = null,
            userType = "STUDENT",
            isActive = true,
            createdAt = null,
            studentProfile = StudentProfileInfo(
                groupId = 1L,
                groupName = "ИВТ-101",
                course = 1,
                instituteId = 1L,
                instituteName = "ИИТ",
            ),
            teacherProfile = null,
            adminProfile = null,
        )

    fun teacherUserProfile(): UserProfileResponse =
        UserProfileResponse(
            id = 20L,
            email = "teacher@test.ru",
            firstName = "Препод",
            lastName = "Тестов",
            middleName = null,
            userType = "TEACHER",
            isActive = true,
            createdAt = null,
            studentProfile = null,
            teacherProfile = TeacherProfileInfo(
                teacherProfileId = 200L,
                universityId = 1L,
                universityName = "Тестовый вуз",
                instituteId = 1L,
                instituteName = "ИИТ",
                institutesFromAssignments = null,
                position = "Доцент",
            ),
            adminProfile = null,
        )

    fun university(id: Long = 1L): UniversityResponse =
        UniversityResponse(id = id, name = "Тестовый вуз", shortName = "ТВ", city = null)

    fun scheduleEntry(id: Long = 1L): ScheduleResponse =
        ScheduleResponse(
            id = id,
            subjectTypeId = 1L,
            subjectName = "Лекция",
            lessonType = "LECTURE",
            teacherId = 1L,
            teacherName = "Препод",
            groupId = 1L,
            groupName = "ИВТ-101",
            classroomId = 1L,
            classroomInfo = "101",
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:30",
            weekNumber = 1,
        )

    fun academicGroup(id: Long = 1L, directionId: Long = 1L): AcademicGroupResponse =
        AcademicGroupResponse(
            id = id,
            name = "ИВТ-101",
            course = 1,
            yearOfAdmission = 2024,
            directionId = directionId,
            directionName = "ИВТ",
        )

    fun institute(id: Long = 1L, universityId: Long = 1L): InstituteResponse =
        InstituteResponse(
            id = id,
            name = "Институт $id",
            shortName = "И$id",
            universityId = universityId,
            universityName = "Тестовый вуз",
        )

    fun studentPerformanceSummary(): StudentPerformanceSummaryResponse =
        StudentPerformanceSummaryResponse(
            courseFilter = null,
            semesterFilter = null,
            plannedSubjects = 5,
            subjectsWithFinalResult = 2,
            subjectsCredited = 1,
            averageNumericGrade = 4.2,
            totalPractices = 3,
            practicesWithResult = 1,
            subjectCompletionPercent = 40.0,
            practiceCompletionPercent = 33.0,
            subjectPracticeProgressByDiscipline = null,
        )

    fun scheduleStatistics(): ScheduleStatisticsResponse =
        ScheduleStatisticsResponse(
            scope = "TEACHER",
            entityId = 1L,
            weekNumberFilter = null,
            totalLessons = 4,
            totalHours = 8.0,
            byDayOfWeek = mapOf("1" to 2L, "3" to 2L),
            byWeekNumber = mapOf("1" to 4L),
        )

    fun subjectStatistics(): SubjectStatisticsResponse =
        SubjectStatisticsResponse(
            subjectDirectionId = 1L,
            directionId = 1L,
            groupIdFilter = null,
            samplingScope = null,
            averagePerformanceScope = null,
            subjectName = "Тестовая дисциплина",
            assessmentType = "EXAM",
            averageGrade = 4.0,
            medianGrade = 4.0,
            creditRate = 0.8,
            totalStudents = 10,
            gradedStudents = 8,
            missingValues = 2,
            gradeDistribution = mapOf("4" to 5L, "5" to 3L),
        )

    fun numericTeacherAssessment(): TeacherStudentAssessmentResponse =
        TeacherStudentAssessmentResponse(
            subjectDirectionId = 1L,
            directionId = 1L,
            instituteId = 1L,
            groupId = 1L,
            studentUserId = 10L,
            instituteName = "ИИТ",
            directionName = "ИВТ",
            subjectName = "Программирование",
            groupName = "ИВТ-101",
            studentDisplayName = "Студент Тестов",
            finalAssessmentType = "EXAM",
            subjectInDirection = null,
            finalGrade = GradeResponse(
                id = 1L,
                studentId = 10L,
                studentName = "Студент Тестов",
                subjectDirectionId = 1L,
                subjectName = "Программирование",
                grade = 4,
                creditStatus = null,
                finalAssessmentType = "EXAM",
                course = 1,
                semester = 1,
                directionName = "ИВТ",
                practiceCount = 2,
            ),
            practices = null,
        )
}
