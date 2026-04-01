package com.example.app_my_university.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    onNavigateBack: () -> Unit,
    viewModel: AdminStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var entityId by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    val tabs = AdminStatTab.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
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
                text = "Агрегаты по вузу и сущностям. Выберите тип, введите ID и нажмите «Загрузить». Для своего вуза откройте вкладку «Вуз» и укажите его идентификатор.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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

            OutlinedTextField(
                value = entityId,
                onValueChange = { entityId = it },
                label = { Text(hintForTab(tabs[tab])) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val id = entityId.toLongOrNull() ?: return@Button
                    when (tabs[tab]) {
                        AdminStatTab.GROUP -> viewModel.loadGroup(id)
                        AdminStatTab.DIRECTION -> viewModel.loadDirection(id)
                        AdminStatTab.INSTITUTE -> viewModel.loadInstitute(id)
                        AdminStatTab.UNIVERSITY -> viewModel.loadUniversity(id)
                        AdminStatTab.SCHEDULE_ROOM -> viewModel.loadClassroomSchedule(id)
                        AdminStatTab.SCHEDULE_TEACHER -> viewModel.loadTeacherSchedule(id)
                        AdminStatTab.SCHEDULE_GROUP -> viewModel.loadGroupSchedule(id)
                    }
                },
                enabled = entityId.toLongOrNull() != null && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Загрузить")
            }

            if (state.isLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
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

private fun hintForTab(tab: AdminStatTab): String = when (tab) {
    AdminStatTab.GROUP -> "ID группы"
    AdminStatTab.DIRECTION -> "ID направления"
    AdminStatTab.INSTITUTE -> "ID института"
    AdminStatTab.UNIVERSITY -> "ID вуза"
    AdminStatTab.SCHEDULE_ROOM -> "ID аудитории"
    AdminStatTab.SCHEDULE_TEACHER -> "ID преподавателя"
    AdminStatTab.SCHEDULE_GROUP -> "ID группы"
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
            text = "Должников: ${g.studentsWithDebt} из ${g.studentCount} (${String.format("%.1f", g.debtRate * 100)}%)",
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
                    .map { (name, v) -> shortLabel(name) to (v * 100).toFloat() },
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
            text = "Средняя успеваемость ${String.format("%.2f", d.averagePerformance)} · Доля должников ${String.format("%.1f", d.debtRate * 100)}%",
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
            text = "Средняя ${String.format("%.2f", i.averagePerformance)} · Должники ${i.studentsWithDebt} (${String.format("%.1f", i.debtRate * 100)}%)",
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
    MuAnalyticsCard(
        title = "Расписание: $scopeTitle",
        subtitle = "Всего занятий: ${s.totalLessons}, часов: ${String.format("%.1f", s.totalHours)}"
    ) {
        val byDay = scheduleDaySeries(s.byDayOfWeek)
        if (byDay.isNotEmpty()) {
            Text("Нагрузка по дням недели", style = MaterialTheme.typography.labelLarge)
            MuVerticalBarChart(entries = byDay, chartHeight = 120.dp, valueFormatter = { it.toInt().toString() })
        }
        val byWeek = scheduleWeekSeries(s.byWeekNumber)
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

private fun scheduleDaySeries(map: Map<String, Long>?): List<Pair<String, Float>> {
    if (map.isNullOrEmpty()) return emptyList()
    fun dayLabel(key: String): String = when (key.uppercase()) {
        "1", "MONDAY" -> "Пн"
        "2", "TUESDAY" -> "Вт"
        "3", "WEDNESDAY" -> "Ср"
        "4", "THURSDAY" -> "Чт"
        "5", "FRIDAY" -> "Пт"
        "6", "SATURDAY" -> "Сб"
        "7", "SUNDAY" -> "Вс"
        else -> key.take(3)
    }
    fun dayIndex(label: String): Int = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").indexOf(label).let { if (it < 0) 8 else it }
    val merged = mutableMapOf<String, Float>()
    map.forEach { (k, v) ->
        val lab = dayLabel(k)
        merged[lab] = (merged[lab] ?: 0f) + v.toFloat()
    }
    return merged.entries.sortedBy { dayIndex(it.key) }.map { it.key to it.value }
}

private fun scheduleWeekSeries(map: Map<String, Long>?): List<Pair<String, Float>> {
    if (map.isNullOrEmpty()) return emptyList()
    return map.entries
        .sortedBy { it.key.toIntOrNull() ?: 0 }
        .map { (k, v) -> "Н$k" to v.toFloat() }
}
