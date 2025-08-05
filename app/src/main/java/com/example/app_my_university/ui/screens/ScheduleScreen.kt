package com.example.app_my_university.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Данные для демонстрации
data class LessonScheduleItem(
    val id: String,
    val subject: String,
    val type: String,
    val time: String,
    val location: String,
    val teacher: String,
    val dayOfWeek: Int
)

@Composable
fun ScheduleScreen() {
    // Текущая дата и выбранный день недели
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedDayOfWeek by remember {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Преобразуем из Calendar.DAY_OF_WEEK (1-Sunday, 2-Monday, ...) в индекс (0-Monday, 1-Tuesday, ...)
        mutableStateOf(if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2)
    }
    
    // Генерация данных для демонстрации
    val lessons = remember {
        listOf(
            LessonScheduleItem(
                id = "1",
                subject = "Математический анализ",
                type = "Лекция",
                time = "09:00 - 10:30",
                location = "А-512",
                teacher = "Иванов И.И.",
                dayOfWeek = 0 // Понедельник
            ),
            LessonScheduleItem(
                id = "2",
                subject = "Программирование",
                type = "Практика",
                time = "10:45 - 12:15",
                location = "Б-302",
                teacher = "Петрова П.П.",
                dayOfWeek = 0 // Понедельник
            ),
            LessonScheduleItem(
                id = "3",
                subject = "Английский язык",
                type = "Семинар",
                time = "13:00 - 14:30",
                location = "В-204",
                teacher = "Сидорова С.С.",
                dayOfWeek = 1 // Вторник
            ),
            LessonScheduleItem(
                id = "4",
                subject = "Физика",
                type = "Лекция",
                time = "09:00 - 10:30",
                location = "А-400",
                teacher = "Кузнецов К.К.",
                dayOfWeek = 2 // Среда
            ),
            LessonScheduleItem(
                id = "5",
                subject = "Дискретная математика",
                type = "Практика",
                time = "10:45 - 12:15",
                location = "Б-401",
                teacher = "Смирнов С.С.",
                dayOfWeek = 4 // Пятница
            )
        )
    }
    
    // Фильтрация занятий для выбранного дня недели
    val filteredLessons = lessons.filter { it.dayOfWeek == selectedDayOfWeek }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Заголовок экрана с отступом 36dp сверху
            Text(
                text = "Расписание",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, bottom = 16.dp)
            )
            
            // Календарь
            CalendarBar(
                selectedDate = selectedDate,
                onDateSelected = { date, dayIndex ->
                    selectedDate = date
                    selectedDayOfWeek = dayIndex
                }
            )
            
            // Расписание на выбранный день
            if (filteredLessons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "На этот день занятий нет",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLessons) { lesson ->
                        LessonCard(lesson = lesson)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarBar(
    selectedDate: Long,
    onDateSelected: (Long, Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    
    // Выбрать начало текущей недели (понедельник)
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2
    calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract)
    
    // Создаем список дней текущей недели
    val weekDays = List(7) { index ->
        val c = Calendar.getInstance()
        c.timeInMillis = calendar.timeInMillis
        c.add(Calendar.DAY_OF_MONTH, index)
        c.timeInMillis
    }
    
    // Дни недели
    val dayNames = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Месяц и год
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                val newWeekDays = List(7) { index ->
                    val c = Calendar.getInstance()
                    c.timeInMillis = calendar.timeInMillis
                    c.add(Calendar.DAY_OF_MONTH, index)
                    c.timeInMillis
                }
                onDateSelected(newWeekDays[0], 0)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Предыдущая неделя"
                )
            }
            
            Text(
                text = monthYearFormat.format(Date(selectedDate)),
                style = MaterialTheme.typography.titleMedium
            )
            
            IconButton(onClick = {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val newWeekDays = List(7) { index ->
                    val c = Calendar.getInstance()
                    c.timeInMillis = calendar.timeInMillis
                    c.add(Calendar.DAY_OF_MONTH, index)
                    c.timeInMillis
                }
                onDateSelected(newWeekDays[0], 0)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Следующая неделя"
                )
            }
        }
        
        // Дни недели
        ScrollableTabRow(
            selectedTabIndex = Calendar.getInstance().let { c ->
                c.timeInMillis = selectedDate
                val day = c.get(Calendar.DAY_OF_WEEK)
                if (day == Calendar.SUNDAY) 6 else day - 2
            },
            edgePadding = 16.dp,
            divider = {}
        ) {
            weekDays.forEachIndexed { index, date ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = date
                
                val isSelected = selectedDate == date
                val isToday = cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
                              cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                
                Tab(
                    selected = isSelected,
                    onClick = { onDateSelected(date, index) },
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = dayNames[index],
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    border = BorderStroke(
                                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateFormat.format(Date(date)),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun LessonCard(lesson: LessonScheduleItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = when (lesson.type) {
            "Лекция" -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
            "Практика" -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            )
            else -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lesson.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = lesson.type,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = lesson.time,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Аудитория ${lesson.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = lesson.teacher,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}