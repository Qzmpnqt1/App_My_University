package com.example.app_my_university.data.api.model

data class UserProfileResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String,
    val isActive: Boolean?,
    val createdAt: String?,
    val studentProfile: StudentProfileInfo?,
    val teacherProfile: TeacherProfileInfo?,
    val adminProfile: AdminProfileInfo?
)

data class StudentProfileInfo(
    val groupId: Long?,
    val groupName: String?,
    val instituteId: Long?,
    val instituteName: String?
)

data class TeacherProfileInfo(
    val instituteId: Long?,
    val instituteName: String?,
    val position: String?
)

data class AdminProfileInfo(
    val universityId: Long?,
    val universityName: String?,
    val role: String?
)

data class ChangeEmailRequest(val newEmail: String, val currentPassword: String)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String
)

data class UpdatePersonalProfileRequest(
    val firstName: String,
    val lastName: String,
    val middleName: String?
)
