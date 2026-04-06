package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.ChatContactResponse
import com.example.app_my_university.data.repository.ChatRepository
import com.example.app_my_university.util.AlphabeticalSort.sortedForDisplayRu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatContactsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val contacts: List<ChatContactResponse> = emptyList(),
    val query: String = ""
)

@HiltViewModel
class ChatContactsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatContactsUiState())
    val uiState: StateFlow<ChatContactsUiState> = _uiState

    init {
        loadContacts()
    }

    fun setQuery(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            chatRepository.getChatContacts().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, contacts = list)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun filteredContacts(): List<ChatContactResponse> {
        val q = _uiState.value.query.trim().lowercase()
        val base = _uiState.value.contacts
        val filtered = if (q.isEmpty()) base else base.filter { c ->
            listOf(c.email, c.firstName, c.lastName, c.middleName, c.userType)
                .filterNotNull()
                .any { it.lowercase().contains(q) }
        }
        return filtered.sortedForDisplayRu()
    }
}
