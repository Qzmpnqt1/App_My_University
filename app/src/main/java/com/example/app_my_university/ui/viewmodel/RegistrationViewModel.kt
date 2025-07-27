package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.AcademicGroupDTO
import com.example.app_my_university.data.api.model.InstituteDTO
import com.example.app_my_university.data.api.model.StudyDirectionDTO
import com.example.app_my_university.data.api.model.SubjectDTO
import com.example.app_my_university.data.repository.UniversityRepository
import com.example.app_my_university.model.Subject
import com.example.app_my_university.ui.screens.Direction
import com.example.app_my_university.ui.screens.Group
import com.example.app_my_university.ui.screens.Institute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: UniversityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    // Аргументы из навигации
    private val universityId: String = checkNotNull(savedStateHandle["universityId"])
    private val universityName: String = checkNotNull(savedStateHandle["universityName"])

    init {
        loadInstitutes()
        loadSubjects()
    }

    fun loadInstitutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInstitutes = true, instituteError = null) }

            try {
                repository.getInstitutes(universityId.toInt())
                    .collect { result ->
                        result.fold(
                            onSuccess = { institutes ->
                                _uiState.update {
                                    it.copy(
                                        institutes = institutes.map { dto ->
                                            Institute(dto.id.toString(), dto.name)
                                        },
                                        isLoadingInstitutes = false
                                    )
                                }
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(
                                        instituteError = e.message,
                                        isLoadingInstitutes = false
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        instituteError = e.message ?: "Неизвестная ошибка",
                        isLoadingInstitutes = false
                    )
                }
            }
        }
    }

    fun loadDirections(instituteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDirections = true, directionError = null) }

            try {
                repository.getDirections(instituteId.toInt())
                    .collect { result ->
                        result.fold(
                            onSuccess = { directions ->
                                _uiState.update {
                                    it.copy(
                                        directions = directions.map { dto ->
                                            Direction(dto.id.toString(), dto.name, instituteId)
                                        },
                                        isLoadingDirections = false
                                    )
                                }
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(
                                        directionError = e.message,
                                        isLoadingDirections = false
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        directionError = e.message ?: "Неизвестная ошибка",
                        isLoadingDirections = false
                    )
                }
            }
        }
    }

    fun loadGroups(directionId: String, course: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGroups = true, groupError = null) }

            try {
                val flow = if (course != null) {
                    repository.getGroupsByCourse(directionId.toInt(), course.toInt())
                } else {
                    repository.getGroups(directionId.toInt())
                }

                flow.collect { result ->
                    result.fold(
                        onSuccess = { groups ->
                            _uiState.update {
                                it.copy(
                                    groups = groups.map { dto ->
                                        Group(dto.id.toString(), dto.name, directionId)
                                    },
                                    isLoadingGroups = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    groupError = e.message,
                                    isLoadingGroups = false
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        groupError = e.message ?: "Неизвестная ошибка",
                        isLoadingGroups = false
                    )
                }
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSubjects = true, subjectError = null) }

            try {
                repository.getSubjectsByUniversity(universityId.toInt())
                    .collect { result ->
                        result.fold(
                            onSuccess = { subjects ->
                                _uiState.update {
                                    it.copy(
                                        subjects = subjects.map { dto ->
                                            Subject(
                                                id = dto.id.toString(),
                                                name = dto.name
                                            )
                                        },
                                        isLoadingSubjects = false
                                    )
                                }
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(
                                        subjectError = e.message,
                                        isLoadingSubjects = false
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        subjectError = e.message ?: "Неизвестная ошибка",
                        isLoadingSubjects = false
                    )
                }
            }
        }
    }

    fun searchSubjects(query: String) {
        if (query.isEmpty()) {
            _uiState.update { it.copy(searchSubjectsResults = emptyList()) }
            return
        }

        // Локальный поиск по уже загруженным предметам
        val filteredSubjects = uiState.value.subjects.filter { subject ->
            subject.name.contains(query, ignoreCase = true)
        }
        
        _uiState.update { 
            it.copy(searchSubjectsResults = filteredSubjects)
        }
    }
}

data class RegistrationUiState(
    val institutes: List<Institute> = emptyList(),
    val directions: List<Direction> = emptyList(),
    val groups: List<Group> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val searchSubjectsResults: List<Subject> = emptyList(),
    val isLoadingInstitutes: Boolean = false,
    val isLoadingDirections: Boolean = false,
    val isLoadingGroups: Boolean = false,
    val isLoadingSubjects: Boolean = false,
    val instituteError: String? = null,
    val directionError: String? = null,
    val groupError: String? = null,
    val subjectError: String? = null
) 