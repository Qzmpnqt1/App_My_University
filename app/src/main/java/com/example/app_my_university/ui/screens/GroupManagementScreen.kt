package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.example.app_my_university.data.api.model.AcademicGroupRequest
import com.example.app_my_university.data.api.model.AcademicGroupResponse
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.test.UiTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AcademicGroupResponse?>(null) }
    var deletingGroup by remember { mutableStateOf<AcademicGroupResponse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }
    LaunchedEffect(
        uiState.adminUniversityId,
        uiState.isSuperAdmin,
    ) {
        viewModel.loadDirections(null)
        viewModel.loadGroups(null)
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            uiState.actionMessage?.let { snackbarHostState.showSnackbar(it) }
            viewModel.clearActionSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val filtered = uiState.groups.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            (it.directionName?.contains(searchQuery, ignoreCase = true) == true)
    }

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        screenTestTag = UiTestTags.Screen.ADMIN_GROUPS,
        topBar = {
            UniformTopAppBar(
                title = "Управление группами",
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingGroup = null
                    showEditor = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить группу")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.screenPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по группе или направлению") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Dimens.spaceS))
            Text(
                text = "Найдено: ${filtered.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.spaceM))
            when {
                uiState.isLoading && uiState.groups.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered) { group ->
                            GroupManagementCard(
                                group = group,
                                onEdit = {
                                    editingGroup = group
                                    showEditor = true
                                },
                                onDelete = { deletingGroup = group }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        GroupEditorDialog(
            group = editingGroup,
            directions = uiState.directions,
            onDismiss = {
                showEditor = false
                editingGroup = null
            },
            onSave = { request, id ->
                if (id != null) viewModel.updateGroup(id, request)
                else viewModel.createGroup(request)
                showEditor = false
                editingGroup = null
            }
        )
    }

    deletingGroup?.let { g ->
        AlertDialog(
            onDismissRequest = { deletingGroup = null },
            title = { Text("Удалить группу?") },
            text = { Text("«${g.name}» будет удалена.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(g.id)
                        deletingGroup = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGroup = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun GroupManagementCard(
    group: AcademicGroupResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.spaceM)) {
            RowBetweenTitleActions(
                title = group.name,
                onEdit = onEdit,
                onDelete = onDelete
            )
            group.directionName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "Курс ${group.course ?: "—"} · Поступление ${group.yearOfAdmission ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RowBetweenTitleActions(
    title: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Изменить")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditorDialog(
    group: AcademicGroupResponse?,
    directions: List<StudyDirectionResponse>,
    onDismiss: () -> Unit,
    onSave: (AcademicGroupRequest, Long?) -> Unit
) {
    var name by remember(group) { mutableStateOf(group?.name ?: "") }
    var course by remember(group) { mutableStateOf(group?.course?.toString() ?: "1") }
    var year by remember(group) { mutableStateOf(group?.yearOfAdmission?.toString() ?: "${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}") }
    var directionId by remember(group) { mutableStateOf(group?.directionId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (group == null) "Новая группа" else "Редактирование группы") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = directions.find { it.id == directionId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Направление *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        directions.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.name) },
                                onClick = {
                                    directionId = d.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = course,
                    onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) course = it },
                    label = { Text("Курс *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) year = it },
                    label = { Text("Год поступления *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = course.toIntOrNull() ?: return@Button
                    val y = year.toIntOrNull() ?: return@Button
                    val dir = directionId ?: return@Button
                    if (name.isBlank()) return@Button
                    onSave(
                        AcademicGroupRequest(
                            name = name.trim(),
                            course = c,
                            yearOfAdmission = y,
                            directionId = dir
                        ),
                        group?.id
                    )
                },
                enabled = name.isNotBlank() && directionId != null &&
                    course.toIntOrNull() != null && year.toIntOrNull() != null
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
