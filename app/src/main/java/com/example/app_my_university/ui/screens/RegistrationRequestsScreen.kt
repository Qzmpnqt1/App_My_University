package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserRole {
    STUDENT,
    TEACHER
}

data class RegistrationRequest(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val universityName: String,
    val additionalInfo: String,
    val requestDate: Date
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationRequestsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Состояние для запросов на регистрацию (в реальном приложении будет загружаться из базы данных)
    val registrationRequests = remember {
        mutableStateListOf(
            RegistrationRequest(
                id = "1",
                fullName = "Иванов Иван Иванович",
                email = "ivanov@example.com",
                role = UserRole.STUDENT,
                universityName = "МГУ",
                additionalInfo = "Факультет: Экономический, Группа: ЭК-101-22",
                requestDate = Date()
            ),
            RegistrationRequest(
                id = "2",
                fullName = "Петров Петр Петрович",
                email = "petrov@example.com",
                role = UserRole.TEACHER,
                universityName = "МГТУ",
                additionalInfo = "Кафедра: Информатики, Предметы: Программирование, Алгоритмы",
                requestDate = Date(System.currentTimeMillis() - 86400000) // вчера
            ),
            RegistrationRequest(
                id = "3",
                fullName = "Сидорова Анна Владимировна",
                email = "sidorova@example.com",
                role = UserRole.STUDENT,
                universityName = "ВШЭ",
                additionalInfo = "Факультет: Компьютерных наук, Группа: КН-203-21",
                requestDate = Date(System.currentTimeMillis() - 172800000) // позавчера
            ),
            RegistrationRequest(
                id = "4",
                fullName = "Козлов Алексей Николаевич",
                email = "kozlov@example.com",
                role = UserRole.TEACHER,
                universityName = "РУДН",
                additionalInfo = "Кафедра: Математики, Предметы: Высшая математика, Дискретная математика",
                requestDate = Date(System.currentTimeMillis() - 259200000) // 3 дня назад
            ),
            RegistrationRequest(
                id = "5",
                fullName = "Новикова Екатерина Сергеевна",
                email = "novikova@example.com",
                role = UserRole.STUDENT,
                universityName = "МГТУ",
                additionalInfo = "Факультет: Робототехники, Группа: РТ-405-20",
                requestDate = Date(System.currentTimeMillis() - 345600000) // 4 дня назад
            )
        )
    }
    
    // Состояние для вкладок
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Все", "Студенты", "Преподаватели")
    
    // Состояние для диалогов
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<RegistrationRequest?>(null) }
    
    // Фильтрация запросов по выбранной вкладке
    val filteredRequests = when (selectedTabIndex) {
        1 -> registrationRequests.filter { it.role == UserRole.STUDENT }
        2 -> registrationRequests.filter { it.role == UserRole.TEACHER }
        else -> registrationRequests
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запросы на регистрацию") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Вкладки для фильтрации
            TabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Запросы на регистрацию отсутствуют",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Список запросов
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRequests) { request ->
                        RequestItem(
                            request = request,
                            onApproveClick = {
                                selectedRequest = request
                                showApproveDialog = true
                            },
                            onRejectClick = {
                                selectedRequest = request
                                showRejectDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Диалог подтверждения регистрации
    if (showApproveDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Подтверждение регистрации") },
            text = { Text("Вы действительно хотите подтвердить регистрацию пользователя ${selectedRequest!!.fullName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        // В реальном приложении здесь будет логика подтверждения регистрации
                        registrationRequests.removeIf { it.id == selectedRequest!!.id }
                        showApproveDialog = false
                    }
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Диалог отклонения регистрации
    if (showRejectDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Отклонение регистрации") },
            text = { Text("Вы действительно хотите отклонить регистрацию пользователя ${selectedRequest!!.fullName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        // В реальном приложении здесь будет логика отклонения регистрации
                        registrationRequests.removeIf { it.id == selectedRequest!!.id }
                        showRejectDialog = false
                    }
                ) {
                    Text("Отклонить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun RequestItem(
    request: RegistrationRequest,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(request.requestDate)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок с именем и ролью
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка роли
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (request.role) {
                                UserRole.STUDENT -> MaterialTheme.colorScheme.primaryContainer
                                UserRole.TEACHER -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (request.role) {
                            UserRole.STUDENT -> Icons.Default.Person
                            UserRole.TEACHER -> Icons.Default.School
                        },
                        contentDescription = null,
                        tint = when (request.role) {
                            UserRole.STUDENT -> MaterialTheme.colorScheme.onPrimaryContainer
                            UserRole.TEACHER -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = request.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = when (request.role) {
                            UserRole.STUDENT -> "Студент"
                            UserRole.TEACHER -> "Преподаватель"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Информация о пользователе
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Email:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = request.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "ВУЗ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = request.universityName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Дополнительная информация:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = request.additionalInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Кнопка отклонения
                IconButton(
                    onClick = onRejectClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Отклонить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                // Кнопка подтверждения
                IconButton(
                    onClick = onApproveClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Подтвердить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
} 