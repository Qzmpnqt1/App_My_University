package com.example.app_my_university.data.network

/**
 * Гарантирует, что базовый URL API задан только со схемой HTTPS.
 */
object ApiBaseUrlValidator {
    fun requireHttpsBaseUrl(url: String) {
        val t = url.trim()
        require(t.startsWith("https://", ignoreCase = true)) {
            "API base URL must use HTTPS (cleartext HTTP is disabled). Got: ${t.take(160)}"
        }
    }
}
