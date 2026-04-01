package com.example.app_my_university.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Блокирует любые исходящие запросы, если схема URL не HTTPS.
 */
class HttpsOnlyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val scheme = request.url.scheme
        if (!scheme.equals("https", ignoreCase = true)) {
            error("Blocked non-HTTPS request (${scheme.uppercase()}): ${request.url.redact()}")
        }
        return chain.proceed(request)
    }
}
