package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.GuestRegistrationStatusResponse
import com.example.app_my_university.data.api.model.RegisterRequest
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.withBelowTopBarContentGap
import com.example.app_my_university.ui.components.picker.MuPickerField
import com.example.app_my_university.ui.components.picker.MuSearchablePickerSheet
import com.example.app_my_university.ui.components.picker.PickerListItem
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.viewmodel.RegistrationStatusViewModel
import com.example.app_my_university.util.formatApiDateTimeForDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationStatusScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistrationStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.error) {
        state.error?.let { viewModel.clearError() }
    }

    Scaffold(
        modifier = Modifier.testTag(UiTestTags.Screen.REGISTRATION_STATUS),
        topBar = {
            UniformTopAppBar(
                title = "Статус заявки",
                onBackPressed = onNavigateBack,
            )
        }
    ) { padding ->
        val contentPadding = padding.withBelowTopBarContentGap()
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Проверка и редактирование заявки (только PENDING). Данные защищены: нужны email и пароль из заявки.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль из заявки") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { viewModel.lookup(email, password) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Проверить статус") }

            state.status?.let { s ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Статус: ${s.status ?: "—"}", style = MaterialTheme.typography.titleMedium)
                        Text("Тип: ${s.userType ?: "—"}")
                        s.rejectionReason?.let { Text("Причина отказа: $it", color = MaterialTheme.colorScheme.error) }
                        s.createdAt?.let {
                            Text(
                                "Создана: ${formatApiDateTimeForDisplay(it)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                if (s.status.equals("PENDING", ignoreCase = true)) {
                    Spacer(Modifier.height(8.dp))
                    Text("Редактирование заявки", style = MaterialTheme.typography.titleSmall)
                    PendingEditForm(
                        viewModel = viewModel,
                        catalogState = state,
                        status = s,
                        defaultEmail = email,
                        onSubmit = { currentPwd, req -> viewModel.updatePending(currentPwd, req) },
                        isLoading = state.isLoading
                    )
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private enum class GuestEntityPick { None, University, Institute, Direction, Group }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingEditForm(
    viewModel: RegistrationStatusViewModel,
    catalogState: com.example.app_my_university.ui.viewmodel.RegistrationStatusUiState,
    status: GuestRegistrationStatusResponse,
    defaultEmail: String,
    onSubmit: (String, RegisterRequest) -> Unit,
    isLoading: Boolean,
) {
    var regEmail by remember { mutableStateOf(defaultEmail) }
    LaunchedEffect(defaultEmail) { regEmail = defaultEmail }
    var currentPwd by remember { mutableStateOf("") }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var mid by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf(status.userType ?: "STUDENT") }
    var userTypeMenu by remember { mutableStateOf(false) }

    var selUniversityId by remember { mutableStateOf<Long?>(status.universityId) }
    var selInstituteId by remember { mutableStateOf<Long?>(status.instituteId) }
    var selDirectionId by remember { mutableStateOf<Long?>(null) }
    var selGroupId by remember { mutableStateOf<Long?>(status.groupId) }

    var labUniversity by remember { mutableStateOf("") }
    var labInstitute by remember { mutableStateOf("") }
    var labDirection by remember { mutableStateOf("") }
    var labGroup by remember { mutableStateOf("") }

    var guestPick by remember { mutableStateOf(GuestEntityPick.None) }

    LaunchedEffect(catalogState.guestUniversities, status.universityId) {
        val uid = status.universityId ?: return@LaunchedEffect
        catalogState.guestUniversities.find { it.id == uid }?.let { labUniversity = it.name }
        viewModel.loadGuestInstitutes(uid)
    }

    LaunchedEffect(catalogState.guestInstitutes, status.instituteId, status.userType) {
        if (!status.userType.equals("STUDENT", ignoreCase = true)) return@LaunchedEffect
        val iid = status.instituteId ?: return@LaunchedEffect
        catalogState.guestInstitutes.find { it.id == iid }?.let { labInstitute = it.name }
        viewModel.loadGuestDirections(iid)
    }

    LaunchedEffect(status.groupId, status.userType) {
        val gid = status.groupId ?: return@LaunchedEffect
        if (!status.userType.equals("STUDENT", ignoreCase = true)) return@LaunchedEffect
        val g = viewModel.fetchGroupForPrefill(gid) ?: return@LaunchedEffect
        selDirectionId = g.directionId
        labDirection = g.directionName?.takeIf { it.isNotBlank() } ?: labDirection
        viewModel.loadGuestGroups(g.directionId)
        labGroup = g.name
    }

    LaunchedEffect(selUniversityId, userType) {
        val uid = selUniversityId ?: return@LaunchedEffect
        if (userType == "STUDENT") {
            viewModel.loadGuestInstitutes(uid)
        }
    }
    LaunchedEffect(selInstituteId, userType) {
        if (userType != "STUDENT") return@LaunchedEffect
        val iid = selInstituteId ?: return@LaunchedEffect
        viewModel.loadGuestDirections(iid)
    }
    LaunchedEffect(selDirectionId, userType) {
        if (userType != "STUDENT") return@LaunchedEffect
        val did = selDirectionId ?: return@LaunchedEffect
        viewModel.loadGuestGroups(did)
    }

    val pickItems: List<PickerListItem> = when (guestPick) {
        GuestEntityPick.University -> catalogState.guestUniversities.map { u ->
            PickerListItem(u.id, u.name, u.city?.takeIf { it.isNotBlank() })
        }
        GuestEntityPick.Institute -> catalogState.guestInstitutes.map { i ->
            PickerListItem(i.id, i.name, i.shortName)
        }
        GuestEntityPick.Direction -> catalogState.guestDirections.map { d ->
            PickerListItem(d.id, d.name, d.instituteName)
        }
        GuestEntityPick.Group -> catalogState.guestGroups.map { g ->
            PickerListItem(g.id, g.name, g.directionName)
        }
        GuestEntityPick.None -> emptyList()
    }

    MuSearchablePickerSheet(
        visible = guestPick != GuestEntityPick.None,
        onDismiss = { guestPick = GuestEntityPick.None },
        title = when (guestPick) {
            GuestEntityPick.University -> "Университет"
            GuestEntityPick.Institute -> "Институт"
            GuestEntityPick.Direction -> "Направление"
            GuestEntityPick.Group -> "Группа"
            GuestEntityPick.None -> ""
        },
        items = pickItems,
        onSelect = { row ->
            when (guestPick) {
                GuestEntityPick.University -> {
                    selUniversityId = row.id
                    labUniversity = row.primary
                    selInstituteId = null
                    labInstitute = ""
                    selDirectionId = null
                    labDirection = ""
                    selGroupId = null
                    labGroup = ""
                }
                GuestEntityPick.Institute -> {
                    selInstituteId = row.id
                    labInstitute = row.primary
                    selDirectionId = null
                    labDirection = ""
                    selGroupId = null
                    labGroup = ""
                }
                GuestEntityPick.Direction -> {
                    selDirectionId = row.id
                    labDirection = row.primary
                    selGroupId = null
                    labGroup = ""
                }
                GuestEntityPick.Group -> {
                    selGroupId = row.id
                    labGroup = row.primary
                }
                GuestEntityPick.None -> Unit
            }
            guestPick = GuestEntityPick.None
        },
    )

    val userTypeOptions = listOf(
        "STUDENT" to "Студент",
        "TEACHER" to "Преподаватель",
        "ADMIN" to "Администратор",
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(regEmail, { regEmail = it }, label = { Text("Email в заявке") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(currentPwd, { currentPwd = it }, label = { Text("Текущий пароль заявки") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(first, { first = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(last, { last = it }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mid, { mid = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pwd, { pwd = it }, label = { Text("Новый пароль (мин. 6)") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(
            expanded = userTypeMenu,
            onExpandedChange = { userTypeMenu = it },
        ) {
            OutlinedTextField(
                value = userTypeOptions.find { it.first == userType }?.second ?: userType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип пользователя") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userTypeMenu) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            ExposedDropdownMenu(expanded = userTypeMenu, onDismissRequest = { userTypeMenu = false }) {
                userTypeOptions.forEach { (code, labelRu) ->
                    DropdownMenuItem(
                        text = { Text(labelRu) },
                        onClick = {
                            userType = code
                            userTypeMenu = false
                            if (code == "TEACHER") {
                                selInstituteId = null
                                labInstitute = ""
                                selDirectionId = null
                                labDirection = ""
                                selGroupId = null
                                labGroup = ""
                            }
                        },
                    )
                }
            }
        }

        MuPickerField(
            label = "Университет",
            valueText = labUniversity,
            placeholder = "Выберите вуз",
            enabled = catalogState.guestUniversities.isNotEmpty(),
            onClick = { guestPick = GuestEntityPick.University },
        )
        if (userType == "STUDENT") {
            MuPickerField(
                label = "Институт *",
                valueText = labInstitute,
                placeholder = "Сначала вуз",
                enabled = selUniversityId != null && catalogState.guestInstitutes.isNotEmpty(),
                onClick = { guestPick = GuestEntityPick.Institute },
            )
        }
        if (userType == "STUDENT") {
            MuPickerField(
                label = "Направление",
                valueText = labDirection,
                placeholder = "Сначала институт",
                enabled = selInstituteId != null && catalogState.guestDirections.isNotEmpty(),
                onClick = { guestPick = GuestEntityPick.Direction },
            )
            MuPickerField(
                label = "Группа",
                valueText = labGroup,
                placeholder = "Сначала направление",
                enabled = selDirectionId != null && catalogState.guestGroups.isNotEmpty(),
                onClick = { guestPick = GuestEntityPick.Group },
            )
        }

        Button(
            onClick = {
                if (regEmail.isBlank()) return@Button
                val u = selUniversityId ?: return@Button
                onSubmit(
                    currentPwd,
                    RegisterRequest(
                        email = regEmail.trim(),
                        password = pwd,
                        firstName = first,
                        lastName = last,
                        middleName = mid.ifBlank { null },
                        userType = userType,
                        universityId = u,
                        groupId = if (userType == "STUDENT") selGroupId else null,
                        instituteId = if (userType == "STUDENT") selInstituteId else null,
                    )
                )
            },
            enabled = !isLoading && selUniversityId != null && (userType != "STUDENT" || (selGroupId != null && selInstituteId != null)),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сохранить изменения заявки") }
    }
}
