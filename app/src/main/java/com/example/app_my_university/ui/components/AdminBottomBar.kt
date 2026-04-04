package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import com.example.app_my_university.ui.navigation.Screen

private val structureRoutes = setOf(
    Screen.AdminStructure.route,
    Screen.AdminUniversities.route,
    Screen.AdminGroups.route,
    Screen.AdminSubjects.route,
    Screen.AdminClassrooms.route,
    Screen.AdminTeacherSubjects.route,
)

private val moreRoutes = setOf(
    Screen.AdminMore.route,
    Screen.Dialogs.route,
    Screen.Profile.route,
    Screen.AdminUsers.route,
    Screen.AdminAudit.route,
    Screen.AdminStatistics.route,
    Screen.ChatContacts.route,
    Screen.StudentPerformance.route,
)

private val adminNavItems = listOf(
    MuBottomNavDestination(Screen.AdminHome.route, Icons.Default.Dashboard, "Главная", "Главная"),
    MuBottomNavDestination(Screen.AdminRequests.route, Icons.Default.HowToReg, "Заявки", "Заявки"),
    MuBottomNavDestination(Screen.AdminStructure.route, Icons.Default.AccountTree, "Структура", "Структура"),
    MuBottomNavDestination(Screen.AdminSchedule.route, Icons.Default.CalendarMonth, "Расписание", "Сетка"),
    MuBottomNavDestination(Screen.AdminMore.route, Icons.Default.MoreHoriz, "Ещё", "Ещё"),
)

private fun isRouteSelected(item: MuBottomNavDestination, currentRoute: String?): Boolean {
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
    onNavigate: (String) -> Unit,
) {
    MuBottomNavigationBar(
        items = adminNavItems,
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        isSelected = { item, route -> isRouteSelected(item, route) },
    )
}
