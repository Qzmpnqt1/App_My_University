package com.example.app_my_university.data.api.model

data class LoginRequest(val email: String, val password: String)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String,
    val universityId: Long,
    val groupId: Long?,
    val instituteId: Long?
)

data class ErrorResponse(val status: Int, val message: String, val timestamp: String?)
