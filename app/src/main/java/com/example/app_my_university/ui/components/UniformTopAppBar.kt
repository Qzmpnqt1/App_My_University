package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.app_my_university.ui.designsystem.AppBar
import com.example.app_my_university.ui.theme.MuPalette

/**
 * Единая верхняя панель: один и тот же тип (center-aligned), одна минимальная высота,
 * тёмный фирменный фон [MuPalette.Ink] и светлый текст на всех экранах.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniformTopAppBar(
    title: String,
    subtitle: String? = null,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val onBar = Color.White
    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = AppBar.titleSp.sp,
        color = onBar,
    )
    val titleComposable: @Composable () -> Unit = {
        if (subtitle.isNullOrBlank()) {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = titleStyle,
                textAlign = TextAlign.Center,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = titleStyle,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = onBar.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    val barColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MuPalette.Ink,
        titleContentColor = onBar,
        navigationIconContentColor = onBar,
        actionIconContentColor = onBar,
    )

    CenterAlignedTopAppBar(
        title = titleComposable,
        navigationIcon = {
            if (onBackPressed != null) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = onBar,
                    )
                }
            }
        },
        actions = actions,
        colors = barColors,
        modifier = Modifier.heightIn(min = AppBar.minHeight),
        windowInsets = WindowInsets.statusBars,
    )
}
