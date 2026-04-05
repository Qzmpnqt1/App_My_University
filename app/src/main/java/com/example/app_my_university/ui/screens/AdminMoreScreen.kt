package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.navigation.navigateWithinAdminFlow
import com.example.app_my_university.ui.theme.Dimens

private data class MoreEntry(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMoreScreen(navController: NavHostController) {
    val fixed = listOf(
        MoreEntry("Сообщения", "Чаты с пользователями", Icons.AutoMirrored.Filled.Chat, Screen.Dialogs.route),
        MoreEntry("Пользователи", "Активация и деактивация", Icons.Default.Person, Screen.AdminUsers.route),
        MoreEntry("Аналитика", "Сводные показатели", Icons.Default.Assessment, Screen.AdminStatistics.route),
        MoreEntry("Аудит", "Журнал действий", Icons.Default.History, Screen.AdminAudit.route),
        MoreEntry("Профиль", "Личные данные и выход", Icons.Default.Settings, Screen.Profile.route)
    )

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        topBar = { UniformTopAppBar(title = "Ещё") },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(AppSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
        ) {
            items(fixed, key = { it.route }) { item ->
                MoreRowCard(item) {
                    navController.navigateWithinAdminFlow(item.route)
                }
            }
        }
    }
}

@Composable
private fun MoreRowCard(entry: MoreEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM)
        ) {
            Icon(entry.icon, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
