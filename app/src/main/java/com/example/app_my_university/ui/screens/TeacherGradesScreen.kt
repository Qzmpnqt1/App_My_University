package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradesScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedStudent by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf(1) } // 1 - предметы, 2 - группы, 3 - студенты, 4 - оценка

    // Временные данные для демонстрации
    val subjects = listOf("Математический анализ", "Линейная алгебра", "Программирование", "Базы данных")
    val groups = listOf("ИС-21-1", "ИС-21-2", "ИС-22-1", "ИС-22-2")
    val students = listOf("Иванов И.И.", "Петров П.П.", "Сидоров С.С.", "Козлов К.К.")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выставление оценок") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Индикатор прогресса
            LinearProgressIndicator(
                progress = currentStep / 4f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Заголовок текущего шага
            Text(
                text = when (currentStep) {
                    1 -> "Выберите предмет"
                    2 -> "Выберите группу"
                    3 -> "Выберите студента"
                    4 -> "Выставьте оценку"
                    else -> ""
                },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (currentStep) {
                1 -> {
                    // Список предметов
                    LazyColumn {
                        items(subjects) { subject ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedSubject = subject
                                        currentStep = 2
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Список групп
                    LazyColumn {
                        items(groups) { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedGroup = group
                                        currentStep = 3
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = group,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Список студентов
                    LazyColumn {
                        items(students) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedStudent = student
                                        currentStep = 4
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = student,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Экран выставления оценки
                    GradeSelectionScreen(
                        subject = selectedSubject ?: "",
                        group = selectedGroup ?: "",
                        student = selectedStudent ?: "",
                        onGradeSubmitted = {
                            // Сброс к началу
                            selectedSubject = null
                            selectedGroup = null
                            selectedStudent = null
                            currentStep = 1
                        }
                    )
                }
            }

            // Кнопка "Назад" для всех шагов кроме первого
            if (currentStep > 1) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        when (currentStep) {
                            2 -> {
                                selectedSubject = null
                                currentStep = 1
                            }
                            3 -> {
                                selectedGroup = null
                                currentStep = 2
                            }
                            4 -> {
                                selectedStudent = null
                                currentStep = 3
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Назад")
                }
            }
        }
    }
}

@Composable
fun GradeSelectionScreen(
    subject: String,
    group: String,
    student: String,
    onGradeSubmitted: () -> Unit
) {
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    var isExam by remember { mutableStateOf(true) } // true - экзамен, false - зачет

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Информация о выборе
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Предмет: $subject",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Группа: $group",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Студент: $student",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор типа оценки
        Text(
            text = "Тип оценки:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isExam,
                onClick = { isExam = true },
                label = { Text("Экзамен") }
            )
            FilterChip(
                selected = !isExam,
                onClick = { isExam = false },
                label = { Text("Зачет") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор оценки
        Text(
            text = if (isExam) "Выберите оценку:" else "Выберите результат:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isExam) {
            // Оценки для экзамена
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("2", "3", "4", "5").forEach { grade ->
                    FilterChip(
                        selected = selectedGrade == grade,
                        onClick = { selectedGrade = grade },
                        label = { Text(grade) }
                    )
                }
            }
        } else {
            // Результат для зачета
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGrade == "Зачет",
                    onClick = { selectedGrade = "Зачет" },
                    label = { Text("Зачет") }
                )
                FilterChip(
                    selected = selectedGrade == "Незачет",
                    onClick = { selectedGrade = "Незачет" },
                    label = { Text("Незачет") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка подтверждения
        Button(
            onClick = onGradeSubmitted,
            enabled = selectedGrade != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выставить оценку")
        }
    }
} 