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

private val adminNavItems = listOf(
    MuBottomNavDestination(Screen.AdminHome.route, Icons.Default.Dashboard, "Главная", "Главная"),
    MuBottomNavDestination(Screen.AdminRequests.route, Icons.Default.HowToReg, "Заявки", "Заявки"),
    MuBottomNavDestination(Screen.AdminStructure.route, Icons.Default.AccountTree, "Структура", "Структура"),
    MuBottomNavDestination(Screen.AdminSchedule.route, Icons.Default.CalendarMonth, "Расписание", "Расписание"),
    MuBottomNavDestination(Screen.AdminMore.route, Icons.Default.MoreHoriz, "Ещё", "Ещё"),
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
