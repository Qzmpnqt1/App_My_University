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
import com.example.app_my_university.ui.theme.MuPalette

enum class MuBadgeTone {
    Neutral,
    Primary,
    Success,
    Warning,
    Error
}

@Composable
fun MuStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: MuBadgeTone = MuBadgeTone.Neutral
) {
    val (bg, fg) = when (tone) {
        MuBadgeTone.Neutral ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        MuBadgeTone.Primary ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MuBadgeTone.Success ->
            MuPalette.AccentLight.copy(alpha = 0.85f) to MuPalette.Success
        MuBadgeTone.Warning ->
            Color(0xFFFFF3E0) to MuPalette.Warning
        MuBadgeTone.Error ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
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
