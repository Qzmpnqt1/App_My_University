package com.example.app_my_university.ui.components.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_my_university.data.api.model.ScheduleCompareSegmentResponse
import com.example.app_my_university.data.api.model.ScheduleResponse
import com.example.app_my_university.ui.theme.Dimens

@Composable
fun ScheduleCompareSummaryCard(
    leftLabel: String?,
    rightLabel: String?,
    weekNumber: Int,
    dayLabel: String,
    both: Int,
    onlyLeft: Int,
    onlyRight: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceS),
        ) {
            Text(
                text = "Сравнение",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${leftLabel.orEmpty()} ↔ ${rightLabel.orEmpty()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Неделя $weekNumber · $dayLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Оба заняты: $both",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = "Только слева: $onlyLeft",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Только справа: $onlyRight",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

private fun segmentTypeRu(type: String?): String = when (type) {
    "BOTH" -> "Пересечение / оба заняты"
    "ONLY_LEFT" -> "Только в вашем расписании"
    "ONLY_RIGHT" -> "Только у выбранного объекта"
    else -> type.orEmpty()
}

@Composable
private fun segmentCardColors(type: String?): Color {
    val scheme = MaterialTheme.colorScheme
    return when (type) {
        "BOTH" -> scheme.tertiaryContainer.copy(alpha = 0.85f)
        "ONLY_LEFT" -> scheme.primaryContainer.copy(alpha = 0.55f)
        "ONLY_RIGHT" -> scheme.secondaryContainer.copy(alpha = 0.65f)
        else -> scheme.surfaceVariant.copy(alpha = 0.5f)
    }
}

@Composable
fun ScheduleCompareSegmentCard(
    segment: ScheduleCompareSegmentResponse,
    leftTitle: String,
    rightTitle: String,
    modifier: Modifier = Modifier,
) {
    val type = segment.segmentType
    val bg = segmentCardColors(type)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceS),
        ) {
            Text(
                text = "${segment.segmentStart.orEmpty()} — ${segment.segmentEnd.orEmpty()}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = segmentTypeRu(type),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            CompareSideBlock(title = leftTitle, entries = segment.leftEntries.orEmpty())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            CompareSideBlock(title = rightTitle, entries = segment.rightEntries.orEmpty())
        }
    }
}

@Composable
private fun CompareSideBlock(
    title: String,
    entries: List<ScheduleResponse>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (entries.isEmpty()) {
            Text(
                text = "Свободно",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            entries.forEach { e ->
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = e.subjectName ?: "Предмет",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    e.teacherName?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    e.groupName?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    e.classroomInfo?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
