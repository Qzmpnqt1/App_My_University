package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.AcademicGroupResponse
import com.example.app_my_university.data.api.model.GuestRegistrationStatusResponse
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.data.api.model.RegisterRequest
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.repository.AuthRepository
import com.example.app_my_university.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationStatusUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val status: GuestRegistrationStatusResponse? = null,
    val updateSuccess: Boolean = false,
    val guestUniversities: List<UniversityResponse> = emptyList(),
    val guestInstitutes: List<InstituteResponse> = emptyList(),
    val guestDirections: List<StudyDirectionResponse> = emptyList(),
    val guestGroups: List<AcademicGroupResponse> = emptyList(),
    val guestCatalogLoading: Boolean = false,
)

@HiltViewModel
class RegistrationStatusViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val educationRepository: EducationRepository,
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
                onSuccess = {
                    _uiState.value = RegistrationStatusUiState(status = it)
                    loadGuestUniversities()
                },
                onFailure = { _uiState.value = RegistrationStatusUiState(error = it.message ?: "Ошибка") }
            )
        }
    }

    fun loadGuestUniversities() {
        viewModelScope.launch {
            _uiState.update { it.copy(guestCatalogLoading = true) }
            educationRepository.getUniversities().fold(
                onSuccess = { _uiState.update { s -> s.copy(guestUniversities = it, guestCatalogLoading = false) } },
                onFailure = { _uiState.update { s -> s.copy(guestCatalogLoading = false) } }
            )
        }
    }

    fun loadGuestInstitutes(universityId: Long) {
        viewModelScope.launch {
            educationRepository.getInstitutes(universityId).fold(
                onSuccess = { _uiState.update { s -> s.copy(guestInstitutes = it, guestDirections = emptyList(), guestGroups = emptyList()) } },
                onFailure = { _uiState.update { s -> s.copy(guestInstitutes = emptyList()) } }
            )
        }
    }

    fun loadGuestDirections(instituteId: Long) {
        viewModelScope.launch {
            educationRepository.getDirections(instituteId).fold(
                onSuccess = { _uiState.update { s -> s.copy(guestDirections = it, guestGroups = emptyList()) } },
                onFailure = { _uiState.update { s -> s.copy(guestDirections = emptyList()) } }
            )
        }
    }

    fun loadGuestGroups(directionId: Long) {
        viewModelScope.launch {
            educationRepository.getGroups(directionId).fold(
                onSuccess = { _uiState.update { s -> s.copy(guestGroups = it) } },
                onFailure = { _uiState.update { s -> s.copy(guestGroups = emptyList()) } }
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

    /** Для префилла формы заявки: направление и подпись группы по id группы (без ввода id пользователем). */
    suspend fun fetchGroupForPrefill(id: Long): AcademicGroupResponse? =
        educationRepository.getGroup(id).getOrNull()
}
