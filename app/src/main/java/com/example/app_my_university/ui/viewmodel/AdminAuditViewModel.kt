package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.api.model.AuditLogResponse
import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminAuditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val logs: List<AuditLogResponse> = emptyList()
)

@HiltViewModel
class AdminAuditViewModel @Inject constructor(
    private val auditRepository: AuditRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAuditUiState())
    val uiState: StateFlow<AdminAuditUiState> = _uiState

    fun load(
        userId: Long? = null,
        action: String? = null,
        entityType: String? = null,
        fromIso: String? = null,
        toIso: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AdminAuditUiState(isLoading = true)
            val scopeUni = if (tokenManager.getUserType()?.equals("SUPER_ADMIN", ignoreCase = true) == true) {
                tokenManager.getSuperAdminScopeUniversityId()
            } else {
                null
            }
            auditRepository.searchLogs(
                userId,
                action?.ifBlank { null },
                entityType?.ifBlank { null },
                fromIso,
                toIso,
                universityId = scopeUni
            ).fold(
                onSuccess = { _uiState.value = AdminAuditUiState(logs = it) },
                onFailure = { _uiState.value = AdminAuditUiState(error = it.message) }
            )
        }
    }
}
