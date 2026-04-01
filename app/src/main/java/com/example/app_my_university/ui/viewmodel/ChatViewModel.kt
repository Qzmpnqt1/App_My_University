package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.ConversationResponse
import com.example.app_my_university.data.api.model.MessageResponse
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversations: List<ConversationResponse> = emptyList(),
    val selectedConversationId: String? = null,
    val selectedParticipantId: Long? = null,
    val selectedParticipantName: String? = null,
    val messages: List<MessageResponse> = emptyList(),
    val currentUserId: Long? = null,
    val sendSuccess: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        viewModelScope.launch {
            val userId = tokenManager.userId.first()
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
        loadConversations()
    }

    fun prepareNewConversation(participantId: Long, participantName: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = null,
            selectedConversationId = null,
            selectedParticipantId = participantId,
            selectedParticipantName = participantName,
            messages = emptyList()
        )
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            chatRepository.getConversations().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, conversations = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun selectConversation(conversation: ConversationResponse) {
        _uiState.value = _uiState.value.copy(
            selectedConversationId = conversation.conversationId,
            selectedParticipantId = conversation.participantId,
            selectedParticipantName = conversation.participantName,
            messages = emptyList()
        )
        loadMessages(conversation.conversationId)
        markAsRead(conversation.conversationId)
    }

    fun loadMessages(conversationId: String, limit: Int = 50) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            chatRepository.getMessages(conversationId, limit).fold(
                onSuccess = { messages ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = messages.reversed()
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun sendMessage(recipientId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null, sendSuccess = false)
            chatRepository.sendMessage(recipientId, text).fold(
                onSuccess = { newMessage ->
                    val updated = _uiState.value.messages + newMessage
                    val convId = newMessage.conversationId ?: _uiState.value.selectedConversationId
                    _uiState.value = _uiState.value.copy(
                        messages = updated,
                        sendSuccess = true,
                        selectedConversationId = convId
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun sendMessageToSelected(text: String) {
        val recipientId = _uiState.value.selectedParticipantId ?: return
        sendMessage(recipientId, text)
    }

    private fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            chatRepository.markAsRead(conversationId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSendSuccess() {
        _uiState.value = _uiState.value.copy(sendSuccess = false)
    }
}
