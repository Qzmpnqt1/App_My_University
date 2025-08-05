package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Элемент нижней панели навигации для администратора
 */
data class AdminBottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

/**
 * Нижняя панель навигации для администратора
 */
@Composable
fun AdminBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        AdminBottomNavItem(
            title = "Главная",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            route = "admin_dashboard"
        ),
        AdminBottomNavItem(
            title = "ВУЗы",
            selectedIcon = Icons.Filled.School,
            unselectedIcon = Icons.Outlined.School,
            route = "university_management"
        ),
        AdminBottomNavItem(
            title = "Заявки",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = "registration_requests"
        ),
        AdminBottomNavItem(
            title = "Расписание",
            selectedIcon = Icons.Filled.Schedule,
            unselectedIcon = Icons.Filled.Schedule,
            route = "schedule_management"
        ),
        AdminBottomNavItem(
            title = "Сообщения",
            selectedIcon = Icons.Filled.Message,
            unselectedIcon = Icons.Outlined.Message,
            route = "messages"
        )
    )

    // Увеличиваем высоту BottomBar на 15% (с 65.dp до 75.dp)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.height(75.dp)
    ) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route)
            
            AdminBottomBarItem(
                item = item,
                selected = selected,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
private fun RowScope.AdminBottomBarItem(
    item: AdminBottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title,
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}