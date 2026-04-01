package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.AuditLogResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepository @Inject constructor(private val api: ApiService) {

    suspend fun searchLogs(
        userId: Long? = null,
        action: String? = null,
        entityType: String? = null,
        fromIso: String? = null,
        toIso: String? = null
    ): Result<List<AuditLogResponse>> =
        safeApiCall { api.getAuditLogs(userId, action, entityType, fromIso, toIso) }
}
