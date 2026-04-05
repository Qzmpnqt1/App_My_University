package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.rememberAppRole
import com.example.app_my_university.ui.components.analytics.MuAnalyticsCard
import com.example.app_my_university.ui.components.analytics.MuAnalyticsEmptyState
import com.example.app_my_university.ui.components.analytics.MuDonutChart
import com.example.app_my_university.ui.components.analytics.MuDonutSegment
import com.example.app_my_university.ui.components.analytics.MuVerticalBarChart
import com.example.app_my_university.ui.components.analytics.MuLabeledProgressMetric
import com.example.app_my_university.ui.viewmodel.StudentPerformanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPerformanceScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: StudentPerformanceViewModel = hiltViewModel()
) {
    val role = rememberAppRole()
    val state by viewModel.uiState.collectAsState()
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    RoleShellScaffold(
        role = role,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Успеваемость",
                onBackPressed = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.m),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            RowFields(course, semester, onCourse = { course = it }, onSemester = { semester = it })
            Button(
                onClick = {
                    viewModel.load(
                        course = course.toIntOrNull(),
                        semester = semester.toIntOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Обновить") }

            when {
                state.isLoading && state.summary == null -> {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null && state.summary == null -> {
                    Text(
                        state.error ?: "Ошибка",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                state.summary == null -> {
                    MuAnalyticsEmptyState("Нет данных. Нажмите «Обновить».")
                }
                else -> {
                    val s = state.summary!!
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    state.error?.let { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = err,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    val filterNote = buildString {
                        if (s.courseFilter != null) append("Курс ${s.courseFilter}")
                        if (s.semesterFilter != null) {
                            if (isNotEmpty()) append(" · ")
                            append("Семестр ${s.semesterFilter}")
                        }
                    }.ifBlank { "Все периоды" }

                    MuAnalyticsCard(title = "Сводка", subtitle = filterNote) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = s.averageNumericGrade?.let { String.format("%.2f", it) } ?: "—",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Средний балл",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "Дисциплин по плану: ${s.plannedSubjects} · С итоговой оценкой: ${s.subjectsWithFinalResult} · Зачтено: ${s.subjectsCredited}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Практик: ${s.totalPractices}, с результатом: ${s.practicesWithResult}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    MuAnalyticsCard(
                        title = "Заполненность",
                        subtitle = "Доля закрытых итогов по дисциплинам и практикам"
                    ) {
                        MuLabeledProgressMetric(
                            label = "Дисциплины (итог выставлен)",
                            value = (s.subjectCompletionPercent / 100.0).toFloat().coerceIn(0f, 1f),
                            valueDescription = String.format("%.1f%%", s.subjectCompletionPercent)
                        )
                        MuLabeledProgressMetric(
                            label = "Практики (есть результат)",
                            value = (s.practiceCompletionPercent / 100.0).toFloat().coerceIn(0f, 1f),
                            valueDescription = String.format("%.1f%%", s.practiceCompletionPercent),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (s.plannedSubjects > 0) {
                        val scheme = MaterialTheme.colorScheme
                        val pending = (s.plannedSubjects - s.subjectsWithFinalResult).coerceAtLeast(0)
                        MuAnalyticsCard(title = "Дисциплины: план и итоги", subtitle = "Соотношение по выбранному фильтру") {
                            MuVerticalBarChart(
                                entries = listOf(
                                    "План" to s.plannedSubjects.toFloat(),
                                    "С итогом" to s.subjectsWithFinalResult.toFloat(),
                                    "Зачтено" to s.subjectsCredited.toFloat()
                                ),
                                chartHeight = 120.dp,
                                valueFormatter = { it.toInt().toString() }
                            )
                            if (pending > 0 || s.subjectsWithFinalResult > 0) {
                                MuDonutChart(
                                    segments = listOf(
                                        MuDonutSegment("С итоговой оценкой", s.subjectsWithFinalResult.toFloat(), scheme.primary),
                                        MuDonutSegment("Без итога", pending.toFloat(), scheme.outline.copy(alpha = 0.55f))
                                    ),
                                    donutSize = 120.dp,
                                    strokeWidth = 16.dp,
                                    centerValue = "${s.subjectsWithFinalResult}",
                                    centerTitle = "из ${s.plannedSubjects}"
                                )
                            }
                        }
                    }

                    if (s.totalPractices > 0) {
                        MuAnalyticsCard(title = "Практики", subtitle = "Сколько работ с выставленным результатом") {
                            MuVerticalBarChart(
                                entries = listOf(
                                    "Всего" to s.totalPractices.toFloat(),
                                    "С оценкой" to s.practicesWithResult.toFloat()
                                ),
                                chartHeight = 110.dp,
                                valueFormatter = { it.toInt().toString() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowFields(
    course: String,
    semester: String,
    onCourse: (String) -> Unit,
    onSemester: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = course,
            onValueChange = onCourse,
            label = { Text("Курс (фильтр)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = semester,
            onValueChange = onSemester,
            label = { Text("Семестр (фильтр)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
