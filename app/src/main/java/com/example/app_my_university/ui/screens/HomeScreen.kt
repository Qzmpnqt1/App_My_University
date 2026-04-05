package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.ui.components.StudentBottomBar
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.analytics.MuAnalyticsCard
import com.example.app_my_university.ui.components.analytics.MuDonutChart
import com.example.app_my_university.ui.components.analytics.MuVerticalBarChart
import com.example.app_my_university.ui.components.analytics.creditBreakdown
import com.example.app_my_university.ui.components.analytics.creditDonutSegments
import com.example.app_my_university.ui.components.analytics.examGradeBarEntries
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.common.MuSectionHeader
import com.example.app_my_university.ui.components.common.MuStatBlock
import com.example.app_my_university.ui.components.schedule.ScheduleLessonCard
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.HomeDashboardTime
import com.example.app_my_university.ui.viewmodel.HomeDashboardViewModel
import com.example.app_my_university.ui.viewmodel.ProfileViewModel
import com.example.app_my_university.ui.components.analytics.examAverage
import com.example.app_my_university.ui.components.analytics.pendingFinalCount
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import java.time.LocalTime

private val dayShort = mapOf(
    1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт",
    5 to "Пт", 6 to "Сб", 7 to "Вс"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    dashboardViewModel: HomeDashboardViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val dash by dashboardViewModel.uiState.collectAsState()
    val currentRoute = navController.currentDestination?.route

    LaunchedEffect(Unit) {
        dashboardViewModel.load(includeGrades = true, includeStudentPerformance = true)
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(title = "Мой ВУЗ", subtitle = "Студент")
        },
        bottomBar = {
            StudentBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.StudentHome.route) { saveState = true }
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
                        message = dash.error ?: "Не удалось загрузить данные",
                        onRetry = { dashboardViewModel.retry() }
                    )
                }
            }
            else -> {
                StudentDashboardBody(
                    padding = padding,
                    profileName = profileState.profile?.let { "${it.firstName} ${it.lastName}" } ?: "Студент",
                    groupLabel = profileState.profile?.studentProfile?.groupName,
                    dash = dash,
                    onWeek = { dashboardViewModel.setWeek(it) },
                    onOpenSchedule = { navController.navigate(Screen.Schedule.route) },
                    onOpenGradeBook = { navController.navigate(Screen.GradeBook.route) },
                    onOpenPerformance = { navController.navigate(Screen.StudentPerformance.route) },
                    onOpenMessages = { navController.navigate(Screen.Dialogs.route) },
                    scheduleByDay = dash.scheduleByDay
                )
            }
        }
    }
}

@Composable
private fun StudentDashboardBody(
    padding: PaddingValues,
    profileName: String,
    groupLabel: String?,
    dash: com.example.app_my_university.ui.viewmodel.HomeDashboardUiState,
    onWeek: (Int) -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenGradeBook: () -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenMessages: () -> Unit,
    scheduleByDay: Map<Int, List<ScheduleResponse>>
) {
    val next = HomeDashboardTime.nextLessonToday(scheduleByDay)
    val todayList = HomeDashboardTime.todayLessons(scheduleByDay)
    val perf = dash.studentPerformanceSummary
    val avg = studentDashboardAverage(perf, dash.grades)
    val disciplinesCount = studentDashboardDisciplinesLabel(perf, dash.grades.size)
    val pending = studentDashboardPendingLabel(perf, dash.grades)
    val homeExamBars = remember(dash.grades) { examGradeBarEntries(dash.grades) }
    val homeCreditBreakdown = remember(dash.grades) { creditBreakdown(dash.grades) }
    val scheduleDayBars = remember(scheduleByDay) {
        (1..7).map { dow -> dayShort[dow]!! to (scheduleByDay[dow]?.size ?: 0).toFloat() }
    }

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
            WelcomeHero(
                userName = profileName,
                groupName = groupLabel
            )
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
            WeekStrip(scheduleByDay = scheduleByDay)
        }

        if (scheduleDayBars.any { it.second > 0f }) {
            item {
                MuAnalyticsCard(
                    title = "Нагрузка по дням",
                    subtitle = "Количество пар на выбранной неделе"
                ) {
                    MuVerticalBarChart(
                        entries = scheduleDayBars,
                        chartHeight = 88.dp,
                        valueFormatter = { it.toInt().toString() },
                        maxValueOverride = scheduleDayBars.maxOf { it.second }.coerceAtLeast(1f)
                    )
                }
            }
        }

        item {
            MuSectionHeader(
                title = "Ближайшее занятие",
                subtitle = if (todayList.isEmpty()) "Сегодня пар нет" else "Сегодня ${todayList.size} ${lessonWord(todayList.size)}",
                actionLabel = "Всё расписание",
                onActionClick = onOpenSchedule
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
                        text = "Свободный день или занятия на другой неделе. Откройте полное расписание.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimens.spaceM)
                    )
                }
            } else {
                val done = todayList.filter { lesson ->
                    val end = HomeDashboardTime.parseStart(lesson.endTime)
                    end != null && end.isBefore(LocalTime.now())
                }.size
                Text(
                    text = if (done >= todayList.size) {
                        "На сегодня пары завершены."
                    } else {
                        "Следующие пары смотрите ниже."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            MuSectionHeader(
                title = "Сегодня",
                actionLabel = "Расписание",
                onActionClick = onOpenSchedule
            )
        }

        items(
            items = todayList.take(4),
            key = { it.id }
        ) { lesson ->
            ScheduleLessonCard(entry = lesson)
        }

        item {
            MuSectionHeader(
                title = "Успеваемость",
                subtitle = if (perf != null) {
                    "По учебному плану (как на экране «Успеваемость»); графики ниже — по строкам зачётки"
                } else {
                    "Сводка по зачётной книжке"
                },
                actionLabel = "Зачётная книжка",
                onActionClick = onOpenGradeBook
            )
        }

        dash.studentPerformanceError?.let { err ->
            item {
                Text(
                    text = "Сводка с сервера недоступна: $err",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = Dimens.spaceXS)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)
            ) {
                MuStatBlock(
                    value = avg,
                    label = "Средний балл",
                    modifier = Modifier.weight(1f)
                )
                MuStatBlock(
                    value = disciplinesCount,
                    label = if (perf != null) "В плане" else "Дисциплин",
                    modifier = Modifier.weight(1f)
                )
                MuStatBlock(
                    value = pending,
                    label = "Без итога",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (homeExamBars.any { it.second > 0f }) {
            item {
                MuAnalyticsCard(
                    title = "Оценки по экзаменам",
                    subtitle = "Компактно по зачётной книжке"
                ) {
                    MuVerticalBarChart(
                        entries = homeExamBars,
                        chartHeight = 100.dp,
                        maxValueOverride = homeExamBars.maxOf { it.second }.coerceAtLeast(1f)
                    )
                }
            }
        }

        if (homeCreditBreakdown.passed + homeCreditBreakdown.failed + homeCreditBreakdown.pending > 0) {
            item {
                MuAnalyticsCard(
                    title = "Зачёты",
                    subtitle = "Доля закрытых зачётов"
                ) {
                    MuDonutChart(
                        segments = creditDonutSegments(homeCreditBreakdown, MaterialTheme.colorScheme),
                        donutSize = 108.dp,
                        strokeWidth = 14.dp
                    )
                }
            }
        }

        item {
            OutlinedCard(
                onClick = onOpenPerformance,
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
                        Icons.AutoMirrored.Filled.TrendingUp,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconM)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Статистика успеваемости",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Графики и детализация по практикам",
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
                subtitle = "Чаты с преподавателями",
                actionLabel = "Открыть",
                onActionClick = onOpenMessages
            )
        }

        item {
            OutlinedCard(
                onClick = onOpenMessages,
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
                            "Диалоги",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Переписка по учебным вопросам",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            MuSectionHeader(title = "Быстрый доступ")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)
            ) {
                QuickTile(
                    icon = Icons.Default.CalendarMonth,
                    label = "Расписание",
                    onClick = onOpenSchedule,
                    modifier = Modifier.weight(1f)
                )
                QuickTile(
                    icon = Icons.Default.Grade,
                    label = "Оценки",
                    onClick = onOpenGradeBook,
                    modifier = Modifier.weight(1f)
                )
                QuickTile(
                    icon = Icons.Default.School,
                    label = "Успеваемость",
                    onClick = onOpenPerformance,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WelcomeHero(
    userName: String,
    groupName: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.spaceM))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(Dimens.spaceL)
    ) {
        Column {
            Text(
                text = "Здравствуйте,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            groupName?.let {
                Spacer(Modifier.height(Dimens.spaceXS))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun WeekStrip(scheduleByDay: Map<Int, List<ScheduleResponse>>) {
    val today = HomeDashboardTime.todayIsoDayOfWeek()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)
    ) {
        items((1..7).toList()) { d ->
            val has = scheduleByDay[d].orEmpty().isNotEmpty()
            val label = dayShort[d] ?: "$d"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (d == today) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
            ) {
                Text(
                    text = if (has) "$label ·" else label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (d == today) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spaceM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceXS)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun studentDashboardAverage(
    perf: StudentPerformanceSummaryResponse?,
    grades: List<GradeResponse>,
): String {
    perf?.averageNumericGrade?.let { return String.format("%.1f", it) }
    return examAverage(grades)?.let { String.format("%.1f", it) } ?: "—"
}

private fun studentDashboardDisciplinesLabel(
    perf: StudentPerformanceSummaryResponse?,
    gradeRowCount: Int,
): String = perf?.plannedSubjects?.toString() ?: "$gradeRowCount"

private fun studentDashboardPendingLabel(
    perf: StudentPerformanceSummaryResponse?,
    grades: List<GradeResponse>,
): String {
    perf?.let {
        return (it.plannedSubjects - it.subjectsWithFinalResult).coerceAtLeast(0).toString()
    }
    return pendingFinalCount(grades).toString()
}

private fun lessonWord(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "занятие"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "занятия"
    else -> "занятий"
}
