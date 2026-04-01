package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.GuestRegistrationLookupRequest
import com.example.app_my_university.data.api.model.GuestRegistrationStatusResponse
import com.example.app_my_university.data.api.model.LoginRequest
import com.example.app_my_university.data.api.model.RegisterRequest
import com.example.app_my_university.data.api.model.UpdatePendingRegistrationRequest
import com.example.app_my_university.domain.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                sessionManager.saveAuthData(auth.token, auth.userId, auth.userType, auth.email, "${auth.lastName} ${auth.firstName}")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Ошибка входа"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Ошибка регистрации"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun isLoggedIn(): Boolean = sessionManager.getToken() != null

    suspend fun getUserType(): String? = sessionManager.getUserType()

    suspend fun lookupRegistrationStatus(email: String, password: String): Result<GuestRegistrationStatusResponse> {
        return try {
            val response = apiService.registrationStatus(GuestRegistrationLookupRequest(email.trim(), password))
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Не удалось получить статус"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePendingRegistration(currentPassword: String, updated: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.updatePendingRegistration(UpdatePendingRegistrationRequest(currentPassword, updated))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Не удалось обновить заявку"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
