package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: UserProfileResponse? = null,
    val emailChangeSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            profileRepository.getProfile().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, profile = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun changeEmail(newEmail: String, currentPassword: String) {
        if (newEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Введите новый email")
            return
        }
        if (currentPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Введите текущий пароль для подтверждения")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, emailChangeSuccess = false)
            profileRepository.changeEmail(newEmail, currentPassword).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, emailChangeSuccess = true)
                    loadProfile()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, newPasswordConfirm: String) {
        if (oldPassword.isBlank() || newPassword.isBlank() || newPasswordConfirm.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Заполните все поля")
            return
        }
        if (newPassword != newPasswordConfirm) {
            _uiState.value = _uiState.value.copy(error = "Новый пароль и подтверждение не совпадают")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Пароль должен содержать минимум 6 символов")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, passwordChangeSuccess = false)
            profileRepository.changePassword(oldPassword, newPassword, newPasswordConfirm).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, passwordChangeSuccess = true)
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

    fun clearSuccessFlags() {
        _uiState.value = _uiState.value.copy(emailChangeSuccess = false, passwordChangeSuccess = false)
    }
}
