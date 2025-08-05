package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.app_my_university.ui.theme.Dimens

/**
 * Унифицированная верхняя панель для всего приложения.
 * Обеспечивает одинаковый размер, цвет и поведение текста во всех экранах.
 * Обновлено: текст центрирован по вертикали, увеличен размер на 30%, убрана кнопка Назад
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniformTopAppBar(
    title: String,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    // Используем фиксированный увеличенный размер текста (на 30% больше стандартного среднего размера)
    val increasedTextSize = 22.sp
    
    TopAppBar(
        title = { 
            // Используем Box для центрирования текста по вертикали
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = increasedTextSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        navigationIcon = {
            // Стрелка назад больше не используется
        },
        actions = { actions?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.height(Dimens.topAppBarHeight),
        windowInsets = WindowInsets.statusBars
    )
}