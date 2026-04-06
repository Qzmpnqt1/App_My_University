package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.AcademicGroupResponse
import com.example.app_my_university.data.api.model.ClassroomResponse
import com.example.app_my_university.data.api.model.DirectionStatisticsResponse
import com.example.app_my_university.data.api.model.GroupStatisticsResponse
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.data.api.model.InstituteStatisticsResponse
import com.example.app_my_university.data.api.model.ScheduleStatisticsResponse
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.data.api.model.UniversityStatisticsResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.ProfileRepository
import com.example.app_my_university.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
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
    val payload: AdminStatisticsPayload? = null,
    val catalogLoading: Boolean = false,
    val catalogLoaded: Boolean = false,
    val catalogError: String? = null,
    /** Для подсказок на экране (вкладка «Вуз», текст про scope). */
    val isSuperAdmin: Boolean = false,
    val adminUniversityId: Long? = null,
    val adminUniversityName: String? = null,
    val groups: List<AcademicGroupResponse> = emptyList(),
    val directions: List<StudyDirectionResponse> = emptyList(),
    val institutes: List<InstituteResponse> = emptyList(),
    val classrooms: List<ClassroomResponse> = emptyList(),
    val teachers: List<UserProfileResponse> = emptyList(),
) {
    /** Справочники не грузятся и каталог не в ошибке — можно открывать выбор и «Загрузить». */
    val catalogReady: Boolean
        get() = catalogLoaded && !catalogLoading && catalogError == null
}

@HiltViewModel
class AdminStatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val educationRepository: EducationRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStatisticsUiState())
    val uiState: StateFlow<AdminStatisticsUiState> = _uiState.asStateFlow()

    /** Отмена предыдущего запроса статистики — при быстром переключении вкладок/сущностей. */
    private var statsJob: Job? = null

    /** Сброс графиков при смене типа аналитики (без справочников). */
    fun clearStalePayload() {
        statsJob?.cancel()
        statsJob = null
        _uiState.update {
            it.copy(payload = null, error = null, isLoading = false)
        }
    }

    private fun launchStats(block: suspend () -> Unit) {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, payload = null) }
            block()
        }
    }

    fun ensureCatalogLoaded() {
        viewModelScope.launch {
            if (_uiState.value.catalogLoading) return@launch
            _uiState.update { it.copy(catalogLoading = true, catalogError = null) }
            val profile = profileRepository.getProfile().getOrNull()
            val isSuper = profile?.userType.equals("SUPER_ADMIN", ignoreCase = true)
            val uni = if (isSuper) {
                tokenManager.getSuperAdminScopeUniversityId()
            } else {
                profile?.adminProfile?.universityId
            }
            if (!isSuper && uni == null) {
                _uiState.update {
                    it.copy(
                        catalogLoading = false,
                        catalogLoaded = true,
                        catalogError = "В профиле администратора не указан вуз — справочники недоступны.",
                        isSuperAdmin = false,
                        adminUniversityId = null,
                        adminUniversityName = null,
                        groups = emptyList(),
                        directions = emptyList(),
                        institutes = emptyList(),
                        classrooms = emptyList(),
                        teachers = emptyList(),
                    )
                }
                return@launch
            }

            val institutes = educationRepository.getInstitutes(uni).getOrElse { emptyList() }
            val directions = institutes.flatMap { inst ->
                educationRepository.getDirections(inst.id, uni).getOrElse { emptyList() }
            }.distinctBy { it.id }

            val groups = directions.flatMap { dir ->
                educationRepository.getGroups(dir.id, uni).getOrElse { emptyList() }
            }.distinctBy { it.id }

            val classrooms = educationRepository.getClassrooms(uni).getOrElse { emptyList() }
            val teachers = educationRepository.getUsers(userType = "TEACHER", universityId = uni)
                .getOrElse { emptyList() }

            val fromProfile = profile?.adminProfile?.universityName
            val displayName = when {
                isSuper && uni == null -> "Все вузы"
                !fromProfile.isNullOrBlank() -> fromProfile
                isSuper && uni != null -> educationRepository.getUniversity(uni).getOrNull()?.name ?: "Выбранный вуз"
                else -> fromProfile
            }

            _uiState.update {
                it.copy(
                    catalogLoading = false,
                    catalogLoaded = true,
                    catalogError = null,
                    isSuperAdmin = isSuper,
                    adminUniversityId = uni,
                    adminUniversityName = displayName,
                    groups = groups,
                    directions = directions,
                    institutes = institutes,
                    classrooms = classrooms,
                    teachers = teachers,
                )
            }
        }
    }

    /** Повторная загрузка справочников (например, после ошибки сети). */
    fun refreshCatalog() {
        _uiState.update {
            it.copy(
                catalogLoaded = false,
                catalogLoading = false,
                catalogError = null,
            )
        }
        ensureCatalogLoaded()
    }

    fun loadGroup(groupId: Long) {
        launchStats {
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
        launchStats {
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
        launchStats {
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
        launchStats {
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
        launchStats {
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
        launchStats {
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
        launchStats {
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
