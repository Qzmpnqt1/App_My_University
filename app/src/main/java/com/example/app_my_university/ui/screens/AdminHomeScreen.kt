package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
data class AdminMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = "",
    val badge: Int? = null
)

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
        adminViewModel.refreshDashboardBadges()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                adminViewModel.refreshDashboardBadges()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pending = adminState.pendingRegistrationCount
    val unread = adminState.unreadMessagesCount

    val menuItems = listOf(
        AdminMenuItem(
            title = "ВУЗы",
            icon = Icons.Default.School,
            route = "admin_universities",
            description = "Университеты и структура"
        ),
        AdminMenuItem(
            title = "Заявки",
            icon = Icons.Default.HowToReg,
            route = "admin_requests",
            description = "Подтверждение регистраций",
            badge = pending.takeIf { it > 0 }
        ),
        AdminMenuItem(
            title = "Сообщения",
            icon = Icons.Outlined.Message,
            route = "dialogs",
            description = "Общение с пользователями",
            badge = unread.takeIf { it > 0 }
        ),
        AdminMenuItem(
            title = "Расписание",
            icon = Icons.Default.Schedule,
            route = "admin_schedule",
            description = "Управление расписанием"
        ),
        AdminMenuItem(
            title = "Предметы",
            icon = Icons.Default.Subject,
            route = "admin_subjects",
            description = "Справочник предметов"
        ),
        AdminMenuItem(
            title = "Группы",
            icon = Icons.Default.Group,
            route = "admin_groups",
            description = "Учебные группы"
        ),
        AdminMenuItem(
            title = "Пользователи",
            icon = Icons.Default.Person,
            route = "admin_users",
            description = "Аккаунты пользователей"
        ),
        AdminMenuItem(
            title = "Аудит",
            icon = Icons.Default.History,
            route = "admin_audit",
            description = "Журнал действий"
        ),
        AdminMenuItem(
            title = "Аналитика",
            icon = Icons.Default.Assessment,
            route = "admin_statistics",
            description = "Сводные показатели"
        ),
        AdminMenuItem(
            title = "Профиль",
            icon = Icons.Default.Settings,
            route = "profile",
            description = "Настройки аккаунта"
        )
    )

    fun navigateByKey(key: String) {
        val route = when (key) {
            "admin_universities" -> Screen.AdminUniversities.route
            "admin_requests" -> Screen.AdminRequests.route
            "admin_schedule" -> Screen.AdminSchedule.route
            "admin_subjects" -> Screen.AdminSubjects.route
            "admin_groups" -> Screen.AdminGroups.route
            "admin_users" -> Screen.AdminUsers.route
            "admin_audit" -> Screen.AdminAudit.route
            "admin_statistics" -> Screen.AdminStatistics.route
            "dialogs" -> Screen.Dialogs.route
            "profile" -> Screen.Profile.route
            else -> return
        }
        navController.navigate(route) {
            popUpTo(Screen.AdminHome.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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
            UniformTopAppBar(title = "Панель администратора")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MuStatBlock(
                    value = pending.toString(),
                    label = "Заявок на рассмотрение",
                    modifier = Modifier.weight(1f)
                )
                MuStatBlock(
                    value = unread.toString(),
                    label = "Непрочитанных сообщений",
                    modifier = Modifier.weight(1f)
                )
            }
            AdminDashboardContent(
                menuItems = menuItems,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                onItemClick = { navigateByKey(it) }
            )
        }
    }
}

@Composable
private fun AdminDashboardContent(
    menuItems: List<AdminMenuItem>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menuItems) { item ->
            AdminMenuCard(item = item, onClick = { onItemClick(item.route) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminMenuCard(
    item: AdminMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                item.badge?.let { badgeCount ->
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                    ) {
                        Text(badgeCount.toString())
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
