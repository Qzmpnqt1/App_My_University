package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_my_university.ui.components.AdminBottomBar
import com.example.app_my_university.ui.components.UniformTopAppBar
import kotlinx.coroutines.launch

data class AdminMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = "",
    val badge: Int? = null
)

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onNavigateToUniversityManagement: () -> Unit,
    onNavigateToRegistrationRequests: () -> Unit,
    onNavigateToScheduleManagement: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onLogout: () -> Unit
) {
    // Данные администратора (в реальном приложении будут загружаться из базы данных)
    val adminName = "Иванов Иван Иванович"
    val adminRole = "Главный администратор"
    
    // Количество запросов на регистрацию
    val registrationRequestsCount = 5
    
    // Количество непрочитанных сообщений
    val unreadMessagesCount = 3
    
    // Список пунктов меню для администратора
    val menuItems = listOf(
        AdminMenuItem(
            title = "ВУЗы",
            icon = Icons.Default.School,
            route = "university_management",
            description = "Управление ВУЗами и институтами"
        ),
        AdminMenuItem(
            title = "Заявки",
            icon = Icons.Default.Person,
            route = "registration_requests",
            description = "Подтверждение регистраций",
            badge = registrationRequestsCount
        ),
        AdminMenuItem(
            title = "Сообщения",
            icon = Icons.Outlined.Message,
            route = "messages",
            description = "Общение с пользователями",
            badge = unreadMessagesCount
        ),
        AdminMenuItem(
            title = "Расписание",
            icon = Icons.Default.Schedule,
            route = "schedule_management",
            description = "Управление расписанием"
        ),
        AdminMenuItem(
            title = "Предметы",
            icon = Icons.Default.Subject,
            route = "subject_management",
            description = "Управление учебными предметами"
        ),
        AdminMenuItem(
            title = "Группы",
            icon = Icons.Default.Group,
            route = "group_management",
            description = "Управление учебными группами"
        ),
        AdminMenuItem(
            title = "Пользователи",
            icon = Icons.Default.Person,
            route = "user_management",
            description = "Управление аккаунтами"
        ),
        AdminMenuItem(
            title = "Профиль",
            icon = Icons.Default.Settings,
            route = "admin_profile",
            description = "Настройки аккаунта"
        )
    )
    
    // Функция для навигации по маршруту
    fun navigateToRoute(route: String) {
        when (route) {
            "university_management" -> onNavigateToUniversityManagement()
            "registration_requests" -> onNavigateToRegistrationRequests()
            "schedule_management" -> onNavigateToScheduleManagement()
            "messages" -> onNavigateToMessages()
            "subject_management", "group_management", "user_management", "admin_profile" -> onNavigate(route)
            else -> onNavigate(route)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Топ-бар с учетом системных инсетов
        UniformTopAppBar(title = "Панель администратора")
        
        // Отображаем содержимое
        AdminDashboardContent(
            menuItems = menuItems,
            paddingValues = PaddingValues(16.dp),
            onItemClick = { route -> navigateToRoute(route) }
        )
    }
}

@Composable
fun AdminDashboardContent(
    menuItems: List<AdminMenuItem>,
    paddingValues: PaddingValues,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                AdminMenuCard(item = item, onClick = { onItemClick(item.route) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuCard(
    item: AdminMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                item.badge?.let { badgeCount ->
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                    ) {
                        Text(badgeCount.toString())
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdminProfileHeader(
    name: String,
    role: String,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onProfileClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Информация о профиле
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Кнопка редактирования
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Редактировать профиль",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AdminStatisticsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Статистика системы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(title = "ВУЗы", value = "15")
                StatisticItem(title = "Институты", value = "48")
                StatisticItem(title = "Направления", value = "124")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(title = "Преподаватели", value = "215")
                StatisticItem(title = "Студенты", value = "3450")
                StatisticItem(title = "Группы", value = "187")
            }
        }
    }
}

@Composable
fun StatisticItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 