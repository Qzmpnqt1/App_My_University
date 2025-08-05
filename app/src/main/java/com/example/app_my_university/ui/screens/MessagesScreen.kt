package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatPreview(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onChatSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val chats = remember {
        listOf(
            ChatPreview(
                id = "1",
                name = "Иванов Иван",
                lastMessage = "Добрый день! Когда будет следующая лекция?",
                timestamp = System.currentTimeMillis() - 3600000,
                unreadCount = 2
            ),
            ChatPreview(
                id = "2",
                name = "Петрова Мария",
                lastMessage = "Спасибо за информацию",
                timestamp = System.currentTimeMillis() - 86400000
            ),
            ChatPreview(
                id = "3",
                name = "Сидоров Алексей",
                lastMessage = "Я отправил вам материалы по следующей теме",
                timestamp = System.currentTimeMillis() - 172800000
            )
        )
    }
    
    val filteredChats = if (searchQuery.isBlank()) {
        chats
    } else {
        chats.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.lastMessage.contains(searchQuery, ignoreCase = true) 
        }
    }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Заголовок экрана с отступом 36dp сверху
            Text(
                text = "Сообщения",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, bottom = 16.dp)
            )
            
            // Поиск
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Поиск сообщений") },
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
                        text = "Сообщений не найдено",
                        style = MaterialTheme.typography.bodyLarge,
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
                            onClick = { onChatSelected(chat.id) }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    Text(
                        text = formatChatTime(chat.timestamp),
                        style = MaterialTheme.typography.bodySmall,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (chat.unreadCount > 0) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        Divider(
            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun formatChatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 24 * 60 * 60 * 1000 -> {
            // Сегодня - показываем время
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        diff < 48 * 60 * 60 * 1000 -> {
            // Вчера
            "Вчера"
        }
        else -> {
            // Дата
            SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}