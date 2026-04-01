package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.components.AdminBottomBar
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.common.MuStatBlock
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val adminState by adminViewModel.uiState.collectAsState()
    val currentRoute = navController.currentDestination?.route
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        adminViewModel.loadAdminContext()
        adminViewModel.loadMyUniversityAndInstitutes()
        adminViewModel.refreshDashboardBadges()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                adminViewModel.refreshDashboardBadges()
                adminViewModel.loadMyUniversityAndInstitutes()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(Screen.AdminHome.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val pending = adminState.pendingRegistrationCount
    val unread = adminState.unreadMessagesCount
    val uniName = adminState.adminUniversityName ?: "Ваш вуз"
    val instituteCount = adminState.institutes.size

    Scaffold(
        bottomBar = {
            AdminBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.AdminHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
            UniformTopAppBar(title = "Администрирование")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = uniName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Панель управления данными вашего вуза",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MuStatBlock(
                            value = pending.toString(),
                            label = "Заявок на рассмотрение",
                            modifier = Modifier.weight(1f)
                        )
                        MuStatBlock(
                            value = unread.toString(),
                            label = "Непрочитанных в чатах",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navigateTo(Screen.AdminRequests.route) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Заявки на регистрацию",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (pending > 0) {
                                        "$pending требуют решения"
                                    } else {
                                        "Новых заявок нет"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (pending > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge { Text(pending.toString()) }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.HowToReg,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Быстрые действия",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = { navigateTo(Screen.AdminRequests.route) }) {
                            Text("Заявки")
                        }
                        FilledTonalButton(onClick = { navigateTo(Screen.AdminStructure.route) }) {
                            Text("Структура")
                        }
                        FilledTonalButton(onClick = { navigateTo(Screen.AdminSchedule.route) }) {
                            Text("Расписание")
                        }
                        FilledTonalButton(onClick = { navigateTo(Screen.AdminClassrooms.route) }) {
                            Text("Аудитории")
                        }
                        FilledTonalButton(onClick = { navigateTo(Screen.AdminTeacherSubjects.route) }) {
                            Text("Преподаватели")
                        }
                        FilledTonalButton(onClick = { navigateTo(Screen.Dialogs.route) }) {
                            Text("Сообщения")
                        }
                    }
                }

                item {
                    Text(
                        text = "Состояние данных",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Институты",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$instituteCount в структуре вуза",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { navigateTo(Screen.AdminStructure.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Учебная структура")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Разделы",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    AdminHomeLinkRow(
                        title = "Расписание занятий",
                        subtitle = "Создание и редактирование записей",
                        icon = Icons.Default.CalendarMonth,
                        onClick = { navigateTo(Screen.AdminSchedule.route) }
                    )
                }
                item {
                    AdminHomeLinkRow(
                        title = "Аудитории",
                        subtitle = "Корпуса, номера, вместимость",
                        icon = Icons.Default.MeetingRoom,
                        onClick = { navigateTo(Screen.AdminClassrooms.route) }
                    )
                }
                item {
                    AdminHomeLinkRow(
                        title = "Преподаватели и дисциплины",
                        subtitle = "Назначение ведения предметов",
                        icon = Icons.Default.PersonAdd,
                        onClick = { navigateTo(Screen.AdminTeacherSubjects.route) }
                    )
                }
                item {
                    AdminHomeLinkRow(
                        title = "Группы и дисциплины",
                        subtitle = "Через раздел «Структура»",
                        icon = Icons.Default.Group,
                        onClick = { navigateTo(Screen.AdminStructure.route) }
                    )
                }
                item {
                    AdminHomeLinkRow(
                        title = "Справочник дисциплин",
                        subtitle = "Предметы и связи с направлениями",
                        icon = Icons.Default.Subject,
                        onClick = {
                            navController.navigate(Screen.AdminSubjects.route) {
                                popUpTo(Screen.AdminHome.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                item {
                    AdminHomeLinkRow(
                        title = "Сообщения и прочее",
                        subtitle = "Пользователи, аналитика, профиль",
                        icon = Icons.Outlined.Message,
                        onClick = { navigateTo(Screen.AdminMore.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHomeLinkRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
