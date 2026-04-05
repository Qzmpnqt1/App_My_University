package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.*
import com.example.app_my_university.data.repository.AuthRepository
import com.example.app_my_university.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    val universities: List<UniversityResponse> = emptyList(),
    val institutes: List<InstituteResponse> = emptyList(),
    val directions: List<StudyDirectionResponse> = emptyList(),
    val groups: List<AcademicGroupResponse> = emptyList(),
    val selectedUniversityId: Long? = null,
    val selectedInstituteId: Long? = null,
    val selectedDirectionId: Long? = null,
    val selectedGroupId: Long? = null,
    val selectedUserType: String = "STUDENT"
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState

    init {
        loadUniversities()
    }

    private fun loadUniversities() {
        viewModelScope.launch {
            educationRepository.getUniversities().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(universities = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectUniversity(universityId: Long) {
        val forStudent = _uiState.value.selectedUserType == "STUDENT"
        _uiState.value = _uiState.value.copy(
            selectedUniversityId = universityId,
            selectedInstituteId = null,
            selectedDirectionId = null,
            selectedGroupId = null,
            institutes = emptyList(),
            directions = emptyList(),
            groups = emptyList()
        )
        if (!forStudent) return
        viewModelScope.launch {
            educationRepository.getInstitutes(universityId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(institutes = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectInstitute(instituteId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedInstituteId = instituteId,
            selectedDirectionId = null,
            selectedGroupId = null,
            directions = emptyList(),
            groups = emptyList()
        )
        viewModelScope.launch {
            educationRepository.getDirections(instituteId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(directions = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectDirection(directionId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedDirectionId = directionId,
            selectedGroupId = null,
            groups = emptyList()
        )
        viewModelScope.launch {
            educationRepository.getGroups(directionId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(groups = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectGroup(groupId: Long) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
    }

    fun selectUserType(userType: String) {
        _uiState.value = _uiState.value.copy(
            selectedUserType = userType,
            selectedInstituteId = null,
            selectedDirectionId = null,
            selectedGroupId = null,
            institutes = emptyList(),
            directions = emptyList(),
            groups = emptyList(),
        )
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ) {
        val state = _uiState.value
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            _uiState.value = state.copy(error = "Заполните все обязательные поля")
            return
        }
        if (state.selectedUniversityId == null) {
            _uiState.value = state.copy(error = "Выберите университет")
            return
        }
        if (state.selectedUserType == "STUDENT" && state.selectedGroupId == null) {
            _uiState.value = state.copy(error = "Выберите группу")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val request = RegisterRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                middleName = middleName,
                userType = state.selectedUserType,
                universityId = state.selectedUniversityId,
                groupId = if (state.selectedUserType == "STUDENT") state.selectedGroupId else null,
                instituteId = null,
            )
            authRepository.register(request).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, isRegistered = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Ошибка регистрации") }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
