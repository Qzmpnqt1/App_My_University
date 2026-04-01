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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.PracticeGradeResponse
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.grades.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Ошибка загрузки",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadStudentGrades() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            uiState.grades.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Оценки пока не выставлены",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                                Text(
                                    text = if (it) "Зачтено" else "Не зачтено",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (it) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            } ?: Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            grade.grade?.let {
                                Text(
                                    text = "Оценка: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } ?: Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
