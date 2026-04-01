package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.components.StudentBottomBar
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val currentRoute = navController.currentDestination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Главная") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val userName = profileState.profile?.let {
                "${it.firstName} ${it.lastName}"
            } ?: "..."

            Text(
                text = "Добро пожаловать,\n$userName!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            profileState.profile?.studentProfile?.let { sp ->
                sp.groupName?.let { groupName ->
                    Text(
                        text = "Группа: $groupName",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            QuickLinkCard(
                title = "Расписание",
                description = "Посмотреть расписание занятий",
                icon = Icons.Default.CalendarMonth,
                onClick = { navController.navigate(Screen.Schedule.route) }
            )

            QuickLinkCard(
                title = "Зачётная книжка",
                description = "Оценки и зачёты по предметам",
                icon = Icons.Default.Grade,
                onClick = { navController.navigate(Screen.GradeBook.route) }
            )

            QuickLinkCard(
                title = "Успеваемость",
                description = "Сводная статистика по дисциплинам и практикам",
                icon = Icons.Default.Assessment,
                onClick = { navController.navigate(Screen.StudentPerformance.route) }
            )

            QuickLinkCard(
                title = "Сообщения",
                description = "Диалоги с преподавателями",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = { navController.navigate(Screen.Dialogs.route) }
            )
        }
    }
}

@Composable
private fun QuickLinkCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
