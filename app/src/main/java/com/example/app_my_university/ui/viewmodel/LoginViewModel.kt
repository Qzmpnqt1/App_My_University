package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val userType: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                val userType = authRepository.getUserType()
                _uiState.value = LoginUiState(isLoggedIn = true, userType = userType)
            }
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Заполните все поля")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(trimmedEmail, password)
            result.fold(
                onSuccess = {
                    val userType = authRepository.getUserType()
                    _uiState.value = LoginUiState(isLoggedIn = true, userType = userType)
                },
                onFailure = {
                    _uiState.value = LoginUiState(error = it.message ?: "Ошибка входа")
                }
            )
        }
    }

    /**
     * Сначала сбрасываем UI-состояние синхронно, иначе [AppNavigation] LaunchedEffect
     * всё ещё видит isLoggedIn=true и с экрана входа снова уводит на главную, пока корутина не завершилась.
     */
    fun logout() {
        _uiState.value = LoginUiState(isLoggedIn = false, userType = null, isLoading = false, error = null)
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
