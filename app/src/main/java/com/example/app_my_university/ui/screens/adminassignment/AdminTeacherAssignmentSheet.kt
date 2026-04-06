package com.example.app_my_university.ui.screens.adminassignment

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.designsystem.AppLayout
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.viewmodel.AdminTeacherAssignmentUiState
import com.example.app_my_university.ui.viewmodel.AdminTeacherAssignmentViewModel
import com.example.app_my_university.ui.viewmodel.TeacherAssignmentSheetStep

@Composable
fun AdminTeacherAssignmentSheet(
    uiState: AdminTeacherAssignmentUiState,
    viewModel: AdminTeacherAssignmentViewModel,
    onDismiss: () -> Unit,
) {
    val draftIds = remember(uiState.draftAssignments) {
        uiState.draftAssignments.map { it.subjectDirectionId }.toSet()
    }
    val filteredSubjects = remember(uiState.subjectsForSheet, uiState.subjectLineSearch) {
        viewModel.filteredSubjectsForPicker(uiState)
    }
    val teacher = uiState.selectedTeacher

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = AppLayout.bottomActionBarDividerElevation, shadowElevation = 6.dp) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(
                            horizontal = AppLayout.bottomActionBarHorizontalPadding,
                            vertical = AppLayout.bottomActionBarVerticalPadding,
                        ),
                ) {
                    HorizontalDivider()
                    Spacer(Modifier.height(AppSpacing.s))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (uiState.sheetStep != TeacherAssignmentSheetStep.PICK_INSTITUTE) {
                            IconButton(
                                onClick = { viewModel.goBackSheetStep() },
                                enabled = !uiState.sheetSaving,
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    modifier = Modifier.size(AppLayout.barIconSize),
                                )
                            }
                        } else {
                            Spacer(Modifier.size(48.dp))
                        }
                        TextButton(
                            onClick = onDismiss,
                            enabled = !uiState.sheetSaving,
                            contentPadding = PaddingValues(horizontal = AppSpacing.s, vertical = AppSpacing.xs),
                        ) {
                            Text(
                                "Отмена",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.s))
                    Button(
                        onClick = { viewModel.saveAllDraftAssignments() },
                        enabled = !uiState.sheetSaving,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = AppSpacing.m, vertical = AppSpacing.s),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (uiState.sheetSaving) {
                            CircularProgressIndicator(
                                Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                "Сохранить всё",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.m)
                .padding(bottom = AppSpacing.m),
        ) {
            SheetTeacherHeader(teacher)
            Spacer(Modifier.height(AppSpacing.m))
            Text(
                "Назначение дисциплин",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Добавляйте позиции из разных институтов и направлений, затем сохраните список одной кнопкой.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = AppSpacing.m),
            )

            AssignmentStepIndicator(current = uiState.sheetStep)

            if (uiState.catalogsLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(AppSpacing.m))
            }

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                    ),
                    modifier = Modifier.padding(bottom = AppSpacing.m),
                ) {
                    Column(Modifier.padding(AppSpacing.m)) {
                        Text(
                            friendlyAssignmentError(uiState.error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        TextButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.align(Alignment.End),
                        ) { Text("Скрыть") }
                    }
                }
            }

            StepContent(
                uiState = uiState,
                viewModel = viewModel,
                draftIds = draftIds,
                filteredSubjects = filteredSubjects,
            )
            Spacer(Modifier.height(AppSpacing.l))
            DraftAssignmentsBlock(
                uiState = uiState,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun SheetTeacherHeader(teacher: UserProfileResponse?) {
    if (teacher == null) return
    val name = teacherDisplayName(teacher)
    val initials = remember(name) {
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "?" }
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        initials,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Преподаватель", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                teacher.email?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun teacherDisplayName(t: UserProfileResponse): String {
    val n = "${t.lastName} ${t.firstName}".trim() + (t.middleName?.let { " $it" } ?: "")
    return n.ifBlank { t.email ?: "—" }
}

@Composable
private fun AssignmentStepIndicator(current: TeacherAssignmentSheetStep) {
    val steps = listOf(
        TeacherAssignmentSheetStep.PICK_INSTITUTE to "Институт",
        TeacherAssignmentSheetStep.PICK_DIRECTION to "Направление",
        TeacherAssignmentSheetStep.PICK_SUBJECTS to "Дисциплины",
    )
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = AppSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, (step, label) ->
            val active = step == current
            val past = steps.indexOfFirst { it.first == current } > index
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when {
                    active -> MaterialTheme.colorScheme.primary
                    past -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = if (active) BorderStroke(0.dp, MaterialTheme.colorScheme.primary) else null,
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            active -> MaterialTheme.colorScheme.onPrimary
                            past -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            active -> MaterialTheme.colorScheme.onPrimary
                            past -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (past) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepContent(
    uiState: AdminTeacherAssignmentUiState,
    viewModel: AdminTeacherAssignmentViewModel,
    draftIds: Set<Long>,
    filteredSubjects: List<SubjectInDirectionResponse>,
) {
    when (uiState.sheetStep) {
        TeacherAssignmentSheetStep.PICK_INSTITUTE -> {
            Text(
                "Шаг 1 — выберите институт",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = AppSpacing.s),
            )
            if (uiState.institutes.isEmpty()) {
                if (uiState.catalogsLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    MuEmptyState(
                        title = "Нет институтов",
                        subtitle = "В каталоге вуза не найдено институтов.",
                    )
                }
            } else {
                uiState.institutes.forEach { inst ->
                    InstituteSheetCard(
                        title = inst.name,
                        subtitle = inst.shortName?.takeIf { it.isNotBlank() },
                        onClick = {
                            if (!uiState.sheetSaving) viewModel.selectInstituteForSheet(inst.id)
                        },
                    )
                }
            }
        }
        TeacherAssignmentSheetStep.PICK_DIRECTION -> {
            Text(
                "Шаг 2 — направление подготовки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = AppSpacing.s),
            )
            if (uiState.directionsForSheet.isEmpty()) {
                MuEmptyState(
                    title = "Нет направлений",
                    subtitle = "У выбранного института нет направлений или они ещё загружаются.",
                )
            } else {
                uiState.directionsForSheet.forEach { dir ->
                    InstituteSheetCard(
                        title = dir.name,
                        subtitle = dir.code,
                        onClick = {
                            if (!uiState.sheetSaving) viewModel.selectDirectionForSheet(dir.id)
                        },
                    )
                }
            }
        }
        TeacherAssignmentSheetStep.PICK_SUBJECTS -> {
            Text(
                "Шаг 3 — дисциплины в плане",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = AppSpacing.s),
            )
            OutlinedTextField(
                value = uiState.subjectLineSearch,
                onValueChange = viewModel::onSubjectLineSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по названию") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(AppSpacing.s))
            if (filteredSubjects.isEmpty()) {
                MuEmptyState(title = "Список пуст", subtitle = "Нет дисциплин или ничего не найдено по запросу.")
            } else {
                filteredSubjects.forEach { sid ->
                    val inDraft = sid.id in draftIds
                    val checked = inDraft || sid.id in uiState.pickedSubjectDirectionIds
                    SubjectPickRow(
                        sid = sid,
                        checked = checked,
                        inDraft = inDraft,
                        enabled = !uiState.sheetSaving,
                        onToggle = { viewModel.togglePickedSubjectDirection(sid.id) },
                    )
                }
            }
            Button(
                onClick = { viewModel.addPickedSubjectsToDraft() },
                enabled = !uiState.sheetSaving && uiState.pickedSubjectDirectionIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.m),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Добавить выбранные в черновик", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InstituteSheetCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(AppSpacing.m)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SubjectPickRow(
    sid: SubjectInDirectionResponse,
    checked: Boolean,
    inDraft: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val assessment = when (sid.finalAssessmentType?.uppercase()) {
        "CREDIT" -> "Зачёт"
        "EXAM" -> "Экзамен"
        else -> null
    }
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled && !inDraft) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = if (checked) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = if (inDraft || !enabled) null else { { onToggle() } },
            )
            Column(Modifier.weight(1f)) {
                Text(
                    sid.subjectName ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sid.course?.let {
                        Text("Курс $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    sid.semester?.let {
                        Text("Сем. $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    assessment?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (inDraft) {
                    Text(
                        "Уже в черновике назначений",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftAssignmentsBlock(
    uiState: AdminTeacherAssignmentUiState,
    viewModel: AdminTeacherAssignmentViewModel,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(Modifier.padding(AppSpacing.m)) {
            Text(
                "Черновик назначений (не сохранено на сервере)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                "${uiState.draftAssignments.size} поз.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(AppSpacing.s))
            if (uiState.draftAssignments.isEmpty()) {
                Text(
                    "Добавьте дисциплины с шага 3 — список можно собрать из разных направлений за одну сессию.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                uiState.draftAssignments.forEach { d ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(d.subjectName, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${d.instituteName} · ${d.directionName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val m = listOfNotNull(d.course?.let { "курс $it" }, d.semester?.let { "сем. $it" }).joinToString(" · ")
                                if (m.isNotEmpty()) {
                                    Text(m, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.removeDraftItem(d.subjectDirectionId) },
                                enabled = !uiState.sheetSaving,
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Убрать из черновика")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun friendlyAssignmentError(raw: String?): String {
    val m = raw ?: return "Операция не выполнена"
    return when {
        m.contains("409", ignoreCase = true) ||
            m.contains("Conflict", ignoreCase = true) ||
            m.lowercase().contains("optimistic") ->
            "Список назначений изменился (конфликт версии). Закройте окно, откройте «Изменить назначения» снова и повторите сохранение."
        else -> m
    }
}
