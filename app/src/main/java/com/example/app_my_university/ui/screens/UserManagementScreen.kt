package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app_my_university.model.UserType

data class User(
    val id: String,
    val name: String,
    val email: String,
    val type: UserType,
    val university: String,
    val faculty: String?,
    val groupName: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) }
    var showDeleteConfirmation by remember { mutableStateOf<User?>(null) }
    
    // В реальном приложении эти данные будут загружаться из БД
    val users = remember {
        listOf(
            User("1", "Иванов Иван", "ivanov@mail.ru", UserType.STUDENT, "МГУ", "Мехмат", "ММ-101"),
            User("2", "Петров Петр", "petrov@mail.ru", UserType.STUDENT, "МГУ", "Физфак", "Ф-203"),
            User("3", "Сидорова Анна", "sidorova@mail.ru", UserType.TEACHER, "МГУ", "Мехмат", null),
            User("4", "Козлов Алексей", "kozlov@mail.ru", UserType.TEACHER, "МГТУ", "Информатика", null),
            User("5", "Николаева Ольга", "nikolaeva@mail.ru", UserType.STUDENT, "МГТУ", "Робототехника", "РТ-301")
        )
    }
    
    val filterOptions = listOf("Все", "Студенты", "Преподаватели")
    
    val filteredUsers = when (selectedFilterIndex) {
        1 -> users.filter { it.type == UserType.STUDENT }
        2 -> users.filter { it.type == UserType.TEACHER }
        else -> users
    }.filter {
        if (searchQuery.isBlank()) true
        else it.name.contains(searchQuery, ignoreCase = true) || 
             it.email.contains(searchQuery, ignoreCase = true)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Верхняя панель
        TopAppBar(
            title = { Text("Управление пользователями") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.height(48.dp)
        )
        
        // Содержимое экрана
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Поисковая строка
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск по имени или email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Фильтры
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                filterOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = filterOptions.size
                        ),
                        label = { Text(label) }
                    )
                }
            }
            
            // Счетчик пользователей
            Text(
                text = "Найдено: ${filteredUsers.size} пользователей",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Список пользователей
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onDeleteClick = { showDeleteConfirmation = user }
                    )
                }
            }
        }
    }
    
    // Диалог подтверждения удаления
    showDeleteConfirmation?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Удаление пользователя") },
            text = { 
                Text("Вы действительно хотите удалить пользователя ${user.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // В реальном приложении здесь будет логика удаления пользователя
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun UserCard(user: User, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватарка
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Информация о пользователе
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Тип пользователя
                    val typeColor = when(user.type) {
                        UserType.STUDENT -> MaterialTheme.colorScheme.primary
                        UserType.TEACHER -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    
                    Text(
                        text = when(user.type) {
                            UserType.STUDENT -> "Студент"
                            UserType.TEACHER -> "Преподаватель"
                            else -> "Администратор"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.2f))
                            .border(1.dp, typeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // ВУЗ
                    Text(
                        text = user.university,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Группа для студентов или кафедра для преподавателей
                    user.groupName?.let {
                        Text(
                            text = " • Группа: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (user.type == UserType.TEACHER && user.faculty != null) {
                        Text(
                            text = " • ${user.faculty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Кнопка удаления
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить пользователя",
                    tint = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserManagementScreenPreview() {
    MaterialTheme {
        UserManagementScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun UserCardPreview() {
    MaterialTheme {
        UserCard(
            user = User(
                id = "1",
                name = "Иванов Иван",
                email = "ivanov@mail.ru",
                type = UserType.STUDENT,
                university = "МГУ",
                faculty = "Мехмат",
                groupName = "ММ-101"
            ),
            onDeleteClick = {}
        )
    }
} 