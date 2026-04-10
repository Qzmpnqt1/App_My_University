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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.data.api.model.SubjectInDirectionRequest
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.test.UiTestTags

private val assessmentOptions = listOf(
    "EXAM" to "Экзамен",
    "CREDIT" to "Зачёт",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectInDirectionManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    /** Выбранное пользователем направление; список направлений зависит от вуза администрирования. */
    var userDirectionId by remember { mutableStateOf<Long?>(null) }
    var directionMenuExpanded by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SubjectInDirectionResponse?>(null) }
    var deleting by remember { mutableStateOf<SubjectInDirectionResponse?>(null) }
    var subjectId by remember { mutableStateOf<Long?>(null) }
    var directionId by remember { mutableStateOf<Long?>(null) }
    var course by remember { mutableStateOf("1") }
    var semester by remember { mutableStateOf("1") }
    var assessment by remember { mutableStateOf("EXAM") }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var directionMenuExpandedDialog by remember { mutableStateOf(false) }
    var assessmentMenuExpanded by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    val resolvedDirectionId = remember(userDirectionId, uiState.directions) {
        val dirs = uiState.directions
        when {
            dirs.isEmpty() -> null
            userDirectionId != null && dirs.any { it.id == userDirectionId } -> userDirectionId
            else -> dirs.first().id
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }

    LaunchedEffect(uiState.adminNavContextReady, uiState.adminUniversityId) {
        if (!uiState.adminNavContextReady) return@LaunchedEffect
        viewModel.loadDirections(null)
        viewModel.loadSubjects(uiState.adminUniversityId)
    }

    LaunchedEffect(resolvedDirectionId) {
        val id = resolvedDirectionId
        if (id == null) {
            viewModel.clearSubjectsInDirections()
        } else {
            viewModel.loadSubjectsInDirections(id, null)
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

    val filteredRows = remember(search, uiState.subjectsInDirections) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) uiState.subjectsInDirections
        else uiState.subjectsInDirections.filter { r ->
            (r.subjectName?.lowercase()?.contains(q) == true) ||
                (r.directionName?.lowercase()?.contains(q) == true)
        }
    }

    val reloadDirection = resolvedDirectionId

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        screenTestTag = UiTestTags.Screen.ADMIN_SUBJECT_PLAN,
        topBar = {
            UniformTopAppBar(
                title = "Предметы в направлениях",
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            if (resolvedDirectionId != null) {
                FloatingActionButton(
                    onClick = {
                        editing = null
                        subjectId = uiState.subjects.firstOrNull()?.id
                        directionId = resolvedDirectionId
                        course = "1"
                        semester = "1"
                        assessment = "EXAM"
                        showEditor = true
                    },
                ) { Icon(Icons.Default.Add, contentDescription = "Добавить связь") }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.screenPadding),
        ) {
            val scopeHint = when {
                uiState.isSuperAdmin && uiState.adminUniversityId == null ->
                    "Направления всех вузов"
                uiState.isSuperAdmin ->
                    "Направления выбранного вуза"
                else ->
                    "Направления вашего вуза"
            }
            DirectionPickerField(
                directions = uiState.directions,
                selectedId = resolvedDirectionId,
                expanded = directionMenuExpanded,
                onExpandedChange = { directionMenuExpanded = it },
                supportingLabel = scopeHint,
                onSelect = { id ->
                    userDirectionId = id
                    directionMenuExpanded = false
                },
            )
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spaceM),
                label = { Text("Поиск") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            Text(
                "Найдено: ${filteredRows.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.spaceS, bottom = Dimens.spaceM),
            )
            when {
                !uiState.adminNavContextReady || (uiState.isLoading && uiState.directions.isEmpty()) ->
                    MuLoadingState(Modifier.fillMaxSize())
                uiState.directions.isEmpty() -> {
                    MuEmptyState(
                        title = "Нет направлений",
                        subtitle = "Создайте направления в разделе структуры или проверьте выбранный вуз.",
                    )
                }
                resolvedDirectionId == null -> {
                    MuEmptyState(
                        title = "Выберите направление",
                        subtitle = "Укажите направление в списке выше.",
                    )
                }
                uiState.isLoading && uiState.subjectsInDirections.isEmpty() ->
                    MuLoadingState(Modifier.fillMaxSize())
                filteredRows.isEmpty() -> {
                    MuEmptyState(
                        title = "Нет записей",
                        subtitle = "Добавьте предмет в учебный план направления.",
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                        contentPadding = PaddingValues(bottom = 88.dp),
                    ) {
                        items(filteredRows, key = { it.id }) { row ->
                            SidCard(
                                row = row,
                                onEdit = {
                                    editing = row
                                    subjectId = row.subjectId
                                    directionId = row.directionId
                                    course = row.course?.toString() ?: "1"
                                    semester = row.semester?.toString() ?: "1"
                                    assessment = row.finalAssessmentType ?: "EXAM"
                                    showEditor = true
                                },
                                onDelete = { deleting = row },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        val subjects = uiState.subjects
        val directions = uiState.directions
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editing == null) "Предмет в направлении" else "Редактирование") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = subjectMenuExpanded,
                        onExpandedChange = { subjectMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = subjects.find { it.id == subjectId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Предмет *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = subjectMenuExpanded,
                            onDismissRequest = { subjectMenuExpanded = false },
                        ) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        subjectId = s.id
                                        subjectMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = directionMenuExpandedDialog,
                        onExpandedChange = { directionMenuExpandedDialog = it },
                    ) {
                        OutlinedTextField(
                            value = directions.find { it.id == directionId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Направление *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionMenuExpandedDialog)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = directionMenuExpandedDialog,
                            onDismissRequest = { directionMenuExpandedDialog = false },
                        ) {
                            directions.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(directionMenuLabel(d)) },
                                    onClick = {
                                        directionId = d.id
                                        directionMenuExpandedDialog = false
                                    },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = course,
                        onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) course = it },
                        label = { Text("Курс *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) semester = it },
                        label = { Text("Семестр *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ExposedDropdownMenuBox(
                        expanded = assessmentMenuExpanded,
                        onExpandedChange = { assessmentMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = assessmentOptions.find { it.first == assessment }?.second ?: assessment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Итоговый контроль") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = assessmentMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = assessmentMenuExpanded,
                            onDismissRequest = { assessmentMenuExpanded = false },
                        ) {
                            assessmentOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.second) },
                                    onClick = {
                                        assessment = opt.first
                                        assessmentMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sid = subjectId ?: return@Button
                        val did = directionId ?: return@Button
                        val c = course.toIntOrNull() ?: return@Button
                        val sem = semester.toIntOrNull() ?: return@Button
                        val req = SubjectInDirectionRequest(
                            subjectId = sid,
                            directionId = did,
                            course = c,
                            semester = sem,
                            finalAssessmentType = assessment,
                        )
                        val rd = reloadDirection
                        if (editing == null) {
                            viewModel.createSubjectInDirection(req, rd, null)
                        } else {
                            viewModel.updateSubjectInDirection(editing!!.id, req, rd, null)
                        }
                        showEditor = false
                    },
                    enabled = subjectId != null && directionId != null &&
                        course.toIntOrNull() != null && semester.toIntOrNull() != null,
                ) { Text("Сохранить") }
            },
            dismissButton = { TextButton({ showEditor = false }) { Text("Отмена") } },
        )
    }

    deleting?.let { row ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Удалить связь?") },
            text = { Text("${row.subjectName ?: "Предмет"} — ${row.directionName ?: "направление"}") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubjectInDirection(row.id, reloadDirection, null)
                        deleting = null
                    },
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton({ deleting = null }) { Text("Отмена") } },
        )
    }
}

private fun directionMenuLabel(d: StudyDirectionResponse): String {
    val inst = d.instituteName?.trim().orEmpty()
    return if (inst.isNotEmpty()) "${d.name} ($inst)" else d.name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectionPickerField(
    directions: List<StudyDirectionResponse>,
    selectedId: Long?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    supportingLabel: String,
    onSelect: (Long) -> Unit,
) {
    val selected = directions.find { it.id == selectedId }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected?.let { directionMenuLabel(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = directions.isNotEmpty(),
            label = { Text("Направление") },
            supportingText = { Text(supportingLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            directions.forEach { d ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(d.name, style = MaterialTheme.typography.bodyLarge)
                            d.instituteName?.takeIf { it.isNotBlank() }?.let { inst ->
                                Text(
                                    inst,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    onClick = { onSelect(d.id) },
                )
            }
        }
    }
}

@Composable
private fun SidCard(
    row: SubjectInDirectionResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceM),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    row.subjectName ?: "Предмет",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    row.directionName ?: "—",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                val meta = buildList {
                    row.course?.let { add("курс $it") }
                    row.semester?.let { add("сем. $it") }
                    row.finalAssessmentType?.let { add(it) }
                }.joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Изменить") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
