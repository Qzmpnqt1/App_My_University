package com.example.app_my_university.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.app_my_university.data.api.model.PracticeStatisticsDetail
import com.example.app_my_university.data.api.model.SubjectInDirectionResponse
import com.example.app_my_university.data.api.model.TeacherGradingPickResponse
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.analytics.MuAnalyticsCard
import com.example.app_my_university.ui.components.analytics.MuAnalyticsEmptyState
import com.example.app_my_university.ui.components.analytics.MuHorizontalBarChart
import com.example.app_my_university.ui.components.analytics.MuLabeledProgressMetric
import com.example.app_my_university.ui.components.analytics.MuLineChart
import com.example.app_my_university.ui.components.analytics.MuVerticalBarChart
import com.example.app_my_university.ui.components.analytics.scheduleDaySeriesForChart
import com.example.app_my_university.ui.components.analytics.scheduleWeekSeriesForChart
import com.example.app_my_university.ui.components.picker.MuPickerField
import com.example.app_my_university.ui.components.picker.MuSearchablePickerSheet
import com.example.app_my_university.ui.components.picker.PickerListItem
import com.example.app_my_university.ui.viewmodel.TeacherStatisticsUiState
import com.example.app_my_university.ui.viewmodel.TeacherStatisticsViewModel
import com.example.app_my_university.ui.viewmodel.TeacherStatsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TeacherStatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var institutePicker by remember { mutableStateOf(false) }
    var directionPicker by remember { mutableStateOf(false) }
    var subjectPicker by remember { mutableStateOf(false) }
    var groupPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.example.app_my_university.core.logging.AppLogger.screen("TeacherStatistics")
        viewModel.ensureCatalogFresh()
    }

    LaunchedEffect(state.section) {
        when (state.section) {
            TeacherStatsSection.SUBJECT,
            TeacherStatsSection.GROUP,
            TeacherStatsSection.DIRECTION -> viewModel.ensureCatalogFresh()
            else -> Unit
        }
    }

    MuSearchablePickerSheet(
        visible = institutePicker,
        onDismiss = { institutePicker = false },
        title = "Институт",
        items = state.institutes.map { i ->
            PickerListItem(
                id = i.id,
                primary = teacherCatalogPrimary(i.name, "Институт", i.id),
                secondary = i.subtitle,
            )
        },
        listLoading = state.catalogLoading,
        onSelect = {
            viewModel.selectInstitute(
                TeacherGradingPickResponse(id = it.id, name = it.primary, subtitle = it.secondary)
            )
            institutePicker = false
        },
    )
    MuSearchablePickerSheet(
        visible = directionPicker,
        onDismiss = { directionPicker = false },
        title = "Направление",
        items = state.directions.map { d ->
            PickerListItem(
                id = d.id,
                primary = teacherCatalogPrimary(d.name, "Направление", d.id),
                secondary = d.subtitle,
            )
        },
        listLoading = state.directionsLoading,
        onSelect = {
            viewModel.selectDirection(
                TeacherGradingPickResponse(id = it.id, name = it.primary, subtitle = it.secondary)
            )
            directionPicker = false
        },
    )
    MuSearchablePickerSheet(
        visible = subjectPicker,
        onDismiss = { subjectPicker = false },
        title = "Дисциплина в направлении",
        items = state.subjectDirections.map { sd ->
            PickerListItem(
                id = sd.id,
                primary = sd.subjectName ?: "Предмет",
                secondary = listOfNotNull(
                    sd.directionName,
                    sd.course?.let { "курс $it" },
                    sd.semester?.let { "сем $it" },
                ).joinToString(" · "),
            )
        },
        listLoading = state.subjectDirectionsLoading,
        onSelect = { row ->
            state.subjectDirections.find { it.id == row.id }?.let { viewModel.selectSubject(it) }
            subjectPicker = false
        },
    )
    MuSearchablePickerSheet(
        visible = groupPicker,
        onDismiss = { groupPicker = false },
        title = "Группа",
        items = state.groups.map { g ->
            PickerListItem(
                id = g.id,
                primary = teacherCatalogPrimary(g.name, "Группа", g.id),
                secondary = g.subtitle,
            )
        },
        listLoading = state.groupsLoading,
        onSelect = {
            viewModel.selectGroup(
                TeacherGradingPickResponse(id = it.id, name = it.primary, subtitle = it.secondary)
            )
            groupPicker = false
        },
    )

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Аналитика",
                onBackPressed = onNavigateBack,
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Нагрузка по расписанию, успеваемость по дисциплинам, группам и направлениям, к которым у вас есть доступ.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.profileError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (state.catalogLoading) {
                Text("Загрузка справочников…", style = MaterialTheme.typography.bodySmall)
            }
            state.catalogError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { viewModel.refreshCatalog() }) { Text("Повторить") }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeacherStatsSection.entries.forEach { sec ->
                    FilterChip(
                        selected = state.section == sec,
                        onClick = { viewModel.setSection(sec) },
                        label = { Text(sec.label) }
                    )
                }
            }

            when (state.section) {
                TeacherStatsSection.OVERVIEW -> TeacherOverviewSection(state, viewModel)
                TeacherStatsSection.SUBJECT -> TeacherSubjectSection(
                    state = state,
                    onOpenInstitute = { institutePicker = true },
                    onOpenDirection = { directionPicker = true },
                    onOpenSubject = { subjectPicker = true },
                    onLoad = { viewModel.loadSubjectAnalytics() },
                )
                TeacherStatsSection.GROUP -> TeacherGroupSection(
                    state = state,
                    onOpenInstitute = { institutePicker = true },
                    onOpenDirection = { directionPicker = true },
                    onOpenSubject = { subjectPicker = true },
                    onOpenGroup = { groupPicker = true },
                    onLoad = { viewModel.loadGroupAnalytics() },
                )
                TeacherStatsSection.DIRECTION -> TeacherDirectionSection(
                    state = state,
                    onOpenInstitute = { institutePicker = true },
                    onOpenDirection = { directionPicker = true },
                    onLoad = { viewModel.loadDirectionAnalytics() },
                )
            }
        }
    }
}

@Composable
private fun TeacherOverviewSection(
    state: TeacherStatisticsUiState,
    viewModel: TeacherStatisticsViewModel,
) {
    Text(
        text = "Вся нагрузка по вашему расписанию (все недели в базе).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Button(
        onClick = { viewModel.loadScheduleStatistics() },
        enabled = !state.isLoading && state.myUserId != null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(4.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text("Обновить")
        }
    }
    state.error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    state.scheduleStats?.let { s ->
        MuAnalyticsCard(
            title = "Расписание",
            subtitle = "Занятий: ${s.totalLessons}, часов: ${String.format("%.1f", s.totalHours)}"
        ) {
            val byDay = scheduleDaySeriesForChart(s.byDayOfWeek)
            if (byDay.isNotEmpty()) {
                Text("По дням недели", style = MaterialTheme.typography.labelLarge)
                MuVerticalBarChart(
                    entries = byDay,
                    chartHeight = 120.dp,
                    valueFormatter = { it.toInt().toString() },
                )
            }
            val byWeek = scheduleWeekSeriesForChart(s.byWeekNumber)
            if (byWeek.size >= 2) {
                Text("По номерам недель", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
                MuLineChart(points = byWeek, chartHeight = 120.dp)
            } else if (byWeek.isNotEmpty()) {
                MuVerticalBarChart(entries = byWeek, chartHeight = 100.dp)
            }
            if (byDay.isEmpty() && byWeek.isEmpty()) {
                MuAnalyticsEmptyState("Нет разбивки по дням или неделям")
            }
        }
    } ?: run {
        if (!state.isLoading && state.myUserId != null && state.error == null) {
            MuAnalyticsEmptyState("Нажмите «Обновить», чтобы загрузить статистику расписания.")
        }
    }
}

@Composable
private fun TeacherSubjectSection(
    state: TeacherStatisticsUiState,
    onOpenInstitute: () -> Unit,
    onOpenDirection: () -> Unit,
    onOpenSubject: () -> Unit,
    onLoad: () -> Unit,
) {
    MuPickerField(
        label = "Институт",
        valueText = state.instituteLabel,
        placeholder = "Выберите институт",
        enabled = !state.catalogLoading,
        onClick = onOpenInstitute,
        modifier = Modifier.fillMaxWidth(),
        supportingText = when {
            state.catalogLoading -> {
                { Text("Загрузка списка…", style = MaterialTheme.typography.bodySmall) }
            }
            state.institutes.isEmpty() -> {
                {
                    Text(
                        "Нет институтов. Нужны назначения на дисциплины (как в разделе «Оценки»).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> null
        },
    )
    MuPickerField(
        label = "Направление",
        valueText = state.directionLabel,
        placeholder = "Сначала институт",
        enabled = state.canPickDirection && !state.directionsLoading,
        onClick = onOpenDirection,
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (state.directionsLoading) {
            { Text("Загрузка…", style = MaterialTheme.typography.bodySmall) }
        } else null,
    )
    MuPickerField(
        label = "Дисциплина",
        valueText = state.subjectLabel,
        placeholder = "Сначала направление",
        enabled = state.canPickSubject && !state.subjectDirectionsLoading,
        onClick = onOpenSubject,
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (state.subjectDirectionsLoading) {
            { Text("Загрузка…", style = MaterialTheme.typography.bodySmall) }
        } else null,
    )
    Button(
        onClick = onLoad,
        enabled = state.selectedSubjectDirectionId != null && !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.padding(4.dp), strokeWidth = 2.dp)
        } else {
            Text("Загрузить статистику дисциплины")
        }
    }
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    state.subjectStats?.let { sub ->
        MuAnalyticsCard(
            title = sub.subjectName ?: "Дисциплина",
            subtitle = "Тип итога: ${sub.assessmentType ?: "—"} · Студентов: ${sub.totalStudents}",
        ) {
            if (sub.assessmentType.equals("CREDIT", ignoreCase = true)) {
                Text(
                    "Доля зачётов среди выставленных: ${String.format("%.1f", sub.creditRate)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "Выставлено: ${sub.gradedStudents} · Без отметки: ${sub.missingValues}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "Средний балл: ${String.format("%.2f", sub.averageGrade)} · Медиана: ${String.format("%.1f", sub.medianGrade)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                sub.gradeDistribution?.takeIf { it.isNotEmpty() }?.let { dist ->
                    Text("Распределение оценок", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    MuVerticalBarChart(
                        entries = dist.entries
                            .sortedBy { it.key.toIntOrNull() ?: 0 }
                            .map { (k, v) -> k to v.toFloat() },
                        chartHeight = 120.dp,
                        valueFormatter = { it.toInt().toString() },
                    )
                }
            }
        }
    }

    state.practiceStats?.let { pr ->
        MuAnalyticsCard(
            title = "Практики: ${pr.subjectName ?: ""}",
            subtitle = "Прогресс по работам (по числу практик с результатом)",
        ) {
            MuLabeledProgressMetric(
                label = "Заполненность практик",
                value = (pr.overallProgress / 100.0).toFloat().coerceIn(0f, 1f),
                valueDescription = String.format("%.1f%%", pr.overallProgress),
            )
            Text(
                "Средний по нормированным оценкам: ${String.format("%.2f", pr.totalScoreAverage)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            pr.practices?.takeIf { it.isNotEmpty() }?.forEach { p ->
                PracticeDetailBlock(p)
            }
        }
    }
}

@Composable
private fun PracticeDetailBlock(p: PracticeStatisticsDetail) {
    val title = p.practiceTitle?.takeIf { it.isNotBlank() }
        ?: "Практика ${p.practiceNumber}"
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        MuLabeledProgressMetric(
            label = "Заполнено",
            value = (p.completionRate / 100.0).toFloat().coerceIn(0f, 1f),
            valueDescription = "${p.withResult} / ${p.totalRecords}",
        )
        if (p.creditRate != null) {
            Text(
                "Зачёт: ${String.format("%.1f", p.creditRate)}% положительных среди выставленных",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (p.normalizedAverage != null) {
            Text(
                "Средняя (норм.): ${String.format("%.2f", p.normalizedAverage)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TeacherGroupSection(
    state: TeacherStatisticsUiState,
    onOpenInstitute: () -> Unit,
    onOpenDirection: () -> Unit,
    onOpenSubject: () -> Unit,
    onOpenGroup: () -> Unit,
    onLoad: () -> Unit,
) {
    MuPickerField(
        label = "Институт",
        valueText = state.instituteLabel,
        placeholder = "Выберите институт",
        enabled = !state.catalogLoading,
        onClick = onOpenInstitute,
        modifier = Modifier.fillMaxWidth(),
        supportingText = when {
            state.catalogLoading -> {
                { Text("Загрузка списка…", style = MaterialTheme.typography.bodySmall) }
            }
            state.institutes.isEmpty() -> {
                {
                    Text(
                        "Нет институтов с вашими дисциплинами.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> null
        },
    )
    MuPickerField(
        label = "Направление",
        valueText = state.directionLabel,
        placeholder = "Сначала институт",
        enabled = state.canPickDirection && !state.directionsLoading,
        onClick = onOpenDirection,
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (state.directionsLoading) {
            { Text("Загрузка…", style = MaterialTheme.typography.bodySmall) }
        } else null,
    )
    MuPickerField(
        label = "Дисциплина",
        valueText = state.subjectLabel,
        placeholder = "Сначала направление",
        enabled = state.canPickSubject && !state.subjectDirectionsLoading,
        onClick = onOpenSubject,
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (state.subjectDirectionsLoading) {
            { Text("Загрузка…", style = MaterialTheme.typography.bodySmall) }
        } else null,
    )
    MuPickerField(
        label = "Группа",
        valueText = state.groupLabel,
        placeholder = "Сначала дисциплина",
        enabled = state.canPickGroup && !state.groupsLoading,
        onClick = onOpenGroup,
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (state.groupsLoading) {
            { Text("Загрузка…", style = MaterialTheme.typography.bodySmall) }
        } else null,
    )
    Button(
        onClick = onLoad,
        enabled = state.selectedGroupId != null && !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.padding(4.dp), strokeWidth = 2.dp)
        } else {
            Text("Загрузить статистику группы")
        }
    }
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    state.groupStats?.let { g ->
        MuAnalyticsCard(
            title = g.groupName ?: "Группа",
            subtitle = "Средний балл ${String.format("%.2f", g.averagePerformance)} · Студентов ${g.studentCount}",
        ) {
            Text(
                "Должников: ${g.studentsWithDebt} (${String.format("%.1f", g.debtRate)}%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
            g.averageBySubject?.takeIf { it.isNotEmpty() }?.let { map ->
                Text("Средний балл по дисциплинам", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                MuHorizontalBarChart(
                    entries = map.entries.sortedByDescending { it.value }.take(12).map { (n, v) ->
                        val short = if (n.length <= 22) n else n.take(21) + "…"
                        short to v.toFloat()
                    },
                )
            }
            g.creditPassPercentBySubject?.takeIf { it.isNotEmpty() }?.let { map ->
                Text("Доля зачётов, %", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                MuHorizontalBarChart(
                    entries = map.entries.sortedByDescending { it.value }.take(12).map { (n, v) ->
                        val short = if (n.length <= 22) n else n.take(21) + "…"
                        short to v.toFloat()
                    },
                    maxValueOverride = 100f,
                    valueFormatter = { String.format("%.0f%%", it) },
                )
            }
            val denom = g.countedValues + g.missingValues
            MuLabeledProgressMetric(
                label = "Итоговые оценки выставлены",
                value = if (denom > 0) g.countedValues.toFloat() / denom.toFloat() else 0f,
                valueDescription = "${g.countedValues} / $denom",
            )
        }
    }
}

@Composable
private fun TeacherDirectionSection(
    state: TeacherStatisticsUiState,
    onOpenInstitute: () -> Unit,
    onOpenDirection: () -> Unit,
    onLoad: () -> Unit,
) {
    MuPickerField(
        label = "Институт",
        valueText = state.instituteLabel,
        placeholder = "Выберите институт",
        enabled = !state.catalogLoading,
        onClick = onOpenInstitute,
        modifier = Modifier.fillMaxWidth(),
        supportingText = when {
            state.catalogLoading -> {
                { Text("Загрузка списка…", style = MaterialTheme.typography.bodySmall) }
            }
            state.institutes.isEmpty() -> {
                {
                    Text(
                        "Нет институтов с вашими дисциплинами.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> null
        },
    )
    MuPickerField(
        label = "Направление",
        valueText = state.directionLabel,
        placeholder = "Сначала институт",
        enabled = state.canPickDirection && !state.directionsLoading,
        onClick = onOpenDirection,
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (state.directionsLoading) {
            { Text("Загрузка…", style = MaterialTheme.typography.bodySmall) }
        } else null,
    )
    Button(
        onClick = onLoad,
        enabled = state.selectedDirectionId != null && !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.padding(4.dp), strokeWidth = 2.dp)
        } else {
            Text("Загрузить статистику направления")
        }
    }
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    state.directionStats?.let { d ->
        MuAnalyticsCard(
            title = d.directionName ?: "Направление",
            subtitle = "Студентов ${d.totalStudents}, групп ${d.groupCount}",
        ) {
            Text(
                "Средняя успеваемость ${String.format("%.2f", d.averagePerformance)} · Доля должников ${String.format("%.1f", d.debtRate)}%",
                style = MaterialTheme.typography.bodySmall,
            )
            d.groups?.takeIf { it.isNotEmpty() }?.let { groups ->
                Text("Средний балл по группам", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                MuVerticalBarChart(
                    entries = groups.map { grp ->
                        val name = grp.groupName ?: "Группа"
                        val short = if (name.length <= 16) name else name.take(15) + "…"
                        short to grp.averagePerformance.toFloat()
                    },
                    chartHeight = 140.dp,
                )
            }
        }
    }
}

private fun teacherCatalogPrimary(name: String?, kind: String, id: Long): String =
    name?.takeIf { it.isNotBlank() } ?: "$kind №$id"
