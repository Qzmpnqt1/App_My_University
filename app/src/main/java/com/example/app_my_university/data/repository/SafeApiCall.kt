package com.example.app_my_university.data.repository

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

private data class ServerErrorJson(
    @SerializedName("message") val message: String? = null,
)

private val gson = Gson()

private fun humanReadableServerError(raw: String?, code: Int): String {
    if (raw.isNullOrBlank()) return "Ошибка сервера: $code"
    return try {
        gson.fromJson(raw, ServerErrorJson::class.java)?.message?.takeIf { it.isNotBlank() } ?: raw
    } catch (_: Exception) {
        raw
    }
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) Result.success(body)
            else if (response.code() in listOf(200, 201, 204)) {
                @Suppress("UNCHECKED_CAST")
                Result.success(Unit as T)
            } else Result.failure(Exception("Пустой ответ сервера"))
        } else {
            val raw = response.errorBody()?.string()
            val errorMsg = humanReadableServerError(raw, response.code())
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
