package com.example.app_my_university.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Данные о семестре
data class Semester(
    val id: String,
    val name: String,
    val subjects: List<Subject>
)

// Данные о предмете
data class Subject(
    val id: String,
    val name: String,
    val examGrade: Int? = null, // Оценка за экзамен (если есть)
    val creditStatus: Boolean? = null // Статус зачета (если есть)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeBookScreen(
    onBackPressed: () -> Unit = {}
) {
    // Пример данных о семестрах и предметах
    val semesters = remember {
        listOf(
            Semester(
                id = "1",
                name = "Семестр 1",
                subjects = listOf(
                    Subject(
                        id = "1",
                        name = "Математический анализ",
                        examGrade = 4,
                        creditStatus = null
                    ),
                    Subject(
                        id = "2",
                        name = "Информатика",
                        examGrade = 5,
                        creditStatus = null
                    ),
                    Subject(
                        id = "3",
                        name = "Иностранный язык",
                        examGrade = null,
                        creditStatus = true
                    ),
                    Subject(
                        id = "4",
                        name = "Введение в программирование",
                        examGrade = 5,
                        creditStatus = null
                    )
                )
            ),
            Semester(
                id = "2",
                name = "Семестр 2",
                subjects = listOf(
                    Subject(
                        id = "5",
                        name = "Дискретная математика",
                        examGrade = 3,
                        creditStatus = null
                    ),
                    Subject(
                        id = "6",
                        name = "Математическая логика",
                        examGrade = 4,
                        creditStatus = null
                    ),
                    Subject(
                        id = "7",
                        name = "Физическая культура",
                        examGrade = null,
                        creditStatus = true
                    ),
                    Subject(
                        id = "8",
                        name = "Объектно-ориентированное программирование",
                        examGrade = 5,
                        creditStatus = null
                    )
                )
            ),
            Semester(
                id = "3",
                name = "Семестр 3",
                subjects = listOf(
                    Subject(
                        id = "9",
                        name = "Теория вероятностей",
                        examGrade = 4,
                        creditStatus = null
                    ),
                    Subject(
                        id = "10",
                        name = "Базы данных",
                        examGrade = 5,
                        creditStatus = null
                    ),
                    Subject(
                        id = "11",
                        name = "Компьютерные сети",
                        examGrade = null,
                        creditStatus = true
                    ),
                    Subject(
                        id = "12",
                        name = "Алгоритмы и структуры данных",
                        examGrade = 4,
                        creditStatus = null
                    )
                )
            )
        )
    }
    
    // Состояние для хранения развернутых семестров
    val expandedSemesterIds = remember { mutableStateOf(setOf<String>()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Зачетная книжка") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            items(semesters) { semester ->
                SemesterCard(
                    semester = semester,
                    isExpanded = expandedSemesterIds.value.contains(semester.id),
                    onExpandToggle = {
                        expandedSemesterIds.value = if (expandedSemesterIds.value.contains(semester.id)) {
                            expandedSemesterIds.value - semester.id
                        } else {
                            expandedSemesterIds.value + semester.id
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterCard(
    semester: Semester,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок семестра с кнопкой разворачивания
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = semester.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    modifier = Modifier.rotate(rotationState)
                )
            }
            
            // Содержимое семестра (отображается только когда семестр развернут)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Разделяем предметы на экзамены и зачеты
                    val exams = semester.subjects.filter { it.examGrade != null }
                    val credits = semester.subjects.filter { it.creditStatus != null }
                    
                    if (exams.isNotEmpty()) {
                        Text(
                            text = "Экзамены",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        exams.forEach { subject ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = subject.name,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Индикатор оценки
                                val grade = subject.examGrade ?: 0
                                val gradeColor = when (grade) {
                                    5 -> Color(0xFF4CAF50) // Зеленый для отлично
                                    4 -> Color(0xFF2196F3) // Синий для хорошо
                                    3 -> Color(0xFFFF9800) // Оранжевый для удовлетворительно
                                    else -> Color.Gray
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(start = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = grade.toString(),
                                        color = gradeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            
                            if (subject != exams.last() || credits.isNotEmpty()) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                    
                    if (credits.isNotEmpty()) {
                        if (exams.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Text(
                            text = "Зачеты",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        credits.forEach { subject ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = subject.name,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Индикатор зачета
                                val creditStatus = subject.creditStatus ?: false
                                val statusText = if (creditStatus) "Зачет" else "Незачет"
                                val statusColor = if (creditStatus) Color(0xFF4CAF50) else Color(0xFFF44336)
                                
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            if (subject != credits.last()) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
} 