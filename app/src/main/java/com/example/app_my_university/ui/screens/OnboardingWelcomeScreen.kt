package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Верхняя часть - заголовок
            Text(
                text = "Добро пожаловать",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Мой университет",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            
            // Центральная часть - изображение/иконка
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Используем большую иконку вместо изображения 
                // (можно заменить на Image для использования реального изображения)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Университет",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Нижняя часть - кнопка "Далее"
            Text(
                text = "Далее →",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNext() }
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            )
        }
    }
} 