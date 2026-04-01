package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.TeacherSubjectResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeacherAssignmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var teacherMenu by remember { mutableStateOf(false) }
    var subjectMenu by remember { mutableStateOf(false) }
    var selectedTeacherId by remember { mutableLongStateOf(0L) }
    var selectedSubjectId by remember { mutableLongStateOf(0L) }
    var deleteId by remember { mutableStateOf<TeacherSubjectResponse?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }
    LaunchedEffect(uiState.adminUniversityId) {
        if (uiState.adminUniversityId != null) {
            viewModel.loadUsers()
            viewModel.loadSubjects()
            viewModel.loadTeacherSubjects()
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

    val teachers = remember(uiState.users) {
        uiState.users.filter { it.userType == "TEACHER" }
    }
    val filteredLinks = remember(search, uiState.teacherSubjects) {
        if (search.isBlank()) uiState.teacherSubjects
        else uiState.teacherSubjects.filter { ts ->
            (ts.teacherName?.contains(search, true) == true) ||
                (ts.subjectName?.contains(search, true) == true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Преподаватели и дисциплины") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (teachers.isNotEmpty() && uiState.subjects.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        selectedTeacherId = teachers.first().id
                        selectedSubjectId = uiState.subjects.first().id
                        showAdd = true
                    }
                ) { Icon(Icons.Default.Add, "Назначить") }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (uiState.adminUniversityId == null && !uiState.isLoading) {
            MuEmptyState(
                title = "Нет данных вуза",
                subtitle = "Обновите экран позже.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }
        if (uiState.isLoading && uiState.teacherSubjects.isEmpty() && uiState.subjects.isEmpty()) {
            MuLoadingState(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
        ) {
            item {
                Text(
                    "Назначения определяют, какие дисциплины преподаватель ведёт в системе (для расписания и журналов).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    search,
                    { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поиск") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
            }
            if (filteredLinks.isEmpty()) {
                item {
                    MuEmptyState(
                        title = "Нет назначений",
                        subtitle = "Добавьте связь преподаватель — дисциплина.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filteredLinks, key = { it.id }) { link ->
                    Card(
                        Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        androidx.compose.foundation.layout.Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spaceM),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    link.subjectName ?: "Предмет #${link.subjectId}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    link.teacherName ?: "Преподаватель #${link.teacherId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { deleteId = link }) {
                                Icon(Icons.Default.Delete, "Снять", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd && teachers.isNotEmpty() && uiState.subjects.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Назначить преподавателя") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = teacherMenu, onExpandedChange = { teacherMenu = it }) {
                        OutlinedTextField(
                            value = teachers.find { it.id == selectedTeacherId }?.let { "${it.lastName} ${it.firstName}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Преподаватель") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(teacherMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = teacherMenu, onDismissRequest = { teacherMenu = false }) {
                            teachers.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.lastName} ${t.firstName}") },
                                    onClick = {
                                        selectedTeacherId = t.id
                                        teacherMenu = false
                                    }
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = subjectMenu, onExpandedChange = { subjectMenu = it }) {
                        OutlinedTextField(
                            value = uiState.subjects.find { it.id == selectedSubjectId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Дисциплина") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subjectMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = subjectMenu, onDismissRequest = { subjectMenu = false }) {
                            uiState.subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectId = s.id
                                        subjectMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createTeacherSubjectLink(selectedTeacherId, selectedSubjectId)
                        showAdd = false
                    },
                    enabled = selectedTeacherId > 0 && selectedSubjectId > 0
                ) { Text("Сохранить") }
            },
            dismissButton = { TextButton({ showAdd = false }) { Text("Отмена") } }
        )
    }

    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Снять назначение?") },
            text = { Text("${deleteId?.teacherName} — ${deleteId?.subjectName}") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteId?.let { viewModel.deleteTeacherSubjectLink(it.id) }
                        deleteId = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Снять") }
            },
            dismissButton = { TextButton({ deleteId = null }) { Text("Отмена") } }
        )
    }
}
