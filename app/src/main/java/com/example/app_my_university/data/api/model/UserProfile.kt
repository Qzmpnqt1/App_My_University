package com.example.app_my_university.data.api.model

data class UserProfile(
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val studentProfile: StudentProfile?,
    val teacherProfile: TeacherProfile?,
    val adminProfile: AdminProfile?
)

data class StudentProfile(
    val id: Int,
    val universityId: Int,
    val universityName: String,
    val instituteId: Int,
    val instituteName: String,
    val directionId: Int,
    val directionName: String,
    val groupId: Int,
    val groupName: String,
    val course: Int,
    val yearOfAdmission: Int,
    val createdAt: String,
    val updatedAt: String
)

data class TeacherProfile(
    val id: Int,
    val universityId: Int,
    val universityName: String,
    val subjects: List<Subject>?,
    val createdAt: String,
    val updatedAt: String
)

data class AdminProfile(
    val id: Int,
    val universityId: Int,
    val universityName: String,
    val role: String?,
    val createdAt: String,
    val updatedAt: String
)

data class Subject(
    val id: Int,
    val name: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfile?
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class ChangePasswordResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
) 