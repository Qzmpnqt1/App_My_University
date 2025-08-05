package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class GradeItem(
    val id: String,
    val subject: String,
    val professor: String,
    val grade: String?,
    val semester: Int
)

@Composable
fun GradeBookScreen() {
    var selectedSemester by remember { mutableStateOf(1) }
    val semesters = listOf(1, 2, 3, 4, 5, 6, 7, 8)
    
    val allGrades = remember {
        mapOf(
            1 to listOf(
                GradeItem(
                    id = "1",
                    subject = "Математический анализ",
                    professor = "Петров В.А.",
                    grade = "Отлично",
                    semester = 1
                ),
                GradeItem(
                    id = "2",
                    subject = "Программирование",
                    professor = "Сидорова Е.П.",
                    grade = "Хорошо",
                    semester = 1
                ),
                GradeItem(
                    id = "3",
                    subject = "Английский язык",
                    professor = "Иванова А.И.",
                    grade = "Отлично",
                    semester = 1
                )
            ),
            2 to listOf(
                GradeItem(
                    id = "4",
                    subject = "Дискретная математика",
                    professor = "Кузнецов И.И.",
                    grade = "Хорошо",
                    semester = 2
                ),
                GradeItem(
                    id = "5",
                    subject = "Алгоритмы и структуры данных",
                    professor = "Петрова О.С.",
                    grade = "Отлично",
                    semester = 2
                )
            ),
            3 to listOf(
                GradeItem(
                    id = "6",
                    subject = "Базы данных",
                    professor = "Смирнов А.Б.",
                    grade = null,
                    semester = 3
                )
            )
        )
    }
    
    val currentGrades = allGrades[selectedSemester] ?: emptyList()
    val averageGrade = calculateAverageGrade(currentGrades)
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Заголовок экрана с отступом 36dp сверху
            Text(
                text = "Зачетная книжка",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
            
            // Вкладки семестров
            TabRow(selectedTabIndex = selectedSemester - 1) {
                semesters.forEach { semester ->
                    Tab(
                        selected = selectedSemester == semester,
                        onClick = { selectedSemester = semester },
                        text = {
                            Text(
                                text = "Семестр $semester",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            }
            
            // Статистика
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Статистика за $selectedSemester семестр",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            title = "Средний балл",
                            value = averageGrade?.let { String.format("%.1f", it) } ?: "-"
                        )
                        
                        StatItem(
                            title = "Предметов",
                            value = currentGrades.size.toString()
                        )
                    }
                }
            }
            
            if (currentGrades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Оценок за $selectedSemester семестр пока нет",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentGrades) { grade ->
                        GradeItemCard(grade = grade)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun GradeItemCard(grade: GradeItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = grade.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Преподаватель: ${grade.professor}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = grade.grade ?: "Не сдан",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (grade.grade != null) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun calculateAverageGrade(grades: List<GradeItem>): Double? {
    val gradeValues = grades.mapNotNull { 
        when (it.grade) {
            "Отлично" -> 5.0
            "Хорошо" -> 4.0
            "Удовлетворительно" -> 3.0
            "Неудовлетворительно" -> 2.0
            else -> null
        }
    }
    
    return if (gradeValues.isNotEmpty()) {
        gradeValues.sum() / gradeValues.size
    } else {
        null
    }
}