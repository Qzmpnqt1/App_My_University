package com.example.app_my_university.data.repository

import com.example.app_my_university.core.database.CacheDao
import com.example.app_my_university.core.database.CacheEntryEntity
import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.*
import com.example.app_my_university.util.AlphabeticalSort.sortedForDisplayRu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val cacheDao: CacheDao,
    private val gson: Gson
) {

    private val conversationsType = object : TypeToken<List<ConversationResponse>>() {}.type

    companion object {
        private const val CACHE_KEY_CONVERSATIONS = "cache_conversations_v1"
    }

    suspend fun getChatContacts(): Result<List<ChatContactResponse>> =
        safeApiCall { apiService.getChatContacts() }.map { it.sortedForDisplayRu() }

    suspend fun getConversations(): Result<List<ConversationResponse>> {
        val result = safeApiCall { apiService.getConversations() }
        result.onSuccess { list ->
            cacheDao.upsert(
                CacheEntryEntity(
                    key = CACHE_KEY_CONVERSATIONS,
                    payload = gson.toJson(list),
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    suspend fun getCachedConversationsOrEmpty(): List<ConversationResponse> {
        val row = cacheDao.get(CACHE_KEY_CONVERSATIONS) ?: return emptyList()
        return try {
            gson.fromJson(row.payload, conversationsType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getMessages(
        conversationId: String,
        limit: Int = 50,
        before: String? = null
    ): Result<List<MessageResponse>> =
        safeApiCall { apiService.getMessages(conversationId, limit, before) }

    suspend fun sendMessage(recipientId: Long, text: String): Result<MessageResponse> =
        safeApiCall { apiService.sendMessage(SendMessageRequest(recipientId, text)) }

    suspend fun markAsRead(conversationId: String): Result<Unit> =
        safeApiCall { apiService.markAsRead(conversationId) }
}
