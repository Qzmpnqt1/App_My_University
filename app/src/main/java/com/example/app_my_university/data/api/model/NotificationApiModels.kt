package com.example.app_my_university.data.api.model

import com.google.gson.annotations.SerializedName

data class InAppNotificationResponse(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String?,
    val readAt: String?,
    val createdAt: String?,
)

data class UnreadNotificationsCountResponse(
    @SerializedName("unreadCount")
    val unreadCount: Long,
)
