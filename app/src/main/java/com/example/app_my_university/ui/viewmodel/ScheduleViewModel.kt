package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.ScheduleRequest
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val scheduleByDay: Map<Int, List<ScheduleResponse>> = emptyMap(),
    val currentWeek: Int = 1,
    /** null — все дни; 1–7 — фильтр по дню (API). */
    val selectedDayOfWeek: Int? = null,
    /** Для админа: расписание группы; null — «моё» расписание. */
    val viewingGroupId: Long? = null,
    val createSuccess: Boolean = false,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState

    fun setViewingGroup(groupId: Long?) {
        _uiState.value = _uiState.value.copy(viewingGroupId = groupId, error = null)
        if (groupId != null) {
            loadSchedule()
        } else {
            _uiState.value = _uiState.value.copy(scheduleByDay = emptyMap(), isLoading = false)
        }
    }

    fun setSelectedDayOfWeek(day: Int?) {
        _uiState.value = _uiState.value.copy(selectedDayOfWeek = day)
        loadSchedule()
    }

    fun loadSchedule(weekNumber: Int? = null) {
        viewModelScope.launch {
            val week = weekNumber ?: _uiState.value.currentWeek
            val day = _uiState.value.selectedDayOfWeek
            val groupId = _uiState.value.viewingGroupId
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentWeek = week)

            val result = if (groupId != null) {
                scheduleRepository.getGroupSchedule(groupId, week, day)
            } else {
                scheduleRepository.getMySchedule(week, day)
            }

            result.fold(
                onSuccess = { entries ->
                    val grouped = entries.groupBy { it.dayOfWeek }
                        .toSortedMap()
                        .mapValues { (_, list) -> list.sortedBy { it.startTime } }
                    _uiState.value = _uiState.value.copy(isLoading = false, scheduleByDay = grouped)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun setWeek(weekNumber: Int) {
        loadSchedule(weekNumber)
    }

    fun createSchedule(request: ScheduleRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, createSuccess = false)
            educationRepository.createSchedule(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, createSuccess = true)
                    loadSchedule()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, deleteSuccess = false)
            educationRepository.deleteSchedule(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, deleteSuccess = true)
                    loadSchedule()
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

    fun clearSuccessFlags() {
        _uiState.value = _uiState.value.copy(createSuccess = false, deleteSuccess = false)
    }
}
