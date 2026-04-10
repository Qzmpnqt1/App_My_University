package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.InAppNotificationResponse
import com.example.app_my_university.data.api.model.UnreadNotificationsCountResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val apiService: ApiService,
) {

    suspend fun getMyNotifications(): Result<List<InAppNotificationResponse>> =
        safeApiCall { apiService.getMyNotifications() }

    suspend fun getUnreadCount(): Result<UnreadNotificationsCountResponse> =
        safeApiCall { apiService.getUnreadNotificationsCount() }

    suspend fun markRead(id: Long): Result<Unit> =
        safeApiCall { apiService.markNotificationRead(id) }

    suspend fun markAllRead(): Result<Unit> =
        safeApiCall { apiService.markAllNotificationsRead() }
}
