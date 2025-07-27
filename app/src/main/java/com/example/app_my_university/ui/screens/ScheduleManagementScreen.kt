package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.model.LessonType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Модель данных для расписания
data class ScheduleItem(
    val id: String,
    val subjectName: String,
    val teacherName: String,
    val groupName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val classroom: String,
    val lessonType: LessonType
)

enum class ScheduleFilter {
    GROUP,
    TEACHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementScreen(
    onNavigateBack: () -> Unit
) {
    // Состояние для расписания (в реальном приложении будет загружаться из базы данных)
    val scheduleItems = remember {
        mutableStateListOf(
            ScheduleItem(
                id = "1",
                subjectName = "Программирование",
                teacherName = "Иванов И.И.",
                groupName = "ИКБО-01-22",
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 30),
                classroom = "А-100",
                lessonType = LessonType.LECTURE
            ),
            ScheduleItem(
                id = "2",
                subjectName = "Математический анализ",
                teacherName = "Петров П.П.",
                groupName = "ИКБО-01-22",
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(10, 40),
                endTime = LocalTime.of(12, 10),
                classroom = "Б-200",
                lessonType = LessonType.LECTURE
            ),
            ScheduleItem(
                id = "3",
                subjectName = "Программирование",
                teacherName = "Сидоров С.С.",
                groupName = "ИКБО-01-22",
                dayOfWeek = DayOfWeek.TUESDAY,
                startTime = LocalTime.of(13, 0),
                endTime = LocalTime.of(14, 30),
                classroom = "В-300",
                lessonType = LessonType.LABORATORY
            ),
            ScheduleItem(
                id = "4",
                subjectName = "Дискретная математика",
                teacherName = "Козлова К.К.",
                groupName = "ИКБО-02-22",
                dayOfWeek = DayOfWeek.WEDNESDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 30),
                classroom = "Г-400",
                lessonType = LessonType.SEMINAR
            ),
            ScheduleItem(
                id = "5",
                subjectName = "Физика",
                teacherName = "Иванов И.И.",
                groupName = "ИКБО-03-22",
                dayOfWeek = DayOfWeek.THURSDAY,
                startTime = LocalTime.of(15, 0),
                endTime = LocalTime.of(16, 30),
                classroom = "Д-500",
                lessonType = LessonType.PRACTICE
            )
        )
    }
    
    // Состояние для фильтра
    var selectedFilter by remember { mutableStateOf(ScheduleFilter.GROUP) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDayIndex by remember { mutableStateOf(0) }
    
    // Состояние для выпадающих списков
    var showGroupDropdown by remember { mutableStateOf(false) }
    var showTeacherDropdown by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf("ИКБО-01-22") }
    var selectedTeacher by remember { mutableStateOf("Иванов И.И.") }
    
    // Состояние для диалогов
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedScheduleItem by remember { mutableStateOf<ScheduleItem?>(null) }
    
    // Получение уникальных групп и преподавателей
    val groups = scheduleItems.map { it.groupName }.distinct().sorted()
    val teachers = scheduleItems.map { it.teacherName }.distinct().sorted()
    
    // Дни недели
    val daysOfWeek = DayOfWeek.values().map { 
        it.getDisplayName(TextStyle.FULL, Locale("ru")) 
    }
    
    // Фильтрация расписания
    val filteredScheduleItems = scheduleItems.filter {
        (selectedFilter == ScheduleFilter.GROUP && it.groupName == selectedGroup ||
        selectedFilter == ScheduleFilter.TEACHER && it.teacherName == selectedTeacher) &&
        it.dayOfWeek == DayOfWeek.values()[selectedDayIndex] &&
        (searchQuery.isEmpty() || 
         it.subjectName.contains(searchQuery, ignoreCase = true) ||
         it.classroom.contains(searchQuery, ignoreCase = true))
    }.sortedBy { it.startTime }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Топ-бар с учетом системных инсетов
            TopAppBar(
                title = { Text("Управление расписанием") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(48.dp)
            )
            
            // Content here...
        }
        
        // FloatingActionButton if needed
        FloatingActionButton(
            onClick = { 
                selectedScheduleItem = null
                showAddEditDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Add, 
                contentDescription = "Добавить занятие",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Dialogs here...
    }
}

@Composable
fun ScheduleItemCard(
    scheduleItem: ScheduleItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Время занятия
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = scheduleItem.startTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = scheduleItem.endTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Вертикальная линия
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(60.dp)
                        .background(
                            color = when (scheduleItem.lessonType) {
                                LessonType.LECTURE -> MaterialTheme.colorScheme.primary
                                LessonType.SEMINAR -> MaterialTheme.colorScheme.secondary
                                LessonType.LABORATORY -> MaterialTheme.colorScheme.tertiary
                                LessonType.PRACTICE -> MaterialTheme.colorScheme.error
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Информация о занятии
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = scheduleItem.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = when (scheduleItem.lessonType) {
                            LessonType.LECTURE -> "Лекция"
                            LessonType.SEMINAR -> "Семинар"
                            LessonType.LABORATORY -> "Лабораторная"
                            LessonType.PRACTICE -> "Практика"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Преподаватель: ${scheduleItem.teacherName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Группа: ${scheduleItem.groupName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Аудитория: ${scheduleItem.classroom}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Кнопки действий
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItemDialog(
    scheduleItem: ScheduleItem?,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit
) {
    val isEditing = scheduleItem != null
    val title = if (isEditing) "Редактирование занятия" else "Добавление занятия"
    
    // Состояние полей формы
    var subjectName by remember { mutableStateOf(scheduleItem?.subjectName ?: "") }
    var teacherName by remember { mutableStateOf(scheduleItem?.teacherName ?: "") }
    var groupName by remember { mutableStateOf(scheduleItem?.groupName ?: "") }
    var dayOfWeek by remember { mutableStateOf(scheduleItem?.dayOfWeek ?: DayOfWeek.MONDAY) }
    var startHour by remember { mutableStateOf(scheduleItem?.startTime?.hour ?: 9) }
    var startMinute by remember { mutableStateOf(scheduleItem?.startTime?.minute ?: 0) }
    var endHour by remember { mutableStateOf(scheduleItem?.endTime?.hour ?: 10) }
    var endMinute by remember { mutableStateOf(scheduleItem?.endTime?.minute ?: 30) }
    var classroom by remember { mutableStateOf(scheduleItem?.classroom ?: "") }
    var lessonType by remember { mutableStateOf(scheduleItem?.lessonType ?: LessonType.LECTURE) }
    
    // Состояние для выпадающих списков
    var showDayDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Название предмета") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Преподаватель") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Группа") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                // Выбор дня недели
                OutlinedTextField(
                    value = dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru")).capitalize(),
                    onValueChange = { },
                    label = { Text("День недели") },
                    trailingIcon = {
                        IconButton(onClick = { showDayDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать день")
                        }
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                DropdownMenu(
                    expanded = showDayDropdown,
                    onDismissRequest = { showDayDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DayOfWeek.values().forEach { day ->
                        DropdownMenuItem(
                            text = { Text(day.getDisplayName(TextStyle.FULL, Locale("ru")).capitalize()) },
                            onClick = {
                                dayOfWeek = day
                                showDayDropdown = false
                            }
                        )
                    }
                }
                
                // Время начала и окончания
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = String.format("%02d:%02d", startHour, startMinute),
                        onValueChange = { },
                        label = { Text("Начало") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = String.format("%02d:%02d", endHour, endMinute),
                        onValueChange = { },
                        label = { Text("Окончание") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("Аудитория") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                // Выбор типа занятия
                OutlinedTextField(
                    value = when (lessonType) {
                        LessonType.LECTURE -> "Лекция"
                        LessonType.SEMINAR -> "Семинар"
                        LessonType.LABORATORY -> "Лабораторная"
                        LessonType.PRACTICE -> "Практика"
                    },
                    onValueChange = { },
                    label = { Text("Тип занятия") },
                    trailingIcon = {
                        IconButton(onClick = { showTypeDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать тип")
                        }
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                DropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text("Лекция") },
                        onClick = {
                            lessonType = LessonType.LECTURE
                            showTypeDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Семинар") },
                        onClick = {
                            lessonType = LessonType.SEMINAR
                            showTypeDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Лабораторная") },
                        onClick = {
                            lessonType = LessonType.LABORATORY
                            showTypeDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Практика") },
                        onClick = {
                            lessonType = LessonType.PRACTICE
                            showTypeDropdown = false
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = ScheduleItem(
                        id = scheduleItem?.id ?: "0",
                        subjectName = subjectName,
                        teacherName = teacherName,
                        groupName = groupName,
                        dayOfWeek = dayOfWeek,
                        startTime = LocalTime.of(startHour, startMinute),
                        endTime = LocalTime.of(endHour, endMinute),
                        classroom = classroom,
                        lessonType = lessonType
                    )
                    onSave(newItem)
                },
                enabled = subjectName.isNotBlank() && teacherName.isNotBlank() && 
                        groupName.isNotBlank() && classroom.isNotBlank()
            ) {
                Text(if (isEditing) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}

// Расширение для капитализации первой буквы строки
fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
} 