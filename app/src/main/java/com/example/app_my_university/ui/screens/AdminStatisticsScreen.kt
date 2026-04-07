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
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.components.picker.MuPickerField
import com.example.app_my_university.ui.components.picker.MuSearchablePickerSheet
import com.example.app_my_university.ui.components.picker.PickerListItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.core.logging.AppLogger
import com.example.app_my_university.data.api.model.DirectionStatisticsResponse
import com.example.app_my_university.data.api.model.GroupStatisticsResponse
import com.example.app_my_university.data.api.model.InstituteStatisticsResponse
import com.example.app_my_university.data.api.model.ScheduleStatisticsResponse
import com.example.app_my_university.data.api.model.UniversityStatisticsResponse
import com.example.app_my_university.ui.components.analytics.MuAnalyticsCard
import com.example.app_my_university.ui.components.analytics.MuDonutChart
import com.example.app_my_university.ui.components.analytics.MuDonutSegment
import com.example.app_my_university.ui.components.analytics.MuHorizontalBarChart
import com.example.app_my_university.ui.components.analytics.MuLineChart
import com.example.app_my_university.ui.components.analytics.MuLabeledProgressMetric
import com.example.app_my_university.ui.components.analytics.MuVerticalBarChart
import com.example.app_my_university.ui.components.analytics.scheduleDaySeriesForChart
import com.example.app_my_university.ui.components.analytics.scheduleWeekSeriesForChart
import com.example.app_my_university.ui.viewmodel.AdminStatisticsPayload
import com.example.app_my_university.ui.viewmodel.AdminStatisticsViewModel

private enum class AdminStatTab(val label: String) {
    GROUP("Группа"),
    DIRECTION("Направление"),
    INSTITUTE("Институт"),
    UNIVERSITY("Вуз"),
    SCHEDULE_ROOM("Аудитория"),
    SCHEDULE_TEACHER("Преподаватель"),
    SCHEDULE_GROUP("Группа (расписание)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    val tabs = AdminStatTab.entries
    var selectionId by remember { mutableStateOf<Long?>(null) }
    var selectionLabel by remember { mutableStateOf("") }
    var entityPickerOpen by remember { mutableStateOf(false) }
    var scheduleWeekFilter by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        AppLogger.screen("AdminStatistics")
        viewModel.ensureCatalogLoaded()
    }

    DisposableEffect(Unit) {
        onDispose { AppLogger.i("Screen", "leave=AdminStatistics") }
    }

    LaunchedEffect(tab) {
        selectionId = null
        selectionLabel = ""
        scheduleWeekFilter = null
        viewModel.clearStalePayload()
    }

    val currentTab = tabs[tab]
    // Явные ключи-списки: remember(state) по data class может не пересчитаться при том же equals.
    val pickerItems: List<PickerListItem> = remember(
        currentTab,
        state.groups,
        state.directions,
        state.institutes,
        state.classrooms,
        state.teachers,
    ) {
        when (currentTab) {
            AdminStatTab.GROUP -> state.groups.map { g ->
                PickerListItem(
                    id = g.id,
                    primary = g.name,
                    secondary = g.directionName?.takeIf { it.isNotBlank() },
                )
            }
            AdminStatTab.DIRECTION -> state.directions.map { d ->
                PickerListItem(id = d.id, primary = d.name, secondary = d.instituteName)
            }
            AdminStatTab.INSTITUTE -> state.institutes.map { i ->
                PickerListItem(id = i.id, primary = i.name, secondary = i.shortName)
            }
            AdminStatTab.UNIVERSITY -> emptyList()
            AdminStatTab.SCHEDULE_ROOM -> state.classrooms.map { c ->
                PickerListItem(
                    id = c.id,
                    primary = "${c.building.orEmpty()} ${c.roomNumber.orEmpty()}".trim().ifBlank { "Аудитория" },
                    secondary = c.capacity?.let { "Вместимость: $it" },
                )
            }
            AdminStatTab.SCHEDULE_TEACHER -> state.teachers.map { t ->
                PickerListItem(
                    id = t.id,
                    primary = "${t.lastName} ${t.firstName}".trim(),
                    secondary = t.email,
                )
            }
            AdminStatTab.SCHEDULE_GROUP -> state.groups.map { g ->
                PickerListItem(
                    id = g.id,
                    primary = g.name,
                    secondary = g.directionName,
                )
            }
        }
    }

    MuSearchablePickerSheet(
        visible = entityPickerOpen,
        onDismiss = { entityPickerOpen = false },
        title = pickerTitle(currentTab),
        items = pickerItems,
        listLoading = state.catalogLoading,
        onSelect = { row ->
            selectionId = row.id
            selectionLabel = listOfNotNull(row.primary, row.secondary).joinToString(" · ")
            entityPickerOpen = false
        },
    )

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Аналитика",
                onBackPressed = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            Text(
                text = "Выберите тип аналитики и сущность, затем нажмите «Загрузить». На вкладке «Вуз» данные привязаны к выбранному вузу администрирования или к вузу из профиля.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.catalogLoading) {
                Text("Загрузка справочников…", style = MaterialTheme.typography.bodySmall)
            }
            state.catalogError?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { viewModel.refreshCatalog() }) {
                    Text("Повторить загрузку справочников")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { i, t ->
                    FilterChip(
                        selected = tab == i,
                        onClick = { tab = i },
                        label = { Text(t.label) }
                    )
                }
            }

            when (currentTab) {
                AdminStatTab.UNIVERSITY -> {
                    val uniId = state.adminUniversityId
                    val superGlobal = state.isSuperAdmin && uniId == null
                    if (superGlobal) {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Загрузить статистику вуза (выберите вуз на главной)")
                        }
                    } else {
                        val label = state.adminUniversityName ?: "Вуз не определён в профиле"
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                if (uniId != null) viewModel.loadUniversity(uniId)
                            },
                            enabled = uniId != null && !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Загрузить статистику вуза")
                        }
                    }
                }
                else -> {
                    MuPickerField(
                        label = pickerTitle(currentTab),
                        valueText = selectionLabel,
                        placeholder = "Выберите из списка",
                        enabled = state.catalogReady,
                        onClick = {
                            AppLogger.userAction("AdminStatistics", "openPicker tab=${currentTab.name}")
                            entityPickerOpen = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = if (state.catalogReady && pickerItems.isEmpty()) {
                            { Text("Нет записей для выбора в вашем вузе", color = MaterialTheme.colorScheme.error) }
                        } else null,
                    )
                    val isScheduleTab = currentTab == AdminStatTab.SCHEDULE_ROOM
                        || currentTab == AdminStatTab.SCHEDULE_TEACHER
                        || currentTab == AdminStatTab.SCHEDULE_GROUP
                    if (isScheduleTab) {
                        Text(
                            "Неделя расписания",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = scheduleWeekFilter == null,
                                onClick = { scheduleWeekFilter = null },
                                label = { Text("Все") },
                            )
                            FilterChip(
                                selected = scheduleWeekFilter == 1,
                                onClick = { scheduleWeekFilter = 1 },
                                label = { Text("Неделя 1") },
                            )
                            FilterChip(
                                selected = scheduleWeekFilter == 2,
                                onClick = { scheduleWeekFilter = 2 },
                                label = { Text("Неделя 2") },
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val id = selectionId ?: return@Button
                            AppLogger.userAction("AdminStatistics", "load tab=${currentTab.name} id=$id")
                            when (currentTab) {
                                AdminStatTab.GROUP -> viewModel.loadGroup(id)
                                AdminStatTab.DIRECTION -> viewModel.loadDirection(id)
                                AdminStatTab.INSTITUTE -> viewModel.loadInstitute(id)
                                AdminStatTab.UNIVERSITY -> Unit
                                AdminStatTab.SCHEDULE_ROOM -> viewModel.loadClassroomSchedule(id, scheduleWeekFilter)
                                AdminStatTab.SCHEDULE_TEACHER -> viewModel.loadTeacherSchedule(id, scheduleWeekFilter)
                                AdminStatTab.SCHEDULE_GROUP -> viewModel.loadGroupSchedule(id, scheduleWeekFilter)
                            }
                        },
                        enabled = selectionId != null && !state.isLoading && state.catalogReady && pickerItems.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Загрузить")
                    }
                }
            }

            if (state.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.s),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = AppSpacing.m),
                        strokeWidth = 2.5.dp,
                    )
                    Text(
                        text = "Загрузка статистики…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            state.payload?.let { p ->
                when (p) {
                    is AdminStatisticsPayload.Group -> GroupStatsVisuals(p.data)
                    is AdminStatisticsPayload.Direction -> DirectionStatsVisuals(p.data)
                    is AdminStatisticsPayload.Institute -> InstituteStatsVisuals(p.data)
                    is AdminStatisticsPayload.University -> UniversityStatsVisuals(p.data)
                    is AdminStatisticsPayload.Schedule -> ScheduleStatsVisuals(p.title, p.data)
                }
            }
        }
    }
}

private fun pickerTitle(tab: AdminStatTab): String = when (tab) {
    AdminStatTab.GROUP -> "Группа"
    AdminStatTab.DIRECTION -> "Направление"
    AdminStatTab.INSTITUTE -> "Институт"
    AdminStatTab.UNIVERSITY -> "Вуз"
    AdminStatTab.SCHEDULE_ROOM -> "Аудитория"
    AdminStatTab.SCHEDULE_TEACHER -> "Преподаватель"
    AdminStatTab.SCHEDULE_GROUP -> "Группа (расписание)"
}

private fun shortLabel(s: String, max: Int = 20): String =
    if (s.length <= max) s else s.take(max - 1) + "…"

@Composable
private fun GroupStatsVisuals(g: GroupStatisticsResponse) {
    val scheme = MaterialTheme.colorScheme
    MuAnalyticsCard(
        title = g.groupName ?: "Группа",
        subtitle = "Средняя успеваемость ${String.format("%.2f", g.averagePerformance)} · Студентов ${g.studentCount}"
    ) {
        Text(
            text = "Должников: ${g.studentsWithDebt} из ${g.studentCount} (${String.format("%.1f", g.debtRate)}%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        val withDebt = g.studentsWithDebt.toFloat()
        val ok = (g.studentCount - g.studentsWithDebt).coerceAtLeast(0).toFloat()
        if (withDebt + ok > 0f) {
            MuDonutChart(
                segments = listOf(
                    MuDonutSegment("Без академического долга", ok, scheme.primary),
                    MuDonutSegment("С долгом", withDebt, scheme.error)
                ),
                centerValue = "${g.studentsWithDebt}",
                centerTitle = "должников"
            )
        }
        g.averageBySubject?.takeIf { it.isNotEmpty() }?.let { map ->
            Text("Средний балл по дисциплинам", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            MuHorizontalBarChart(
                entries = map.entries
                    .sortedByDescending { it.value }
                    .take(10)
                    .map { (name, v) -> shortLabel(name) to v.toFloat() }
            )
        }
        g.creditPassPercentBySubject?.takeIf { it.isNotEmpty() }?.let { map ->
            Text("Доля зачётов по дисциплинам, %", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            MuHorizontalBarChart(
                entries = map.entries
                    .sortedByDescending { it.value }
                    .take(10)
                    .map { (name, v) -> shortLabel(name) to v.toFloat() },
                maxValueOverride = 100f,
                valueFormatter = { String.format("%.0f%%", it) }
            )
        }
        MuLabeledProgressMetric(
            label = "Заполненность итоговых оценок",
            value = if (g.countedValues + g.missingValues > 0) {
                g.countedValues.toFloat() / (g.countedValues + g.missingValues.toFloat())
            } else 0f,
            valueDescription = "${g.countedValues} / ${g.countedValues + g.missingValues}"
        )
    }
}

@Composable
private fun DirectionStatsVisuals(d: DirectionStatisticsResponse) {
    val scheme = MaterialTheme.colorScheme
    MuAnalyticsCard(
        title = d.directionName ?: "Направление",
        subtitle = "Студентов ${d.totalStudents}, групп ${d.groupCount}"
    ) {
        Text(
            text = "Средняя успеваемость ${String.format("%.2f", d.averagePerformance)} · Доля должников ${String.format("%.1f", d.debtRate)}%",
            style = MaterialTheme.typography.bodySmall
        )
        val debt = d.studentsWithDebt.toFloat()
        val ok = (d.totalStudents - d.studentsWithDebt).coerceAtLeast(0).toFloat()
        if (debt + ok > 0f) {
            MuDonutChart(
                segments = listOf(
                    MuDonutSegment("Без долга", ok, scheme.primary),
                    MuDonutSegment("С долгом", debt, scheme.error)
                ),
                centerValue = "${d.studentsWithDebt}",
                centerTitle = "должников"
            )
        }
        d.groups?.takeIf { it.isNotEmpty() }?.let { groupList ->
            Text("Средний балл по группам", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            MuVerticalBarChart(
                entries = groupList.map { grp ->
                    shortLabel(grp.groupName ?: "Группа") to grp.averagePerformance.toFloat()
                },
                chartHeight = 140.dp
            )
        }
    }
}

@Composable
private fun InstituteStatsVisuals(i: InstituteStatisticsResponse) {
    val scheme = MaterialTheme.colorScheme
    MuAnalyticsCard(
        title = i.instituteName ?: "Институт",
        subtitle = "Направлений: ${i.directionCount}, студентов: ${i.totalStudents}"
    ) {
        Text(
            text = "Средняя ${String.format("%.2f", i.averagePerformance)} · Должники ${i.studentsWithDebt} (${String.format("%.1f", i.debtRate)}%)",
            style = MaterialTheme.typography.bodySmall
        )
        val debt = i.studentsWithDebt.toFloat()
        val ok = (i.totalStudents - i.studentsWithDebt).coerceAtLeast(0).toFloat()
        if (debt + ok > 0f) {
            MuDonutChart(
                segments = listOf(
                    MuDonutSegment("Без долга", ok, scheme.tertiary),
                    MuDonutSegment("С долгом", debt, scheme.error)
                ),
                centerValue = "${i.studentsWithDebt}",
                centerTitle = "должников"
            )
        }
        i.directions?.takeIf { it.isNotEmpty() }?.let { directionList ->
            Text("Студенты по направлениям", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            MuVerticalBarChart(
                entries = directionList.map { dir ->
                    shortLabel(dir.directionName ?: "Напр.") to dir.studentCount.toFloat()
                },
                valueFormatter = { it.toInt().toString() },
                chartHeight = 140.dp
            )
            Text("Средний балл по направлениям", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            MuHorizontalBarChart(
                entries = directionList.map { dir ->
                    shortLabel(dir.directionName ?: "Напр.") to dir.averagePerformance.toFloat()
                }
            )
        }
    }
}

@Composable
private fun UniversityStatsVisuals(u: UniversityStatisticsResponse) {
    val scheme = MaterialTheme.colorScheme
    MuAnalyticsCard(
        title = u.universityName ?: "Вуз",
        subtitle = "Институтов: ${u.instituteCount}, студентов: ${u.totalStudents}"
    ) {
        Text(
            text = "Средняя успеваемость ${String.format("%.2f", u.averagePerformance)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        val debt = u.studentsWithDebt.toFloat()
        val ok = (u.totalStudents - u.studentsWithDebt).coerceAtLeast(0).toFloat()
        if (debt + ok > 0f) {
            MuDonutChart(
                segments = listOf(
                    MuDonutSegment("Без долга", ok, scheme.primary),
                    MuDonutSegment("С долгом", debt, scheme.error)
                ),
                centerValue = "${u.studentsWithDebt}",
                centerTitle = "должников"
            )
        }
        u.institutes?.takeIf { it.isNotEmpty() }?.let { instituteList ->
            Text("Студенты по институтам", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            MuVerticalBarChart(
                entries = instituteList.map { x -> shortLabel(x.instituteName ?: "Ин-т") to x.studentCount.toFloat() },
                valueFormatter = { it.toInt().toString() },
                chartHeight = 150.dp
            )
            Text("Средний балл по институтам", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            MuHorizontalBarChart(
                entries = instituteList.map { x -> shortLabel(x.instituteName ?: "Ин-т") to x.averagePerformance.toFloat() }
            )
        }
    }
}

@Composable
private fun ScheduleStatsVisuals(scopeTitle: String, s: ScheduleStatisticsResponse) {
    val weekNote = s.weekNumberFilter?.let { " · только неделя $it" } ?: " · все недели"
    MuAnalyticsCard(
        title = "Расписание: $scopeTitle",
        subtitle = "Всего занятий: ${s.totalLessons}, часов: ${String.format("%.1f", s.totalHours)}$weekNote"
    ) {
        val byDay = scheduleDaySeriesForChart(s.byDayOfWeek)
        if (byDay.isNotEmpty()) {
            Text("Нагрузка по дням недели", style = MaterialTheme.typography.labelLarge)
            MuVerticalBarChart(entries = byDay, chartHeight = 120.dp, valueFormatter = { it.toInt().toString() })
        }
        val byWeek = scheduleWeekSeriesForChart(s.byWeekNumber)
        if (byWeek.size >= 2) {
            Text("Динамика по номерам недель", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            MuLineChart(points = byWeek, chartHeight = 120.dp)
        } else if (byWeek.isNotEmpty()) {
            MuVerticalBarChart(entries = byWeek, chartHeight = 100.dp)
        }
        if (byDay.isEmpty() && byWeek.isEmpty()) {
            Text(
                "Нет разбивки по дням или неделям в ответе API.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

