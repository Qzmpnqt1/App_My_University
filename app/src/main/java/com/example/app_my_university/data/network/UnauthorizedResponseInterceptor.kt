package com.example.app_my_university.data.network

import com.example.app_my_university.domain.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedResponseInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            val path = chain.request().url.encodedPath
            if (!path.contains("api/v1/auth/login")) {
                runBlocking { sessionManager.clearSession() }
            }
        }
        return response
    }
}
