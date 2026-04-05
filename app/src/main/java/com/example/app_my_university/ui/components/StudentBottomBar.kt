package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import com.example.app_my_university.ui.navigation.BottomNavTabMapping
import com.example.app_my_university.ui.navigation.Screen

private val studentNavItems = listOf(
    MuBottomNavDestination(Screen.StudentHome.route, Icons.Default.Home, "Главная", "Главная"),
    MuBottomNavDestination(Screen.Schedule.route, Icons.Default.CalendarMonth, "Расписание", "Расписание"),
    MuBottomNavDestination(Screen.GradeBook.route, Icons.Default.Grade, "Оценки", "Оценки"),
    MuBottomNavDestination(Screen.Dialogs.route, Icons.AutoMirrored.Filled.Chat, "Сообщения", "Сообщения"),
    MuBottomNavDestination(Screen.Profile.route, Icons.Default.Person, "Профиль", "Профиль"),
)

@Composable
fun StudentBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    MuBottomNavigationBar(
        items = studentNavItems,
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        isSelected = { dest, route ->
            BottomNavTabMapping.studentTabRouteForSelection(route) == dest.route
        },
    )
}
