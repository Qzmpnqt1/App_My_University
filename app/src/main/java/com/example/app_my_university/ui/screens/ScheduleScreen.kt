package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.core.logging.AppLogger
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.AppLayout
import com.example.app_my_university.ui.navigation.rememberAppRole
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.picker.MuSearchablePickerSheet
import com.example.app_my_university.ui.components.picker.PickerListItem
import com.example.app_my_university.ui.components.schedule.ScheduleCompareSegmentCard
import com.example.app_my_university.ui.components.schedule.ScheduleCompareSummaryCard
import com.example.app_my_university.ui.components.schedule.ScheduleLessonCard
import com.example.app_my_university.ui.viewmodel.ScheduleScreenMode
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
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val role = rememberAppRole()
    val uiState by viewModel.uiState.collectAsState()
    var showToolsSheet by remember { mutableStateOf(false) }
    var showGroupPick by remember { mutableStateOf(false) }
    var showTeacherPick by remember { mutableStateOf(false) }
    var showClassroomPick by remember { mutableStateOf(false) }
    var classroomPickForCompare by remember { mutableStateOf(true) }
    val toolsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        AppLogger.screen("Schedule")
        viewModel.setViewingGroup(null)
        viewModel.loadSchedule()
    }

    LaunchedEffect(showToolsSheet) {
        if (showToolsSheet) {
            AppLogger.userAction("ScheduleCompare", "open tools sheet")
            viewModel.loadComparePickerLists()
        }
    }

    val dayTabs = listOf<Pair<String, Int?>>( "Все" to null) + (1..7).map { (dayNames[it] ?: "$it") to it }

    val daySummaryLabel = when (val d = uiState.selectedDayOfWeek) {
        null -> "Все дни"
        else -> dayNamesLong[d] ?: "День $d"
    }

    val groupPickerItems = remember(uiState.pickGroups) {
        uiState.pickGroups.mapNotNull { g ->
            val id = g.id ?: return@mapNotNull null
            PickerListItem(
                id = id,
                primary = g.name.orEmpty(),
                secondary = listOfNotNull(g.directionName, g.instituteName).joinToString(" · "),
            )
        }
    }
    val teacherPickerItems = remember(uiState.pickTeachers) {
        uiState.pickTeachers.mapNotNull { t ->
            val id = t.userId ?: return@mapNotNull null
            PickerListItem(
                id = id,
                primary = t.displayName.orEmpty(),
                secondary = listOfNotNull(t.instituteName, t.position).joinToString(" · "),
            )
        }
    }
    val classroomPickerItems = remember(uiState.pickClassrooms) {
        uiState.pickClassrooms.mapNotNull { c ->
            val id = c.id ?: return@mapNotNull null
            PickerListItem(
                id = id,
                primary = c.label ?: "${c.building.orEmpty()}, ауд. ${c.roomNumber.orEmpty()}",
                secondary = c.capacity?.let { "Вместимость: $it" },
            )
        }
    }

    RoleShellScaffold(
        role = role,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = when (uiState.screenMode) {
                    ScheduleScreenMode.NORMAL -> "Расписание"
                    ScheduleScreenMode.COMPARE -> "Сравнение"
                    ScheduleScreenMode.CLASSROOM_ONLY -> "Аудитория"
                },
                subtitle = when {
                    uiState.screenMode == ScheduleScreenMode.CLASSROOM_ONLY -> uiState.classroomLabel
                    else -> null
                },
                onBackPressed = onNavigateBack,
                actions = {
                    when (uiState.screenMode) {
                        ScheduleScreenMode.NORMAL -> {
                            IconButton(
                                onClick = { showToolsSheet = true },
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = "Сравнение и аудитории",
                                    modifier = Modifier.size(AppLayout.barIconSize),
                                )
                            }
                        }
                        else -> {
                            TextButton(
                                onClick = {
                                    AppLogger.userAction("ScheduleCompare", "exit to normal")
                                    viewModel.exitCompareAndClassroom()
                                }
                            ) {
                                Text("К расписанию")
                            }
                        }
                    }
                }
            )
        },
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

            when (uiState.screenMode) {
                ScheduleScreenMode.NORMAL -> NormalScheduleBody(uiState, viewModel)
                ScheduleScreenMode.COMPARE -> CompareScheduleBody(
                    uiState = uiState,
                    daySummaryLabel = daySummaryLabel,
                    onRetry = { viewModel.retryCurrentAlternateLoad() },
                )
                ScheduleScreenMode.CLASSROOM_ONLY -> ClassroomOnlyBody(
                    uiState = uiState,
                    onRetry = { viewModel.retryCurrentAlternateLoad() },
                )
            }
        }
    }

    if (showToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToolsSheet = false },
            sheetState = toolsSheetState,
        ) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Сравнение и просмотр",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (uiState.pickListLoading) {
                    MuLoadingState(modifier = Modifier.fillMaxWidth().padding(32.dp))
                } else {
                    ListItem(
                        headlineContent = { Text("Сравнить с группой") },
                        supportingContent = { Text("Показать пересечения по времени") },
                        modifier = Modifier.clickable {
                            showToolsSheet = false
                            showGroupPick = true
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    )
                    ListItem(
                        headlineContent = { Text("Сравнить с преподавателем") },
                        supportingContent = { Text("Поиск по ФИО") },
                        modifier = Modifier.clickable {
                            showToolsSheet = false
                            showTeacherPick = true
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    )
                    ListItem(
                        headlineContent = { Text("Сравнить с аудиторией") },
                        modifier = Modifier.clickable {
                            classroomPickForCompare = true
                            showToolsSheet = false
                            showClassroomPick = true
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    )
                    ListItem(
                        headlineContent = { Text("Просмотреть расписание аудитории") },
                        supportingContent = { Text("Без сравнения") },
                        modifier = Modifier.clickable {
                            classroomPickForCompare = false
                            showToolsSheet = false
                            showClassroomPick = true
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    )
                }
            }
        }
    }

    MuSearchablePickerSheet(
        visible = showGroupPick,
        onDismiss = { showGroupPick = false },
        title = "Выберите группу",
        items = groupPickerItems,
        searchPlaceholder = "Название, направление…",
        onSelect = {
            AppLogger.userAction("ScheduleCompare", "picked group id=${it.id}")
            viewModel.runMyCompare("GROUP", it.id)
            showGroupPick = false
        },
    )
    MuSearchablePickerSheet(
        visible = showTeacherPick,
        onDismiss = { showTeacherPick = false },
        title = "Выберите преподавателя",
        items = teacherPickerItems,
        searchPlaceholder = "ФИО…",
        onSelect = {
            AppLogger.userAction("ScheduleCompare", "picked teacher id=${it.id}")
            viewModel.runMyCompare("TEACHER", it.id)
            showTeacherPick = false
        },
    )
    MuSearchablePickerSheet(
        visible = showClassroomPick,
        onDismiss = { showClassroomPick = false },
        title = if (classroomPickForCompare) "Аудитория для сравнения" else "Аудитория",
        items = classroomPickerItems,
        searchPlaceholder = "Корпус, номер…",
        onSelect = {
            AppLogger.userAction(
                "ScheduleClassroom",
                "picked classroom id=${it.id} compare=$classroomPickForCompare"
            )
            if (classroomPickForCompare) {
                viewModel.runMyCompare("CLASSROOM", it.id)
            } else {
                viewModel.openClassroomView(it.id, it.primary)
            }
            showClassroomPick = false
        },
    )
}

@Composable
private fun NormalScheduleBody(
    uiState: com.example.app_my_university.ui.viewmodel.ScheduleUiState,
    viewModel: ScheduleViewModel,
) {
    when {
        uiState.isLoading -> MuLoadingState(modifier = Modifier.fillMaxSize())
        uiState.error != null -> MuErrorState(
            message = uiState.error ?: "Ошибка загрузки",
            modifier = Modifier.fillMaxSize(),
            onRetry = { viewModel.loadSchedule() }
        )
        uiState.scheduleByDay.isEmpty() -> MuEmptyState(
            title = "Нет занятий",
            subtitle = "На выбранную неделю и день пары не запланированы. Смените фильтр или обновите позже.",
            modifier = Modifier.fillMaxSize(),
            actionLabel = "Обновить",
            onAction = { viewModel.loadSchedule() }
        )
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

@Composable
private fun CompareScheduleBody(
    uiState: com.example.app_my_university.ui.viewmodel.ScheduleUiState,
    daySummaryLabel: String,
    onRetry: () -> Unit,
) {
    when {
        uiState.compareLoading -> MuLoadingState(modifier = Modifier.fillMaxSize())
        uiState.compareError != null -> MuErrorState(
            message = uiState.compareError ?: "Ошибка",
            modifier = Modifier.fillMaxSize(),
            onRetry = onRetry,
        )
        uiState.compareResult == null -> MuEmptyState(
            title = "Нет данных",
            subtitle = "Выберите объект для сравнения через меню сверху.",
            modifier = Modifier.fillMaxSize(),
        )
        else -> {
            val result = uiState.compareResult!!
            val daysToShow = remember(result.days, uiState.selectedDayOfWeek) {
                val all = result.days.orEmpty()
                val sd = uiState.selectedDayOfWeek
                if (sd != null) all.filter { it.dayOfWeek == sd } else all
            }
            val leftTitle = result.leftLabel ?: "Слева"
            val rightTitle = result.rightLabel ?: "Справа"
            val hasSegments = daysToShow.any { !(it.segments.isNullOrEmpty()) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    ScheduleCompareSummaryCard(
                        leftLabel = result.leftLabel,
                        rightLabel = result.rightLabel,
                        weekNumber = result.weekNumber ?: uiState.currentWeek,
                        dayLabel = daySummaryLabel,
                        both = result.segmentsBothSidesBusy,
                        onlyLeft = result.segmentsOnlyLeft,
                        onlyRight = result.segmentsOnlyRight,
                    )
                }
                if (!hasSegments) {
                    item {
                        MuEmptyState(
                            title = "Нет занятий",
                            subtitle = "На выбранные фильтры нет сегментов для отображения.",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    daysToShow.forEach { day ->
                        val segs = day.segments.orEmpty()
                        if (segs.isEmpty()) return@forEach
                        item {
                            Text(
                                text = dayNamesLong[day.dayOfWeek ?: 0] ?: "День ${day.dayOfWeek}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        items(segs, key = { "${day.dayOfWeek}-${it.segmentStart}-${it.segmentEnd}-${it.segmentType}" }) { seg ->
                            ScheduleCompareSegmentCard(
                                segment = seg,
                                leftTitle = leftTitle,
                                rightTitle = rightTitle,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassroomOnlyBody(
    uiState: com.example.app_my_university.ui.viewmodel.ScheduleUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.classroomLoading -> MuLoadingState(modifier = Modifier.fillMaxSize())
        uiState.classroomError != null -> MuErrorState(
            message = uiState.classroomError ?: "Ошибка",
            modifier = Modifier.fillMaxSize(),
            onRetry = onRetry,
        )
        uiState.classroomByDay.isEmpty() -> MuEmptyState(
            title = "Нет занятий",
            subtitle = "В этой аудитории на выбранные фильтры пары не запланированы.",
            modifier = Modifier.fillMaxSize(),
            actionLabel = "Обновить",
            onAction = onRetry,
        )
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.classroomByDay.forEach { (day, entries) ->
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
