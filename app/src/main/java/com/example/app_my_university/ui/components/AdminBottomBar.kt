package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import com.example.app_my_university.ui.navigation.BottomNavTabMapping
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.test.UiTestTags

private val adminNavItems = listOf(
    MuBottomNavDestination(Screen.AdminHome.route, Icons.Default.Dashboard, "Главная", "Главная", UiTestTags.BottomNav.ADMIN_HOME),
    MuBottomNavDestination(Screen.AdminRequests.route, Icons.Default.HowToReg, "Заявки", "Заявки", UiTestTags.BottomNav.ADMIN_REQUESTS),
    MuBottomNavDestination(Screen.AdminStructure.route, Icons.Default.AccountTree, "Структура", "Структура", UiTestTags.BottomNav.ADMIN_STRUCTURE),
    MuBottomNavDestination(Screen.AdminSchedule.route, Icons.Default.CalendarMonth, "Расписание", "Расписание", UiTestTags.BottomNav.ADMIN_SCHEDULE),
    MuBottomNavDestination(Screen.AdminMore.route, Icons.Default.MoreHoriz, "Ещё", "Ещё", UiTestTags.BottomNav.ADMIN_MORE),
)

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    MuBottomNavigationBar(
        items = adminNavItems,
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        isSelected = { item, route -> BottomNavTabMapping.adminTabSelected(item.route, route) },
    )
}
