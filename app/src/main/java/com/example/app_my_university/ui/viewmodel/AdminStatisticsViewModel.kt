package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.DirectionStatisticsResponse
import com.example.app_my_university.data.api.model.GroupStatisticsResponse
import com.example.app_my_university.data.api.model.InstituteStatisticsResponse
import com.example.app_my_university.data.api.model.ScheduleStatisticsResponse
import com.example.app_my_university.data.api.model.UniversityStatisticsResponse
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminStatisticsPayload {
    data class Group(val data: GroupStatisticsResponse) : AdminStatisticsPayload()
    data class Direction(val data: DirectionStatisticsResponse) : AdminStatisticsPayload()
    data class Institute(val data: InstituteStatisticsResponse) : AdminStatisticsPayload()
    data class University(val data: UniversityStatisticsResponse) : AdminStatisticsPayload()
    data class Schedule(val title: String, val data: ScheduleStatisticsResponse) : AdminStatisticsPayload()
}

data class AdminStatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val payload: AdminStatisticsPayload? = null
)

@HiltViewModel
class AdminStatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStatisticsUiState())
    val uiState: StateFlow<AdminStatisticsUiState> = _uiState.asStateFlow()

    fun loadGroup(groupId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getGroupStatistics(groupId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isLoading = false, payload = AdminStatisticsPayload.Group(it), error = null)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun loadDirection(directionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getDirectionStatistics(directionId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isLoading = false, payload = AdminStatisticsPayload.Direction(it), error = null)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun loadUniversity(universityId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getUniversityStatistics(universityId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isLoading = false, payload = AdminStatisticsPayload.University(it), error = null)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun loadInstitute(instituteId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getInstituteStatistics(instituteId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isLoading = false, payload = AdminStatisticsPayload.Institute(it), error = null)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun loadClassroomSchedule(classroomId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getClassroomScheduleStatistics(classroomId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            payload = AdminStatisticsPayload.Schedule("Аудитория", it),
                            error = null
                        )
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun loadTeacherSchedule(teacherId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getTeacherScheduleStatistics(teacherId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            payload = AdminStatisticsPayload.Schedule("Преподаватель", it),
                            error = null
                        )
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun loadGroupSchedule(groupId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.getGroupScheduleStatistics(groupId).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            payload = AdminStatisticsPayload.Schedule("Группа", it),
                            error = null
                        )
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
