package com.example.app_my_university.domain.session

import com.example.app_my_university.core.database.AppDatabase
import com.example.app_my_university.data.auth.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val appDatabase: AppDatabase
) {

    suspend fun saveAuthData(token: String, userId: Long, userType: String, email: String, name: String) {
        tokenManager.saveAuthData(token, userId, userType, email, name)
    }

    suspend fun getToken(): String? = tokenManager.getToken()

    suspend fun getUserType(): String? = tokenManager.getUserType()

    suspend fun clearSession() {
        tokenManager.clearAuthData()
        withContext(Dispatchers.IO) {
            appDatabase.clearAllTables()
        }
    }
}
