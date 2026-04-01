package com.example.app_my_university.data.api.model

data class ChatContactResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val userType: String?
)

data class ConversationResponse(
    val conversationId: String,
    val participantId: Long,
    val participantName: String?,
    val lastMessageText: String?,
    val lastMessageAt: String?,
    val unreadCount: Int?
)

data class MessageResponse(
    val messageId: String,
    val conversationId: String?,
    val senderId: Long,
    val senderName: String?,
    val text: String,
    val sentAt: String?
)

data class SendMessageRequest(
    val recipientId: Long,
    val text: String
)
