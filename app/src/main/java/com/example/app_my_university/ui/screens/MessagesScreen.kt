package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Модель данных для предпросмотра чата
data class ChatPreview(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navigateToChat: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Пример списка чатов
    val chats = remember {
        listOf(
            ChatPreview(
                id = "1",
                name = "Иванов Иван Иванович",
                lastMessage = "Экзамен состоится 15 июня в 10:00 в аудитории 302.",
                timestamp = System.currentTimeMillis() - 1200000,
                unreadCount = 1,
                isOnline = true
            ),
            ChatPreview(
                id = "2",
                name = "Петров Петр Петрович",
                lastMessage = "Пожалуйста, ознакомьтесь с материалами перед следующей лекцией.",
                timestamp = System.currentTimeMillis() - 86400000,
                unreadCount = 0,
                isOnline = false
            ),
            ChatPreview(
                id = "3",
                name = "Учебный отдел",
                lastMessage = "Расписание на следующий семестр будет доступно в личном кабинете.",
                timestamp = System.currentTimeMillis() - 172800000,
                unreadCount = 0,
                isOnline = true
            )
        )
    }

    val filteredChats = if (searchQuery.isBlank()) {
        chats
    } else {
        chats.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Сообщения") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Поиск по контактам...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                singleLine = true
            )

            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет доступных чатов",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredChats) { chat ->
                        ChatItem(
                            chat = chat,
                            onClick = { navigateToChat(chat.id) }
                        )
                        Divider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    val dateFormat = if (isToday(chat.timestamp)) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    } else {
        SimpleDateFormat("dd MMM", Locale.getDefault())
    }
    val formattedDate = dateFormat.format(Date(chat.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (chat.isOnline) {
                Surface(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = Color.Green
                ) {}
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val today = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    return today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
}
