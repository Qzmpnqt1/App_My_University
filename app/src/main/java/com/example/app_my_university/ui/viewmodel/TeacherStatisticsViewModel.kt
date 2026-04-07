package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.*
import com.example.app_my_university.data.repository.GradeRepository
import com.example.app_my_university.data.repository.ProfileRepository
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TeacherStatsSection(val label: String) {
    OVERVIEW("Обзор"),
    SUBJECT("Дисциплина"),
    GROUP("Группа"),
    DIRECTION("Направление"),
}

data class TeacherStatisticsUiState(
    val section: TeacherStatsSection = TeacherStatsSection.OVERVIEW,
    val myUserId: Long? = null,
    val profileError: String? = null,
    val catalogLoading: Boolean = false,
    val directionsLoading: Boolean = false,
    val subjectDirectionsLoading: Boolean = false,
    val groupsLoading: Boolean = false,
    val catalogError: String? = null,
    val institutes: List<TeacherGradingPickResponse> = emptyList(),
    val directions: List<TeacherGradingPickResponse> = emptyList(),
    val subjectDirections: List<SubjectInDirectionResponse> = emptyList(),
    val groups: List<TeacherGradingPickResponse> = emptyList(),
    val selectedInstituteId: Long? = null,
    val instituteLabel: String = "",
    val selectedDirectionId: Long? = null,
    val directionLabel: String = "",
    val selectedSubjectDirectionId: Long? = null,
    val subjectLabel: String = "",
    val selectedGroupId: Long? = null,
    val groupLabel: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val scheduleStats: ScheduleStatisticsResponse? = null,
    /** null — все недели; 1 или 2 — фильтр для API расписания. */
    val scheduleWeekFilter: Int? = null,
    val subjectStats: SubjectStatisticsResponse? = null,
    val practiceStats: PracticeStatisticsResponse? = null,
    val groupStats: GroupStatisticsResponse? = null,
    val directionStats: DirectionStatisticsResponse? = null,
) {
    val canPickDirection: Boolean get() = selectedInstituteId != null
    val canPickSubject: Boolean get() = selectedDirectionId != null
    val canPickGroup: Boolean get() = selectedSubjectDirectionId != null
}

@HiltViewModel
class TeacherStatisticsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val gradeRepository: GradeRepository,
    private val statisticsRepository: StatisticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherStatisticsUiState())
    val uiState: StateFlow<TeacherStatisticsUiState> = _uiState.asStateFlow()

    private var statsJob: Job? = null

    init {
        // Каталог teacher-catalog не зависит от профиля — грузим сразу (не ждём getProfile).
        loadInstitutes()
        viewModelScope.launch {
            profileRepository.getProfile().fold(
                onSuccess = { p ->
                    _uiState.update {
                        it.copy(myUserId = p.id, profileError = null)
                    }
                    loadScheduleStatistics()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(profileError = e.message)
                    }
                }
            )
        }
    }

    /** Повторная подгрузка справочников при входе на экран / смене раздела. */
    fun ensureCatalogFresh() {
        val s = _uiState.value
        if (s.catalogLoading || s.directionsLoading || s.subjectDirectionsLoading || s.groupsLoading) return
        if (s.institutes.isEmpty()) {
            loadInstitutes()
        }
    }

    fun setSection(section: TeacherStatsSection) {
        statsJob?.cancel()
        statsJob = null
        _uiState.update {
            it.copy(
                section = section,
                isLoading = false,
                error = null,
                scheduleStats = null,
                subjectStats = null,
                practiceStats = null,
                groupStats = null,
                directionStats = null,
            )
        }
        if (section == TeacherStatsSection.OVERVIEW) {
            loadScheduleStatistics()
        }
    }

    private fun loadInstitutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(catalogLoading = true, catalogError = null) }
            gradeRepository.getTeacherGradingInstitutes().fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(catalogLoading = false, institutes = list) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(catalogLoading = false, catalogError = e.message)
                    }
                }
            )
        }
    }

    fun refreshCatalog() {
        loadInstitutes()
    }

    fun selectInstitute(item: TeacherGradingPickResponse) {
        val id = item.id
        val label = listOfNotNull(item.name, item.subtitle).joinToString(" · ")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedInstituteId = id,
                    instituteLabel = label,
                    selectedDirectionId = null,
                    directionLabel = "",
                    selectedSubjectDirectionId = null,
                    subjectLabel = "",
                    selectedGroupId = null,
                    groupLabel = "",
                    directions = emptyList(),
                    subjectDirections = emptyList(),
                    groups = emptyList(),
                    directionsLoading = true,
                    catalogError = null,
                )
            }
            gradeRepository.getTeacherGradingDirections(id).fold(
                onSuccess = { dirs ->
                    _uiState.update { s -> s.copy(directions = dirs, directionsLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(directionsLoading = false, catalogError = e.message)
                    }
                }
            )
        }
    }

    fun selectDirection(item: TeacherGradingPickResponse) {
        val id = item.id
        val label = listOfNotNull(item.name, item.subtitle).joinToString(" · ")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedDirectionId = id,
                    directionLabel = label,
                    selectedSubjectDirectionId = null,
                    subjectLabel = "",
                    selectedGroupId = null,
                    groupLabel = "",
                    subjectDirections = emptyList(),
                    groups = emptyList(),
                    subjectDirectionsLoading = true,
                    catalogError = null,
                )
            }
            gradeRepository.getTeacherGradingSubjectDirections(id).fold(
                onSuccess = { subs ->
                    _uiState.update { s -> s.copy(subjectDirections = subs, subjectDirectionsLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(subjectDirectionsLoading = false, catalogError = e.message)
                    }
                }
            )
        }
    }

    fun selectSubject(sd: SubjectInDirectionResponse) {
        val label = buildString {
            append(sd.subjectName ?: "Предмет")
            sd.course?.let { append(" · курс $it") }
            sd.semester?.let { append(" · сем $it") }
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedSubjectDirectionId = sd.id,
                    subjectLabel = label,
                    selectedGroupId = null,
                    groupLabel = "",
                    groups = emptyList(),
                    groupsLoading = true,
                    catalogError = null,
                )
            }
            gradeRepository.getTeacherGradingGroups(sd.id).fold(
                onSuccess = { gr -> _uiState.update { s -> s.copy(groups = gr, groupsLoading = false) } },
                onFailure = { e ->
                    _uiState.update { s -> s.copy(groupsLoading = false, catalogError = e.message) }
                }
            )
        }
    }

    fun selectGroup(item: TeacherGradingPickResponse) {
        val label = listOfNotNull(item.name, item.subtitle).joinToString(" · ")
        _uiState.update {
            it.copy(selectedGroupId = item.id, groupLabel = label)
        }
    }

    fun setScheduleWeekFilter(week: Int?) {
        _uiState.update { it.copy(scheduleWeekFilter = week) }
    }

    fun loadScheduleStatistics() {
        val uid = _uiState.value.myUserId ?: return
        val week = _uiState.value.scheduleWeekFilter
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, scheduleStats = null) }
            statisticsRepository.getTeacherScheduleStatistics(uid, week).fold(
                onSuccess = { res ->
                    _uiState.update { s -> s.copy(isLoading = false, scheduleStats = res, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { s -> s.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun loadSubjectAnalytics() {
        val sid = _uiState.value.selectedSubjectDirectionId ?: return
        val groupId = _uiState.value.selectedGroupId
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    subjectStats = null,
                    practiceStats = null,
                )
            }
            val sub = statisticsRepository.getSubjectStatistics(sid, groupId)
            val pr = statisticsRepository.getPracticeStatistics(sid, groupId)
            if (sub.isFailure) {
                _uiState.update { s ->
                    s.copy(isLoading = false, error = sub.exceptionOrNull()?.message)
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    subjectStats = sub.getOrNull(),
                    practiceStats = pr.getOrNull(),
                    error = pr.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun loadGroupAnalytics() {
        val gid = _uiState.value.selectedGroupId ?: return
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, groupStats = null) }
            statisticsRepository.getGroupStatistics(gid).fold(
                onSuccess = { res ->
                    _uiState.update { s -> s.copy(isLoading = false, groupStats = res, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { s -> s.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun loadDirectionAnalytics() {
        val did = _uiState.value.selectedDirectionId ?: return
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, directionStats = null) }
            statisticsRepository.getDirectionStatistics(did).fold(
                onSuccess = { res ->
                    _uiState.update { s -> s.copy(isLoading = false, directionStats = res, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { s -> s.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
