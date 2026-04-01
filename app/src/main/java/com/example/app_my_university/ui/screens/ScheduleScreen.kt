package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.ui.viewmodel.ScheduleViewModel

private val dayNames = mapOf(
    1 to "Пн",
    2 to "Вт",
    3 to "Ср",
    4 to "Чт",
    5 to "Пт",
    6 to "Сб",
    7 to "Вс"
)

private val dayNamesLong = mapOf(
    1 to "Понедельник",
    2 to "Вторник",
    3 to "Среда",
    4 to "Четверг",
    5 to "Пятница",
    6 to "Суббота",
    7 to "Воскресенье"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setViewingGroup(null)
        viewModel.loadSchedule()
    }

    val dayTabs = listOf<Pair<String, Int?>>( "Все" to null) + (1..7).map { (dayNames[it] ?: "$it") to it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = uiState.currentWeek == 1,
                    onClick = { viewModel.setWeek(1) },
                    label = { Text("Неделя 1") }
                )
                FilterChip(
                    selected = uiState.currentWeek == 2,
                    onClick = { viewModel.setWeek(2) },
                    label = { Text("Неделя 2") }
                )
            }

            ScrollableTabRow(
                selectedTabIndex = dayTabs.indexOfFirst { it.second == uiState.selectedDayOfWeek }.coerceAtLeast(0),
                edgePadding = 16.dp
            ) {
                dayTabs.forEach { (label, day) ->
                    Tab(
                        selected = uiState.selectedDayOfWeek == day,
                        onClick = { viewModel.setSelectedDayOfWeek(day) },
                        text = { Text(label) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "Ошибка загрузки",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadSchedule() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                uiState.scheduleByDay.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "На выбранный период занятий нет",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.scheduleByDay.forEach { (day, entries) ->
                            item {
                                Text(
                                    text = dayNamesLong[day] ?: "День $day",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(entries) { entry ->
                                ScheduleEntryCard(entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleEntryCard(entry: ScheduleResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.startTime} — ${entry.endTime}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                entry.lessonType?.let { type ->
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (type) {
                                    "LECTURE" -> "Лекция"
                                    "SEMINAR" -> "Семинар"
                                    "LABORATORY" -> "Лаб."
                                    "PRACTICE" -> "Практика"
                                    else -> type
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            Text(
                text = entry.subjectName ?: "Предмет не указан",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            entry.teacherName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            entry.groupName?.let {
                Text(
                    text = "Группа: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            entry.classroomInfo?.let {
                Text(
                    text = "Аудитория: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
