package com.example.app_my_university.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    
    private var authToken: String? = null
    
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Добавляем токен только для защищенных эндпоинтов
        val isProtectedEndpoint = originalRequest.url.encodedPath.startsWith("/api/profile") ||
                                 originalRequest.url.encodedPath.startsWith("/api/auth")
        
        if (isProtectedEndpoint && authToken != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
            return chain.proceed(newRequest)
        }
        
        return chain.proceed(originalRequest)
    }
} 