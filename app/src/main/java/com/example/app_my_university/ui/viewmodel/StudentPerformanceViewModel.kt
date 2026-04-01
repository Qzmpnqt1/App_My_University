package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentPerformanceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val summary: StudentPerformanceSummaryResponse? = null
)

@HiltViewModel
class StudentPerformanceViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentPerformanceUiState())
    val uiState: StateFlow<StudentPerformanceUiState> = _uiState

    fun load(course: Int? = null, semester: Int? = null) {
        viewModelScope.launch {
            _uiState.value = StudentPerformanceUiState(isLoading = true)
            statisticsRepository.getMyStudentPerformance(course, semester).fold(
                onSuccess = { _uiState.value = StudentPerformanceUiState(summary = it) },
                onFailure = { _uiState.value = StudentPerformanceUiState(error = it.message) }
            )
        }
    }
}
