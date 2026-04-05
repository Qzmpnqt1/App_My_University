package com.example.app_my_university.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app_my_university.ui.theme.AppThemeExtras

enum class MuBadgeTone {
    Neutral,
    Primary,
    Success,
    Warning,
    Error,
    Info,
}

@Composable
fun MuStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: MuBadgeTone = MuBadgeTone.Neutral,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = AppThemeExtras.extendedColors
    val (bg: Color, fg: Color) = when (tone) {
        MuBadgeTone.Neutral ->
            scheme.surfaceVariant to scheme.onSurfaceVariant
        MuBadgeTone.Primary ->
            scheme.primaryContainer to scheme.onPrimaryContainer
        MuBadgeTone.Success ->
            ext.successContainer to ext.onSuccessContainer
        MuBadgeTone.Warning ->
            ext.warningContainer to ext.onWarningContainer
        MuBadgeTone.Error ->
            scheme.errorContainer to scheme.onErrorContainer
        MuBadgeTone.Info ->
            ext.infoContainer to ext.onInfoContainer
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

fun lessonTypeRu(type: String?): String = when (type) {
    "LECTURE" -> "Лекция"
    "SEMINAR" -> "Семинар"
    "LABORATORY" -> "Лабораторная"
    "PRACTICE" -> "Практика"
    null, "" -> "Занятие"
    else -> type
}
