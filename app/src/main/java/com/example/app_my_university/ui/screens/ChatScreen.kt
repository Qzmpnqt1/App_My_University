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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromMe: Boolean,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit
) {
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Пример сообщений для демонстрации
    val messages = remember {
        listOf(
            ChatMessage(
                id = "1",
                content = "Добрый день! Когда будет доступен материал по следующей теме?",
                isFromMe = false,
                timestamp = System.currentTimeMillis() - 3600000
            ),
            ChatMessage(
                id = "2",
                content = "Здравствуйте! Материалы будут доступны завтра после 14:00",
                isFromMe = true,
                timestamp = System.currentTimeMillis() - 3500000
            ),
            ChatMessage(
                id = "3",
                content = "Отлично, спасибо за информацию!",
                isFromMe = false,
                timestamp = System.currentTimeMillis() - 3400000
            ),
            ChatMessage(
                id = "4",
                content = "Не за что! Если будут вопросы, обращайтесь",
                isFromMe = true,
                timestamp = System.currentTimeMillis() - 3300000
            )
        )
    }
    
    // Название чата для демонстрации
    val chatName = "Студент Иванов Иван"
    
    var isMoreMenuExpanded by remember { mutableStateOf(false) }
    
    // Прокрутка к последнему сообщению при открытии чата
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Заголовок чата с отступом 36dp сверху
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(top = 36.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = { isMoreMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Ещё",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                DropdownMenu(
                    expanded = isMoreMenuExpanded,
                    onDismissRequest = { isMoreMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Очистить историю") },
                        onClick = {
                            isMoreMenuExpanded = false
                            // Логика очистки истории
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
            
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
                }
            }
            
            // Поле ввода сообщения
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromMe) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else 
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (message.isFromMe) 
                        MaterialTheme.colorScheme.onPrimary
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(message.timestamp))
                
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isFromMe) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}