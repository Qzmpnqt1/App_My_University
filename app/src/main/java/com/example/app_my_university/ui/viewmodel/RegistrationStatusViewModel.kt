package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.GuestRegistrationStatusResponse
import com.example.app_my_university.data.api.model.RegisterRequest
import com.example.app_my_university.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationStatusUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val status: GuestRegistrationStatusResponse? = null,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class RegistrationStatusViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationStatusUiState())
    val uiState: StateFlow<RegistrationStatusUiState> = _uiState

    fun lookup(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Укажите email и пароль из заявки")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegistrationStatusUiState(isLoading = true)
            authRepository.lookupRegistrationStatus(email, password).fold(
                onSuccess = { _uiState.value = RegistrationStatusUiState(status = it) },
                onFailure = { _uiState.value = RegistrationStatusUiState(error = it.message ?: "Ошибка") }
            )
        }
    }

    fun updatePending(currentPassword: String, updated: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, updateSuccess = false)
            authRepository.updatePendingRegistration(currentPassword, updated).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, updateSuccess = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
