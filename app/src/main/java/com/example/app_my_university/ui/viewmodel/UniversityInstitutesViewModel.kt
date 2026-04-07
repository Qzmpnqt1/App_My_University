package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.InstituteRequest
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.data.api.model.UniversityRequest
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UniversityInstitutesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val university: UniversityResponse? = null,
    val institutes: List<InstituteResponse> = emptyList(),
    val actionSuccess: Boolean = false,
    val actionMessage: String? = null,
)

@HiltViewModel
class UniversityInstitutesViewModel @Inject constructor(
    private val educationRepository: EducationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversityInstitutesUiState())
    val uiState: StateFlow<UniversityInstitutesUiState> = _uiState.asStateFlow()

    fun load(universityId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            educationRepository.getUniversity(universityId).fold(
                onSuccess = { u ->
                    educationRepository.getInstitutes(universityId).fold(
                        onSuccess = { inst ->
                            _uiState.update {
                                it.copy(isLoading = false, university = u, institutes = inst, error = null)
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(isLoading = false, university = u, error = e.message)
                            }
                        },
                    )
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearActionSuccess() {
        _uiState.update { it.copy(actionSuccess = false, actionMessage = null) }
    }

    fun updateUniversity(universityId: Long, request: UniversityRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, actionSuccess = false) }
            educationRepository.updateUniversity(universityId, request).fold(
                onSuccess = { u ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            university = u,
                            actionSuccess = true,
                            actionMessage = "Вуз обновлён",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }

    fun createInstitute(request: InstituteRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, actionSuccess = false) }
            educationRepository.createInstitute(request).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionSuccess = true,
                            actionMessage = "Институт создан",
                        )
                    }
                    load(request.universityId)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }

    fun updateInstitute(id: Long, request: InstituteRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, actionSuccess = false) }
            educationRepository.updateInstitute(id, request).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionSuccess = true,
                            actionMessage = "Институт обновлён",
                        )
                    }
                    load(request.universityId)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }

    fun deleteInstitute(instituteId: Long, universityId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, actionSuccess = false) }
            educationRepository.deleteInstitute(instituteId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionSuccess = true,
                            actionMessage = "Институт удалён",
                        )
                    }
                    load(universityId)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }
}
