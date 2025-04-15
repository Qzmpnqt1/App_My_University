package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок
            Text(
                text = "Вход в систему",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Поле ввода логина
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Логин или email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Поле ввода пароля
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопка входа
            Button(
                onClick = {
                    // Заглушка для демонстрации
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        onLoginSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ссылка на экран регистрации
            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRegister() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
