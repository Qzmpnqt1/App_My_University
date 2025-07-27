package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.AuthRequest
import com.example.app_my_university.data.api.model.AuthResponse
import com.example.app_my_university.data.repository.UniversityRepository
import com.example.app_my_university.model.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UniversityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // Валидация на клиенте
        val validationError = validateLoginData(email, password)
        if (validationError != null) {
            _uiState.update {
                it.copy(loginError = validationError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loginError = null,
                    loginSuccess = null
                )
            }

            try {
                val request = AuthRequest(email = email, password = password)
                repository.login(request)
                    .collect { result ->
                        result.fold(
                            onSuccess = { authResponse ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        loginSuccess = "Вход выполнен успешно",
                                        userData = authResponse
                                    )
                                }
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        loginError = e.message
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    fun clearLoginState() {
        _uiState.update {
            it.copy(
                loginError = null,
                loginSuccess = null
            )
        }
    }

    private fun validateLoginData(email: String, password: String): String? {
        if (email.isBlank()) return "Email обязателен"
        if (!isValidEmail(email)) return "Неверный формат email"
        if (password.isBlank()) return "Пароль обязателен"
        if (password.length < 6) return "Пароль должен содержать не менее 6 символов"
        return null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val loginSuccess: String? = null,
    val userData: AuthResponse? = null
) 