package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.*
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.ChatRepository
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuperAdmin: Boolean = false,
    /** Вуз текущего администратора (область видимости). */
    val adminUniversityId: Long? = null,
    val adminUniversityName: String? = null,
    val registrationRequests: List<RegistrationRequestResponse> = emptyList(),
    val users: List<UserProfileResponse> = emptyList(),
    val universities: List<UniversityResponse> = emptyList(),
    val institutes: List<InstituteResponse> = emptyList(),
    val directions: List<StudyDirectionResponse> = emptyList(),
    val groups: List<AcademicGroupResponse> = emptyList(),
    val subjects: List<SubjectResponse> = emptyList(),
    /** Связки предмет–направление (для subjectTypeId в расписании). */
    val subjectsInDirections: List<SubjectInDirectionResponse> = emptyList(),
    val classrooms: List<ClassroomResponse> = emptyList(),
    val actionSuccess: Boolean = false,
    val actionMessage: String? = null,
    val pendingRegistrationCount: Int = 0,
    val unreadMessagesCount: Int = 0
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val educationRepository: EducationRepository,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    /** Загрузить universityId администратора из профиля (для фильтрации и экранов). */
    fun loadAdminContext() {
        viewModelScope.launch {
            profileRepository.getProfile().fold(
                onSuccess = { p ->
                    val superUser = p.userType.equals("SUPER_ADMIN", ignoreCase = true)
                    val (uid, uname) = if (superUser) {
                        val saved = tokenManager.getSuperAdminScopeUniversityId()
                        if (saved != null) {
                            Pair(saved, _uiState.value.adminUniversityName)
                        } else {
                            Pair(null, null)
                        }
                    } else {
                        Pair(p.adminProfile?.universityId, p.adminProfile?.universityName)
                    }
                    _uiState.value = _uiState.value.copy(
                        isSuperAdmin = superUser,
                        adminUniversityId = uid,
                        adminUniversityName = uname
                    )
                    if (superUser) {
                        loadUniversities()
                        uid?.let { syncUniversityNameForScope(it) }
                    }
                },
                onFailure = { /* оставляем null */ }
            )
        }
    }

    fun setSuperAdminScopeUniversity(universityId: Long, universityName: String) {
        viewModelScope.launch {
            tokenManager.setSuperAdminScopeUniversityId(universityId)
            _uiState.value = _uiState.value.copy(
                adminUniversityId = universityId,
                adminUniversityName = universityName
            )
            loadMyUniversityAndInstitutes()
        }
    }

    /** Сброс выбранного вуза — глобальный режим для SUPER_ADMIN. */
    fun clearSuperAdminScope() {
        viewModelScope.launch {
            tokenManager.setSuperAdminScopeUniversityId(null)
            _uiState.value = _uiState.value.copy(
                adminUniversityId = null,
                adminUniversityName = null
            )
            loadUniversities()
            loadMyUniversityAndInstitutes()
        }
    }

    private suspend fun syncUniversityNameForScope(universityId: Long) {
        educationRepository.getUniversity(universityId).fold(
            onSuccess = { u ->
                _uiState.value = _uiState.value.copy(adminUniversityName = u.name)
            },
            onFailure = { }
        )
    }

    fun loadRegistrationRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val uni = _uiState.value.adminUniversityId
            educationRepository.getRegistrationRequests(universityId = uni).fold(
                onSuccess = {
                    val pending = it.count { r -> r.status.equals("PENDING", ignoreCase = true) }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registrationRequests = it,
                        pendingRegistrationCount = pending
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun refreshDashboardBadges() {
        viewModelScope.launch {
            var uni = _uiState.value.adminUniversityId
            if (uni == null) {
                val profile = profileRepository.getProfile().getOrNull()
                val isSuper = profile?.userType.equals("SUPER_ADMIN", ignoreCase = true)
                if (!isSuper) {
                    profile?.adminProfile?.universityId?.let { u ->
                        uni = u
                        _uiState.value = _uiState.value.copy(adminUniversityId = u)
                    }
                }
            }
            educationRepository.getRegistrationRequests(universityId = uni).fold(
                onSuccess = {
                    val pending = it.count { r -> r.status.equals("PENDING", ignoreCase = true) }
                    _uiState.value = _uiState.value.copy(pendingRegistrationCount = pending)
                },
                onFailure = { /* ignore badge errors */ }
            )
            chatRepository.getConversations().fold(
                onSuccess = { convs ->
                    val unread = convs.sumOf { c -> c.unreadCount ?: 0 }
                    _uiState.value = _uiState.value.copy(unreadMessagesCount = unread)
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun approveRequest(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.approveRequest(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Заявка одобрена"
                    )
                    loadRegistrationRequests()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun rejectRequest(id: Long, reason: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.rejectRequest(id, reason).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Заявка отклонена"
                    )
                    loadRegistrationRequests()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val uni = _uiState.value.adminUniversityId
            educationRepository.getUsers(universityId = uni).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, users = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createAdminAccount(request: CreateAdminAccountRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createAdminAccount(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Учётная запись создана"
                    )
                    loadUsers()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun activateUser(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.activateUser(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Пользователь активирован"
                    )
                    loadUsers()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deactivateUser(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deactivateUser(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Пользователь деактивирован"
                    )
                    loadUsers()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadUniversities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            educationRepository.getUniversities().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, universities = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    /** Вуз(ы) и институты в рамках effective scope: кампусный админ — один вуз; супер без выбора — все вузы и институты. */
    fun loadMyUniversityAndInstitutes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            profileRepository.getProfile().fold(
                onSuccess = { p ->
                    val superUser = p.userType.equals("SUPER_ADMIN", ignoreCase = true)
                    val uid: Long?
                    val uname: String?
                    if (superUser) {
                        uid = _uiState.value.adminUniversityId ?: tokenManager.getSuperAdminScopeUniversityId()
                        uname = _uiState.value.adminUniversityName
                    } else {
                        uid = p.adminProfile?.universityId
                        uname = p.adminProfile?.universityName
                    }
                    if (superUser && uid == null) {
                        _uiState.value = _uiState.value.copy(
                            isSuperAdmin = true,
                            adminUniversityId = null,
                            adminUniversityName = null,
                        )
                        if (_uiState.value.universities.isEmpty()) {
                            educationRepository.getUniversities().fold(
                                onSuccess = { allU ->
                                    educationRepository.getInstitutes(null).fold(
                                        onSuccess = { inst ->
                                            _uiState.value = _uiState.value.copy(
                                                isLoading = false,
                                                universities = allU,
                                                institutes = inst,
                                                error = null,
                                            )
                                        },
                                        onFailure = { e ->
                                            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                                        },
                                    )
                                },
                                onFailure = { e ->
                                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                                },
                            )
                        } else {
                            educationRepository.getInstitutes(null).fold(
                                onSuccess = { inst ->
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        institutes = inst,
                                        error = null,
                                    )
                                },
                                onFailure = { e ->
                                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                                },
                            )
                        }
                        return@launch
                    }
                    if (!superUser && uid == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "В профиле не указан вуз",
                            universities = emptyList(),
                            institutes = emptyList(),
                        )
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        isSuperAdmin = superUser,
                        adminUniversityId = uid,
                        adminUniversityName = uname,
                    )
                    educationRepository.getUniversity(uid!!).fold(
                        onSuccess = { u ->
                            educationRepository.getInstitutes(uid).fold(
                                onSuccess = { inst ->
                                    val keepUnis = if (superUser) {
                                        val cur = _uiState.value.universities
                                        if (cur.isEmpty()) listOf(u) else cur
                                    } else {
                                        listOf(u)
                                    }
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        universities = keepUnis,
                                        institutes = inst
                                    )
                                },
                                onFailure = { e ->
                                    val keepUnis = if (superUser) {
                                        val cur = _uiState.value.universities
                                        if (cur.isEmpty()) listOf(u) else cur
                                    } else {
                                        listOf(u)
                                    }
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        universities = keepUnis,
                                        error = e.message
                                    )
                                }
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun createUniversity(request: UniversityRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createUniversity(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Университет создан"
                    )
                    loadUniversities()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteUniversity(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteUniversity(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Университет удалён"
                    )
                    loadUniversities()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updateUniversity(id: Long, request: UniversityRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.updateUniversity(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Университет обновлён"
                    )
                    if (_uiState.value.adminUniversityId != null) loadMyUniversityAndInstitutes()
                    else loadUniversities()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadInstitutes(universityId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            educationRepository.getInstitutes(universityId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, institutes = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createInstitute(request: InstituteRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createInstitute(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Институт создан"
                    )
                    loadInstitutes(request.universityId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteInstitute(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteInstitute(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Институт удалён"
                    )
                    loadInstitutes(_uiState.value.adminUniversityId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadDirections(instituteId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val scopeUni = _uiState.value.adminUniversityId
            educationRepository.getDirections(instituteId, scopeUni).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, directions = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadGroups(directionId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val scopeUni = _uiState.value.adminUniversityId
            educationRepository.getGroups(directionId, scopeUni).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, groups = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    /**
     * Справочник предметов. Для SUPER_ADMIN с выбранным вузом и для ADMIN передаётся [universityId] на API,
     * чтобы не подгружать предметы, используемые только в других вузах.
     */
    fun loadSubjects(scopeUniversityId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val effectiveUniversityId = scopeUniversityId ?: _uiState.value.adminUniversityId
            educationRepository.getSubjects(effectiveUniversityId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, subjects = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    /**
     * @param universityId только если [directionId] == null: для SUPER_ADMIN в scoped-режиме ограничить вузом.
     */
    fun loadSubjectsInDirections(directionId: Long? = null, universityId: Long? = null) {
        viewModelScope.launch {
            val uni = if (directionId != null) null else (universityId ?: _uiState.value.adminUniversityId)
            educationRepository.getSubjectsInDirections(directionId, uni).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(subjectsInDirections = it)
                },
                onFailure = { /* не блокируем экран расписания */ }
            )
        }
    }

    fun createDirection(request: StudyDirectionRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createDirection(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Направление создано"
                    )
                    loadDirections(null)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updateDirection(id: Long, request: StudyDirectionRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.updateDirection(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Направление обновлено"
                    )
                    loadDirections(null)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteDirection(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteDirection(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Направление удалено"
                    )
                    loadDirections(null)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createSubjectInDirection(
        request: SubjectInDirectionRequest,
        reloadDirectionId: Long? = null,
        reloadUniversityId: Long? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createSubjectInDirection(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Связь предмет–направление создана"
                    )
                    loadSubjectsInDirections(reloadDirectionId, reloadUniversityId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updateSubjectInDirection(
        id: Long,
        request: SubjectInDirectionRequest,
        reloadDirectionId: Long? = null,
        reloadUniversityId: Long? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.updateSubjectInDirection(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Связь обновлена"
                    )
                    loadSubjectsInDirections(reloadDirectionId, reloadUniversityId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteSubjectInDirection(
        id: Long,
        reloadDirectionId: Long? = null,
        reloadUniversityId: Long? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteSubjectInDirection(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Связь удалена"
                    )
                    loadSubjectsInDirections(reloadDirectionId, reloadUniversityId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createSubject(request: SubjectRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createSubject(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Предмет создан"
                    )
                    loadSubjects()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updateSubject(id: Long, request: SubjectRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.updateSubject(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Предмет обновлён"
                    )
                    loadSubjects()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteSubject(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Предмет удалён"
                    )
                    loadSubjects()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createGroup(request: AcademicGroupRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createGroup(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Группа создана"
                    )
                    loadGroups()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun updateGroup(id: Long, request: AcademicGroupRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.updateGroup(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Группа обновлена"
                    )
                    loadGroups()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteGroup(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Группа удалена"
                    )
                    loadGroups()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun loadClassrooms(universityId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            educationRepository.getClassrooms(universityId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, classrooms = it)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun createClassroom(request: ClassroomRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.createClassroom(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Аудитория добавлена"
                    )
                    loadClassrooms(_uiState.value.adminUniversityId ?: request.universityId)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun updateClassroom(id: Long, request: ClassroomRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.updateClassroom(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Аудитория обновлена"
                    )
                    loadClassrooms(_uiState.value.adminUniversityId ?: request.universityId)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun deleteClassroom(id: Long, listContextUniversityId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionSuccess = false)
            educationRepository.deleteClassroom(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "Аудитория удалена"
                    )
                    loadClassrooms(_uiState.value.adminUniversityId ?: listContextUniversityId)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearActionSuccess() {
        _uiState.value = _uiState.value.copy(actionSuccess = false, actionMessage = null)
    }
}
