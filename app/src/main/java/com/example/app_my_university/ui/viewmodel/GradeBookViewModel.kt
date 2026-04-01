package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.*
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.GradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GradeBookUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val grades: List<GradeResponse> = emptyList(),
    val practiceGrades: List<PracticeGradeResponse> = emptyList(),
    /** К какому subjectDirectionId относятся текущие practiceGrades (для зачётки). */
    val practiceGradesSubjectDirectionId: Long? = null,
    val subjects: List<SubjectInDirectionResponse> = emptyList(),
    val practices: List<SubjectPracticeResponse> = emptyList(),
    val selectedSubjectDirectionId: Long? = null,
    val gradesByPractice: List<PracticeGradeResponse> = emptyList(),
    /** Итоговые оценки по выбранному предмету (преподаватель). */
    val finalGradesBySubject: List<GradeResponse> = emptyList(),
    val saveSuccess: Boolean = false
)

@HiltViewModel
class GradeBookViewModel @Inject constructor(
    private val gradeRepository: GradeRepository,
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradeBookUiState())
    val uiState: StateFlow<GradeBookUiState> = _uiState

    fun loadStudentGrades() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            gradeRepository.getMyGrades().fold(
                onSuccess = { grades ->
                    _uiState.value = _uiState.value.copy(isLoading = false, grades = grades)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadStudentPracticeGrades(subjectDirectionId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            gradeRepository.getMyPracticeGrades(subjectDirectionId).fold(
                onSuccess = { practiceGrades ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        practiceGrades = practiceGrades,
                        practiceGradesSubjectDirectionId = subjectDirectionId
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadSubjectsInDirection(directionId: Long? = null) {
        viewModelScope.launch {
            educationRepository.getSubjectsInDirections(directionId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(subjects = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectSubjectDirection(subjectDirectionId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedSubjectDirectionId = subjectDirectionId,
            gradesByPractice = emptyList(),
            finalGradesBySubject = emptyList()
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            educationRepository.getSubjectPractices(subjectDirectionId).fold(
                onSuccess = { practices ->
                    _uiState.value = _uiState.value.copy(practices = practices)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
            gradeRepository.getGradesBySubjectDirection(subjectDirectionId).fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(finalGradesBySubject = list)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadPracticeGradesByPractice(practiceId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            gradeRepository.getPracticeGradesByPractice(practiceId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, gradesByPractice = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun reloadFinalGradesForSelectedSubject() {
        val id = _uiState.value.selectedSubjectDirectionId ?: return
        viewModelScope.launch {
            gradeRepository.getGradesBySubjectDirection(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(finalGradesBySubject = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun createGrade(request: GradeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, saveSuccess = false)
            gradeRepository.createGrade(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
                    reloadFinalGradesForSelectedSubject()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updateGrade(id: Long, request: GradeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, saveSuccess = false)
            gradeRepository.updateGrade(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
                    reloadFinalGradesForSelectedSubject()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createPracticeGrade(request: PracticeGradeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, saveSuccess = false)
            gradeRepository.createPracticeGrade(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updatePracticeGrade(id: Long, request: PracticeGradeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, saveSuccess = false)
            gradeRepository.updatePracticeGrade(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
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

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
