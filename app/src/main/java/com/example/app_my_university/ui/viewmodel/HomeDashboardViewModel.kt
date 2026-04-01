package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.data.repository.GradeRepository
import com.example.app_my_university.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val grades: List<GradeResponse> = emptyList()
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val gradeRepository: GradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState

    private var lastIncludeGrades: Boolean = true

    fun load(includeGrades: Boolean = true, weekNumber: Int? = null) {
        lastIncludeGrades = includeGrades
        viewModelScope.launch {
            val week = weekNumber ?: _uiState.value.currentWeek
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentWeek = week)
            val gradesJob = if (includeGrades) {
                async { gradeRepository.getMyGrades() }
            } else null
            val schedResult = scheduleRepository.getMySchedule(week, null)
            val gradesList: List<GradeResponse> = if (gradesJob != null) {
                gradesJob.await().getOrElse { emptyList() }
            } else {
                emptyList()
            }
            schedResult.fold(
                onSuccess = { entries ->
                    val grouped = entries.groupBy { it.dayOfWeek }
                        .toSortedMap()
                        .mapValues { (_, list) -> list.sortedBy { it.startTime } }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scheduleByDay = grouped,
                        grades = if (includeGrades) gradesList else emptyList()
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message,
                        scheduleByDay = emptyMap(),
                        grades = if (includeGrades) gradesList else emptyList()
                    )
                }
            )
        }
    }

    fun setWeek(week: Int) {
        load(includeGrades = lastIncludeGrades, weekNumber = week)
    }

    fun retry() {
        load(includeGrades = lastIncludeGrades)
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

private fun GradeResponse.isExamFinal(): Boolean =
    finalAssessmentType.equals("EXAM", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && grade != null)

private fun GradeResponse.isCreditFinal(): Boolean =
    finalAssessmentType.equals("CREDIT", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && creditStatus != null && grade == null)

fun examAverage(grades: List<GradeResponse>): Double? {
    val nums = grades.filter { it.isExamFinal() }
        .mapNotNull { it.grade }
        .filter { it in 2..5 }
    return if (nums.isEmpty()) null else nums.average()
}

fun pendingFinalCount(grades: List<GradeResponse>): Int =
    grades.count { g ->
        when {
            g.isCreditFinal() -> g.creditStatus == null
            g.isExamFinal() -> g.grade == null
            else -> g.grade == null && g.creditStatus == null
        }
    }
