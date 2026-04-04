package com.example.app_my_university.data.network

import com.example.app_my_university.core.logging.AppLogger
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Логирует запросы/ответы. Тело запроса не читается (иначе ломается отправка).
 * Ответ — через peekBody. Секреты в заголовках маскируются.
 */
class SafeHttpLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.nanoTime()
        AppLogger.i(
            "HTTP",
            "--> ${request.method} ${request.url} headers=${sanitizeHeaders(request.headers)} " +
                "contentType=${request.body?.contentType()} len=${request.body?.contentLength() ?: 0}",
        )
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            AppLogger.e("HTTP", "<-- FAILED ${request.url} ${e.message}", e)
            throw e
        }
        val took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
        val peek = try {
            response.peekBody(16 * 1024L).string()
        } catch (_: Exception) {
            ""
        }
        AppLogger.i(
            "HTTP",
            "<-- ${response.code} ${request.url} (${took}ms) body=${sanitizeBody(peek).take(800)}",
        )
        return response
    }

    private fun sanitizeHeaders(headers: Headers): String {
        return (0 until headers.size).joinToString(", ") { i ->
            val n = headers.name(i)
            val v = if (n.equals("Authorization", ignoreCase = true)) "[REDACTED]" else headers.value(i)
            "$n=$v"
        }
    }

    private fun sanitizeBody(raw: String): String {
        if (raw.isBlank()) return "{}"
        return raw
            .replace(Regex("(\"password\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE), "$1***$3")
            .replace(Regex("(\"newPassword\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE), "$1***$3")
            .replace(Regex("(\"oldPassword\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE), "$1***$3")
            .replace(Regex("(\"currentPassword\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE), "$1***$3")
            .replace(Regex("(\"accessToken\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE), "$1***$3")
            .replace(Regex("(\"refreshToken\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE), "$1***$3")
    }
}
