package com.example.app_my_university.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Модель данных для занятия
data class Lesson(
    val id: String,
    val subject: String,
    val type: LessonType,
    val startTime: String,
    val endTime: String,
    val classRoom: String,
    val teacherName: String,
    val week: Int,
    val dayOfWeek: DayOfWeek,
    val groupName: String
)

// Тип занятия
enum class LessonType(val displayName: String, val color: Color) {
    LECTURE("Лекция", Color(0xFF2196F3)),
    SEMINAR("Семинар", Color(0xFF4CAF50)),
    LABORATORY("Лабораторная", Color(0xFFF44336)),
    PRACTICE("Практика", Color(0xFFFF9800))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleScreen(
    onNavigateToMessages: () -> Unit = {},
    onNavigateToGradeBook: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Текущая дата и день недели
    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM") }

    // Список дней недели для отображения
    val daysOfWeek = remember {
        DayOfWeek.values().map { it to it.getDisplayName(TextStyle.SHORT, Locale("ru")) }
    }

    // Текущая выбранная неделя: 1 или 2 (нечетная/четная)
    var selectedWeek by remember { mutableIntStateOf(if (today.dayOfYear % 2 == 0) 2 else 1) }

    // Текущий выбранный день недели
    var selectedDayOfWeek by remember { mutableStateOf(today.dayOfWeek) }

    // Для сравнения с другой группой/преподавателем
    var isComparingEnabled by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var comparisonType by remember { mutableStateOf("Группа") } // "Группа" или "Преподаватель"
    var isSearchMenuOpen by remember { mutableStateOf(false) }
    var selectedComparisonTarget by remember { mutableStateOf<String?>(null) }

    // Пример списка групп
    val groups = remember { listOf("БПИ21-01", "БПИ21-02", "ПИН23-01", "ИВТ22-01") }

    // Пример списка преподавателей
    val teachers = remember {
        listOf(
            "Иванов И.И.",
            "Петров П.П.",
            "Сидорова А.А.",
            "Кузнецов В.В.",
            "Смирнова Е.А."
        )
    }

    // Фильтрованный список в зависимости от критерия поиска и запроса
    val filteredSearchResults = remember(searchQuery, comparisonType) {
        when (comparisonType) {
            "Группа" -> groups.filter { it.contains(searchQuery, ignoreCase = true) }
            else -> teachers.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Пример занятий (в реальном приложении будут загружаться из базы данных)
    val lessons = remember {
        mutableStateListOf(
            Lesson(
                id = "1",
                subject = "Математический анализ",
                type = LessonType.LECTURE,
                startTime = "9:00",
                endTime = "10:30",
                classRoom = "А-100",
                teacherName = "Иванов И.И.",
                week = 1,
                dayOfWeek = DayOfWeek.MONDAY,
                groupName = "БПИ21-01"
            ),
            Lesson(
                id = "2",
                subject = "Программирование",
                type = LessonType.PRACTICE,
                startTime = "10:45",
                endTime = "12:15",
                classRoom = "Б-505",
                teacherName = "Петров П.П.",
                week = 1,
                dayOfWeek = DayOfWeek.MONDAY,
                groupName = "БПИ21-01"
            ),
            Lesson(
                id = "3",
                subject = "Базы данных",
                type = LessonType.LABORATORY,
                startTime = "13:00",
                endTime = "14:30",
                classRoom = "В-404",
                teacherName = "Сидорова А.А.",
                week = 1,
                dayOfWeek = DayOfWeek.TUESDAY,
                groupName = "БПИ21-01"
            ),
            Lesson(
                id = "4",
                subject = "Английский язык",
                type = LessonType.SEMINAR,
                startTime = "14:45",
                endTime = "16:15",
                classRoom = "Г-303",
                teacherName = "Смирнова Е.А.",
                week = 1,
                dayOfWeek = DayOfWeek.WEDNESDAY,
                groupName = "БПИ21-01"
            ),
            Lesson(
                id = "5",
                subject = "Физика",
                type = LessonType.LECTURE,
                startTime = "9:00",
                endTime = "10:30",
                classRoom = "А-101",
                teacherName = "Кузнецов В.В.",
                week = 2,
                dayOfWeek = DayOfWeek.MONDAY,
                groupName = "БПИ21-01"
            ),
            Lesson(
                id = "6",
                subject = "Математический анализ",
                type = LessonType.SEMINAR,
                startTime = "10:45",
                endTime = "12:15",
                classRoom = "Б-202",
                teacherName = "Иванов И.И.",
                week = 2,
                dayOfWeek = DayOfWeek.TUESDAY,
                groupName = "БПИ21-01"
            )
        )
    }

    // Пример занятий для сравнения (в реальном приложении будут загружаться динамически)
    val comparisonLessons = remember(selectedComparisonTarget) {
        if (selectedComparisonTarget == null) {
            emptyList()
        } else if (comparisonType == "Группа") {
            // Для примера, создаем другие занятия для выбранной группы
            listOf(
                Lesson(
                    id = "c1",
                    subject = "Дискретная математика",
                    type = LessonType.LECTURE,
                    startTime = "9:00",
                    endTime = "10:30",
                    classRoom = "Д-200",
                    teacherName = "Николаев Н.Н.",
                    week = selectedWeek,
                    dayOfWeek = selectedDayOfWeek,
                    groupName = selectedComparisonTarget!!
                ),
                Lesson(
                    id = "c2",
                    subject = "Физика",
                    type = LessonType.LABORATORY,
                    startTime = "10:45",
                    endTime = "12:15",
                    classRoom = "Е-101",
                    teacherName = "Кузнецов В.В.",
                    week = selectedWeek,
                    dayOfWeek = selectedDayOfWeek,
                    groupName = selectedComparisonTarget!!
                )
            )
        } else {
            // Для примера, создаем занятия для выбранного преподавателя
            listOf(
                Lesson(
                    id = "t1",
                    subject = "Математический анализ",
                    type = LessonType.LECTURE,
                    startTime = "13:00",
                    endTime = "14:30",
                    classRoom = "А-300",
                    teacherName = selectedComparisonTarget!!,
                    week = selectedWeek,
                    dayOfWeek = selectedDayOfWeek,
                    groupName = "ИВТ22-01"
                )
            )
        }
    }

    // Фильтрация занятий по выбранной неделе и дню
    val filteredLessons = lessons.filter {
        it.week == selectedWeek && it.dayOfWeek == selectedDayOfWeek && it.groupName == "БПИ21-01"
    }.sortedBy { it.startTime }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { isComparingEnabled = !isComparingEnabled }) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Сравнить расписание",
                            tint = if (isComparingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box {
                        IconButton(onClick = { isSearchMenuOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Меню"
                            )
                        }

                        DropdownMenu(
                            expanded = isSearchMenuOpen,
                            onDismissRequest = { isSearchMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Моя группа (БПИ21-01)") },
                                onClick = {
                                    selectedComparisonTarget = null
                                    isComparingEnabled = false
                                    isSearchMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Четная неделя") },
                                onClick = {
                                    selectedWeek = 2
                                    isSearchMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Нечетная неделя") },
                                onClick = {
                                    selectedWeek = 1
                                    isSearchMenuOpen = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Выбор недели
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedWeek == 1,
                    onClick = { selectedWeek = 1 },
                    label = { Text("Нечетная неделя") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = selectedWeek == 2,
                    onClick = { selectedWeek = 2 },
                    label = { Text("Четная неделя") }
                )
            }

            // Выбор дня недели
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(daysOfWeek) { (dayOfWeek, shortName) ->
                    val isSelectedDay = dayOfWeek == selectedDayOfWeek
                    val isToday = dayOfWeek == today.dayOfWeek

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { selectedDayOfWeek = dayOfWeek }
                    ) {
                        // Индикатор "сегодня"
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // День недели
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelectedDay) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        // Индикатор количества пар
                        val lessonsCount = lessons.count { it.dayOfWeek == dayOfWeek && it.week == selectedWeek }
                        if (lessonsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelectedDay) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lessonsCount.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            // Блок для сравнения расписаний
            AnimatedVisibility(
                visible = isComparingEnabled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    // Вкладки для переключения между группой и преподавателем
                    TabRow(
                        selectedTabIndex = if (comparisonType == "Группа") 0 else 1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = comparisonType == "Группа",
                            onClick = { comparisonType = "Группа" },
                            text = { Text("Группа") }
                        )
                        Tab(
                            selected = comparisonType == "Преподаватель",
                            onClick = { comparisonType = "Преподаватель" },
                            text = { Text("Преподаватель") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Поле поиска
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Искать ${if (comparisonType == "Группа") "группу" else "преподавателя"}") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск"
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Результаты поиска
                    if (searchQuery.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(filteredSearchResults) { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedComparisonTarget = result
                                            searchQuery = ""
                                        }
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (comparisonType == "Группа") Icons.Default.Groups else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(text = result)
                                }

                                Divider()
                            }
                        }
                    }

                    // Выбранная цель для сравнения
                    selectedComparisonTarget?.let { target ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Сравнение с: $target",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Разделитель
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Заголовок с датой
            Text(
                text = "Расписание на ${selectedDayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            // Список занятий для выбранного дня
            if (filteredLessons.isEmpty()) {
                // Если нет занятий
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет занятий",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Если есть занятия, отображаем их
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredLessons) { lesson ->
                        LessonCard(lesson)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Отображаем занятия для сравнения, если выбрана цель
                    if (selectedComparisonTarget != null && isComparingEnabled && comparisonLessons.isNotEmpty()) {
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "Расписание ${if (comparisonType == "Группа") "группы" else ""} $selectedComparisonTarget",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(comparisonLessons) { lesson ->
                            LessonCard(lesson)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Верхняя часть с временем и типом занятия
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Время
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${lesson.startTime} - ${lesson.endTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Тип занятия
                Box(
                    modifier = Modifier
                        .background(
                            color = lesson.type.color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = lesson.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = lesson.type.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Название предмета
            Text(
                text = lesson.subject,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Аудитория
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Аудитория: ${lesson.classRoom}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Преподаватель
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Преподаватель: ${lesson.teacherName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Группа (показываем, только если это не текущая группа)
            if (lesson.groupName != "БПИ21-01") {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Subject,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Группа: ${lesson.groupName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}