package com.example.app_my_university.data.api.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: AuthResponse? = null
) 