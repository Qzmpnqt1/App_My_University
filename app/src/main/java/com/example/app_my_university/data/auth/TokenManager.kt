package com.example.app_my_university.data.auth

import com.example.app_my_university.data.network.AuthInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val authInterceptor: AuthInterceptor
) {
    
    fun setToken(token: String?) {
        authInterceptor.setAuthToken(token)
    }
    
    fun clearToken() {
        authInterceptor.setAuthToken(null)
    }
} 