package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isFromCurrentUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit = {}
) {
    // В реальном приложении эти данные будут загружаться из базы данных
    val chatName = remember { "Чат #$chatId" }
    val currentUserId = remember { "current_user_id" }
    
    // Пример сообщений для демонстрации
    val messages = remember {
        listOf(
            ChatMessage(
                id = "1",
                senderId = "user1",
                senderName = "Иванов Иван",
                content = "Здравствуйте! У меня вопрос по расписанию.",
                timestamp = System.currentTimeMillis() - 3600000,
                isFromCurrentUser = false
            ),
            ChatMessage(
                id = "2",
                senderId = currentUserId,
                senderName = "Вы",
                content = "Добрый день! Какой именно вопрос вас интересует?",
                timestamp = System.currentTimeMillis() - 3500000,
                isFromCurrentUser = true
            ),
            ChatMessage(
                id = "3",
                senderId = "user1",
                senderName = "Иванов Иван",
                content = "Будет ли перенос занятий на следующей неделе в связи с праздниками?",
                timestamp = System.currentTimeMillis() - 3400000,
                isFromCurrentUser = false
            ),
            ChatMessage(
                id = "4",
                senderId = currentUserId,
                senderName = "Вы",
                content = "Да, занятия со вторника переносятся на субботу. Подробное расписание будет опубликовано завтра.",
                timestamp = System.currentTimeMillis() - 3300000,
                isFromCurrentUser = true
            ),
            ChatMessage(
                id = "5",
                senderId = "user1",
                senderName = "Иванов Иван",
                content = "Спасибо за информацию!",
                timestamp = System.currentTimeMillis() - 3200000,
                isFromCurrentUser = false
            )
        )
    }
    
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Прокрутка к последнему сообщению при открытии чата
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatName) },
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
            // Список сообщений
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                items(messages) { message ->
                    MessageItem(message = message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Поле для ввода нового сообщения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Функционал прикрепления файлов */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Прикрепить файл"
                    )
                }
                
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    placeholder = { Text("Введите сообщение...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    maxLines = 3
                )
                
                IconButton(
                    onClick = {
                        if (newMessage.isNotBlank()) {
                            // В реальном приложении здесь будет отправка сообщения
                            newMessage = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Отправить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    val alignment = if (message.isFromCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isFromCurrentUser) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.surfaceVariant
    
    val textColor = if (message.isFromCurrentUser) 
        MaterialTheme.colorScheme.onPrimaryContainer 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant
    
    val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = dateFormatter.format(Date(message.timestamp))
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!message.isFromCurrentUser) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        
        Card(
            modifier = Modifier.padding(vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = textColor
                )
                
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
 