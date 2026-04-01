package com.example.app_my_university.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.PracticeGradeResponse
import com.example.app_my_university.ui.components.common.MuBadgeTone
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.analytics.MuAnalyticsCard
import com.example.app_my_university.ui.components.analytics.MuDonutChart
import com.example.app_my_university.ui.components.analytics.MuVerticalBarChart
import com.example.app_my_university.ui.components.analytics.creditBreakdown
import com.example.app_my_university.ui.components.analytics.creditDonutSegments
import com.example.app_my_university.ui.components.analytics.examGradeBarEntries
import com.example.app_my_university.ui.components.common.MuStatusBadge
import com.example.app_my_university.ui.viewmodel.GradeBookViewModel

private fun GradeResponse.isCreditFinalDiscipline(): Boolean =
    finalAssessmentType.equals("CREDIT", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && creditStatus != null && grade == null)

private fun GradeResponse.isExamFinalDiscipline(): Boolean =
    finalAssessmentType.equals("EXAM", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && grade != null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: GradeBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStudentGrades()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Зачётная книжка") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.grades.isEmpty() -> {
                MuLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            uiState.error != null && uiState.grades.isEmpty() -> {
                MuErrorState(
                    message = uiState.error ?: "Ошибка загрузки",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onRetry = { viewModel.loadStudentGrades() }
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
                    onAction = { viewModel.loadStudentGrades() }
                )
            }
            else -> {
                val examAvg = remember(uiState.grades) {
                    val nums = uiState.grades
                        .filter { it.isExamFinalDiscipline() }
                        .mapNotNull { it.grade }
                        .filter { it in 2..5 }
                    if (nums.isEmpty()) null else nums.average()
                }
                val creditSummary = remember(uiState.grades) {
                    val rows = uiState.grades.filter { it.isCreditFinalDiscipline() }
                    if (rows.isEmpty()) null
                    else {
                        val passed = rows.count { it.creditStatus == true }
                        passed to rows.size
                    }
                }
                val examBars = remember(uiState.grades) { examGradeBarEntries(uiState.grades) }
                val creditBreakdownMemo = remember(uiState.grades) { creditBreakdown(uiState.grades) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = examAvg?.let { String.format("%.1f", it) } ?: "—",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Средний (экзамены 2–5)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = creditSummary?.let { (p, t) -> "$p / $t" } ?: "—",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Зачёты (зач / всего)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${uiState.grades.size}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Предметов",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    if (examBars.any { it.second > 0f }) {
                        item {
                            MuAnalyticsCard(
                                title = "Распределение оценок",
                                subtitle = "Экзамены и дифференцированный зачёт (оценки 2–5)"
                            ) {
                                MuVerticalBarChart(
                                    entries = examBars,
                                    chartHeight = 140.dp,
                                    maxValueOverride = examBars.maxOf { it.second }.coerceAtLeast(1f)
                                )
                            }
                        }
                    }
                    if (creditBreakdownMemo.passed + creditBreakdownMemo.failed + creditBreakdownMemo.pending > 0) {
                        item {
                            MuAnalyticsCard(
                                title = "Зачёты",
                                subtitle = creditSummary?.let { (p, t) -> "Зачтено $p из $t по зачётам" } ?: "Статус зачётов"
                            ) {
                                MuDonutChart(
                                    segments = creditDonutSegments(creditBreakdownMemo, MaterialTheme.colorScheme),
                                    donutSize = 128.dp,
                                    strokeWidth = 18.dp,
                                    centerValue = creditSummary?.let { (p, _) -> "$p" } ?: "—",
                                    centerTitle = "зачтено"
                                )
                            }
                        }
                    }
                    items(uiState.grades) { grade ->
                        GradeCard(grade = grade, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeCard(
    grade: GradeResponse,
    viewModel: GradeBookViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val state = viewModel.uiState.collectAsState().value
    val practiceGradesForCard = if (
        expanded &&
        state.practiceGradesSubjectDirectionId == grade.subjectDirectionId
    ) {
        state.practiceGrades
    } else {
        emptyList()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        if (expanded) {
                            viewModel.loadStudentPracticeGrades(grade.subjectDirectionId)
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.subjectName ?: "Предмет #${grade.subjectDirectionId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    val kindLabel = when {
                        grade.finalAssessmentType.equals("CREDIT", ignoreCase = true) -> "Зачёт"
                        grade.finalAssessmentType.equals("EXAM", ignoreCase = true) -> "Экзамен"
                        grade.isCreditFinalDiscipline() -> "Зачёт"
                        else -> "Экзамен"
                    }
                    Text(
                        text = kindLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (grade.isCreditFinalDiscipline()) {
                            grade.creditStatus?.let {
                                MuStatusBadge(
                                    text = if (it) "Зачтено" else "Незачёт",
                                    tone = if (it) MuBadgeTone.Success else MuBadgeTone.Error
                                )
                            } ?: MuStatusBadge(
                                text = "Не выставлено",
                                tone = MuBadgeTone.Warning
                            )
                        } else {
                            grade.grade?.let {
                                MuStatusBadge(text = "Оценка $it", tone = MuBadgeTone.Primary)
                            } ?: MuStatusBadge(
                                text = "Не выставлено",
                                tone = MuBadgeTone.Warning
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                if (practiceGradesForCard.isEmpty()) {
                    Text(
                        text = "Практические оценки не найдены",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HorizontalDivider()
                        Text(
                            text = "Практические работы:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        practiceGradesForCard.forEach { pg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = pg.practiceTitle ?: "Практика #${pg.practiceNumber ?: pg.practiceId}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                PracticeGradeInline(pg)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeGradeInline(pg: PracticeGradeResponse) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (pg.practiceIsCredit) {
            true -> {
                pg.creditStatus?.let {
                    Text(
                        text = if (it) "Зачтено" else "Не зачтено",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (it) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                } ?: Text("—", style = MaterialTheme.typography.bodySmall)
            }
            false -> {
                pg.grade?.let {
                    Text(
                        text = "$it" + (pg.maxGrade?.let { max -> "/$max" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } ?: Text("—", style = MaterialTheme.typography.bodySmall)
            }
            null -> {
                pg.grade?.let {
                    Text(
                        text = "$it" + (pg.maxGrade?.let { max -> "/$max" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                pg.creditStatus?.let {
                    Text(
                        text = if (it) "Зачтено" else "Не зачтено",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (it) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
                if (pg.grade == null && pg.creditStatus == null) {
                    Text("—", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
