package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.data.api.model.TeacherAssignmentDraftUi
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.TeacherSubjectReplaceRequest
import com.example.app_my_university.data.api.model.TeacherSubjectResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.ProfileRepository
import com.example.app_my_university.ui.components.profile.teacherWorkplaceSummary
import com.example.app_my_university.ui.components.picker.PickerListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TeacherAssignmentSheetStep {
    PICK_INSTITUTE,
    PICK_DIRECTION,
    PICK_SUBJECTS
}

data class AdminTeacherAssignmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionSuccess: Boolean = false,
    val actionMessage: String? = null,
    /** Для SUPER_ADMIN в режиме «все вузы» может быть null — это не ошибка. */
    val isSuperAdmin: Boolean = false,
    val adminUniversityId: Long? = null,
    val teacherSearchQuery: String = "",
    val allTeachers: List<UserProfileResponse> = emptyList(),
    val selectedTeacher: UserProfileResponse? = null,
    val assignments: List<TeacherSubjectResponse> = emptyList(),
    val assignmentsLoading: Boolean = false,
    val assignmentSheetOpen: Boolean = false,
    val sheetStep: TeacherAssignmentSheetStep = TeacherAssignmentSheetStep.PICK_INSTITUTE,
    val institutes: List<InstituteResponse> = emptyList(),
    val catalogsLoading: Boolean = false,
    val directionsForSheet: List<StudyDirectionResponse> = emptyList(),
    val subjectsForSheet: List<SubjectInDirectionResponse> = emptyList(),
    val selectedInstituteId: Long? = null,
    val selectedDirectionId: Long? = null,
    val subjectLineSearch: String = "",
    val pickedSubjectDirectionIds: Set<Long> = emptySet(),
    val draftAssignments: List<TeacherAssignmentDraftUi> = emptyList(),
    val expectedCountForOptimisticLock: Int? = null,
    val sheetSaving: Boolean = false,
    /**
     * Вуз для справочников в листе назначений (институты/направления).
     * Берётся из профиля выбранного преподавателя, а не из scope супера, чтобы в глобальном режиме
     * не подгружать институты всех ВУЗов.
     */
    val sheetCatalogUniversityId: Long? = null,
)

@HiltViewModel
class AdminTeacherAssignmentViewModel @Inject constructor(
    private val educationRepository: EducationRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTeacherAssignmentUiState())
    val uiState: StateFlow<AdminTeacherAssignmentUiState> = _uiState

    private var teacherSearchJob: Job? = null

    fun loadAdminContextAndTeachers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val profile = profileRepository.getProfile().getOrNull()
            val isSuper = profile?.userType.equals("SUPER_ADMIN", ignoreCase = true)
            val uni = if (isSuper) {
                tokenManager.getSuperAdminScopeUniversityId()
            } else {
                profile?.adminProfile?.universityId
            }
            if (!isSuper && uni == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    adminUniversityId = null,
                    error = "В профиле не указан вуз"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                adminUniversityId = uni,
                isSuperAdmin = isSuper,
            )
            teacherSearchJob?.cancel()
            fetchTeachersFromServer(_uiState.value.teacherSearchQuery.trim())
        }
    }

    fun onTeacherSearchChange(q: String) {
        _uiState.value = _uiState.value.copy(teacherSearchQuery = q)
        teacherSearchJob?.cancel()
        teacherSearchJob = viewModelScope.launch {
            delay(320)
            fetchTeachersFromServer(q.trim())
        }
    }

    private suspend fun fetchTeachersFromServer(query: String) {
        val uni = _uiState.value.adminUniversityId
        val isSuper = _uiState.value.isSuperAdmin
        if (!isSuper && uni == null) return
        val emptyList = _uiState.value.allTeachers.isEmpty()
        if (emptyList) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }
        educationRepository.getUsers(
            userType = "TEACHER",
            universityId = uni,
            searchQuery = query.ifBlank { null },
        ).fold(
            onSuccess = { teachers ->
                _uiState.value = _uiState.value.copy(isLoading = false, allTeachers = teachers)
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            },
        )
    }

    fun teacherPickerItems(state: AdminTeacherAssignmentUiState = _uiState.value): List<PickerListItem> {
        return state.allTeachers.map { t ->
            val name = "${t.lastName} ${t.firstName}".trim() + (t.middleName?.let { " $it" } ?: "")
            PickerListItem(
                id = t.id,
                primary = name.ifBlank { t.email },
                secondary = t.teacherWorkplaceSummary(),
            )
        }
    }

    fun selectTeacher(teacher: UserProfileResponse?) {
        if (teacher == null) {
            _uiState.value = _uiState.value.copy(selectedTeacher = null, assignments = emptyList())
            return
        }
        if (teacher.teacherProfile?.teacherProfileId == null) {
            _uiState.value = _uiState.value.copy(
                selectedTeacher = teacher,
                assignments = emptyList(),
                error = "Для преподавателя не найден teacherProfileId. Обновите приложение или список пользователей."
            )
            return
        }
        _uiState.value = _uiState.value.copy(selectedTeacher = teacher, error = null)
        loadAssignmentsForSelected()
    }

    fun selectTeacherByUserId(userId: Long) {
        val t = _uiState.value.allTeachers.find { it.id == userId }
        if (t != null) selectTeacher(t)
    }

    /**
     * Вуз, в рамках которого нужно показывать институты/направления для выбранного преподавателя.
     */
    private suspend fun resolveTeacherWorkUniversityId(teacher: UserProfileResponse): Long? {
        val tp = teacher.teacherProfile
        tp?.universityId?.let { return it }
        val profileInstituteId = tp?.instituteId
        if (profileInstituteId != null) {
            educationRepository.getInstitute(profileInstituteId).getOrNull()?.universityId?.let { return it }
        }
        val assignmentInstituteId =
            _uiState.value.assignments.firstOrNull { it.instituteId != null }?.instituteId
        if (assignmentInstituteId != null) {
            educationRepository.getInstitute(assignmentInstituteId).getOrNull()?.universityId?.let { return it }
        }
        return _uiState.value.adminUniversityId
    }

    private fun loadAssignmentsForSelected() {
        val teacher = _uiState.value.selectedTeacher ?: return
        val tpId = teacher.teacherProfile?.teacherProfileId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(assignmentsLoading = true, error = null)
            educationRepository.getTeacherSubjects(teacherId = tpId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(assignmentsLoading = false, assignments = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(assignmentsLoading = false, error = it.message)
                }
            )
        }
    }

    fun openAssignmentSheet() {
        val teacher = _uiState.value.selectedTeacher ?: return
        if (teacher.teacherProfile?.teacherProfileId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Нельзя редактировать: не найден профиль преподавателя (teacherProfileId)."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(catalogsLoading = true, error = null)
            val catalogUni = resolveTeacherWorkUniversityId(teacher)
            if (catalogUni == null) {
                _uiState.value = _uiState.value.copy(
                    catalogsLoading = false,
                    error = "Не удалось определить вуз преподавателя для списка институтов. " +
                        "Проверьте профиль преподавателя (вуз/институт) или выберите вуз в главной панели администратора.",
                )
                return@launch
            }
            educationRepository.getInstitutes(catalogUni).fold(
                onSuccess = { inst ->
                    val draft = _uiState.value.assignments.mapNotNull { a ->
                        val sid = a.subjectDirectionId ?: return@mapNotNull null
                        TeacherAssignmentDraftUi(
                            subjectDirectionId = sid,
                            subjectName = a.subjectName ?: "—",
                            directionName = a.directionName ?: "—",
                            instituteName = a.instituteName ?: "—",
                            course = a.course,
                            semester = a.semester,
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        catalogsLoading = false,
                        assignmentSheetOpen = true,
                        sheetStep = TeacherAssignmentSheetStep.PICK_INSTITUTE,
                        sheetCatalogUniversityId = catalogUni,
                        institutes = inst,
                        directionsForSheet = emptyList(),
                        subjectsForSheet = emptyList(),
                        selectedInstituteId = null,
                        selectedDirectionId = null,
                        subjectLineSearch = "",
                        pickedSubjectDirectionIds = emptySet(),
                        draftAssignments = draft,
                        expectedCountForOptimisticLock = _uiState.value.assignments.size,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(catalogsLoading = false, error = it.message)
                }
            )
        }
    }

    fun dismissAssignmentSheet() {
        _uiState.value = _uiState.value.copy(
            assignmentSheetOpen = false,
            sheetStep = TeacherAssignmentSheetStep.PICK_INSTITUTE,
            pickedSubjectDirectionIds = emptySet(),
            sheetCatalogUniversityId = null,
            error = null,
        )
    }

    fun selectInstituteForSheet(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedInstituteId = id,
                selectedDirectionId = null,
                subjectsForSheet = emptyList(),
                sheetStep = TeacherAssignmentSheetStep.PICK_DIRECTION,
            )
            val catalogUni = _uiState.value.sheetCatalogUniversityId
                ?: _uiState.value.adminUniversityId
            educationRepository.getDirections(id, catalogUni).fold(
                onSuccess = { dirs ->
                    _uiState.value = _uiState.value.copy(directionsForSheet = dirs)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun selectDirectionForSheet(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedDirectionId = id,
                sheetStep = TeacherAssignmentSheetStep.PICK_SUBJECTS,
                pickedSubjectDirectionIds = emptySet(),
                subjectLineSearch = "",
            )
            educationRepository.getSubjectsInDirections(id).fold(
                onSuccess = { rows ->
                    _uiState.value = _uiState.value.copy(subjectsForSheet = rows)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun goBackSheetStep() {
        when (_uiState.value.sheetStep) {
            TeacherAssignmentSheetStep.PICK_SUBJECTS -> {
                _uiState.value = _uiState.value.copy(
                    sheetStep = TeacherAssignmentSheetStep.PICK_DIRECTION,
                    subjectsForSheet = emptyList(),
                    pickedSubjectDirectionIds = emptySet(),
                    subjectLineSearch = "",
                )
            }
            TeacherAssignmentSheetStep.PICK_DIRECTION -> {
                _uiState.value = _uiState.value.copy(
                    sheetStep = TeacherAssignmentSheetStep.PICK_INSTITUTE,
                    directionsForSheet = emptyList(),
                    selectedInstituteId = null,
                    selectedDirectionId = null,
                )
            }
            TeacherAssignmentSheetStep.PICK_INSTITUTE -> Unit
        }
    }

    fun onSubjectLineSearchChange(s: String) {
        _uiState.value = _uiState.value.copy(subjectLineSearch = s)
    }

    fun togglePickedSubjectDirection(id: Long) {
        val cur = _uiState.value.pickedSubjectDirectionIds.toMutableSet()
        if (!cur.add(id)) cur.remove(id)
        _uiState.value = _uiState.value.copy(pickedSubjectDirectionIds = cur)
    }

    fun addPickedSubjectsToDraft() {
        val picked = _uiState.value.pickedSubjectDirectionIds
        if (picked.isEmpty()) return
        val rows = _uiState.value.subjectsForSheet
        val instituteName =
            _uiState.value.institutes.find { it.id == _uiState.value.selectedInstituteId }?.name ?: "—"
        val directionName =
            _uiState.value.directionsForSheet.find { it.id == _uiState.value.selectedDirectionId }?.name ?: "—"
        val existing = _uiState.value.draftAssignments.map { it.subjectDirectionId }.toSet()
        val newItems = picked.mapNotNull { pid ->
            if (pid in existing) null
            else rows.find { it.id == pid }?.let { sid ->
                TeacherAssignmentDraftUi(
                    subjectDirectionId = sid.id,
                    subjectName = sid.subjectName ?: "—",
                    directionName = directionName,
                    instituteName = instituteName,
                    course = sid.course,
                    semester = sid.semester,
                )
            }
        }
        _uiState.value = _uiState.value.copy(
            draftAssignments = _uiState.value.draftAssignments + newItems,
            pickedSubjectDirectionIds = emptySet(),
            sheetStep = TeacherAssignmentSheetStep.PICK_INSTITUTE,
            selectedInstituteId = null,
            selectedDirectionId = null,
            directionsForSheet = emptyList(),
            subjectsForSheet = emptyList(),
        )
    }

    fun removeDraftItem(subjectDirectionId: Long) {
        _uiState.value = _uiState.value.copy(
            draftAssignments = _uiState.value.draftAssignments.filter { it.subjectDirectionId != subjectDirectionId }
        )
    }

    fun saveAllDraftAssignments() {
        val teacher = _uiState.value.selectedTeacher ?: return
        val tpId = teacher.teacherProfile?.teacherProfileId ?: return
        val ids = _uiState.value.draftAssignments.map { it.subjectDirectionId }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sheetSaving = true, error = null)
            educationRepository.replaceTeacherAssignments(
                teacherProfileId = tpId,
                request = TeacherSubjectReplaceRequest(
                    subjectDirectionIds = ids,
                    expectedAssignmentCount = _uiState.value.expectedCountForOptimisticLock,
                ),
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        sheetSaving = false,
                        assignmentSheetOpen = false,
                        actionSuccess = true,
                        actionMessage = "Назначения сохранены",
                    )
                    loadAssignmentsForSelected()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(sheetSaving = false, error = it.message)
                },
            )
        }
    }

    fun deleteAssignment(assignmentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(assignmentsLoading = true, error = null)
            educationRepository.deleteTeacherSubject(assignmentId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        assignmentsLoading = false,
                        actionSuccess = true,
                        actionMessage = "Назначение снято",
                    )
                    loadAssignmentsForSelected()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(assignmentsLoading = false, error = it.message)
                },
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearActionSuccess() {
        _uiState.value = _uiState.value.copy(actionSuccess = false, actionMessage = null)
    }

    fun filteredSubjectsForPicker(state: AdminTeacherAssignmentUiState = _uiState.value): List<SubjectInDirectionResponse> {
        val q = state.subjectLineSearch.trim().lowercase()
        if (q.isEmpty()) return state.subjectsForSheet
        return state.subjectsForSheet.filter { row ->
            (row.subjectName?.lowercase()?.contains(q) == true)
        }
    }
}
