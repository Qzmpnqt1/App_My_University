package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminStatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastText: String? = null
)

@HiltViewModel
class AdminStatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStatisticsUiState())
    val uiState: StateFlow<AdminStatisticsUiState> = _uiState

    fun loadGroup(groupId: Long) {
        viewModelScope.launch {
            _uiState.value = AdminStatisticsUiState(isLoading = true)
            statisticsRepository.getGroupStatistics(groupId).fold(
                onSuccess = {
                    _uiState.value = AdminStatisticsUiState(
                        lastText = "Группа ${it.groupName}: средний ${it.averagePerformance}, должники ${it.studentsWithDebt}/${it.studentCount}"
                    )
                },
                onFailure = { _uiState.value = AdminStatisticsUiState(error = it.message) }
            )
        }
    }

    fun loadUniversity(universityId: Long) {
        viewModelScope.launch {
            _uiState.value = AdminStatisticsUiState(isLoading = true)
            statisticsRepository.getUniversityStatistics(universityId).fold(
                onSuccess = {
                    _uiState.value = AdminStatisticsUiState(
                        lastText = "Вуз ${it.universityName}: студентов ${it.totalStudents}, средняя успеваемость ${it.averagePerformance}"
                    )
                },
                onFailure = { _uiState.value = AdminStatisticsUiState(error = it.message) }
            )
        }
    }

    fun loadInstitute(instituteId: Long) {
        viewModelScope.launch {
            _uiState.value = AdminStatisticsUiState(isLoading = true)
            statisticsRepository.getInstituteStatistics(instituteId).fold(
                onSuccess = {
                    _uiState.value = AdminStatisticsUiState(
                        lastText = "Институт ${it.instituteName}: студентов ${it.totalStudents}, средняя ${it.averagePerformance}"
                    )
                },
                onFailure = { _uiState.value = AdminStatisticsUiState(error = it.message) }
            )
        }
    }

    fun loadClassroom(classroomId: Long) {
        viewModelScope.launch {
            _uiState.value = AdminStatisticsUiState(isLoading = true)
            statisticsRepository.getClassroomScheduleStatistics(classroomId).fold(
                onSuccess = {
                    _uiState.value = AdminStatisticsUiState(
                        lastText = "Аудитория: занятий ${it.totalLessons}, часов ${it.totalHours}"
                    )
                },
                onFailure = { _uiState.value = AdminStatisticsUiState(error = it.message) }
            )
        }
    }
}
