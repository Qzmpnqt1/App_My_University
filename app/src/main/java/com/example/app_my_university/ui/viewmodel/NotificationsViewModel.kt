package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.InAppNotificationResponse
import com.example.app_my_university.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val loading: Boolean = true,
    val items: List<InAppNotificationResponse> = emptyList(),
    val error: String? = null,
    val busy: Boolean = false,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            notificationsRepository.getMyNotifications().fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(loading = false, items = list) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            notificationsRepository.markRead(id).fold(
                onSuccess = {
                    _uiState.update { it.copy(busy = false) }
                    load()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(busy = false, error = e.message) }
                },
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            notificationsRepository.markAllRead().fold(
                onSuccess = {
                    _uiState.update { it.copy(busy = false) }
                    load()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(busy = false, error = e.message) }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
