package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.ChangePasswordRequest
import com.example.app_my_university.data.api.model.UpdateProfileRequest
import com.example.app_my_university.data.api.model.UserProfile
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UniversityRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                repository.getUserProfile()
                    .collect { result ->
                        result.fold(
                            onSuccess = { profile ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        userProfile = profile,
                                        error = null
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = exception.message ?: "Ошибка загрузки профиля"
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        middleName: String?,
        email: String
    ) {
        val validationError = validateProfileData(firstName, lastName, email)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            try {
                val request = UpdateProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = middleName,
                    email = email
                )

                repository.updateUserProfile(request)
                    .collect { result ->
                        result.fold(
                            onSuccess = { profile ->
                                _uiState.update {
                                    it.copy(
                                        isUpdating = false,
                                        userProfile = profile,
                                        updateSuccess = "Профиль успешно обновлен",
                                        error = null
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update {
                                    it.copy(
                                        isUpdating = false,
                                        error = exception.message ?: "Ошибка обновления профиля"
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val validationError = validatePasswordData(currentPassword, newPassword, confirmPassword)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, error = null) }

            try {
                val request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )

                repository.changePassword(request)
                    .collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                _uiState.update {
                                    it.copy(
                                        isChangingPassword = false,
                                        passwordChangeSuccess = "Пароль успешно изменен",
                                        error = null
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update {
                                    it.copy(
                                        isChangingPassword = false,
                                        error = exception.message ?: "Ошибка смены пароля"
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    /**
     * Выход из аккаунта
     * 
     * @return true - если выход успешен, false - если возникла ошибка
     */
    fun logout() {
        try {
            // Очищаем токен
            tokenManager.clearToken()
            
            // Обновляем состояние UI
            _uiState.update { 
                it.copy(
                    isLoggingOut = true,
                    logoutSuccess = true
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = "Ошибка при выходе из аккаунта: ${e.message}",
                    logoutSuccess = false,
                    isLoggingOut = false
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                error = null,
                updateSuccess = null,
                passwordChangeSuccess = null
            )
        }
    }

    private fun validateProfileData(firstName: String, lastName: String, email: String): String? {
        if (firstName.isBlank()) return "Имя обязательно"
        if (lastName.isBlank()) return "Фамилия обязательна"
        if (email.isBlank()) return "Email обязателен"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Неверный формат email"
        return null
    }

    private fun validatePasswordData(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        if (currentPassword.isBlank()) return "Текущий пароль обязателен"
        if (newPassword.isBlank()) return "Новый пароль обязателен"
        if (newPassword.length < 6) return "Новый пароль должен содержать не менее 6 символов"
        if (newPassword != confirmPassword) return "Новый пароль и подтверждение не совпадают"
        return null
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isChangingPassword: Boolean = false,
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val updateSuccess: String? = null,
    val passwordChangeSuccess: String? = null
)