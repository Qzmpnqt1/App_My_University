package com.example.app_my_university.data.api

import com.example.app_my_university.data.api.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth / guest registration ─────────────────────────────────
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("api/v1/auth/registration/status")
    suspend fun registrationStatus(@Body request: GuestRegistrationLookupRequest): Response<GuestRegistrationStatusResponse>

    @PUT("api/v1/auth/registration/pending")
    suspend fun updatePendingRegistration(@Body request: UpdatePendingRegistrationRequest): Response<Unit>

    // ── Registration requests (admin) ─────────────────────────────
    @GET("api/v1/registration-requests")
    suspend fun getRegistrationRequests(
        @Query("status") status: String? = null,
        @Query("userType") userType: String? = null,
        @Query("universityId") universityId: Long? = null,
        @Query("instituteId") instituteId: Long? = null
    ): Response<List<RegistrationRequestResponse>>

    @GET("api/v1/registration-requests/{id}")
    suspend fun getRegistrationRequest(@Path("id") id: Long): Response<RegistrationRequestResponse>

    @PUT("api/v1/registration-requests/{id}/approve")
    suspend fun approveRegistrationRequest(@Path("id") id: Long): Response<Unit>

    @PUT("api/v1/registration-requests/{id}/reject")
    suspend fun rejectRegistrationRequest(@Path("id") id: Long, @Body request: RejectRequest): Response<Unit>

    // ── Profile ───────────────────────────────────────────────────
    @GET("api/v1/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @GET("api/v1/profile/me")
    suspend fun getProfileMe(): Response<UserProfileResponse>

    @PUT("api/v1/profile")
    suspend fun updatePersonalProfile(@Body request: UpdatePersonalProfileRequest): Response<Unit>

    @PUT("api/v1/profile/email")
    suspend fun changeEmail(@Body request: ChangeEmailRequest): Response<Unit>

    @PUT("api/v1/profile/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // ── Universities ──────────────────────────────────────────────
    @GET("api/v1/universities")
    suspend fun getUniversities(): Response<List<UniversityResponse>>

    @GET("api/v1/universities/{id}")
    suspend fun getUniversity(@Path("id") id: Long): Response<UniversityResponse>

    @POST("api/v1/universities")
    suspend fun createUniversity(@Body request: UniversityRequest): Response<UniversityResponse>

    @PUT("api/v1/universities/{id}")
    suspend fun updateUniversity(@Path("id") id: Long, @Body request: UniversityRequest): Response<UniversityResponse>

    @DELETE("api/v1/universities/{id}")
    suspend fun deleteUniversity(@Path("id") id: Long): Response<Unit>

    // ── Institutes ────────────────────────────────────────────────
    @GET("api/v1/institutes")
    suspend fun getInstitutes(@Query("universityId") universityId: Long? = null): Response<List<InstituteResponse>>

    @GET("api/v1/institutes/{id}")
    suspend fun getInstitute(@Path("id") id: Long): Response<InstituteResponse>

    @POST("api/v1/institutes")
    suspend fun createInstitute(@Body request: InstituteRequest): Response<InstituteResponse>

    @PUT("api/v1/institutes/{id}")
    suspend fun updateInstitute(@Path("id") id: Long, @Body request: InstituteRequest): Response<InstituteResponse>

    @DELETE("api/v1/institutes/{id}")
    suspend fun deleteInstitute(@Path("id") id: Long): Response<Unit>

    // ── Directions ────────────────────────────────────────────────
    @GET("api/v1/directions")
    suspend fun getDirections(
        @Query("instituteId") instituteId: Long? = null,
        @Query("universityId") universityId: Long? = null
    ): Response<List<StudyDirectionResponse>>

    @GET("api/v1/directions/{id}")
    suspend fun getDirection(@Path("id") id: Long): Response<StudyDirectionResponse>

    @POST("api/v1/directions")
    suspend fun createDirection(@Body request: StudyDirectionRequest): Response<StudyDirectionResponse>

    @PUT("api/v1/directions/{id}")
    suspend fun updateDirection(@Path("id") id: Long, @Body request: StudyDirectionRequest): Response<StudyDirectionResponse>

    @DELETE("api/v1/directions/{id}")
    suspend fun deleteDirection(@Path("id") id: Long): Response<Unit>

    // ── Groups ────────────────────────────────────────────────────
    @GET("api/v1/groups")
    suspend fun getGroups(
        @Query("directionId") directionId: Long? = null,
        @Query("universityId") universityId: Long? = null
    ): Response<List<AcademicGroupResponse>>

    @GET("api/v1/groups/{id}")
    suspend fun getGroup(@Path("id") id: Long): Response<AcademicGroupResponse>

    @POST("api/v1/groups")
    suspend fun createGroup(@Body request: AcademicGroupRequest): Response<AcademicGroupResponse>

    @PUT("api/v1/groups/{id}")
    suspend fun updateGroup(@Path("id") id: Long, @Body request: AcademicGroupRequest): Response<AcademicGroupResponse>

    @DELETE("api/v1/groups/{id}")
    suspend fun deleteGroup(@Path("id") id: Long): Response<Unit>

    // ── Subjects ──────────────────────────────────────────────────
    @GET("api/v1/subjects")
    suspend fun getSubjects(@Query("universityId") universityId: Long? = null): Response<List<SubjectResponse>>

    @GET("api/v1/subjects/{id}")
    suspend fun getSubject(@Path("id") id: Long): Response<SubjectResponse>

    @POST("api/v1/subjects")
    suspend fun createSubject(@Body request: SubjectRequest): Response<SubjectResponse>

    @PUT("api/v1/subjects/{id}")
    suspend fun updateSubject(@Path("id") id: Long, @Body request: SubjectRequest): Response<SubjectResponse>

    @DELETE("api/v1/subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: Long): Response<Unit>

    // ── Subjects in directions ───────────────────────────────────
    @GET("api/v1/subjects-in-directions")
    suspend fun getSubjectsInDirections(
        @Query("directionId") directionId: Long? = null,
        @Query("universityId") universityId: Long? = null
    ): Response<List<SubjectInDirectionResponse>>

    @GET("api/v1/subjects-in-directions/{id}")
    suspend fun getSubjectInDirection(@Path("id") id: Long): Response<SubjectInDirectionResponse>

    @POST("api/v1/subjects-in-directions")
    suspend fun createSubjectInDirection(@Body request: SubjectInDirectionRequest): Response<SubjectInDirectionResponse>

    @PUT("api/v1/subjects-in-directions/{id}")
    suspend fun updateSubjectInDirection(@Path("id") id: Long, @Body request: SubjectInDirectionRequest): Response<SubjectInDirectionResponse>

    @DELETE("api/v1/subjects-in-directions/{id}")
    suspend fun deleteSubjectInDirection(@Path("id") id: Long): Response<Unit>

    // ── Subject lesson types ───────────────────────────────────────
    @GET("api/v1/subject-lesson-types")
    suspend fun getSubjectLessonTypes(@Query("subjectDirectionId") subjectDirectionId: Long? = null): Response<List<SubjectLessonTypeResponse>>

    @POST("api/v1/subject-lesson-types")
    suspend fun createSubjectLessonType(@Body request: SubjectLessonTypeRequest): Response<SubjectLessonTypeResponse>

    @DELETE("api/v1/subject-lesson-types/{id}")
    suspend fun deleteSubjectLessonType(@Path("id") id: Long): Response<Unit>

    // ── Subject practices ─────────────────────────────────────────
    @GET("api/v1/subject-practices")
    suspend fun getSubjectPractices(@Query("subjectDirectionId") subjectDirectionId: Long): Response<List<SubjectPracticeResponse>>

    @GET("api/v1/subject-practices/{id}")
    suspend fun getSubjectPractice(@Path("id") id: Long): Response<SubjectPracticeResponse>

    @POST("api/v1/subject-practices")
    suspend fun createSubjectPractice(@Body request: SubjectPracticeRequest): Response<SubjectPracticeResponse>

    @PUT("api/v1/subject-practices/{id}")
    suspend fun updateSubjectPractice(@Path("id") id: Long, @Body request: SubjectPracticeRequest): Response<SubjectPracticeResponse>

    @DELETE("api/v1/subject-practices/{id}")
    suspend fun deleteSubjectPractice(@Path("id") id: Long): Response<Unit>

    // ── Teacher subjects ──────────────────────────────────────────
    @GET("api/v1/teacher-subjects")
    suspend fun getTeacherSubjects(@Query("teacherId") teacherId: Long? = null): Response<List<TeacherSubjectResponse>>

    @POST("api/v1/teacher-subjects")
    suspend fun createTeacherSubject(@Body request: TeacherSubjectRequest): Response<TeacherSubjectResponse>

    @PUT("api/v1/teacher-subjects/teachers/{teacherProfileId}/assignments")
    suspend fun replaceTeacherAssignments(
        @Path("teacherProfileId") teacherProfileId: Long,
        @Body request: TeacherSubjectReplaceRequest
    ): Response<List<TeacherSubjectResponse>>

    @DELETE("api/v1/teacher-subjects/{id}")
    suspend fun deleteTeacherSubject(@Path("id") id: Long): Response<Unit>

    // ── Classrooms ────────────────────────────────────────────────
    @GET("api/v1/classrooms")
    suspend fun getClassrooms(@Query("universityId") universityId: Long? = null): Response<List<ClassroomResponse>>

    @GET("api/v1/classrooms/{id}")
    suspend fun getClassroom(@Path("id") id: Long): Response<ClassroomResponse>

    @POST("api/v1/classrooms")
    suspend fun createClassroom(@Body request: ClassroomRequest): Response<ClassroomResponse>

    @PUT("api/v1/classrooms/{id}")
    suspend fun updateClassroom(@Path("id") id: Long, @Body request: ClassroomRequest): Response<ClassroomResponse>

    @DELETE("api/v1/classrooms/{id}")
    suspend fun deleteClassroom(@Path("id") id: Long): Response<Unit>

    // ── Schedule ──────────────────────────────────────────────────
    @GET("api/v1/schedule")
    suspend fun querySchedule(
        @Query("groupId") groupId: Long? = null,
        @Query("teacherId") teacherId: Long? = null,
        @Query("universityId") universityId: Long? = null,
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null
    ): Response<List<ScheduleResponse>>

    @GET("api/v1/schedule/my")
    suspend fun getMySchedule(
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null
    ): Response<List<ScheduleResponse>>

    @GET("api/v1/schedule/my/linked-groups")
    suspend fun getLinkedGroupsSchedule(
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null
    ): Response<List<ScheduleResponse>>

    @GET("api/v1/schedule/group/{groupId}")
    suspend fun getGroupSchedule(
        @Path("groupId") groupId: Long,
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null
    ): Response<List<ScheduleResponse>>

    @GET("api/v1/schedule/teacher/{teacherId}")
    suspend fun getTeacherSchedule(
        @Path("teacherId") teacherId: Long,
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null
    ): Response<List<ScheduleResponse>>

    @GET("api/v1/schedule/classroom/{classroomId}")
    suspend fun getClassroomSchedule(
        @Path("classroomId") classroomId: Long,
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null
    ): Response<List<ScheduleResponse>>

    @POST("api/v1/schedule/compare")
    suspend fun compareSchedule(@Body body: ScheduleCompareRequest): Response<ScheduleCompareResultResponse>

    @GET("api/v1/schedule/compare/institutes")
    suspend fun listScheduleCompareInstitutes(
        @Query("universityId") universityId: Long? = null
    ): Response<List<ScheduleCompareInstituteOptionResponse>>

    @GET("api/v1/schedule/compare/directions")
    suspend fun listScheduleCompareDirections(
        @Query("universityId") universityId: Long? = null,
        @Query("instituteId") instituteId: Long
    ): Response<List<ScheduleCompareDirectionOptionResponse>>

    @GET("api/v1/schedule/compare/groups")
    suspend fun listScheduleCompareGroups(
        @Query("universityId") universityId: Long? = null,
        @Query("instituteId") instituteId: Long? = null,
        @Query("directionId") directionId: Long? = null,
        @Query("q") q: String? = null
    ): Response<List<ScheduleCompareGroupOptionResponse>>

    @GET("api/v1/schedule/compare/teachers")
    suspend fun listScheduleCompareTeachers(
        @Query("universityId") universityId: Long? = null,
        @Query("q") q: String? = null
    ): Response<List<ScheduleCompareTeacherOptionResponse>>

    @GET("api/v1/schedule/compare/classrooms")
    suspend fun listScheduleCompareClassrooms(
        @Query("universityId") universityId: Long? = null,
        @Query("q") q: String? = null
    ): Response<List<ScheduleCompareClassroomOptionResponse>>

    @GET("api/v1/schedule/{id}")
    suspend fun getScheduleById(@Path("id") id: Long): Response<ScheduleResponse>

    @POST("api/v1/schedule")
    suspend fun createSchedule(@Body request: ScheduleRequest): Response<ScheduleResponse>

    @PUT("api/v1/schedule/{id}")
    suspend fun updateSchedule(@Path("id") id: Long, @Body request: ScheduleRequest): Response<ScheduleResponse>

    @DELETE("api/v1/schedule/{id}")
    suspend fun deleteSchedule(@Path("id") id: Long): Response<Unit>

    // ── Grades ───────────────────────────────────────────────────
    @GET("api/v1/grades/my")
    suspend fun getMyGrades(): Response<List<GradeResponse>>

    @GET("api/v1/grades/by-student/{studentId}")
    suspend fun getGradesByStudent(@Path("studentId") studentId: Long): Response<List<GradeResponse>>

    @GET("api/v1/grades/by-subject-direction/{subjectDirectionId}")
    suspend fun getGradesBySubjectDirection(@Path("subjectDirectionId") id: Long): Response<List<GradeResponse>>

    @GET("api/v1/grades/journal/{subjectDirectionId}")
    suspend fun getTeacherJournal(@Path("subjectDirectionId") subjectDirectionId: Long): Response<TeacherJournalResponse>

    @POST("api/v1/grades")
    suspend fun createGrade(@Body request: GradeRequest): Response<GradeResponse>

    @PUT("api/v1/grades/{id}")
    suspend fun updateGrade(@Path("id") id: Long, @Body request: GradeRequest): Response<GradeResponse>

    @GET("api/v1/grades/teacher-catalog/institutes")
    suspend fun getTeacherGradingInstitutes(): Response<List<TeacherGradingPickResponse>>

    @GET("api/v1/grades/teacher-catalog/directions")
    suspend fun getTeacherGradingDirections(@Query("instituteId") instituteId: Long): Response<List<TeacherGradingPickResponse>>

    @GET("api/v1/grades/teacher-catalog/subject-directions")
    suspend fun getTeacherGradingSubjectDirections(@Query("directionId") directionId: Long): Response<List<SubjectInDirectionResponse>>

    @GET("api/v1/grades/teacher-catalog/groups")
    suspend fun getTeacherGradingGroups(@Query("subjectDirectionId") subjectDirectionId: Long): Response<List<TeacherGradingPickResponse>>

    @GET("api/v1/grades/teacher-catalog/students")
    suspend fun getTeacherGradingStudents(
        @Query("subjectDirectionId") subjectDirectionId: Long,
        @Query("groupId") groupId: Long,
    ): Response<List<TeacherGradingPickResponse>>

    @GET("api/v1/grades/teacher-catalog/assessment")
    suspend fun getTeacherStudentAssessment(
        @Query("subjectDirectionId") subjectDirectionId: Long,
        @Query("groupId") groupId: Long,
        @Query("studentUserId") studentUserId: Long,
    ): Response<TeacherStudentAssessmentResponse>

    // ── Practice grades ────────────────────────────────────────────
    @GET("api/v1/practice-grades/my")
    suspend fun getMyPracticeGrades(@Query("subjectDirectionId") subjectDirectionId: Long? = null): Response<List<PracticeGradeResponse>>

    @GET("api/v1/practice-grades/my/subject/{subjectDirectionId}/slots")
    suspend fun getMyPracticeSlots(@Path("subjectDirectionId") subjectDirectionId: Long): Response<List<StudentPracticeSlotResponse>>

    @GET("api/v1/practice-grades/by-practice/{practiceId}")
    suspend fun getPracticeGradesByPractice(@Path("practiceId") practiceId: Long): Response<List<PracticeGradeResponse>>

    @POST("api/v1/practice-grades")
    suspend fun createPracticeGrade(@Body request: PracticeGradeRequest): Response<PracticeGradeResponse>

    @PUT("api/v1/practice-grades/{id}")
    suspend fun updatePracticeGrade(@Path("id") id: Long, @Body request: PracticeGradeRequest): Response<PracticeGradeResponse>

    // ── Statistics ─────────────────────────────────────────────────
    @GET("api/v1/statistics/me/student")
    suspend fun getMyStudentStatistics(
        @Query("course") course: Int? = null,
        @Query("semester") semester: Int? = null
    ): Response<StudentPerformanceSummaryResponse>

    @GET("api/v1/statistics/subject/{subjectDirectionId}")
    suspend fun getSubjectStatistics(
        @Path("subjectDirectionId") subjectDirectionId: Long,
        @Query("groupId") groupId: Long? = null,
    ): Response<SubjectStatisticsResponse>

    @GET("api/v1/statistics/practices/{subjectDirectionId}")
    suspend fun getPracticeStatistics(
        @Path("subjectDirectionId") subjectDirectionId: Long,
        @Query("groupId") groupId: Long? = null,
    ): Response<PracticeStatisticsResponse>

    @GET("api/v1/statistics/group/{groupId}")
    suspend fun getGroupStatistics(@Path("groupId") groupId: Long): Response<GroupStatisticsResponse>

    @GET("api/v1/statistics/direction/{directionId}")
    suspend fun getDirectionStatistics(@Path("directionId") directionId: Long): Response<DirectionStatisticsResponse>

    @GET("api/v1/statistics/institute/{instituteId}")
    suspend fun getInstituteStatistics(@Path("instituteId") instituteId: Long): Response<InstituteStatisticsResponse>

    @GET("api/v1/statistics/university/{universityId}")
    suspend fun getUniversityStatistics(@Path("universityId") universityId: Long): Response<UniversityStatisticsResponse>

    @GET("api/v1/statistics/schedule/teacher/{teacherId}")
    suspend fun getTeacherScheduleStatistics(
        @Path("teacherId") teacherId: Long,
        @Query("weekNumber") weekNumber: Int? = null,
    ): Response<ScheduleStatisticsResponse>

    @GET("api/v1/statistics/schedule/group/{groupId}")
    suspend fun getGroupScheduleStatistics(
        @Path("groupId") groupId: Long,
        @Query("weekNumber") weekNumber: Int? = null,
    ): Response<ScheduleStatisticsResponse>

    @GET("api/v1/statistics/schedule/classroom/{classroomId}")
    suspend fun getClassroomScheduleStatistics(
        @Path("classroomId") classroomId: Long,
        @Query("weekNumber") weekNumber: Int? = null,
    ): Response<ScheduleStatisticsResponse>

    // ── Audit ─────────────────────────────────────────────────────
    @GET("api/v1/audit/logs")
    suspend fun getAuditLogs(
        @Query("userId") userId: Long? = null,
        @Query("action") action: String? = null,
        @Query("entityType") entityType: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("universityId") universityId: Long? = null
    ): Response<List<AuditLogResponse>>

    // ── Chat ──────────────────────────────────────────────────────
    @GET("api/v1/chats")
    suspend fun getConversations(): Response<List<ConversationResponse>>

    @GET("api/v1/chats/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<List<MessageResponse>>

    @POST("api/v1/chats/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>

    @PATCH("api/v1/chats/{conversationId}/read")
    suspend fun markAsRead(@Path("conversationId") conversationId: String): Response<Unit>

    @GET("api/v1/chats/contacts")
    suspend fun getChatContacts(): Response<List<ChatContactResponse>>

    // ── Users ─────────────────────────────────────────────────────
    @GET("api/v1/users")
    suspend fun getUsers(
        @Query("userType") userType: String? = null,
        @Query("isActive") isActive: Boolean? = null,
        @Query("universityId") universityId: Long? = null,
        @Query("instituteId") instituteId: Long? = null,
        @Query("groupId") groupId: Long? = null,
        @Query("q") searchQuery: String? = null
    ): Response<List<UserProfileResponse>>

    @GET("api/v1/users/{id}")
    suspend fun getUser(@Path("id") id: Long): Response<UserProfileResponse>

    @PUT("api/v1/users/{id}/activate")
    suspend fun activateUser(@Path("id") id: Long): Response<Unit>

    @PUT("api/v1/users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/users")
    suspend fun createAdminAccount(@Body body: CreateAdminAccountRequest): Response<UserProfileResponse>
}
