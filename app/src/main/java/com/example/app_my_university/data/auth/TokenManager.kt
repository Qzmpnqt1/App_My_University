package com.example.app_my_university.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val SUPER_ADMIN_SCOPE_UNIVERSITY_KEY = longPreferencesKey("super_admin_scope_university_id")
    }

    val superAdminScopeUniversityId: Flow<Long?> =
        context.dataStore.data.map { it[SUPER_ADMIN_SCOPE_UNIVERSITY_KEY] }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userType: Flow<String?> = context.dataStore.data.map { it[USER_TYPE_KEY] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[USER_ID_KEY] }

    suspend fun saveAuthData(token: String, userId: Long, userType: String, email: String, name: String) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[USER_ID_KEY] = userId
            it[USER_TYPE_KEY] = userType
            it[USER_EMAIL_KEY] = email
            it[USER_NAME_KEY] = name
            if (!userType.equals("SUPER_ADMIN", ignoreCase = true)) {
                it.remove(SUPER_ADMIN_SCOPE_UNIVERSITY_KEY)
            }
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.map { it[TOKEN_KEY] }.first()

    suspend fun getUserType(): String? = context.dataStore.data.map { it[USER_TYPE_KEY] }.first()

    suspend fun getUserId(): Long? = context.dataStore.data.map { it[USER_ID_KEY] }.first()

    suspend fun clearAuthData() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getSuperAdminScopeUniversityId(): Long? =
        context.dataStore.data.map { it[SUPER_ADMIN_SCOPE_UNIVERSITY_KEY] }.first()

    suspend fun setSuperAdminScopeUniversityId(id: Long?) {
        context.dataStore.edit {
            if (id == null) it.remove(SUPER_ADMIN_SCOPE_UNIVERSITY_KEY)
            else it[SUPER_ADMIN_SCOPE_UNIVERSITY_KEY] = id
        }
    }
}
