package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.schedule.ScheduleLessonCard
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
                    MuLoadingState(modifier = Modifier.fillMaxSize())
                }
                uiState.error != null -> {
                    MuErrorState(
                        message = uiState.error ?: "Ошибка загрузки",
                        modifier = Modifier.fillMaxSize(),
                        onRetry = { viewModel.loadSchedule() }
                    )
                }
                uiState.scheduleByDay.isEmpty() -> {
                    MuEmptyState(
                        title = "Нет занятий",
                        subtitle = "На выбранную неделю и день пары не запланированы. Смените фильтр или обновите позже.",
                        modifier = Modifier.fillMaxSize(),
                        actionLabel = "Обновить",
                        onAction = { viewModel.loadSchedule() }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.scheduleByDay.forEach { (day, entries) ->
                            item {
                                Text(
                                    text = dayNamesLong[day] ?: "День $day",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(entries, key = { it.id }) { entry ->
                                ScheduleLessonCard(entry = entry)
                            }
                        }
                    }
                }
            }
        }
    }
}
