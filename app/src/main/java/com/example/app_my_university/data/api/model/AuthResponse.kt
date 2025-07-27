package com.example.app_my_university.data.api.model

data class AuthResponse(
    val token: String,
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String
) 