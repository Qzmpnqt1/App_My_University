package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.ScheduleRequest
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.AdminBottomBar
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.viewmodel.ScheduleViewModel

private val adminDayNames = mapOf(
    1 to "Понедельник",
    2 to "Вторник",
    3 to "Среда",
    4 to "Четверг",
    5 to "Пятница",
    6 to "Суббота",
    7 to "Воскресенье"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val adminState by adminViewModel.uiState.collectAsState()
    val currentRoute = navController.currentDestination?.route
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingEntry by remember { mutableStateOf<ScheduleResponse?>(null) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var groupMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        adminViewModel.loadAdminContext()
    }
    LaunchedEffect(adminState.adminUniversityId) {
        adminViewModel.loadUsers()
        adminViewModel.loadGroups(null)
        adminViewModel.loadClassrooms(adminState.adminUniversityId)
        adminViewModel.loadSubjectsInDirections()
    }

    LaunchedEffect(selectedGroupId) {
        viewModel.setViewingGroup(selectedGroupId)
    }

    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            snackbarHostState.showSnackbar("Запись расписания создана")
            viewModel.clearSuccessFlags()
        }
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            snackbarHostState.showSnackbar("Запись расписания удалена")
            viewModel.clearSuccessFlags()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Управление расписанием",
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            if (selectedGroupId != null) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }
        },
        bottomBar = {
            AdminBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.AdminHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ExposedDropdownMenuBox(
                expanded = groupMenuExpanded,
                onExpandedChange = { groupMenuExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = adminState.groups.find { it.id == selectedGroupId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Группа *") },
                    placeholder = { Text("Выберите группу") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = groupMenuExpanded,
                    onDismissRequest = { groupMenuExpanded = false }
                ) {
                    adminState.groups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.name) },
                            onClick = {
                                selectedGroupId = g.id
                                groupMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = uiState.currentWeek == 1,
                    onClick = { viewModel.setWeek(1) },
                    label = { Text("Неделя 1") }
                )
                FilterChip(
                    selected = uiState.currentWeek == 2,
                    onClick = { viewModel.setWeek(2) },
                    label = { Text("Неделя 2") }
                )
            }

            when {
                selectedGroupId == null -> {
                    Text(
                        text = "Выберите учебную группу, чтобы просматривать и редактировать расписание",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                uiState.isLoading && uiState.scheduleByDay.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
                uiState.scheduleByDay.isEmpty() -> {
                    Text(
                        text = "Расписание пусто",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.scheduleByDay.forEach { (day, entries) ->
                            item {
                                Text(
                                    text = adminDayNames[day] ?: "День $day",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(entries) { entry ->
                                AdminScheduleEntryCard(
                                    entry = entry,
                                    onDelete = {
                                        deletingEntry = entry
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && selectedGroupId != null) {
        AddScheduleDialog(
            groupId = selectedGroupId!!,
            groupName = adminState.groups.find { it.id == selectedGroupId }?.name.orEmpty(),
            subjectsInDirections = adminState.subjectsInDirections,
            teachers = adminState.users.filter { it.userType == "TEACHER" },
            classrooms = adminState.classrooms,
            onDismiss = { showAddDialog = false },
            onCreate = { request ->
                viewModel.createSchedule(request)
                showAddDialog = false
            }
        )
    }

    if (showDeleteDialog && deletingEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись") },
            text = {
                Text(
                    "Удалить \"${deletingEntry?.subjectName ?: "запись"}\" " +
                        "(${adminDayNames[deletingEntry?.dayOfWeek] ?: ""} " +
                        "${deletingEntry?.startTime} — ${deletingEntry?.endTime})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletingEntry?.let { viewModel.deleteSchedule(it.id) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun AdminScheduleEntryCard(
    entry: ScheduleResponse,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.startTime} — ${entry.endTime}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = entry.subjectName ?: "Предмет",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                entry.teacherName?.let {
                    Text(
                        text = "Преподаватель: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                entry.groupName?.let {
                    Text(
                        text = "Группа: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                entry.classroomInfo?.let {
                    Text(
                        text = "Аудитория: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(
    groupId: Long,
    groupName: String,
    subjectsInDirections: List<SubjectInDirectionResponse>,
    teachers: List<UserProfileResponse>,
    classrooms: List<com.example.app_my_university.data.api.model.ClassroomResponse>,
    onDismiss: () -> Unit,
    onCreate: (ScheduleRequest) -> Unit
) {
    var selectedSubjectLink by remember { mutableStateOf<SubjectInDirectionResponse?>(null) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var selectedTeacher by remember { mutableStateOf<UserProfileResponse?>(null) }
    var teacherMenuExpanded by remember { mutableStateOf(false) }
    var selectedClassroom by remember { mutableStateOf<com.example.app_my_university.data.api.model.ClassroomResponse?>(null) }
    var classroomMenuExpanded by remember { mutableStateOf(false) }
    var dayOfWeek by remember { mutableStateOf("1") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var weekNumber by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись расписания") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubjectLink?.let {
                            "${it.subjectName ?: "Предмет"} (${it.directionName ?: ""})"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Предмет в направлении *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjectsInDirections.forEach { sid ->
                            DropdownMenuItem(
                                text = { Text("${sid.subjectName ?: ""} (${sid.directionName ?: ""})") },
                                onClick = {
                                    selectedSubjectLink = sid
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = teacherMenuExpanded,
                    onExpandedChange = { teacherMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTeacher?.let { "${it.lastName} ${it.firstName}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Преподаватель *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherMenuExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = teacherMenuExpanded,
                        onDismissRequest = { teacherMenuExpanded = false }
                    ) {
                        teachers.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.lastName} ${t.firstName} (${t.email})") },
                                onClick = {
                                    selectedTeacher = t
                                    teacherMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = if (groupName.isNotBlank()) {
                        "Группа: $groupName"
                    } else {
                        "Запись будет добавлена для выбранной группы"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = classroomMenuExpanded,
                    onExpandedChange = { classroomMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClassroom?.let { "${it.building ?: ""} ${it.roomNumber ?: ""}".trim() }
                            ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Аудитория *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = classroomMenuExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = classroomMenuExpanded,
                        onDismissRequest = { classroomMenuExpanded = false }
                    ) {
                        classrooms.forEach { c ->
                            val place = listOfNotNull(c.building, c.roomNumber)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                                .ifBlank { "Аудитория" }
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(place)
                                        c.capacity?.let { cap ->
                                            Text(
                                                "До $cap мест",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedClassroom = c
                                    classroomMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("День недели", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Пн" to "1", "Вт" to "2", "Ср" to "3", "Чт" to "4", "Пт" to "5", "Сб" to "6").forEachIndexed { index, (label, value) ->
                        SegmentedButton(
                            selected = dayOfWeek == value,
                            onClick = { dayOfWeek = value },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = 6
                            )
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Начало") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Конец") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = weekNumber == "1",
                        onClick = { weekNumber = "1" },
                        label = { Text("Неделя 1") }
                    )
                    FilterChip(
                        selected = weekNumber == "2",
                        onClick = { weekNumber = "2" },
                        label = { Text("Неделя 2") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sid = selectedSubjectLink?.id ?: return@Button
                    val tid = selectedTeacher?.id ?: return@Button
                    val cid = selectedClassroom?.id ?: return@Button
                    val dow = dayOfWeek.toIntOrNull() ?: return@Button
                    val wn = weekNumber.toIntOrNull() ?: return@Button
                    onCreate(
                        ScheduleRequest(
                            subjectTypeId = sid,
                            teacherId = tid,
                            groupId = groupId,
                            classroomId = cid,
                            dayOfWeek = dow,
                            startTime = startTime,
                            endTime = endTime,
                            weekNumber = wn
                        )
                    )
                },
                enabled = selectedSubjectLink != null && selectedTeacher != null && selectedClassroom != null
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
