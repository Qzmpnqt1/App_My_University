package com.example.app_my_university.data.api.model

data class RegistrationRequestResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String,
    val status: String,
    val rejectionReason: String?,
    val universityId: Long?,
    val universityName: String?,
    val groupId: Long?,
    val groupName: String?,
    val instituteId: Long?,
    val instituteName: String?,
    val createdAt: String?
)

data class RejectRequest(
    val rejectionReason: String?
)
