package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.ui.designsystem.AppLayout

data class MuBottomNavDestination(
    val route: String,
    val icon: ImageVector,
    val contentDescription: String,
    /** Короткая подпись под иконкой (1 строка). */
    val label: String,
    /** Стабильный testTag для UI-тестов (опционально). */
    val testTag: String? = null,
)

@Composable
fun MuBottomNavigationBar(
    items: List<MuBottomNavDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: (MuBottomNavDestination, String?) -> Boolean = { dest, route -> route == dest.route },
) {
    NavigationBar(
        modifier = modifier.heightIn(min = AppLayout.bottomNavigationMinHeight),
        tonalElevation = 1.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        items.forEach { item ->
            val selected = isSelected(item, currentRoute)
            NavigationBarItem(
                modifier = if (item.testTag != null) Modifier.testTag(item.testTag) else Modifier,
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        modifier = Modifier.size(AppLayout.barIconSize),
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
