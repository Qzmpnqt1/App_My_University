package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.UniversityDTO
import com.example.app_my_university.data.repository.UniversityRepository
import com.example.app_my_university.model.University
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UniversitySelectionViewModel @Inject constructor(
    private val repository: UniversityRepository
) : ViewModel() {
    
    // UI states
    private val _uiState = MutableStateFlow<UniversitySelectionUiState>(UniversitySelectionUiState.Loading)
    val uiState: StateFlow<UniversitySelectionUiState> = _uiState.asStateFlow()
    
    init {
        loadUniversities()
    }
    
    fun loadUniversities() {
        viewModelScope.launch {
            _uiState.value = UniversitySelectionUiState.Loading
            
            repository.getUniversities()
                .catch { e -> 
                    _uiState.value = UniversitySelectionUiState.Error(e.message ?: "Неизвестная ошибка")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { universities ->
                            if (universities.isEmpty()) {
                                _uiState.value = UniversitySelectionUiState.Empty
                            } else {
                                _uiState.value = UniversitySelectionUiState.Success(
                                    universities.map { it.toUniversity() }
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.value = UniversitySelectionUiState.Error(e.message ?: "Неизвестная ошибка")
                        }
                    )
                }
        }
    }
    
    private fun UniversityDTO.toUniversity(): University {
        return University(
            id = id.toString(),
            name = name,
            shortName = shortName,
            city = city
        )
    }
}

sealed class UniversitySelectionUiState {
    object Loading : UniversitySelectionUiState()
    object Empty : UniversitySelectionUiState()
    data class Success(val universities: List<University>) : UniversitySelectionUiState()
    data class Error(val message: String) : UniversitySelectionUiState()
} 