package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.core.logging.AppLogger
import com.example.app_my_university.data.api.model.GradeRequest
import com.example.app_my_university.data.api.model.PracticeGradeRequest
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.TeacherGradingPickResponse
import com.example.app_my_university.data.api.model.TeacherStudentAssessmentResponse
import com.example.app_my_university.data.repository.GradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherGradingUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val institutes: List<TeacherGradingPickResponse> = emptyList(),
    val directions: List<TeacherGradingPickResponse> = emptyList(),
    val subjectDirections: List<SubjectInDirectionResponse> = emptyList(),
    val groups: List<TeacherGradingPickResponse> = emptyList(),
    val students: List<TeacherGradingPickResponse> = emptyList(),
    val selectedInstituteId: Long? = null,
    val selectedDirectionId: Long? = null,
    val selectedSubjectDirectionId: Long? = null,
    val selectedGroupId: Long? = null,
    val selectedStudentUserId: Long? = null,
    val studentSearchQuery: String = "",
    val assessment: TeacherStudentAssessmentResponse? = null,
    val assessmentLoading: Boolean = false,
    val saving: Boolean = false,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class TeacherGradingViewModel @Inject constructor(
    private val gradeRepository: GradeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherGradingUiState())
    val uiState: StateFlow<TeacherGradingUiState> = _uiState.asStateFlow()

    init {
        AppLogger.screen("TeacherGrading")
        refreshInstitutes()
    }

    fun refreshInstitutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            gradeRepository.getTeacherGradingInstitutes().fold(
                onSuccess = { list ->
                    AppLogger.i("TeacherGrading", "institutes loaded count=${list.size}")
                    _uiState.update {
                        it.copy(
                            loading = false,
                            institutes = list,
                            directions = emptyList(),
                            subjectDirections = emptyList(),
                            groups = emptyList(),
                            students = emptyList(),
                            selectedInstituteId = null,
                            selectedDirectionId = null,
                            selectedSubjectDirectionId = null,
                            selectedGroupId = null,
                            selectedStudentUserId = null,
                            assessment = null,
                        )
                    }
                },
                onFailure = { e ->
                    AppLogger.e("TeacherGrading", "institutes failed: ${e.message}")
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun selectInstitute(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedInstituteId = id,
                    selectedDirectionId = null,
                    selectedSubjectDirectionId = null,
                    selectedGroupId = null,
                    selectedStudentUserId = null,
                    directions = emptyList(),
                    subjectDirections = emptyList(),
                    groups = emptyList(),
                    students = emptyList(),
                    assessment = null,
                    loading = true,
                    error = null,
                )
            }
            gradeRepository.getTeacherGradingDirections(id).fold(
                onSuccess = { dirs ->
                    AppLogger.i("TeacherGrading", "directions for institute=$id count=${dirs.size}")
                    _uiState.update { it.copy(loading = false, directions = dirs) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun selectDirection(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedDirectionId = id,
                    selectedSubjectDirectionId = null,
                    selectedGroupId = null,
                    selectedStudentUserId = null,
                    subjectDirections = emptyList(),
                    groups = emptyList(),
                    students = emptyList(),
                    assessment = null,
                    loading = true,
                    error = null,
                )
            }
            gradeRepository.getTeacherGradingSubjectDirections(id).fold(
                onSuccess = { subs ->
                    AppLogger.i("TeacherGrading", "subject-directions for direction=$id count=${subs.size}")
                    _uiState.update { it.copy(loading = false, subjectDirections = subs) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun selectSubjectDirection(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedSubjectDirectionId = id,
                    selectedGroupId = null,
                    selectedStudentUserId = null,
                    groups = emptyList(),
                    students = emptyList(),
                    assessment = null,
                    loading = true,
                    error = null,
                )
            }
            gradeRepository.getTeacherGradingGroups(id).fold(
                onSuccess = { gr ->
                    AppLogger.i("TeacherGrading", "groups for sid=$id count=${gr.size}")
                    _uiState.update { it.copy(loading = false, groups = gr) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun selectGroup(id: Long?) {
        if (id == null) return
        val sid = _uiState.value.selectedSubjectDirectionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedGroupId = id,
                    selectedStudentUserId = null,
                    students = emptyList(),
                    assessment = null,
                    loading = true,
                    error = null,
                )
            }
            gradeRepository.getTeacherGradingStudents(sid, id).fold(
                onSuccess = { st ->
                    AppLogger.i("TeacherGrading", "students group=$id count=${st.size}")
                    _uiState.update { it.copy(loading = false, students = st) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    fun onStudentSearchChange(q: String) {
        _uiState.update { it.copy(studentSearchQuery = q) }
    }

    fun selectStudent(userId: Long?) {
        if (userId == null) return
        val sid = _uiState.value.selectedSubjectDirectionId ?: return
        val gid = _uiState.value.selectedGroupId ?: return
        _uiState.update { it.copy(selectedStudentUserId = userId, assessment = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(assessmentLoading = true, error = null) }
            gradeRepository.getTeacherStudentAssessment(sid, gid, userId).fold(
                onSuccess = { a ->
                    AppLogger.i("TeacherGrading", "assessment loaded student=$userId")
                    _uiState.update { it.copy(assessmentLoading = false, assessment = a) }
                },
                onFailure = { e ->
                    AppLogger.e("TeacherGrading", "assessment failed: ${e.message}")
                    _uiState.update { it.copy(assessmentLoading = false, error = e.message) }
                },
            )
        }
    }

    fun dismissAssessment() {
        _uiState.update { it.copy(assessment = null, selectedStudentUserId = null) }
    }

    fun saveAssessment(
        finalGrade: Int?,
        finalCredit: Boolean?,
        practiceValues: Map<Long, PracticeGradeDraft>,
    ) {
        val ctx = _uiState.value.assessment ?: return
        val sid = ctx.subjectDirectionId ?: return
        val gid = ctx.groupId ?: return
        val st = ctx.studentUserId ?: return
        val groupId = gid
        val isCreditSubject = ctx.finalAssessmentType?.equals("CREDIT", ignoreCase = true) == true

        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null, saveSuccess = false) }

            if (isCreditSubject) {
                if (finalCredit == null) {
                    _uiState.update { it.copy(saving = false, error = "Укажите зачёт или незачёт") }
                    return@launch
                }
                val req = GradeRequest(
                    studentId = st,
                    subjectDirectionId = sid,
                    grade = null,
                    creditStatus = finalCredit,
                    groupId = groupId,
                )
                val fg = ctx.finalGrade
                val fr = if (fg != null) {
                    gradeRepository.updateGrade(fg.id, req)
                } else {
                    gradeRepository.createGrade(req)
                }
                fr.onFailure { e ->
                    _uiState.update { it.copy(saving = false, error = e.message) }
                    return@launch
                }
            } else {
                val g = finalGrade
                if (g == null || g !in 2..5) {
                    _uiState.update { it.copy(saving = false, error = "Итоговая оценка должна быть от 2 до 5") }
                    return@launch
                }
                val req = GradeRequest(
                    studentId = st,
                    subjectDirectionId = sid,
                    grade = g,
                    creditStatus = null,
                    groupId = groupId,
                )
                val fg = ctx.finalGrade
                val fr = if (fg != null) {
                    gradeRepository.updateGrade(fg.id, req)
                } else {
                    gradeRepository.createGrade(req)
                }
                fr.onFailure { e ->
                    _uiState.update { it.copy(saving = false, error = e.message) }
                    return@launch
                }
            }

            for (p in ctx.practices.orEmpty()) {
                val draft = practiceValues[p.practiceId] ?: continue
                if (p.creditPractice == true) {
                    if (draft.credit == null) continue
                    val req = PracticeGradeRequest(
                        studentId = st,
                        practiceId = p.practiceId,
                        grade = null,
                        creditStatus = draft.credit,
                        groupId = groupId,
                    )
                    val r = if (p.gradeRowId != null) {
                        gradeRepository.updatePracticeGrade(p.gradeRowId, req)
                    } else {
                        gradeRepository.createPracticeGrade(req)
                    }
                    r.onFailure { e ->
                        _uiState.update { it.copy(saving = false, error = e.message) }
                        return@launch
                    }
                } else {
                    val gv = draft.grade ?: continue
                    val max = p.maxGrade
                    if (max != null) {
                        if (gv < 0 || gv > max) {
                            _uiState.update {
                                it.copy(saving = false, error = "Оценка по практике «${p.practiceTitle}» должна быть от 0 до $max")
                            }
                            return@launch
                        }
                    } else if (gv !in 2..5) {
                        _uiState.update {
                            it.copy(saving = false, error = "Оценка по практике «${p.practiceTitle}» должна быть от 2 до 5")
                        }
                        return@launch
                    }
                    val req = PracticeGradeRequest(
                        studentId = st,
                        practiceId = p.practiceId,
                        grade = gv,
                        creditStatus = null,
                        groupId = groupId,
                    )
                    val r = if (p.gradeRowId != null) {
                        gradeRepository.updatePracticeGrade(p.gradeRowId, req)
                    } else {
                        gradeRepository.createPracticeGrade(req)
                    }
                    r.onFailure { e ->
                        _uiState.update { it.copy(saving = false, error = e.message) }
                        return@launch
                    }
                }
            }

            gradeRepository.getTeacherStudentAssessment(sid, gid, st).fold(
                onSuccess = { refreshed ->
                    _uiState.update {
                        it.copy(saving = false, saveSuccess = true, assessment = refreshed)
                    }
                    AppLogger.i("TeacherGrading", "assessment saved")
                },
                onFailure = { e ->
                    val msg = e.message?.takeIf { it.isNotBlank() }
                    _uiState.update {
                        it.copy(
                            saving = false,
                            saveSuccess = false,
                            error = if (msg != null) {
                                "Изменения сохранены, но не удалось обновить форму: $msg"
                            } else {
                                "Изменения сохранены, но не удалось обновить форму."
                            },
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun filteredStudents(): List<TeacherGradingPickResponse> {
        val q = _uiState.value.studentSearchQuery.trim().lowercase()
        val list = _uiState.value.students
        if (q.isEmpty()) return list
        return list.filter { row ->
            (row.name?.lowercase()?.contains(q) == true) ||
                (row.subtitle?.lowercase()?.contains(q) == true)
        }
    }
}

data class PracticeGradeDraft(
    val grade: Int? = null,
    val credit: Boolean? = null,
)
