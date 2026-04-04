package com.example.app_my_university.ui.screens.teachergrading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.TeacherGradingPickResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.TeacherGradingUiState

private const val INSTITUTE_SEARCH_THRESHOLD = 5
private const val SUBJECT_SEARCH_THRESHOLD = 6

@Composable
fun TeacherGradingPathSummary(
    ui: TeacherGradingUiState,
    modifier: Modifier = Modifier,
) {
    val instituteLabel = ui.institutes.find { it.id == ui.selectedInstituteId }?.name
        ?: ui.institutes.find { it.id == ui.selectedInstituteId }?.subtitle
    val directionLabel = ui.directions.find { it.id == ui.selectedDirectionId }?.name
    val subjectLabel = ui.subjectDirections.find { it.id == ui.selectedSubjectDirectionId }?.subjectName
    val groupLabel = ui.groups.find { it.id == ui.selectedGroupId }?.name
    val studentLabel = ui.students.find { it.id == ui.selectedStudentUserId }?.name

    val focusStep = when {
        ui.selectedInstituteId == null -> 0
        ui.selectedDirectionId == null -> 1
        ui.selectedSubjectDirectionId == null -> 2
        ui.selectedGroupId == null -> 3
        ui.selectedStudentUserId == null -> 4
        else -> 5
    }

    val steps = listOf(
        "Институт" to instituteLabel,
        "Направление" to directionLabel,
        "Дисциплина" to subjectLabel,
        "Группа" to groupLabel,
        "Студент" to studentLabel,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(Dimens.spaceM)) {
            Text(
                text = "Контекст оценивания",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(Dimens.spaceS))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { index, (title, value) ->
                    PathStepChip(
                        stepTitle = title,
                        value = value,
                        state = when {
                            value != null -> PathStepVisualState.Filled
                            index == focusStep -> PathStepVisualState.Active
                            index < focusStep -> PathStepVisualState.Pending
                            else -> PathStepVisualState.Waiting
                        },
                    )
                    if (index < steps.lastIndex) {
                        Text(
                            "→",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

private enum class PathStepVisualState { Filled, Active, Pending, Waiting }

@Composable
private fun PathStepChip(
    stepTitle: String,
    value: String?,
    state: PathStepVisualState,
) {
    val borderColor = when (state) {
        PathStepVisualState.Filled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        PathStepVisualState.Active -> MaterialTheme.colorScheme.primary
        PathStepVisualState.Pending -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        PathStepVisualState.Waiting -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    val bg = when (state) {
        PathStepVisualState.Filled -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        PathStepVisualState.Active -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        PathStepVisualState.Pending -> MaterialTheme.colorScheme.surface
        PathStepVisualState.Waiting -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            Modifier
                .width(120.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                stepTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value ?: if (state == PathStepVisualState.Active) "Выберите…" else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (value != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (value != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun WizardSectionCard(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(Dimens.spaceM)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(Dimens.spaceM))
            content()
        }
    }
}

@Composable
fun SectionLoadingSkeleton(lines: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
        repeat(lines) { i ->
            Box(
                Modifier
                    .fillMaxWidth(if (i == lines - 1) 0.65f else 1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            )
        }
    }
}

@Composable
fun InstitutePickerBlock(
    ui: TeacherGradingUiState,
    onSelect: (Long) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val showSearch = ui.institutes.size >= INSTITUTE_SEARCH_THRESHOLD
    val filtered = remember(ui.institutes, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) ui.institutes
        else ui.institutes.filter {
            (it.name?.lowercase()?.contains(q) == true) ||
                (it.subtitle?.lowercase()?.contains(q) == true)
        }
    }
    if (showSearch) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Поиск института") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(Modifier.height(Dimens.spaceM))
    }
    if (filtered.isEmpty()) {
        MuEmptyState(
            title = "Ничего не найдено",
            subtitle = "Измените запрос или сбросьте поиск.",
            modifier = Modifier.padding(vertical = Dimens.spaceS),
        )
        return
    }
    val useLargeCards = ui.institutes.size <= 4
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
        filtered.forEach { row ->
            InstituteOrPickCard(
                title = row.name ?: "—",
                subtitle = row.subtitle,
                selected = ui.selectedInstituteId == row.id,
                emphasized = useLargeCards,
                onClick = { onSelect(row.id) },
            )
        }
    }
}

@Composable
fun DirectionPickerBlock(
    ui: TeacherGradingUiState,
    onSelect: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    when {
        ui.loading && ui.directions.isEmpty() -> SectionLoadingSkeleton(4)
        ui.error != null && ui.directions.isEmpty() && !ui.loading -> {
            MuErrorState(message = ui.error ?: "Ошибка загрузки", onRetry = onRetry)
        }
        ui.directions.isEmpty() -> {
            MuEmptyState(
                title = "Нет направлений",
                subtitle = "Для выбранного института направления не найдены или не назначены дисциплины.",
                modifier = Modifier.padding(vertical = Dimens.spaceS),
            )
        }
        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                ui.directions.forEach { row ->
                    InstituteOrPickCard(
                        title = row.name ?: "—",
                        subtitle = row.subtitle,
                        selected = ui.selectedDirectionId == row.id,
                        emphasized = false,
                        onClick = { onSelect(row.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectDirectionPickerBlock(
    ui: TeacherGradingUiState,
    onSelect: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(ui.subjectDirections, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) ui.subjectDirections
        else ui.subjectDirections.filter {
            (it.subjectName?.lowercase()?.contains(q) == true) ||
                (it.directionName?.lowercase()?.contains(q) == true)
        }
    }
    when {
        ui.loading && ui.subjectDirections.isEmpty() -> SectionLoadingSkeleton(5)
        ui.error != null && ui.subjectDirections.isEmpty() && !ui.loading -> {
            MuErrorState(message = ui.error ?: "Ошибка загрузки", onRetry = onRetry)
        }
        ui.subjectDirections.isEmpty() -> {
            MuEmptyState(
                title = "Нет дисциплин в плане",
                subtitle = "По выбранному направлению нет привязанных дисциплин.",
                modifier = Modifier.padding(vertical = Dimens.spaceS),
            )
        }
        else -> {
            if (ui.subjectDirections.size >= SUBJECT_SEARCH_THRESHOLD) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поиск дисциплины") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(Dimens.spaceM))
            }
            if (filtered.isEmpty()) {
                MuEmptyState(
                    title = "Нет совпадений",
                    subtitle = "Попробуйте другой запрос.",
                    modifier = Modifier.padding(vertical = Dimens.spaceS),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                    filtered.forEach { sd ->
                        SubjectDirectionCard(
                            sd = sd,
                            selected = ui.selectedSubjectDirectionId == sd.id,
                            onClick = { onSelect(sd.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupPickerBlock(
    ui: TeacherGradingUiState,
    onSelect: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Text(
        "Только группы выбранного направления подготовки.",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = Dimens.spaceS),
    )
    when {
        ui.loading && ui.groups.isEmpty() -> SectionLoadingSkeleton(3)
        ui.error != null && ui.groups.isEmpty() && !ui.loading -> {
            MuErrorState(message = ui.error ?: "Ошибка загрузки", onRetry = onRetry)
        }
        ui.groups.isEmpty() -> {
            MuEmptyState(
                title = "Нет групп",
                subtitle = "Для этой дисциплины в направлении не найдены учебные группы.",
                modifier = Modifier.padding(vertical = Dimens.spaceS),
            )
        }
        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
            ) {
                ui.groups.forEach { g ->
                    val selected = ui.selectedGroupId == g.id
                    FilterChip(
                        selected = selected,
                        onClick = { onSelect(g.id) },
                        label = {
                            Text(
                                g.name ?: "Группа",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                    )
                }
            }
        }
    }
}

@Composable
fun StudentPickerBlock(
    ui: TeacherGradingUiState,
    filteredStudents: List<TeacherGradingPickResponse>,
    onSearchChange: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    OutlinedTextField(
        value = ui.studentSearchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Начните вводить фамилию или имя") },
        label = { Text("Поиск по ФИО") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
    Spacer(Modifier.height(Dimens.spaceM))
    when {
        ui.loading && ui.students.isEmpty() -> SectionLoadingSkeleton(6)
        ui.error != null && ui.students.isEmpty() && !ui.loading -> {
            MuErrorState(message = ui.error ?: "Ошибка загрузки", onRetry = onRetry)
        }
        ui.students.isEmpty() -> {
            MuEmptyState(
                title = "Нет студентов в группе",
                subtitle = "В выбранной группе нет записанных студентов.",
                modifier = Modifier.padding(vertical = Dimens.spaceS),
            )
        }
        filteredStudents.isEmpty() -> {
            MuEmptyState(
                title = "Никого не найдено",
                subtitle = "Измените строку поиска.",
                modifier = Modifier.padding(vertical = Dimens.spaceS),
            )
        }
        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                filteredStudents.forEach { st ->
                    StudentCard(
                        name = st.name ?: "Студент",
                        subtitle = st.subtitle,
                        selected = ui.selectedStudentUserId == st.id,
                        onClick = { onSelect(st.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InstituteOrPickCard(
    title: String,
    subtitle: String?,
    selected: Boolean,
    emphasized: Boolean,
    onClick: () -> Unit,
) {
    val paddingV = if (emphasized) Dimens.spaceM else Dimens.spaceS
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            },
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasized) 2.dp else 0.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spaceM, vertical = paddingV),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = Dimens.spaceS),
                )
            }
        }
    }
}

@Composable
private fun SubjectDirectionCard(
    sd: SubjectInDirectionResponse,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val control = when (sd.finalAssessmentType?.uppercase()) {
        "CREDIT" -> "Зачёт с оценкой зачёт/незачёт"
        "EXAM" -> "Экзамен (2–5)"
        else -> null
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            },
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(Dimens.spaceM)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    sd.subjectName ?: "Дисциплина",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                if (selected) {
                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                ) {
                    Text(
                        "Курс ${sd.course ?: "—"}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                ) {
                    Text(
                        "Сем. ${sd.semester ?: "—"}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            sd.directionName?.takeIf { it.isNotBlank() }?.let { dir ->
                Spacer(Modifier.height(6.dp))
                Text(
                    dir,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            control?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StudentCard(
    name: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            },
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
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
                    name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (selected) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
