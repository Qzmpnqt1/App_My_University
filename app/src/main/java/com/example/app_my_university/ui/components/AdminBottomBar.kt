package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.app_my_university.ui.navigation.Screen

data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val adminNavItems = listOf(
    AdminNavItem("Главная", Icons.Default.Dashboard, Screen.AdminHome.route),
    AdminNavItem("Заявки", Icons.Default.HowToReg, Screen.AdminRequests.route),
    AdminNavItem("Структура", Icons.Default.AccountTree, Screen.AdminUniversities.route),
    AdminNavItem("Расписание", Icons.Default.CalendarMonth, Screen.AdminSchedule.route),
    AdminNavItem("Сообщения", Icons.AutoMirrored.Filled.Chat, Screen.Dialogs.route),
    AdminNavItem("Профиль", Icons.Default.Person, Screen.Profile.route)
)

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        adminNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = null
            )
        }
    }
}
