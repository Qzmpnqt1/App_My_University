package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UniversitySelectionUiState {
    data object Loading : UniversitySelectionUiState()
    data class Success(val universities: List<UniversityResponse>) : UniversitySelectionUiState()
    data class Error(val message: String) : UniversitySelectionUiState()
    data object Empty : UniversitySelectionUiState()
}

@HiltViewModel
class UniversitySelectionViewModel @Inject constructor(
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UniversitySelectionUiState>(UniversitySelectionUiState.Loading)
    val uiState: StateFlow<UniversitySelectionUiState> = _uiState

    init {
        loadUniversities()
    }

    fun loadUniversities() {
        viewModelScope.launch {
            _uiState.value = UniversitySelectionUiState.Loading
            educationRepository.getUniversities().fold(
                onSuccess = { list ->
                    _uiState.value = if (list.isEmpty()) {
                        UniversitySelectionUiState.Empty
                    } else {
                        UniversitySelectionUiState.Success(list)
                    }
                },
                onFailure = {
                    _uiState.value = UniversitySelectionUiState.Error(
                        it.message ?: "Не удалось загрузить список университетов"
                    )
                }
            )
        }
    }
}
