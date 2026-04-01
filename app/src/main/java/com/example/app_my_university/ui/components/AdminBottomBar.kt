package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.app_my_university.ui.navigation.Screen

data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val structureRoutes = setOf(
    Screen.AdminStructure.route,
    Screen.AdminUniversities.route,
    Screen.AdminGroups.route,
    Screen.AdminSubjects.route,
    Screen.AdminClassrooms.route,
    Screen.AdminTeacherSubjects.route
)

private val moreRoutes = setOf(
    Screen.AdminMore.route,
    Screen.Dialogs.route,
    Screen.Profile.route,
    Screen.AdminUsers.route,
    Screen.AdminAudit.route,
    Screen.AdminStatistics.route,
    Screen.ChatContacts.route,
    Screen.StudentPerformance.route
)

private val adminNavItems = listOf(
    AdminNavItem("Главная", Icons.Default.Dashboard, Screen.AdminHome.route),
    AdminNavItem("Заявки", Icons.Default.HowToReg, Screen.AdminRequests.route),
    AdminNavItem("Структура", Icons.Default.AccountTree, Screen.AdminStructure.route),
    AdminNavItem("Расписание", Icons.Default.CalendarMonth, Screen.AdminSchedule.route),
    AdminNavItem("Ещё", Icons.Default.MoreHoriz, Screen.AdminMore.route)
)

private fun isRouteSelected(item: AdminNavItem, currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    if (currentRoute.startsWith("chat/")) return item.route == Screen.AdminMore.route
    return when (item.route) {
        Screen.AdminStructure.route -> currentRoute in structureRoutes
        Screen.AdminMore.route -> currentRoute in moreRoutes
        else -> currentRoute == item.route
    }
}

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        adminNavItems.forEach { item ->
            NavigationBarItem(
                selected = isRouteSelected(item, currentRoute),
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true
            )
        }
    }
}
