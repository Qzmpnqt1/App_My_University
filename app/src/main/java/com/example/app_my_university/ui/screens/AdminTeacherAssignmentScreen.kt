package com.example.app_my_university.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.data.api.model.TeacherSubjectResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.screens.adminassignment.AdminTeacherAssignmentSheet
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminTeacherAssignmentViewModel

private data class AssignmentSection(
    val instituteName: String,
    val directionName: String,
    val rows: List<TeacherSubjectResponse>,
)

private fun groupedAssignments(list: List<TeacherSubjectResponse>): List<AssignmentSection> {
    return list
        .groupBy { (it.instituteName ?: "—") to (it.directionName ?: "—") }
        .entries
        .sortedWith(compareBy({ it.key.first }, { it.key.second }))
        .map { (k, v) ->
            AssignmentSection(
                instituteName = k.first,
                directionName = k.second,
                rows = v.sortedBy { it.subjectName ?: "" },
            )
        }
}

private fun teacherDisplayName(t: UserProfileResponse): String {
    val n = "${t.lastName} ${t.firstName}".trim() + (t.middleName?.let { " $it" } ?: "")
    return n.ifBlank { t.email ?: "—" }
}

private fun teacherInitials(name: String): String {
    return name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeacherAssignmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminTeacherAssignmentViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredTeachers = remember(uiState.allTeachers, uiState.teacherSearchQuery) {
        viewModel.filteredTeachers(uiState)
    }

    val assignmentSections = remember(uiState.assignments) {
        groupedAssignments(uiState.assignments)
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContextAndTeachers()
    }
    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            uiState.actionMessage?.let { snackbar.showSnackbar(it) }
            viewModel.clearActionSuccess()
        }
    }
    LaunchedEffect(uiState.error, uiState.assignmentSheetOpen) {
        uiState.error?.let { err ->
            if (!uiState.assignmentSheetOpen) {
                snackbar.showSnackbar(err)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Назначение дисциплин",
                onBackPressed = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (uiState.isLoading && uiState.allTeachers.isEmpty()) {
            MuLoadingState(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        if (uiState.adminUniversityId == null && !uiState.isLoading) {
            MuEmptyState(
                title = "Нет доступа к вузу",
                subtitle = uiState.error ?: "Проверьте профиль администратора.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
        ) {
            item {
                Text(
                    "Зона 1 — выбор преподавателя. Зона 2 — текущие назначения и изменение списка дисциплин.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        "1. Выбор преподавателя",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = uiState.teacherSearchQuery,
                    onValueChange = viewModel::onTeacherSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Фамилия, имя, e-mail или институт") },
                    label = { Text("Поиск преподавателя") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
            if (filteredTeachers.isEmpty() && uiState.allTeachers.isNotEmpty()) {
                item {
                    MuEmptyState(
                        title = "Никого не найдено",
                        subtitle = "Измените запрос поиска.",
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
            if (uiState.allTeachers.isEmpty() && !uiState.isLoading) {
                item {
                    MuEmptyState(
                        title = "Нет преподавателей",
                        subtitle = "В вашем вузе пока нет учётных записей преподавателей.",
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
            items(filteredTeachers, key = { it.id }) { t ->
                TeacherPickCard(
                    teacher = t,
                    selected = uiState.selectedTeacher?.id == t.id,
                    onClick = { viewModel.selectTeacher(t) },
                )
            }

            if (uiState.selectedTeacher != null) {
                item {
                    Spacer(Modifier.padding(top = Dimens.spaceS))
                    HorizontalDivider()
                    Spacer(Modifier.padding(top = Dimens.spaceM))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            "2. Назначения выбранного преподавателя",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                item {
                    SelectedTeacherSummaryCard(teacher = uiState.selectedTeacher!!)
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Текущие назначения",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Button(
                            onClick = { viewModel.openAssignmentSheet() },
                            enabled = uiState.selectedTeacher?.teacherProfile?.teacherProfileId != null &&
                                !uiState.catalogsLoading,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, Modifier.size(20.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Добавить / изменить", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (uiState.catalogsLoading && uiState.selectedTeacher != null) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
                        ) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("Загрузка каталога…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (uiState.assignmentsLoading) {
                    item {
                        Column {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text(
                                "Обновляем список назначений…",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                if (!uiState.assignmentsLoading && assignmentSections.isEmpty()) {
                    item {
                        MuEmptyState(
                            title = "Пока нет назначенных дисциплин",
                            subtitle = "Нажмите «Добавить / изменить», чтобы собрать список по институтам и направлениям.",
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                }
                assignmentSections.forEach { section ->
                    item {
                        AssignmentSectionHeader(
                            instituteName = section.instituteName,
                            directionName = section.directionName,
                        )
                    }
                    items(section.rows, key = { it.id }) { row ->
                        AssignmentRowCard(row = row, onDelete = { viewModel.deleteAssignment(row.id) })
                    }
                }
            } else if (uiState.allTeachers.isNotEmpty()) {
                item {
                    MuEmptyState(
                        title = "Преподаватель не выбран",
                        subtitle = "Выберите строку выше, чтобы увидеть и редактировать назначения.",
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
        }
    }

    if (uiState.assignmentSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { if (!uiState.sheetSaving) viewModel.dismissAssignmentSheet() },
            sheetState = sheetState,
        ) {
            AdminTeacherAssignmentSheet(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissAssignmentSheet() },
            )
        }
    }
}

@Composable
private fun TeacherPickCard(
    teacher: UserProfileResponse,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val name = teacherDisplayName(teacher)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            },
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM),
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        teacherInitials(name),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                teacher.email?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                teacher.teacherProfile?.instituteName?.let { inst ->
                    Text(
                        inst,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                teacher.teacherProfile?.position?.let { pos ->
                    Text(
                        pos,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedTeacherSummaryCard(teacher: UserProfileResponse) {
    val name = teacherDisplayName(teacher)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            Modifier.padding(Dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        teacherInitials(name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Выбран преподаватель",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                teacher.email?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                teacher.teacherProfile?.instituteName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                teacher.teacherProfile?.position?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentSectionHeader(
    instituteName: String,
    directionName: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(horizontal = Dimens.spaceM, vertical = Dimens.spaceS)) {
            Text(
                instituteName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                directionName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AssignmentRowCard(
    row: TeacherSubjectResponse,
    onDelete: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(14.dp),
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
                    row.subjectName ?: "Предмет",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                row.directionName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                row.instituteName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val meta = buildList {
                    row.course?.let { add("курс $it") }
                    row.semester?.let { add("сем. $it") }
                }.joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            FilledTonalButton(
                onClick = onDelete,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Снять назначение", Modifier.size(20.dp))
            }
        }
    }
}
