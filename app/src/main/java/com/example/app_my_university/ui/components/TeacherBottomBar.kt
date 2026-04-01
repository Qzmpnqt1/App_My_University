package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.app_my_university.ui.navigation.Screen

data class TeacherNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val teacherNavItems = listOf(
    TeacherNavItem("Расписание", Icons.Default.CalendarMonth, Screen.Schedule.route),
    TeacherNavItem("Оценки", Icons.Default.Grade, Screen.TeacherGrades.route),
    TeacherNavItem("Сообщения", Icons.AutoMirrored.Filled.Chat, Screen.Dialogs.route),
    TeacherNavItem("Профиль", Icons.Default.Person, Screen.Profile.route)
)

@Composable
fun TeacherBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        teacherNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = null
            )
        }
    }
}
