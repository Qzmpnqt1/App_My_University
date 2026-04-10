package com.example.app_my_university.ui

import com.example.app_my_university.data.api.model.ClassroomResponse
import com.example.app_my_university.data.api.model.GroupStatisticsResponse
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.MessageResponse
import com.example.app_my_university.data.api.model.PracticeGradeResponse
import com.example.app_my_university.data.api.model.PracticeStatisticsResponse
import com.example.app_my_university.data.api.model.ScheduleCompareResultResponse
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.SubjectResponse
import com.example.app_my_university.data.api.model.TeacherGradingPickResponse
import com.example.app_my_university.data.api.model.TeacherSubjectResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.AuditRepository
import com.example.app_my_university.data.repository.ChatRepository
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.GradeRepository
import com.example.app_my_university.data.repository.NotificationsRepository
import com.example.app_my_university.data.repository.ProfileRepository
import com.example.app_my_university.data.repository.ScheduleRepository
import com.example.app_my_university.data.repository.StatisticsRepository
import com.example.app_my_university.data.theme.AppThemePreference
import com.example.app_my_university.data.theme.ThemePreferenceRepository
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

fun mockTokenManager(
    userType: String = "STUDENT",
    userId: Long = 1L,
    superScopeUniversityId: Long? = null,
): TokenManager {
    val tm = mockk<TokenManager>(relaxed = true)
    every { tm.userId } returns flowOf(userId)
    every { tm.userType } returns flowOf(userType)
    coEvery { tm.getUserType() } returns userType
    coEvery { tm.getUserId() } returns userId
    coEvery { tm.getSuperAdminScopeUniversityId() } returns superScopeUniversityId
    return tm
}

fun mockThemePreferenceRepository(): ThemePreferenceRepository =
    mockk(relaxed = true) {
        every { observeThemePreference() } returns flowOf(AppThemePreference.SYSTEM)
        coEvery { setThemePreference(any()) } returns Unit
    }

fun mockProfileRepository(profile: UserProfileResponse): ProfileRepository =
    mockk(relaxed = true) {
        coEvery { getProfile() } returns Result.success(profile)
        coEvery { getProfileMe() } returns Result.success(profile)
        coEvery { updatePersonalProfile(any(), any(), any()) } returns Result.success(Unit)
        coEvery { changeEmail(any(), any()) } returns Result.success(Unit)
        coEvery { changePassword(any(), any(), any()) } returns Result.success(Unit)
    }

/** Универсальные успешные ответы для админских и регистрационных сценариев. */
fun mockEducationRepository(): EducationRepository {
    val uni = TestModelFixtures.university()
    val inst = TestModelFixtures.institute()
    val direction = StudyDirectionResponse(1L, "ИВТ", "09.03.01", 1L, "ИИТ")
    val group = TestModelFixtures.academicGroup()
    val subject = SubjectResponse(1L, "Дисциплина")
    val sid = SubjectInDirectionResponse(1L, 1L, "Дисциплина", 1L, "ИВТ", 1, 1, "EXAM")
    val classroom = ClassroomResponse(1L, "К1", "101", 30, 1L)
    val teacherAssignment = TeacherSubjectResponse(
        id = 1L,
        teacherId = 200L,
        teacherName = "Препод Тестов",
        subjectDirectionId = 1L,
        subjectId = 1L,
        subjectName = "Дисциплина",
        directionId = 1L,
        directionName = "ИВТ",
        instituteId = 1L,
        instituteName = "ИИТ",
        course = 1,
        semester = 1,
    )
    val scheduleRow = TestModelFixtures.scheduleEntry()
    return mockk(relaxed = true) {
        coEvery { getUniversities() } returns Result.success(listOf(uni))
        coEvery { getUniversity(any()) } returns Result.success(uni)
        coEvery { createUniversity(any()) } returns Result.success(uni)
        coEvery { updateUniversity(any(), any()) } returns Result.success(uni)
        coEvery { deleteUniversity(any()) } returns Result.success(Unit)
        coEvery { getInstitutes(any()) } returns Result.success(listOf(inst))
        coEvery { getInstitute(any()) } returns Result.success(inst)
        coEvery { createInstitute(any()) } returns Result.success(inst)
        coEvery { updateInstitute(any(), any()) } returns Result.success(inst)
        coEvery { deleteInstitute(any()) } returns Result.success(Unit)
        coEvery { getDirections(any(), any()) } returns Result.success(listOf(direction))
        coEvery { getDirection(any()) } returns Result.success(direction)
        coEvery { createDirection(any()) } returns Result.success(direction)
        coEvery { updateDirection(any(), any()) } returns Result.success(direction)
        coEvery { deleteDirection(any()) } returns Result.success(Unit)
        coEvery { getGroups(any(), any()) } returns Result.success(listOf(group))
        coEvery { getGroup(any()) } returns Result.success(group)
        coEvery { createGroup(any()) } returns Result.success(group)
        coEvery { updateGroup(any(), any()) } returns Result.success(group)
        coEvery { deleteGroup(any()) } returns Result.success(Unit)
        coEvery { getSubjects(any()) } returns Result.success(listOf(subject))
        coEvery { getSubject(any()) } returns Result.success(subject)
        coEvery { createSubject(any()) } returns Result.success(subject)
        coEvery { updateSubject(any(), any()) } returns Result.success(subject)
        coEvery { deleteSubject(any()) } returns Result.success(Unit)
        coEvery { getSubjectsInDirections(any(), any()) } returns Result.success(listOf(sid))
        coEvery { getSubjectInDirection(any()) } returns Result.success(sid)
        coEvery { createSubjectInDirection(any()) } returns Result.success(sid)
        coEvery { updateSubjectInDirection(any(), any()) } returns Result.success(sid)
        coEvery { deleteSubjectInDirection(any()) } returns Result.success(Unit)
        coEvery { getClassrooms(any()) } returns Result.success(listOf(classroom))
        coEvery { getClassroom(any()) } returns Result.success(classroom)
        coEvery { createClassroom(any()) } returns Result.success(classroom)
        coEvery { updateClassroom(any(), any()) } returns Result.success(classroom)
        coEvery { deleteClassroom(any()) } returns Result.success(Unit)
        coEvery { getUsers(any(), any(), any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getRegistrationRequests(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { approveRequest(any()) } returns Result.success(Unit)
        coEvery { rejectRequest(any(), any()) } returns Result.success(Unit)
        coEvery { createAdminAccount(any()) } returns Result.success(TestModelFixtures.adminUserProfile())
        coEvery { activateUser(any()) } returns Result.success(Unit)
        coEvery { deactivateUser(any()) } returns Result.success(Unit)
        coEvery { querySchedule(any(), any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { createSchedule(any()) } returns Result.success(scheduleRow)
        coEvery { updateSchedule(any(), any()) } returns Result.success(scheduleRow)
        coEvery { deleteSchedule(any()) } returns Result.success(Unit)
        coEvery { getScheduleById(any()) } returns Result.success(scheduleRow)
        coEvery { getTeacherSubjects(any()) } returns Result.success(listOf(teacherAssignment))
        coEvery { replaceTeacherAssignments(any(), any()) } returns Result.success(listOf(teacherAssignment))
        coEvery { deleteTeacherSubject(any()) } returns Result.success(Unit)
    }
}

fun mockScheduleRepository(): ScheduleRepository {
    val compare = ScheduleCompareResultResponse(
        weekNumber = 1,
        leftLabel = "A",
        rightLabel = "B",
        segmentsBothSidesBusy = 0,
        segmentsOnlyLeft = 0,
        segmentsOnlyRight = 0,
        days = emptyList(),
    )
    return mockk(relaxed = true) {
        coEvery { getMySchedule(any(), any()) } returns Result.success(emptyList())
        coEvery { getLinkedGroupsSchedule(any(), any()) } returns Result.success(emptyList())
        coEvery { getGroupSchedule(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getTeacherSchedule(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getClassroomSchedule(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { compareSchedule(any()) } returns Result.success(compare)
        coEvery { listCompareGroups(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { listCompareTeachers(any(), any()) } returns Result.success(emptyList())
        coEvery { listCompareClassrooms(any(), any()) } returns Result.success(emptyList())
        coEvery { listCompareInstitutes(any()) } returns Result.success(emptyList())
        coEvery { listCompareDirections(any(), any()) } returns Result.success(emptyList())
        coEvery { getScheduleById(any()) } returns Result.success(TestModelFixtures.scheduleEntry())
    }
}

fun mockGradeRepository(): GradeRepository {
    val institutePick = TeacherGradingPickResponse(1L, "ИИТ", null)
    return mockk(relaxed = true) {
        coEvery { getMyGrades() } returns Result.success(emptyList())
        coEvery { getTeacherGradingInstitutes() } returns Result.success(listOf(institutePick))
        coEvery { getTeacherGradingDirections(any()) } returns Result.success(emptyList())
        coEvery { getTeacherGradingSubjectDirections(any()) } returns Result.success(emptyList())
        coEvery { getTeacherGradingGroups(any()) } returns Result.success(emptyList())
        coEvery { getTeacherGradingStudents(any(), any()) } returns Result.success(emptyList())
        coEvery { getTeacherStudentAssessment(any(), any(), any()) } returns Result.success(TestModelFixtures.numericTeacherAssessment())
        coEvery { createGrade(any()) } returns Result.success(
            GradeResponse(
                id = 1L,
                studentId = 1L,
                studentName = "Студент",
                subjectDirectionId = 1L,
                subjectName = "Дисциплина",
                grade = 4,
                creditStatus = null,
                finalAssessmentType = "EXAM",
                course = 1,
                semester = 1,
                directionName = "ИВТ",
                practiceCount = null,
            ),
        )
        coEvery { updateGrade(any(), any()) } returns Result.success(
            GradeResponse(
                id = 1L,
                studentId = 1L,
                studentName = "Студент",
                subjectDirectionId = 1L,
                subjectName = "Дисциплина",
                grade = 4,
                creditStatus = null,
                finalAssessmentType = "EXAM",
                course = 1,
                semester = 1,
                directionName = "ИВТ",
                practiceCount = null,
            ),
        )
        coEvery { createPracticeGrade(any()) } returns Result.success(
            PracticeGradeResponse(
                id = 1L,
                studentId = 1L,
                studentName = "Студент",
                practiceId = 1L,
                practiceTitle = "П1",
                practiceNumber = 1,
                grade = 4,
                creditStatus = null,
                maxGrade = 5,
                practiceIsCredit = false,
            ),
        )
        coEvery { updatePracticeGrade(any(), any()) } returns Result.success(
            PracticeGradeResponse(
                id = 1L,
                studentId = 1L,
                studentName = "Студент",
                practiceId = 1L,
                practiceTitle = "П1",
                practiceNumber = 1,
                grade = 4,
                creditStatus = null,
                maxGrade = 5,
                practiceIsCredit = false,
            ),
        )
    }
}

fun mockStatisticsRepository(): StatisticsRepository {
    val perf = TestModelFixtures.studentPerformanceSummary()
    val schedStats = TestModelFixtures.scheduleStatistics()
    val subj = TestModelFixtures.subjectStatistics()
    val practice = PracticeStatisticsResponse(
        subjectDirectionId = 1L,
        directionId = 1L,
        groupIdFilter = null,
        samplingScope = null,
        subjectName = "Практика",
        overallProgress = 0.5,
        totalScoreAverage = 4.0,
        averageNormalizedPercentAcrossNumericPractices = null,
        completionPercentage = 50.0,
        totalPractices = 2,
        countedValues = 1,
        missingValues = 1,
        practices = emptyList(),
    )
    val groupStats = GroupStatisticsResponse(
        groupId = 1L,
        groupName = "ИВТ-101",
        averagePerformanceScope = null,
        averagePerformance = 4.0,
        debtRate = 0.1,
        studentCount = 20,
        studentsWithDebt = 2,
        countedValues = 18,
        missingValues = 2,
        averageBySubject = mapOf("Дисциплина" to 4.0),
        creditPassPercentBySubject = null,
    )
    return mockk(relaxed = true) {
        // ViewModel вызывает getMyStudentPerformance(null, null); any() в MockK null не матчит.
        coEvery { getMyStudentPerformance(null, null) } returns Result.success(perf)
        coEvery { getMyStudentPerformance(any(), any()) } returns Result.success(perf)
        coEvery { getTeacherScheduleStatistics(any(), any()) } returns Result.success(schedStats)
        coEvery { getSubjectStatistics(any(), any()) } returns Result.success(subj)
        coEvery { getPracticeStatistics(any(), any()) } returns Result.success(practice)
        coEvery { getGroupStatistics(any()) } returns Result.success(groupStats)
        coEvery { getDirectionStatistics(any()) } returns Result.success(
            com.example.app_my_university.data.api.model.DirectionStatisticsResponse(
                directionId = 1L,
                directionName = "ИВТ",
                averagePerformanceScope = null,
                averagePerformance = 4.0,
                debtRate = 0.1,
                totalStudents = 40,
                studentsWithDebt = 4,
                groupCount = 2,
                groups = emptyList(),
            ),
        )
        coEvery { getInstituteStatistics(any()) } returns Result.success(
            com.example.app_my_university.data.api.model.InstituteStatisticsResponse(
                instituteId = 1L,
                instituteName = "ИИТ",
                averagePerformanceScope = null,
                averagePerformance = 4.0,
                debtRate = 0.1,
                totalStudents = 100,
                studentsWithDebt = 10,
                directionCount = 3,
                directions = emptyList(),
            ),
        )
        coEvery { getUniversityStatistics(any()) } returns Result.success(
            com.example.app_my_university.data.api.model.UniversityStatisticsResponse(
                universityId = 1L,
                universityName = "Тестовый вуз",
                averagePerformanceScope = null,
                averagePerformance = 4.0,
                debtRate = 0.1,
                totalStudents = 500,
                studentsWithDebt = 50,
                instituteCount = 5,
                institutes = emptyList(),
            ),
        )
        coEvery { getGroupScheduleStatistics(any(), any()) } returns Result.success(schedStats)
        coEvery { getClassroomScheduleStatistics(any(), any()) } returns Result.success(schedStats)
    }
}

fun mockChatRepository(): ChatRepository {
    val sent = MessageResponse(
        messageId = "m1",
        conversationId = "c1",
        senderId = 1L,
        senderName = "Я",
        text = "Привет",
        sentAt = "2026-01-01T12:00:00",
    )
    return mockk(relaxed = true) {
        coEvery { getConversations() } returns Result.success(emptyList())
        coEvery { getChatContacts() } returns Result.success(emptyList())
        coEvery { getMessages(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { sendMessage(any(), any()) } returns Result.success(sent)
        coEvery { markAsRead(any()) } returns Result.success(Unit)
    }
}

fun mockNotificationsRepository(): NotificationsRepository =
    mockk(relaxed = true) {
        coEvery { getMyNotifications() } returns Result.success(emptyList())
        coEvery { markRead(any()) } returns Result.success(Unit)
        coEvery { markAllRead() } returns Result.success(Unit)
    }

fun mockAuditRepository(): AuditRepository =
    mockk(relaxed = true) {
        coEvery { searchLogs(any(), any(), any(), any(), any(), any()) } returns Result.success(emptyList())
    }

fun adminViewModelCampusAdmin(universityId: Long = 1L): AdminViewModel {
    val edu = mockEducationRepository()
    val chat = mockChatRepository()
    val profile = mockProfileRepository(TestModelFixtures.adminUserProfile(universityId))
    val token = mockTokenManager("ADMIN", 1L, null)
    return AdminViewModel(edu, chat, profile, token)
}

fun adminViewModelSuperAdmin(): AdminViewModel {
    val edu = mockEducationRepository()
    val chat = mockChatRepository()
    val profile = mockProfileRepository(TestModelFixtures.superAdminUserProfile())
    val token = mockTokenManager("SUPER_ADMIN", 2L, null)
    return AdminViewModel(edu, chat, profile, token)
}

/** SUPER_ADMIN с выбранным вузом в scope (как после выбора на главной). */
fun adminViewModelSuperAdminScoped(scopeUniversityId: Long = 1L): AdminViewModel {
    val edu = mockEducationRepository()
    val chat = mockChatRepository()
    val profile = mockProfileRepository(TestModelFixtures.superAdminUserProfile())
    val token = mockTokenManager("SUPER_ADMIN", 2L, scopeUniversityId)
    return AdminViewModel(edu, chat, profile, token)
}
