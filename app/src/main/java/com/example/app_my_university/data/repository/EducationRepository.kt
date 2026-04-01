package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getUniversities(): Result<List<UniversityResponse>> = safeApiCall { apiService.getUniversities() }
    suspend fun getUniversity(id: Long): Result<UniversityResponse> = safeApiCall { apiService.getUniversity(id) }
    suspend fun createUniversity(request: UniversityRequest): Result<UniversityResponse> = safeApiCall { apiService.createUniversity(request) }
    suspend fun updateUniversity(id: Long, request: UniversityRequest): Result<UniversityResponse> =
        safeApiCall { apiService.updateUniversity(id, request) }
    suspend fun deleteUniversity(id: Long): Result<Unit> = safeApiCall { apiService.deleteUniversity(id) }

    suspend fun getInstitutes(universityId: Long? = null): Result<List<InstituteResponse>> = safeApiCall { apiService.getInstitutes(universityId) }
    suspend fun getInstitute(id: Long): Result<InstituteResponse> = safeApiCall { apiService.getInstitute(id) }
    suspend fun createInstitute(request: InstituteRequest): Result<InstituteResponse> = safeApiCall { apiService.createInstitute(request) }
    suspend fun updateInstitute(id: Long, request: InstituteRequest): Result<InstituteResponse> =
        safeApiCall { apiService.updateInstitute(id, request) }
    suspend fun deleteInstitute(id: Long): Result<Unit> = safeApiCall { apiService.deleteInstitute(id) }

    suspend fun getDirections(instituteId: Long? = null): Result<List<StudyDirectionResponse>> = safeApiCall { apiService.getDirections(instituteId) }
    suspend fun getDirection(id: Long): Result<StudyDirectionResponse> = safeApiCall { apiService.getDirection(id) }
    suspend fun createDirection(request: StudyDirectionRequest): Result<StudyDirectionResponse> = safeApiCall { apiService.createDirection(request) }
    suspend fun updateDirection(id: Long, request: StudyDirectionRequest): Result<StudyDirectionResponse> =
        safeApiCall { apiService.updateDirection(id, request) }
    suspend fun deleteDirection(id: Long): Result<Unit> = safeApiCall { apiService.deleteDirection(id) }

    suspend fun getGroups(directionId: Long? = null): Result<List<AcademicGroupResponse>> = safeApiCall { apiService.getGroups(directionId) }
    suspend fun getGroup(id: Long): Result<AcademicGroupResponse> = safeApiCall { apiService.getGroup(id) }
    suspend fun createGroup(request: AcademicGroupRequest): Result<AcademicGroupResponse> = safeApiCall { apiService.createGroup(request) }
    suspend fun updateGroup(id: Long, request: AcademicGroupRequest): Result<AcademicGroupResponse> =
        safeApiCall { apiService.updateGroup(id, request) }
    suspend fun deleteGroup(id: Long): Result<Unit> = safeApiCall { apiService.deleteGroup(id) }

    suspend fun getSubjects(): Result<List<SubjectResponse>> = safeApiCall { apiService.getSubjects() }
    suspend fun getSubject(id: Long): Result<SubjectResponse> = safeApiCall { apiService.getSubject(id) }
    suspend fun createSubject(request: SubjectRequest): Result<SubjectResponse> = safeApiCall { apiService.createSubject(request) }
    suspend fun updateSubject(id: Long, request: SubjectRequest): Result<SubjectResponse> =
        safeApiCall { apiService.updateSubject(id, request) }
    suspend fun deleteSubject(id: Long): Result<Unit> = safeApiCall { apiService.deleteSubject(id) }

    suspend fun getSubjectsInDirections(directionId: Long? = null): Result<List<SubjectInDirectionResponse>> =
        safeApiCall { apiService.getSubjectsInDirections(directionId) }
    suspend fun getSubjectInDirection(id: Long): Result<SubjectInDirectionResponse> = safeApiCall { apiService.getSubjectInDirection(id) }
    suspend fun createSubjectInDirection(request: SubjectInDirectionRequest): Result<SubjectInDirectionResponse> =
        safeApiCall { apiService.createSubjectInDirection(request) }
    suspend fun updateSubjectInDirection(id: Long, request: SubjectInDirectionRequest): Result<SubjectInDirectionResponse> =
        safeApiCall { apiService.updateSubjectInDirection(id, request) }
    suspend fun deleteSubjectInDirection(id: Long): Result<Unit> = safeApiCall { apiService.deleteSubjectInDirection(id) }

    suspend fun getSubjectLessonTypes(subjectDirectionId: Long? = null): Result<List<SubjectLessonTypeResponse>> =
        safeApiCall { apiService.getSubjectLessonTypes(subjectDirectionId) }
    suspend fun createSubjectLessonType(request: SubjectLessonTypeRequest): Result<SubjectLessonTypeResponse> =
        safeApiCall { apiService.createSubjectLessonType(request) }
    suspend fun deleteSubjectLessonType(id: Long): Result<Unit> = safeApiCall { apiService.deleteSubjectLessonType(id) }

    suspend fun getSubjectPractices(subjectDirectionId: Long): Result<List<SubjectPracticeResponse>> =
        safeApiCall { apiService.getSubjectPractices(subjectDirectionId) }
    suspend fun getSubjectPractice(id: Long): Result<SubjectPracticeResponse> = safeApiCall { apiService.getSubjectPractice(id) }
    suspend fun createSubjectPractice(request: SubjectPracticeRequest): Result<SubjectPracticeResponse> =
        safeApiCall { apiService.createSubjectPractice(request) }
    suspend fun updateSubjectPractice(id: Long, request: SubjectPracticeRequest): Result<SubjectPracticeResponse> =
        safeApiCall { apiService.updateSubjectPractice(id, request) }
    suspend fun deleteSubjectPractice(id: Long): Result<Unit> = safeApiCall { apiService.deleteSubjectPractice(id) }

    suspend fun getTeacherSubjects(teacherId: Long? = null): Result<List<TeacherSubjectResponse>> =
        safeApiCall { apiService.getTeacherSubjects(teacherId) }
    suspend fun createTeacherSubject(request: TeacherSubjectRequest): Result<TeacherSubjectResponse> =
        safeApiCall { apiService.createTeacherSubject(request) }
    suspend fun deleteTeacherSubject(id: Long): Result<Unit> = safeApiCall { apiService.deleteTeacherSubject(id) }

    suspend fun getClassrooms(universityId: Long? = null): Result<List<ClassroomResponse>> = safeApiCall { apiService.getClassrooms(universityId) }
    suspend fun getClassroom(id: Long): Result<ClassroomResponse> = safeApiCall { apiService.getClassroom(id) }
    suspend fun createClassroom(request: ClassroomRequest): Result<ClassroomResponse> = safeApiCall { apiService.createClassroom(request) }
    suspend fun updateClassroom(id: Long, request: ClassroomRequest): Result<ClassroomResponse> =
        safeApiCall { apiService.updateClassroom(id, request) }
    suspend fun deleteClassroom(id: Long): Result<Unit> = safeApiCall { apiService.deleteClassroom(id) }

    suspend fun querySchedule(
        groupId: Long? = null,
        teacherId: Long? = null,
        weekNumber: Int? = null,
        dayOfWeek: Int? = null
    ): Result<List<ScheduleResponse>> = safeApiCall { apiService.querySchedule(groupId, teacherId, weekNumber, dayOfWeek) }

    suspend fun createSchedule(request: ScheduleRequest): Result<ScheduleResponse> = safeApiCall { apiService.createSchedule(request) }
    suspend fun updateSchedule(id: Long, request: ScheduleRequest): Result<ScheduleResponse> =
        safeApiCall { apiService.updateSchedule(id, request) }
    suspend fun deleteSchedule(id: Long): Result<Unit> = safeApiCall { apiService.deleteSchedule(id) }
    suspend fun getScheduleById(id: Long): Result<ScheduleResponse> = safeApiCall { apiService.getScheduleById(id) }

    suspend fun getRegistrationRequests(
        status: String? = null,
        userType: String? = null,
        universityId: Long? = null,
        instituteId: Long? = null
    ): Result<List<RegistrationRequestResponse>> =
        safeApiCall { apiService.getRegistrationRequests(status, userType, universityId, instituteId) }

    suspend fun getRegistrationRequest(id: Long): Result<RegistrationRequestResponse> =
        safeApiCall { apiService.getRegistrationRequest(id) }

    suspend fun approveRequest(id: Long): Result<Unit> = safeApiCall { apiService.approveRegistrationRequest(id) }
    suspend fun rejectRequest(id: Long, reason: String?): Result<Unit> =
        safeApiCall { apiService.rejectRegistrationRequest(id, RejectRequest(reason)) }

    suspend fun getUsers(
        userType: String? = null,
        isActive: Boolean? = null,
        universityId: Long? = null,
        instituteId: Long? = null,
        groupId: Long? = null
    ): Result<List<UserProfileResponse>> =
        safeApiCall { apiService.getUsers(userType, isActive, universityId, instituteId, groupId) }

    suspend fun getUser(id: Long): Result<UserProfileResponse> = safeApiCall { apiService.getUser(id) }
    suspend fun activateUser(id: Long): Result<Unit> = safeApiCall { apiService.activateUser(id) }
    suspend fun deactivateUser(id: Long): Result<Unit> = safeApiCall { apiService.deactivateUser(id) }
}
