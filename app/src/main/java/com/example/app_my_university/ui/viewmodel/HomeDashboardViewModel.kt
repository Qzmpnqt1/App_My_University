package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import com.example.app_my_university.data.repository.GradeRepository
import com.example.app_my_university.data.repository.ScheduleRepository
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentWeek: Int = 1,
    val scheduleByDay: Map<Int, List<ScheduleResponse>> = emptyMap(),
    val grades: List<GradeResponse> = emptyList(),
    /** Сводка с backend (`/statistics/me/student`), совпадает с экраном «Успеваемость» без фильтров. */
    val studentPerformanceSummary: StudentPerformanceSummaryResponse? = null,
    val studentPerformanceError: String? = null,
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val gradeRepository: GradeRepository,
    private val statisticsRepository: StatisticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState

    private var lastIncludeGrades: Boolean = true
    private var lastIncludeStudentPerformance: Boolean = false
    private var dashboardJob: Job? = null

    fun load(
        includeGrades: Boolean = true,
        weekNumber: Int? = null,
        includeStudentPerformance: Boolean = false,
    ) {
        lastIncludeGrades = includeGrades
        lastIncludeStudentPerformance = includeStudentPerformance
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            val week = weekNumber ?: _uiState.value.currentWeek
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                currentWeek = week,
                studentPerformanceError = null,
            )
            val gradesDeferred = if (includeGrades) {
                async { gradeRepository.getMyGrades() }
            } else null
            val perfDeferred = if (includeStudentPerformance) {
                async { statisticsRepository.getMyStudentPerformance(null, null) }
            } else null

            val schedResult = scheduleRepository.getMySchedule(week, null)
            val gradesList: List<GradeResponse> = if (gradesDeferred != null) {
                gradesDeferred.await().getOrElse { emptyList() }
            } else {
                emptyList()
            }
            val perfResult = perfDeferred?.await()

            schedResult.fold(
                onSuccess = { entries ->
                    val grouped = entries.groupBy { it.dayOfWeek }
                        .toSortedMap()
                        .mapValues { (_, list) -> list.sortedBy { it.startTime } }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scheduleByDay = grouped,
                        grades = if (includeGrades) gradesList else emptyList(),
                        studentPerformanceSummary = if (includeStudentPerformance) {
                            perfResult?.getOrNull()
                        } else {
                            null
                        },
                        studentPerformanceError = if (includeStudentPerformance) {
                            perfResult?.exceptionOrNull()?.message
                        } else {
                            null
                        },
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message,
                        scheduleByDay = emptyMap(),
                        grades = if (includeGrades) gradesList else emptyList(),
                        studentPerformanceSummary = if (includeStudentPerformance) {
                            perfResult?.getOrNull()
                        } else {
                            null
                        },
                        studentPerformanceError = if (includeStudentPerformance) {
                            perfResult?.exceptionOrNull()?.message
                        } else {
                            null
                        },
                    )
                }
            )
        }
    }

    fun setWeek(week: Int) {
        load(
            includeGrades = lastIncludeGrades,
            weekNumber = week,
            includeStudentPerformance = lastIncludeStudentPerformance,
        )
    }

    fun retry() {
        load(
            includeGrades = lastIncludeGrades,
            includeStudentPerformance = lastIncludeStudentPerformance,
        )
    }
}

object HomeDashboardTime {
    private val flexible: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")

    fun todayIsoDayOfWeek(): Int = LocalDate.now().dayOfWeek.value

    fun parseStart(time: String): LocalTime? = try {
        LocalTime.parse(time.trim(), flexible)
    } catch (_: Exception) {
        try {
            LocalTime.parse(time.trim(), DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            null
        }
    }

    fun nextLessonToday(scheduleByDay: Map<Int, List<ScheduleResponse>>): ScheduleResponse? {
        val dow = todayIsoDayOfWeek()
        val today = scheduleByDay[dow].orEmpty()
        if (today.isEmpty()) return null
        val now = LocalTime.now()
        val sorted = today.sortedBy { parseStart(it.startTime) ?: LocalTime.MAX }
        return sorted.firstOrNull { lesson ->
            val start = parseStart(lesson.startTime) ?: return@firstOrNull false
            !start.isBefore(now)
        }
    }

    fun todayLessons(scheduleByDay: Map<Int, List<ScheduleResponse>>): List<ScheduleResponse> {
        val dow = todayIsoDayOfWeek()
        return scheduleByDay[dow].orEmpty().sortedBy { parseStart(it.startTime) ?: LocalTime.MAX }
    }
}
