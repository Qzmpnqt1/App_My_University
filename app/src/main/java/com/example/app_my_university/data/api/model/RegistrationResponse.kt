package com.example.app_my_university.data.api.model

data class RegistrationResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
) 