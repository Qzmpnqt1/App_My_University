package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.ui.components.TeacherBottomBar
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.common.MuSectionHeader
import com.example.app_my_university.ui.components.schedule.ScheduleLessonCard
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.HomeDashboardTime
import com.example.app_my_university.ui.viewmodel.HomeDashboardViewModel
import com.example.app_my_university.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    dashboardViewModel: HomeDashboardViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val dash by dashboardViewModel.uiState.collectAsState()
    val currentRoute = navController.currentDestination?.route

    LaunchedEffect(Unit) {
        dashboardViewModel.load(includeGrades = false)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Мой ВУЗ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Преподаватель",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            TeacherBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.TeacherHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        when {
            dash.isLoading && dash.scheduleByDay.isEmpty() -> {
                MuLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            dash.error != null && dash.scheduleByDay.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    MuErrorState(
                        message = dash.error ?: "Не удалось загрузить расписание",
                        onRetry = { dashboardViewModel.retry() }
                    )
                }
            }
            else -> {
                TeacherDashboardBody(
                    padding = padding,
                    userName = profileState.profile?.let { "${it.firstName} ${it.lastName}" }
                        ?: "Преподаватель",
                    institute = profileState.profile?.teacherProfile?.instituteName,
                    dash = dash,
                    scheduleByDay = dash.scheduleByDay,
                    onWeek = { dashboardViewModel.setWeek(it) },
                    onSchedule = { navController.navigate(Screen.Schedule.route) },
                    onGrades = { navController.navigate(Screen.TeacherGrades.route) },
                    onChats = { navController.navigate(Screen.Dialogs.route) }
                )
            }
        }
    }
}

@Composable
private fun TeacherDashboardBody(
    padding: PaddingValues,
    userName: String,
    institute: String?,
    dash: com.example.app_my_university.ui.viewmodel.HomeDashboardUiState,
    scheduleByDay: Map<Int, List<ScheduleResponse>>,
    onWeek: (Int) -> Unit,
    onSchedule: () -> Unit,
    onGrades: () -> Unit,
    onChats: () -> Unit
) {
    val next = HomeDashboardTime.nextLessonToday(scheduleByDay)
    val todayList = HomeDashboardTime.todayLessons(scheduleByDay)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            end = Dimens.screenPadding,
            bottom = Dimens.spaceXL
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.spaceM))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(Dimens.spaceL)
            ) {
                Column {
                    Text(
                        "Рабочий день",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Text(
                        userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    institute?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)
            ) {
                FilterChip(
                    selected = dash.currentWeek == 1,
                    onClick = { onWeek(1) },
                    label = { Text("Неделя 1") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = dash.currentWeek == 2,
                    onClick = { onWeek(2) },
                    label = { Text("Неделя 2") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            MuSectionHeader(
                title = "Ближайшая пара",
                subtitle = "Сегодня: ${todayList.size} ${teacherLessonWord(todayList.size)}",
                actionLabel = "Всё расписание",
                onActionClick = onSchedule
            )
        }

        item {
            if (next != null) {
                ScheduleLessonCard(entry = next, emphasize = true)
            } else if (todayList.isEmpty()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        "Сегодня занятий нет или они на другой учебной неделе.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimens.spaceM)
                    )
                }
            } else {
                Text(
                    "Ближайшие пары — ниже в списке.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            MuSectionHeader(
                title = "Сегодня",
                actionLabel = "Расписание",
                onActionClick = onSchedule
            )
        }

        items(
            items = todayList.take(5),
            key = { it.id }
        ) { lesson ->
            ScheduleLessonCard(entry = lesson)
        }

        item {
            MuSectionHeader(
                title = "Оценивание",
                subtitle = "Итоги и практики по группам",
                actionLabel = "Открыть",
                onActionClick = onGrades
            )
        }

        item {
            OutlinedCard(
                onClick = onGrades,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM)
                ) {
                    Icon(
                        Icons.Default.Grade,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconM)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Выставление оценок",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Итоговые оценки и практики",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            MuSectionHeader(
                title = "Сообщения",
                actionLabel = "Чаты",
                onActionClick = onChats
            )
        }

        item {
            OutlinedCard(
                onClick = onChats,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(Dimens.iconM)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Диалоги со студентами",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Быстрый доступ к переписке",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)
            ) {
                Card(
                    onClick = onSchedule,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spaceM),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Расписание", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Card(
                    onClick = onGrades,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spaceM),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Grade, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Оценки", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun teacherLessonWord(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "занятие"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "занятия"
    else -> "занятий"
}
