package com.example.app_my_university.data.repository

import retrofit2.Response

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
            val errorMsg = response.errorBody()?.string() ?: "Ошибка сервера: ${response.code()}"
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
