package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.app_my_university.ui.designsystem.AppLayout

/**
 * Единая верхняя панель: выравнивание по центру, фиксированная минимальная высота,
 * цвета из [MaterialTheme.colorScheme] (surface tier + onSurface), без отдельной «чужой» полосы.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniformTopAppBar(
    title: String,
    subtitle: String? = null,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val onBar = scheme.onSurface
    val onBarMuted = scheme.onSurfaceVariant
    val barBackground = scheme.surfaceContainerHigh

    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = AppLayout.titleSp.sp,
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
                    color = onBarMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    val barColors = TopAppBarDefaults.topAppBarColors(
        containerColor = barBackground,
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
                        modifier = Modifier.size(AppLayout.barIconSize),
                    )
                }
            }
        },
        actions = actions,
        colors = barColors,
        modifier = Modifier.heightIn(min = AppLayout.topAppBarMinHeight),
        windowInsets = WindowInsets.statusBars,
    )
}
