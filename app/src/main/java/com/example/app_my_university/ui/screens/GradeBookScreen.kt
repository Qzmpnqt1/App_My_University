package com.example.app_my_university.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import com.example.app_my_university.data.api.model.StudentPracticeSlotResponse
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.analytics.MuAnalyticsCard
import com.example.app_my_university.ui.components.analytics.MuDonutChart
import com.example.app_my_university.ui.components.analytics.MuVerticalBarChart
import com.example.app_my_university.ui.components.analytics.creditBreakdown
import com.example.app_my_university.ui.components.analytics.creditDonutSegments
import com.example.app_my_university.ui.components.analytics.examAverage
import com.example.app_my_university.ui.components.analytics.examGradeBarEntries
import com.example.app_my_university.ui.components.analytics.hasRecordedFinalResult
import com.example.app_my_university.ui.components.analytics.isDisciplineCreditFinal
import com.example.app_my_university.ui.components.common.MuBadgeTone
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.common.MuStatusBadge
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.GradeBookListFilter
import com.example.app_my_university.ui.viewmodel.GradeBookViewModel
import com.example.app_my_university.ui.viewmodel.PracticeSlotsUiState

private const val SORT_UNKNOWN_COURSE = 999

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: GradeBookViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    val filteredGrades = remember(uiState.grades, uiState.searchQuery, uiState.listFilter) {
        filterGrades(uiState.grades, uiState.searchQuery, uiState.listFilter)
    }

    val grouped = remember(filteredGrades) {
        filteredGrades
            .groupBy { g ->
                val c = g.course ?: SORT_UNKNOWN_COURSE
                val s = g.semester ?: SORT_UNKNOWN_COURSE
                c to s
            }
            .entries
            .toList()
            .sortedWith(compareBy({ it.key.first }, { it.key.second }))
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Зачётная книжка",
                onBackPressed = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.initialLoading,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.initialLoading && uiState.grades.isEmpty() -> {
                MuLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            uiState.screenError != null && uiState.grades.isEmpty() -> {
                MuErrorState(
                    message = uiState.screenError ?: "Ошибка загрузки",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onRetry = { viewModel.refresh() },
                )
            }

            uiState.grades.isEmpty() -> {
                MuEmptyState(
                    title = "Зачётная книжка пуста",
                    subtitle = "Когда преподаватели выставят оценки, они появятся здесь.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    actionLabel = "Обновить",
                    onAction = { viewModel.refresh() },
                )
            }

            else -> {
                val examBars = remember(uiState.grades) { examGradeBarEntries(uiState.grades) }
                val creditMemo = remember(uiState.grades) { creditBreakdown(uiState.grades) }
                val creditRowsForDonut = remember(uiState.grades) {
                    uiState.grades.filter { it.isDisciplineCreditFinal() }
                }
                val creditSummaryPair = remember(creditRowsForDonut) {
                    if (creditRowsForDonut.isEmpty()) null
                    else creditRowsForDonut.count { it.creditStatus == true } to creditRowsForDonut.size
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = Dimens.screenPadding,
                        end = Dimens.screenPadding,
                        bottom = Dimens.spaceXL,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                ) {
                    if (uiState.isRefreshing) {
                        item {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    uiState.screenError?.let { err ->
                        item {
                            ScreenErrorBanner(
                                message = err,
                                onDismiss = { viewModel.dismissScreenError() },
                                onRetry = { viewModel.refresh() },
                            )
                        }
                    }

                    item {
                        FactualSummarySection(
                            grades = uiState.grades,
                            plan = uiState.planSummary,
                        )
                    }

                    uiState.planSummaryError?.let { err ->
                        item {
                            Text(
                                text = "Не удалось загрузить сводку по плану: $err",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    uiState.planSummary?.let { plan ->
                        item {
                            PlanProgressCard(plan = plan)
                        }
                    }

                    if (examBars.any { it.second > 0f }) {
                        item {
                            MuAnalyticsCard(
                                title = "Распределение оценок",
                                subtitle = "Экзамены (оценки 2–5)",
                            ) {
                                MuVerticalBarChart(
                                    entries = examBars,
                                    chartHeight = 140.dp,
                                    maxValueOverride = examBars.maxOf { it.second }.coerceAtLeast(1f),
                                )
                            }
                        }
                    }

                    if (creditMemo.passed + creditMemo.failed + creditMemo.pending > 0) {
                        item {
                            MuAnalyticsCard(
                                title = "Зачёты по дисциплинам",
                                subtitle = creditSummaryPair?.let { (p, t) -> "Зачтено $p из $t" }
                                    ?: "Статус зачётов в зачётной книжке",
                            ) {
                                MuDonutChart(
                                    segments = creditDonutSegments(creditMemo, MaterialTheme.colorScheme),
                                    donutSize = 128.dp,
                                    strokeWidth = 18.dp,
                                    centerValue = creditSummaryPair?.let { (p, _) -> "$p" } ?: "—",
                                    centerTitle = "зачтено",
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Поиск по названию предмета") },
                            singleLine = true,
                            shape = RoundedCornerShape(Dimens.spaceS),
                        )
                    }

                    item {
                        GradeBookFilterChipsRow(
                            selected = uiState.listFilter,
                            onSelect = viewModel::onListFilterChange,
                        )
                    }

                    if (filteredGrades.isEmpty()) {
                        item {
                            Text(
                                text = "Нет дисциплин по выбранным условиям.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = Dimens.spaceL),
                            )
                        }
                    } else {
                        grouped.forEach { entry ->
                            item {
                                SectionHeader(course = entry.key.first, semester = entry.key.second)
                            }
                            items(
                                items = entry.value,
                                key = { it.subjectDirectionId },
                            ) { grade ->
                                GradeSubjectCard(
                                    grade = grade,
                                    expanded = uiState.expandedSubjectIds.contains(grade.subjectDirectionId),
                                    practiceState = uiState.practiceStates[grade.subjectDirectionId],
                                    onToggleExpand = {
                                        viewModel.setSubjectExpanded(
                                            grade.subjectDirectionId,
                                            !uiState.expandedSubjectIds.contains(grade.subjectDirectionId),
                                        )
                                    },
                                    onRetryPractices = { viewModel.loadPracticeSlots(grade.subjectDirectionId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        ),
    ) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = onDismiss) { Text("Скрыть") }
                TextButton(onClick = onRetry) { Text("Повторить") }
            }
        }
    }
}

@Composable
private fun FactualSummarySection(
    grades: List<GradeResponse>,
    plan: StudentPerformanceSummaryResponse?,
) {
    val avg = remember(grades) { examAverage(grades) }
    val withFinal = remember(grades) { grades.count { it.hasRecordedFinalResult() } }
    val totalRows = grades.size
    val creditRows = remember(grades) { grades.filter { it.isDisciplineCreditFinal() } }
    val creditPassed = remember(creditRows) { creditRows.count { it.creditStatus == true } }
    val creditTotal = creditRows.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(Dimens.spaceM),
    ) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Text(
                text = "Фактические результаты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "По записям в вашей зачётной книжке (как выставлено сейчас).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.spaceM))
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                    SummaryStatCell(
                        value = avg?.let { String.format("%.1f", it) } ?: "—",
                        label = "Средний балл",
                        sub = "экзамены 2–5",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryStatCell(
                        value = if (totalRows == 0) "—" else "$withFinal / $totalRows",
                        label = "Итог выставлен",
                        sub = "из записей в книжке",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                    SummaryStatCell(
                        value = if (creditTotal == 0) "—" else "$creditPassed / $creditTotal",
                        label = "Зачёты",
                        sub = "зачтено / всего зачётных",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryStatCell(
                        value = plan?.let { "${it.practicesWithResult} / ${it.totalPractices}" } ?: "—",
                        label = "Практики",
                        sub = "по учебному плану",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCell(
    value: String,
    label: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier, shape = RoundedCornerShape(Dimens.spaceS)) {
        Column(
            Modifier.padding(horizontal = Dimens.spaceM, vertical = Dimens.spaceS),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlanProgressCard(
    plan: StudentPerformanceSummaryResponse,
) {
    MuAnalyticsCard(
        title = "Прогресс по учебному плану",
        subtitle = "Сводка по направлению (все курсы и семестры). Может отличаться от списка записей в зачётке.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceXS)) {
            Text(
                text = buildString {
                    append("Дисциплин в плане: ${plan.plannedSubjects}")
                    append(" · С итогом: ${plan.subjectsWithFinalResult}")
                    append(" · Без итога: ")
                    append((plan.plannedSubjects - plan.subjectsWithFinalResult).coerceAtLeast(0))
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Практики: ${plan.practicesWithResult} из ${plan.totalPractices} с результатом " +
                    "(${String.format("%.0f", plan.practiceCompletionPercent)}%)",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Средний балл по экзаменам (по плану): " +
                    (plan.averageNumericGrade?.let { String.format("%.2f", it) } ?: "—"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GradeBookFilterChipsRow(
    selected: GradeBookListFilter,
    onSelect: (GradeBookListFilter) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val items = listOf(
            GradeBookListFilter.ALL to "Все",
            GradeBookListFilter.EXAMS to "Экзамены",
            GradeBookListFilter.CREDITS to "Зачёты",
            GradeBookListFilter.WITH_FINAL to "С итогом",
            GradeBookListFilter.WITHOUT_FINAL to "Без итога",
            GradeBookListFilter.WITH_PRACTICES to "Есть практики",
        )
        items(items.size) { i ->
            val (f, label) = items[i]
            FilterChip(
                selected = selected == f,
                onClick = { onSelect(f) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun SectionHeader(course: Int, semester: Int) {
    val text = if (course == SORT_UNKNOWN_COURSE || semester == SORT_UNKNOWN_COURSE) {
        "Без указания курса или семестра"
    } else {
        "Курс $course · Семестр $semester"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Dimens.spaceS, bottom = Dimens.spaceXS),
    )
}

@Composable
private fun GradeSubjectCard(
    grade: GradeResponse,
    expanded: Boolean,
    practiceState: PracticeSlotsUiState?,
    onToggleExpand: () -> Unit,
    onRetryPractices: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.spaceM),
    ) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.subjectName ?: "Предмет #${grade.subjectDirectionId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                    )
                    val meta = buildList {
                        if (grade.isDisciplineCreditFinal()) add("Зачёт")
                        else add("Экзамен")
                        grade.course?.let { add("курс $it") }
                        grade.semester?.let { add("сем. $it") }
                        grade.directionName?.let { add(it) }
                    }.joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(Dimens.spaceXS))
                    FinalResultBadgeRow(grade = grade)
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = Dimens.spaceM)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(Dimens.spaceM))
                    Text(
                        text = "Итог по дисциплине",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(Dimens.spaceXS))
                    FinalResultBadgeRow(grade = grade)
                    if ((grade.practiceCount ?: 0) > 0) {
                        Text(
                            text = "Практик в плане: ${grade.practiceCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(Dimens.spaceM))
                    Text(
                        text = "Практические работы",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(Dimens.spaceS))
                    PracticePanel(
                        state = practiceState,
                        onRetry = onRetryPractices,
                    )
                }
            }
        }
    }
}

@Composable
private fun FinalResultBadgeRow(grade: GradeResponse) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
        if (grade.isDisciplineCreditFinal()) {
            grade.creditStatus?.let {
                MuStatusBadge(
                    text = if (it) "Зачтено" else "Незачёт",
                    tone = if (it) MuBadgeTone.Success else MuBadgeTone.Error,
                )
            } ?: MuStatusBadge(text = "Итог не выставлен", tone = MuBadgeTone.Warning)
        } else {
            grade.grade?.let {
                MuStatusBadge(text = "Оценка $it", tone = MuBadgeTone.Primary)
            } ?: MuStatusBadge(text = "Итог не выставлен", tone = MuBadgeTone.Warning)
        }
    }
}

@Composable
private fun PracticePanel(
    state: PracticeSlotsUiState?,
    onRetry: () -> Unit,
) {
    when {
        state == null || (state.isLoading && state.error == null) -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Text("Загрузка практик…", style = MaterialTheme.typography.bodyMedium)
            }
        }

        state.error != null -> {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = onRetry) { Text("Повторить") }
            }
        }

        state.slots.isEmpty() -> {
            Text(
                text = "Практики для этой дисциплины не заведены.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.slots.none { it.hasResult } -> {
            Text(
                text = "Практики есть, но результаты пока не выставлены.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.spaceS),
            )
            state.slots.forEach { slot ->
                PracticeSlotRow(slot = slot)
                Spacer(Modifier.height(Dimens.spaceXS))
            }
        }

        else -> {
            state.slots.forEach { slot ->
                PracticeSlotRow(slot = slot)
                Spacer(Modifier.height(Dimens.spaceXS))
            }
        }
    }
}

@Composable
private fun PracticeSlotRow(slot: StudentPracticeSlotResponse) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.spaceS),
    ) {
        Row(
            Modifier.padding(horizontal = Dimens.spaceM, vertical = Dimens.spaceS),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val num = slot.practiceNumber?.let { "№$it " } ?: ""
                Text(
                    text = "$num${slot.practiceTitle ?: "Практика"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val kind = when (slot.isCredit) {
                    true -> "Зачётная практика"
                    false -> "Оценочная практика"
                    null -> "Практика"
                }
                Text(
                    text = kind,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.CenterEnd) {
                PracticeSlotResult(slot = slot)
            }
        }
    }
}

@Composable
private fun PracticeSlotResult(slot: StudentPracticeSlotResponse) {
    when (slot.isCredit) {
        true -> {
            slot.creditStatus?.let {
                Text(
                    text = if (it) "Зачтено" else "Не зачтено",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            } ?: Text("—", style = MaterialTheme.typography.bodyMedium)
        }
        false -> {
            slot.grade?.let { g ->
                val max = slot.maxGrade?.let { "/$it" } ?: ""
                Text(
                    text = "$g$max",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } ?: Text("—", style = MaterialTheme.typography.bodyMedium)
        }
        null -> {
            when {
                slot.grade != null -> {
                    val max = slot.maxGrade?.let { "/$it" } ?: ""
                    Text(
                        text = "${slot.grade}$max",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                slot.creditStatus != null -> {
                    val it = slot.creditStatus
                    Text(
                        text = if (it == true) "Зачтено" else "Не зачтено",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (it == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    )
                }
                else -> Text("—", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun filterGrades(
    grades: List<GradeResponse>,
    rawQuery: String,
    filter: GradeBookListFilter,
): List<GradeResponse> {
    val q = rawQuery.trim().lowercase()
    var list = grades
    if (q.isNotEmpty()) {
        list = list.filter { g ->
            g.subjectName?.lowercase()?.contains(q) == true
        }
    }
    return when (filter) {
        GradeBookListFilter.ALL -> list
        GradeBookListFilter.EXAMS -> list.filter { !it.isDisciplineCreditFinal() }
        GradeBookListFilter.CREDITS -> list.filter { it.isDisciplineCreditFinal() }
        GradeBookListFilter.WITH_FINAL -> list.filter { it.hasRecordedFinalResult() }
        GradeBookListFilter.WITHOUT_FINAL -> list.filter { !it.hasRecordedFinalResult() }
        GradeBookListFilter.WITH_PRACTICES -> list.filter { (it.practiceCount ?: 0) > 0 }
    }
}
