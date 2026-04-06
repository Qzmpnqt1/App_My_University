package com.example.app_my_university.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.TeacherSubjectResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.profile.teacherWorkplaceSummary
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
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
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminTeacherAssignmentViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
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
            return@RoleShellScaffold
        }
        if (!uiState.isSuperAdmin && uiState.adminUniversityId == null && !uiState.isLoading) {
            MuEmptyState(
                title = "Нет доступа к вузу",
                subtitle = uiState.error ?: "Проверьте профиль администратора.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@RoleShellScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.screenPadding),
        ) {
            if (uiState.isSuperAdmin && uiState.adminUniversityId == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.spaceM),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                ) {
                    Text(
                        "Глобальный режим: список преподавателей со всех вузов. В панели «Ещё» выберите вуз, чтобы работать в рамках одного кампуса.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            Text(
                "Поиск выполняется на сервере",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.spaceS),
            )
            OutlinedTextField(
                value = uiState.teacherSearchQuery,
                onValueChange = viewModel::onTeacherSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Фамилия, имя, e-mail…") },
                label = { Text("Поиск преподавателя") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(Dimens.spaceM))

            if (uiState.selectedTeacher == null) {
                when {
                    uiState.isLoading && uiState.allTeachers.isEmpty() -> {
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.allTeachers.isEmpty() && !uiState.isLoading -> {
                        MuEmptyState(
                            title = if (uiState.teacherSearchQuery.isBlank()) "Нет преподавателей" else "Никого не найдено",
                            subtitle = if (uiState.teacherSearchQuery.isBlank()) {
                                "В выбранной области нет учётных записей преподавателей."
                            } else {
                                "Попробуйте другой запрос."
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceS),
                            contentPadding = PaddingValues(bottom = Dimens.screenPadding),
                        ) {
                            items(uiState.allTeachers, key = { it.id }) { t ->
                                TeacherPickCard(
                                    teacher = t,
                                    selected = false,
                                    onClick = { viewModel.selectTeacher(t) },
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { viewModel.selectTeacher(null) }) {
                        Text("Сменить преподавателя")
                    }
                }
                SelectedTeacherSummaryCard(teacher = uiState.selectedTeacher!!)
                Spacer(Modifier.height(Dimens.spaceM))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spaceM),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                    ) {
                        Text(
                            "Текущие назначения",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Добавьте или измените дисциплины по институтам и направлениям.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = { viewModel.openAssignmentSheet() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.selectedTeacher?.teacherProfile?.teacherProfileId != null &&
                                !uiState.catalogsLoading,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, Modifier.size(20.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Добавить / изменить назначения", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (uiState.catalogsLoading) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.spaceS),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Загрузка каталога…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (uiState.assignmentsLoading) {
                    Column(Modifier.padding(top = Dimens.spaceS)) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text(
                            "Обновляем список назначений…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                if (!uiState.assignmentsLoading && assignmentSections.isEmpty()) {
                    MuEmptyState(
                        title = "Пока нет назначенных дисциплин",
                        subtitle = "Нажмите кнопку выше, чтобы собрать список по институтам и направлениям.",
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = Dimens.screenPadding),
                ) {
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
                teacher.teacherWorkplaceSummary()?.let { workplace ->
                    Text(
                        workplace,
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
                teacher.teacherWorkplaceSummary()?.let {
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
