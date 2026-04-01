package com.example.app_my_university.data.api.model

data class GuestRegistrationLookupRequest(
    val email: String,
    val password: String
)

data class GuestRegistrationStatusResponse(
    val id: Long?,
    val status: String?,
    val userType: String?,
    val universityId: Long?,
    val groupId: Long?,
    val instituteId: Long?,
    val rejectionReason: String?,
    val createdAt: String?
)

data class UpdatePendingRegistrationRequest(
    val currentPassword: String,
    val updated: RegisterRequest
)
