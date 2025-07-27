package com.example.app_my_university.data.api.model
 
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
) 