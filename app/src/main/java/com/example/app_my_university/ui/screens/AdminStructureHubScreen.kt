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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.MenuBook
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

private data class StructureEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStructureHubScreen(navController: NavHostController) {
    val entries = listOf(
        StructureEntry(
            "Вуз и институты",
            "Данные вашего вуза и список институтов",
            Icons.Default.School,
            Screen.AdminUniversities.route
        ),
        StructureEntry(
            "Направления подготовки",
            "Код, название, привязка к институту",
            Icons.Default.AccountTree,
            Screen.AdminDirections.route
        ),
        StructureEntry(
            "Учебные группы",
            "Группы по направлениям",
            Icons.Default.Group,
            Screen.AdminGroups.route
        ),
        StructureEntry(
            "Справочник предметов",
            "Глобальные названия дисциплин",
            Icons.Default.Subject,
            Screen.AdminSubjects.route
        ),
        StructureEntry(
            "Предметы в направлениях",
            "Учебный план: курс, семестр, контроль",
            Icons.Default.MenuBook,
            Screen.AdminSubjectPlan.route
        ),
        StructureEntry(
            "Аудитории",
            "Корпуса, номера, вместимость",
            Icons.Default.MeetingRoom,
            Screen.AdminClassrooms.route
        ),
        StructureEntry(
            "Преподаватели и дисциплины",
            "Кто какой предмет ведёт",
            Icons.Default.PersonAdd,
            Screen.AdminTeacherSubjects.route
        )
    )

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Учебная структура",
                onBackPressed = { navController.navigateUp() },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(AppSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
        ) {
            item {
                Text(
                    "Разделы вашего вуза",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.spaceS)
                )
            }
            items(entries, key = { it.route }) { e ->
                StructureHubCard(entry = e) {
                    navController.navigateWithinAdminFlow(e.route)
                }
            }
        }
    }
}

@Composable
private fun StructureHubCard(
    entry: StructureEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM)
        ) {
            Icon(
                entry.icon,
                null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
