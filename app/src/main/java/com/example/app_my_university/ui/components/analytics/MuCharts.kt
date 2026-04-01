package com.example.app_my_university.ui.components.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MuDonutSegment(
    val label: String,
    val value: Float,
    val color: Color
)

/** Вертикальные столбцы: подписи снизу, значения над столбцами. */
@Composable
fun MuVerticalBarChart(
    entries: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 160.dp,
    barColors: List<Color>? = null,
    maxValueOverride: Float? = null,
    valueFormatter: (Float) -> String = { v -> if (v % 1f == 0f) v.toInt().toString() else String.format("%.1f", v) }
) {
    if (entries.isEmpty()) {
        MuAnalyticsEmptyState("Нет данных для диаграммы")
        return
    }
    val maxV = maxValueOverride ?: entries.maxOfOrNull { it.second }?.takeIf { it > 0f } ?: 1f
    val scheme = MaterialTheme.colorScheme
    val defaultColors = listOf(
        scheme.primary,
        scheme.secondary,
        scheme.tertiary,
        scheme.primary.copy(alpha = 0.65f),
        scheme.secondary.copy(alpha = 0.65f)
    )
    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val n = entries.size
            val gap = 6.dp
            val barW = maxOf((maxWidth - gap * (n + 1)) / n, 8.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight + 52.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.Start),
                verticalAlignment = Alignment.Bottom
            ) {
                entries.forEachIndexed { i, (label, value) ->
                    val frac = (value / maxV).coerceIn(0f, 1f)
                    val color = barColors?.getOrNull(i) ?: defaultColors[i % defaultColors.size]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(maxOf(barW, 36.dp))
                    ) {
                        Text(
                            text = valueFormatter(value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Canvas(
                            modifier = Modifier
                                .width(barW)
                                .height(chartHeight)
                        ) {
                            val barH = size.height * frac
                            val top = size.height - barH
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(0f, top),
                                size = Size(size.width, barH),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/** Горизонтальные полосы: подпись слева, шкала справа. */
@Composable
fun MuHorizontalBarChart(
    entries: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    maxValueOverride: Float? = null,
    valueFormatter: (Float) -> String = { v -> String.format("%.1f", v) }
) {
    if (entries.isEmpty()) {
        MuAnalyticsEmptyState("Нет данных для сравнения")
        return
    }
    val maxV = maxValueOverride ?: entries.maxOfOrNull { it.second }?.takeIf { it > 0f } ?: 1f
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        entries.forEachIndexed { i, (label, value) ->
            val frac = (value / maxV).coerceIn(0f, 1f)
            val color = when (i % 3) {
                0 -> scheme.primary
                1 -> scheme.secondary
                else -> scheme.tertiary
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = valueFormatter(value),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    val track = scheme.surfaceVariant.copy(alpha = 0.9f)
                    drawRoundRect(
                        color = track,
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        size = size
                    )
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        size = Size(size.width * frac, size.height)
                    )
                }
            }
        }
    }
}

/** Кольцевая диаграмма с опциональным центром и легендой. */
@Composable
fun MuDonutChart(
    segments: List<MuDonutSegment>,
    modifier: Modifier = Modifier,
    donutSize: Dp = 132.dp,
    strokeWidth: Dp = 20.dp,
    centerTitle: String? = null,
    centerValue: String? = null
) {
    if (segments.isEmpty() || segments.all { it.value <= 0f }) {
        MuAnalyticsEmptyState("Нет данных для круговой диаграммы")
        return
    }
    val total = segments.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(donutSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = strokeWidth.toPx()
                val r = (this.size.minDimension - stroke) / 2f
                val c = Offset(this.size.width / 2f, this.size.height / 2f)
                var start = -90f
                segments.forEach { seg ->
                    val sweep = 360f * (seg.value / total)
                    if (sweep > 0.1f) {
                        drawArc(
                            color = seg.color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(c.x - r, c.y - r),
                            size = Size(r * 2, r * 2),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        start += sweep
                    }
                }
            }
            if (centerValue != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = centerValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    centerTitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            segments.filter { it.value > 0f }.forEach { seg ->
                val pct = 100f * seg.value / total
                MuLegendRow(
                    label = seg.label,
                    valueText = "${String.format("%.0f", pct)}%",
                    color = seg.color
                )
            }
        }
    }
}

/** Компактное кольцо с крупной подписью по центру. */
@Composable
fun MuDonutChartWithCenter(
    segments: List<MuDonutSegment>,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 18.dp,
    centerTitle: String,
    centerValue: String
) {
    if (segments.isEmpty() || segments.all { it.value <= 0f }) {
        MuAnalyticsEmptyState("Нет данных")
        return
    }
    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val r = (this.size.minDimension - stroke) / 2f
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            var start = -90f
            segments.forEach { seg ->
                val sweep = 360f * (seg.value / total)
                if (sweep > 0.05f) {
                    drawArc(
                        color = seg.color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(c.x - r, c.y - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = centerTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Линейный график по точкам (подпись X — произвольная строка). */
@Composable
fun MuLineChart(
    points: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 140.dp
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    if (points.isEmpty()) {
        MuAnalyticsEmptyState("Нет данных для динамики")
        return
    }
    if (points.size == 1) {
        MuVerticalBarChart(points.map { it.first to it.second }, chartHeight = chartHeight)
        return
    }
    val maxV = points.maxOf { it.second }.takeIf { it > 0f } ?: 1f
    val minV = minOf(points.minOf { it.second }, 0f)
    val range = (maxV - minV).takeIf { it > 0f } ?: 1f
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(vertical = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val pad = 8.dp.toPx()
            for (i in 0..3) {
                val y = pad + (h - 2 * pad) * i / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(pad, y),
                    end = Offset(w - pad, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            val path = Path()
            val denom = (points.size - 1).coerceAtLeast(1)
            points.forEachIndexed { i, (_, v) ->
                val x = pad + (w - 2 * pad) * i / denom
                val y = h - pad - (h - 2 * pad) * ((v - minV) / range)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            points.forEachIndexed { i, (_, v) ->
                val x = pad + (w - 2 * pad) * i / denom
                val y = h - pad - (h - 2 * pad) * ((v - minV) / range)
                drawCircle(lineColor, 5.dp.toPx(), Offset(x, y))
                drawCircle(Color.White, 2.5.dp.toPx(), Offset(x, y))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            points.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
