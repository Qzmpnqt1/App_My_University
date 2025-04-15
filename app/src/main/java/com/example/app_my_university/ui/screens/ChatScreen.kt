package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Модель данных для сообщений (используем LocalDateTime)
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBackPressed: () -> Unit
) {
    // Название чата можно подстраивать в зависимости от chatId
    val chatTitle = "Чат: $chatId"

    // Пример списка сообщений (в реальном приложении данные следует получать через ViewModel)
    val messages = remember {
        mutableStateListOf(
            Message(
                id = "msg1",
                chatId = chatId,
                senderId = "user1",
                text = "Здравствуйте! Не забудьте отправить домашнее задание до пятницы.",
                timestamp = LocalDateTime.now().minusHours(2),
                isRead = true
            ),
            Message(
                id = "msg2",
                chatId = chatId,
                senderId = "user2",
                text = "Добрый день! Хотел уточнить, можно ли сдать его раньше, в среду?",
                timestamp = LocalDateTime.now().minusHours(1),
                isRead = true
            ),
            Message(
                id = "msg3",
                chatId = chatId,
                senderId = "user1",
                text = "Конечно, буду рад получить работу пораньше. Если возникнут вопросы, пишите.",
                timestamp = LocalDateTime.now().minusMinutes(30),
                isRead = true
            )
        )
    }

    var newMessageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Список сообщений (reverseLayout для отображения новых сообщений снизу)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages.asReversed()) { message ->
                    MessageItem(
                        message = message,
                        isFromCurrentUser = message.senderId == "user2"
                    )
                }
            }

            // Поле для ввода нового сообщения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    placeholder = { Text("Введите сообщение...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            val newMessage = Message(
                                id = "msg${messages.size + 1}",
                                chatId = chatId,
                                senderId = "user2",
                                text = newMessageText,
                                timestamp = LocalDateTime.now(),
                                isRead = false
                            )
                            messages.add(newMessage)
                            newMessageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Отправить",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    isFromCurrentUser: Boolean
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isFromCurrentUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isFromCurrentUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = message.timestamp.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
