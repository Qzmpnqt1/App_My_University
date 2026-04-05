package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import com.example.app_my_university.data.api.model.StudentPracticeSlotResponse
import com.example.app_my_university.data.repository.GradeRepository
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

data class PracticeSlotsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val slots: List<StudentPracticeSlotResponse> = emptyList(),
    val loaded: Boolean = false,
)

enum class GradeBookListFilter {
    ALL,
    EXAMS,
    CREDITS,
    WITH_FINAL,
    WITHOUT_FINAL,
    WITH_PRACTICES,
}

data class GradeBookUiState(
    val initialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val screenError: String? = null,
    val grades: List<GradeResponse> = emptyList(),
    val planSummary: StudentPerformanceSummaryResponse? = null,
    val planSummaryError: String? = null,
    val searchQuery: String = "",
    val listFilter: GradeBookListFilter = GradeBookListFilter.ALL,
    val expandedSubjectIds: Set<Long> = emptySet(),
    val practiceStates: Map<Long, PracticeSlotsUiState> = emptyMap(),
)

@HiltViewModel
class GradeBookViewModel @Inject constructor(
    private val gradeRepository: GradeRepository,
    private val statisticsRepository: StatisticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradeBookUiState())
    val uiState: StateFlow<GradeBookUiState> = _uiState

    private val practiceJobs = ConcurrentHashMap<Long, Job>()

    fun loadInitial() {
        load(isRefresh = false)
    }

    fun refresh() {
        load(isRefresh = true)
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            val hadData = _uiState.value.grades.isNotEmpty()
            _uiState.update { s ->
                when {
                    !isRefresh && !hadData -> s.copy(
                        initialLoading = true,
                        isRefreshing = false,
                        screenError = null,
                        planSummaryError = null,
                    )
                    else -> s.copy(
                        initialLoading = false,
                        isRefreshing = true,
                        screenError = null,
                        planSummaryError = null,
                    )
                }
            }
            val gradesDeferred = async { gradeRepository.getMyGrades() }
            val planDeferred = async { statisticsRepository.getMyStudentPerformance(null, null) }
            val planResult = planDeferred.await()
            val gradesResult = gradesDeferred.await()
            gradesResult.fold(
                onSuccess = { list ->
                    _uiState.update { s ->
                        s.copy(
                            initialLoading = false,
                            isRefreshing = false,
                            grades = list,
                            planSummary = planResult.getOrNull(),
                            planSummaryError = planResult.exceptionOrNull()?.message,
                            screenError = null,
                            practiceStates = if (isRefresh) emptyMap() else s.practiceStates,
                            expandedSubjectIds = if (isRefresh) emptySet() else s.expandedSubjectIds,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { s ->
                        s.copy(
                            initialLoading = false,
                            isRefreshing = false,
                            screenError = err.message,
                            grades = if (hadData) s.grades else emptyList(),
                            planSummary = planResult.getOrNull(),
                            planSummaryError = planResult.exceptionOrNull()?.message,
                        )
                    }
                },
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onListFilterChange(filter: GradeBookListFilter) {
        _uiState.update { it.copy(listFilter = filter) }
    }

    fun setSubjectExpanded(subjectDirectionId: Long, expanded: Boolean) {
        _uiState.update { s ->
            val next = s.expandedSubjectIds.toMutableSet()
            if (expanded) next.add(subjectDirectionId) else next.remove(subjectDirectionId)
            s.copy(expandedSubjectIds = next)
        }
        if (expanded) {
            val st = _uiState.value.practiceStates[subjectDirectionId]
            if (st == null || st.error != null || !st.loaded) {
                loadPracticeSlots(subjectDirectionId)
            }
        }
    }

    fun loadPracticeSlots(subjectDirectionId: Long) {
        practiceJobs[subjectDirectionId]?.cancel()
        practiceJobs[subjectDirectionId] = viewModelScope.launch {
            _uiState.update { s ->
                val prev = s.practiceStates[subjectDirectionId] ?: PracticeSlotsUiState()
                s.copy(
                    practiceStates = s.practiceStates + (
                        subjectDirectionId to prev.copy(
                            isLoading = true,
                            error = null,
                            loaded = false,
                        )
                        ),
                )
            }
            val result = gradeRepository.getMyPracticeSlots(subjectDirectionId)
            practiceJobs.remove(subjectDirectionId)
            result.fold(
                onSuccess = { slots ->
                    _uiState.update { s ->
                        s.copy(
                            practiceStates = s.practiceStates + (
                                subjectDirectionId to PracticeSlotsUiState(
                                    isLoading = false,
                                    error = null,
                                    slots = slots,
                                    loaded = true,
                                )
                                ),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(
                            practiceStates = s.practiceStates + (
                                subjectDirectionId to PracticeSlotsUiState(
                                    isLoading = false,
                                    error = e.message ?: "Не удалось загрузить практики",
                                    slots = emptyList(),
                                    loaded = true,
                                )
                                ),
                        )
                    }
                },
            )
        }
    }

    fun dismissScreenError() {
        _uiState.update { it.copy(screenError = null) }
    }
}
