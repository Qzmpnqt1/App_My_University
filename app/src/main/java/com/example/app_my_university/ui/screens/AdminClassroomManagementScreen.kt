package com.example.app_my_university.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.ClassroomRequest
import com.example.app_my_university.data.api.model.ClassroomResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.test.UiTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassroomManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ClassroomResponse?>(null) }
    var deleteTarget by remember { mutableStateOf<ClassroomResponse?>(null) }
    var building by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var createUniversityId by remember { mutableStateOf<Long?>(null) }
    var universityMenuExpanded by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }
    LaunchedEffect(uiState.adminUniversityId, uiState.isSuperAdmin) {
        when {
            uiState.isSuperAdmin && uiState.adminUniversityId == null ->
                viewModel.loadClassrooms(null)
            uiState.isSuperAdmin || uiState.adminUniversityId != null ->
                viewModel.loadClassrooms(uiState.adminUniversityId)
            else -> Unit
        }
    }
    LaunchedEffect(showForm) {
        if (showForm && uiState.isSuperAdmin && uiState.adminUniversityId == null && uiState.universities.isEmpty()) {
            viewModel.loadUniversities()
        }
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            uiState.actionMessage?.let { snackbar.showSnackbar(it) }
            viewModel.clearActionSuccess()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val uniId = uiState.adminUniversityId
    val filtered = remember(search, uiState.classrooms) {
        if (search.isBlank()) uiState.classrooms
        else uiState.classrooms.filter { c ->
            (c.building?.contains(search, true) == true) ||
                (c.roomNumber?.contains(search, true) == true)
        }
    }

    val formTargetUniversityId = editing?.universityId ?: uniId ?: createUniversityId

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        screenTestTag = UiTestTags.Screen.ADMIN_CLASSROOMS,
        topBar = {
            UniformTopAppBar(
                title = "Аудитории",
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            if (uniId != null || uiState.isSuperAdmin) {
                FloatingActionButton(
                    onClick = {
                        editing = null
                        building = ""
                        room = ""
                        capacity = ""
                        createUniversityId = uniId
                        showForm = true
                    }
                ) { Icon(Icons.Default.Add, "Добавить") }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            uniId == null && !uiState.isSuperAdmin && !uiState.isLoading -> {
                MuEmptyState(
                    title = "Вуз не определён",
                    subtitle = "Не удалось загрузить контекст администратора.",
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
            uiState.isLoading && uiState.classrooms.isEmpty() -> {
                MuLoadingState(Modifier.fillMaxSize().padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
                ) {
                    item {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Поиск по корпусу или номеру") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true
                        )
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                "Нет аудиторий — добавьте первую.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { c ->
                            ClassroomRow(
                                c = c,
                                onEdit = {
                                    editing = c
                                    building = c.building ?: ""
                                    room = c.roomNumber ?: ""
                                    capacity = c.capacity?.toString() ?: ""
                                    createUniversityId = null
                                    showForm = true
                                },
                                onDelete = { deleteTarget = c }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        val canSave = building.isNotBlank() && room.isNotBlank() && formTargetUniversityId != null
        AlertDialog(
            onDismissRequest = {
                showForm = false
                createUniversityId = null
            },
            title = { Text(if (editing == null) "Новая аудитория" else "Редактирование") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editing == null && uiState.isSuperAdmin && uniId == null) {
                        ExposedDropdownMenuBox(
                            expanded = universityMenuExpanded,
                            onExpandedChange = { universityMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.universities.find { it.id == createUniversityId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Вуз *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = universityMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = universityMenuExpanded,
                                onDismissRequest = { universityMenuExpanded = false }
                            ) {
                                uiState.universities.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u.name) },
                                        onClick = {
                                            createUniversityId = u.id
                                            universityMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        building,
                        { building = it },
                        label = { Text("Корпус *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        room,
                        { room = it },
                        label = { Text("Номер *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        capacity,
                        { capacity = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Вместимость") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = capacity.toIntOrNull()
                        val uid = formTargetUniversityId ?: return@Button
                        val req = ClassroomRequest(
                            building = building.trim(),
                            roomNumber = room.trim(),
                            capacity = cap,
                            universityId = uid
                        )
                        if (editing == null) viewModel.createClassroom(req)
                        else viewModel.updateClassroom(editing!!.id, req)
                        showForm = false
                        createUniversityId = null
                    },
                    enabled = canSave
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton({
                    showForm = false
                    createUniversityId = null
                }) { Text("Отмена") }
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Удалить аудиторию?") },
            text = { Text("${deleteTarget?.building} ${deleteTarget?.roomNumber}".trim()) },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTarget?.let { viewModel.deleteClassroom(it.id, it.universityId) }
                        deleteTarget = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton({ deleteTarget = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun ClassroomRow(
    c: ClassroomResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${c.building ?: ""} ${c.roomNumber ?: ""}".trim().ifBlank { "—" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                c.capacity?.let {
                    Text(
                        "Вместимость: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Edit, "Изменить")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
