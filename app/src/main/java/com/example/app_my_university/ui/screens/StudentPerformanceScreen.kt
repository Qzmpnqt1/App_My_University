package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.ui.viewmodel.StudentPerformanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPerformanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentPerformanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Успеваемость") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            state.summary?.let { s ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("План / результат", style = MaterialTheme.typography.titleMedium)
                        Text("Дисциплин по плану: ${s.plannedSubjects}")
                        Text("С итоговой оценкой: ${s.subjectsWithFinalResult}")
                        Text("Зачтено: ${s.subjectsCredited}")
                        Text("Средний балл: ${s.averageNumericGrade?.let { String.format("%.2f", it) } ?: "—"}")
                        Text("Практик всего: ${s.totalPractices}, с оценкой: ${s.practicesWithResult}")
                        Text("Заполненность дисциплин: ${String.format("%.1f", s.subjectCompletionPercent)}%")
                        Text("Заполненность практик: ${String.format("%.1f", s.practiceCompletionPercent)}%")
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
