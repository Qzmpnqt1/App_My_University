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

data class StudentNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val studentNavItems = listOf(
    StudentNavItem("Расписание", Icons.Default.CalendarMonth, Screen.Schedule.route),
    StudentNavItem("Оценки", Icons.Default.Grade, Screen.GradeBook.route),
    StudentNavItem("Сообщения", Icons.AutoMirrored.Filled.Chat, Screen.Dialogs.route),
    StudentNavItem("Профиль", Icons.Default.Person, Screen.Profile.route)
)

@Composable
fun StudentBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        studentNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = null
            )
        }
    }
}
