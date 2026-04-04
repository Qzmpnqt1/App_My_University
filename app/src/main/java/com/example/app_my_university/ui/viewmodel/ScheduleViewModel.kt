package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.core.logging.AppLogger
import com.example.app_my_university.data.api.model.ScheduleCompareClassroomOptionResponse
import com.example.app_my_university.data.api.model.ScheduleCompareGroupOptionResponse
import com.example.app_my_university.data.api.model.ScheduleCompareRequest
import com.example.app_my_university.data.api.model.ScheduleCompareResultResponse
import com.example.app_my_university.data.api.model.ScheduleCompareTeacherOptionResponse
import com.example.app_my_university.data.api.model.ScheduleRequest
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScheduleScreenMode {
    NORMAL,
    COMPARE,
    CLASSROOM_ONLY,
}

enum class ScheduleCompareRunMode {
    /** Не активно */
    NONE,
    /** Студент / преподаватель: моя сторона с сервера */
    MY_WITH_OTHER,
    /** Админ: обе стороны в запросе */
    FULL,
}

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val scheduleByDay: Map<Int, List<ScheduleResponse>> = emptyMap(),
    val currentWeek: Int = 1,
    val selectedDayOfWeek: Int? = null,
    val viewingGroupId: Long? = null,
    val createSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val screenMode: ScheduleScreenMode = ScheduleScreenMode.NORMAL,
    val compareRunMode: ScheduleCompareRunMode = ScheduleCompareRunMode.NONE,
    val compareLoading: Boolean = false,
    val compareError: String? = null,
    val compareResult: ScheduleCompareResultResponse? = null,
    val myCompareRightKind: String? = null,
    val myCompareRightId: Long? = null,
    val fullCompareLeftKind: String? = null,
    val fullCompareLeftId: Long? = null,
    val fullCompareRightKind: String? = null,
    val fullCompareRightId: Long? = null,
    val classroomLoading: Boolean = false,
    val classroomError: String? = null,
    val classroomId: Long? = null,
    val classroomLabel: String? = null,
    val classroomByDay: Map<Int, List<ScheduleResponse>> = emptyMap(),
    val pickListLoading: Boolean = false,
    val pickGroups: List<ScheduleCompareGroupOptionResponse> = emptyList(),
    val pickTeachers: List<ScheduleCompareTeacherOptionResponse> = emptyList(),
    val pickClassrooms: List<ScheduleCompareClassroomOptionResponse> = emptyList(),
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState

    fun setViewingGroup(groupId: Long?) {
        _uiState.update { it.copy(viewingGroupId = groupId, error = null) }
        if (groupId != null) {
            loadSchedule()
        } else {
            _uiState.update { it.copy(scheduleByDay = emptyMap(), isLoading = false) }
        }
    }

    fun setSelectedDayOfWeek(day: Int?) {
        _uiState.update { it.copy(selectedDayOfWeek = day) }
        when (_uiState.value.screenMode) {
            ScheduleScreenMode.NORMAL -> loadSchedule()
            ScheduleScreenMode.COMPARE -> refreshCompare()
            ScheduleScreenMode.CLASSROOM_ONLY -> refreshClassroom()
        }
    }

    fun loadSchedule(weekNumber: Int? = null) {
        if (_uiState.value.screenMode != ScheduleScreenMode.NORMAL) return
        viewModelScope.launch {
            val week = weekNumber ?: _uiState.value.currentWeek
            val day = _uiState.value.selectedDayOfWeek
            val groupId = _uiState.value.viewingGroupId
            _uiState.update { it.copy(isLoading = true, error = null, currentWeek = week) }

            val result = if (groupId != null) {
                scheduleRepository.getGroupSchedule(groupId, week, day)
            } else {
                scheduleRepository.getMySchedule(week, day)
            }

            result.fold(
                onSuccess = { entries ->
                    val grouped = entries.groupBy { it.dayOfWeek }
                        .toSortedMap()
                        .mapValues { (_, list) -> list.sortedBy { it.startTime } }
                    _uiState.update { it.copy(isLoading = false, scheduleByDay = grouped) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun setWeek(weekNumber: Int) {
        _uiState.update { it.copy(currentWeek = weekNumber) }
        when (_uiState.value.screenMode) {
            ScheduleScreenMode.NORMAL -> loadSchedule(weekNumber)
            ScheduleScreenMode.COMPARE -> refreshCompare()
            ScheduleScreenMode.CLASSROOM_ONLY -> refreshClassroom()
        }
    }

    fun loadComparePickerLists() {
        viewModelScope.launch {
            AppLogger.userAction("ScheduleCompare", "load picker lists")
            _uiState.update { it.copy(pickListLoading = true) }
            val g = async { scheduleRepository.listCompareGroups(null, null, null) }
            val t = async { scheduleRepository.listCompareTeachers(null) }
            val c = async { scheduleRepository.listCompareClassrooms(null) }
            val gr = g.await().getOrElse { emptyList() }
            val tr = t.await().getOrElse { emptyList() }
            val cr = c.await().getOrElse { emptyList() }
            _uiState.update {
                it.copy(
                    pickListLoading = false,
                    pickGroups = gr,
                    pickTeachers = tr,
                    pickClassrooms = cr,
                )
            }
            AppLogger.i("ScheduleCompare", "picker loaded groups=${gr.size} teachers=${tr.size} classrooms=${cr.size}")
        }
    }

    fun runMyCompare(rightKind: String, rightId: Long) {
        viewModelScope.launch {
            AppLogger.userAction("ScheduleCompare", "my compare rightKind=$rightKind rightId=$rightId")
            _uiState.update {
                it.copy(
                    compareLoading = true,
                    compareError = null,
                    screenMode = ScheduleScreenMode.COMPARE,
                    compareRunMode = ScheduleCompareRunMode.MY_WITH_OTHER,
                    myCompareRightKind = rightKind,
                    myCompareRightId = rightId,
                )
            }
            val s = _uiState.value
            val req = ScheduleCompareRequest(
                mode = "MY_WITH_OTHER",
                rightKind = rightKind,
                rightId = rightId,
                weekNumber = s.currentWeek,
                dayOfWeek = s.selectedDayOfWeek,
            )
            scheduleRepository.compareSchedule(req).fold(
                onSuccess = { res ->
                    _uiState.update { it.copy(compareLoading = false, compareResult = res) }
                    AppLogger.i("ScheduleCompare", "my compare success segmentsBoth=${res.segmentsBothSidesBusy}")
                },
                onFailure = { e ->
                    _uiState.update { it.copy(compareLoading = false, compareError = e.message) }
                    AppLogger.e("ScheduleCompare", "my compare failed: ${e.message}")
                }
            )
        }
    }

    fun runFullCompare(
        leftKind: String,
        leftId: Long,
        rightKind: String,
        rightId: Long,
    ) {
        viewModelScope.launch {
            AppLogger.userAction(
                "ScheduleCompare",
                "full compare left=$leftKind:$leftId right=$rightKind:$rightId"
            )
            _uiState.update {
                it.copy(
                    compareLoading = true,
                    compareError = null,
                    screenMode = ScheduleScreenMode.COMPARE,
                    compareRunMode = ScheduleCompareRunMode.FULL,
                    fullCompareLeftKind = leftKind,
                    fullCompareLeftId = leftId,
                    fullCompareRightKind = rightKind,
                    fullCompareRightId = rightId,
                    myCompareRightKind = null,
                    myCompareRightId = null,
                )
            }
            val s = _uiState.value
            val req = ScheduleCompareRequest(
                mode = "FULL",
                leftKind = leftKind,
                leftId = leftId,
                rightKind = rightKind,
                rightId = rightId,
                weekNumber = s.currentWeek,
                dayOfWeek = s.selectedDayOfWeek,
            )
            scheduleRepository.compareSchedule(req).fold(
                onSuccess = { res ->
                    _uiState.update { it.copy(compareLoading = false, compareResult = res) }
                    AppLogger.i("ScheduleCompare", "full compare success")
                },
                onFailure = { e ->
                    _uiState.update { it.copy(compareLoading = false, compareError = e.message) }
                    AppLogger.e("ScheduleCompare", "full compare failed: ${e.message}")
                }
            )
        }
    }

    fun retryCurrentAlternateLoad() {
        when (_uiState.value.screenMode) {
            ScheduleScreenMode.COMPARE -> refreshCompare()
            ScheduleScreenMode.CLASSROOM_ONLY -> refreshClassroom()
            ScheduleScreenMode.NORMAL -> loadSchedule()
        }
    }

    private fun refreshCompare() {
        when (_uiState.value.compareRunMode) {
            ScheduleCompareRunMode.MY_WITH_OTHER -> {
                val k = _uiState.value.myCompareRightKind ?: return
                val id = _uiState.value.myCompareRightId ?: return
                runMyCompare(k, id)
            }
            ScheduleCompareRunMode.FULL -> {
                val lk = _uiState.value.fullCompareLeftKind ?: return
                val lid = _uiState.value.fullCompareLeftId ?: return
                val rk = _uiState.value.fullCompareRightKind ?: return
                val rid = _uiState.value.fullCompareRightId ?: return
                runFullCompare(lk, lid, rk, rid)
            }
            ScheduleCompareRunMode.NONE -> Unit
        }
    }

    fun openClassroomView(classroomId: Long, label: String) {
        AppLogger.userAction("ScheduleClassroom", "open id=$classroomId")
        _uiState.update {
            it.copy(
                screenMode = ScheduleScreenMode.CLASSROOM_ONLY,
                compareRunMode = ScheduleCompareRunMode.NONE,
                classroomId = classroomId,
                classroomLabel = label,
                classroomLoading = true,
                classroomError = null,
                compareResult = null,
            )
        }
        refreshClassroom()
    }

    private fun refreshClassroom() {
        val id = _uiState.value.classroomId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(classroomLoading = true, classroomError = null) }
            val s = _uiState.value
            scheduleRepository.getClassroomSchedule(id, s.currentWeek, s.selectedDayOfWeek).fold(
                onSuccess = { entries ->
                    val grouped = entries.groupBy { it.dayOfWeek }
                        .toSortedMap()
                        .mapValues { (_, list) -> list.sortedBy { it.startTime } }
                    _uiState.update { it.copy(classroomLoading = false, classroomByDay = grouped) }
                    AppLogger.i("ScheduleClassroom", "loaded slots=${entries.size}")
                },
                onFailure = { e ->
                    _uiState.update { it.copy(classroomLoading = false, classroomError = e.message) }
                    AppLogger.e("ScheduleClassroom", "failed: ${e.message}")
                }
            )
        }
    }

    /** Сброс режимов сравнения / аудитории без перезагрузки расписания (для админ-вкладки «Управление»). */
    fun clearAnalyzeModes() {
        _uiState.update {
            it.copy(
                screenMode = ScheduleScreenMode.NORMAL,
                compareRunMode = ScheduleCompareRunMode.NONE,
                compareResult = null,
                compareError = null,
                compareLoading = false,
                myCompareRightKind = null,
                myCompareRightId = null,
                fullCompareLeftKind = null,
                fullCompareLeftId = null,
                fullCompareRightKind = null,
                fullCompareRightId = null,
                classroomId = null,
                classroomLabel = null,
                classroomByDay = emptyMap(),
                classroomError = null,
                classroomLoading = false,
            )
        }
    }

    fun exitCompareAndClassroom() {
        AppLogger.userAction("ScheduleCompare", "reset to normal")
        clearAnalyzeModes()
        loadSchedule()
    }

    fun createSchedule(request: ScheduleRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, createSuccess = false) }
            educationRepository.createSchedule(request).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, createSuccess = true) }
                    loadSchedule()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, deleteSuccess = false) }
            educationRepository.deleteSchedule(id).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, deleteSuccess = true) }
                    loadSchedule()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, compareError = null, classroomError = null) }
    }

    fun clearSuccessFlags() {
        _uiState.update { it.copy(createSuccess = false, deleteSuccess = false) }
    }
}
