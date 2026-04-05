package com.example.app_my_university.ui.components.analytics

/**
 * Преобразование ответа [com.example.app_my_university.data.api.model.ScheduleStatisticsResponse]
 * в ряды для графиков (дни недели, номера недель).
 */
fun scheduleDaySeriesForChart(map: Map<String, Long>?): List<Pair<String, Float>> {
    if (map.isNullOrEmpty()) return emptyList()
    fun dayLabel(key: String): String = when (key.uppercase()) {
        "1", "MONDAY" -> "Пн"
        "2", "TUESDAY" -> "Вт"
        "3", "WEDNESDAY" -> "Ср"
        "4", "THURSDAY" -> "Чт"
        "5", "FRIDAY" -> "Пт"
        "6", "SATURDAY" -> "Сб"
        "7", "SUNDAY" -> "Вс"
        else -> key.take(3)
    }
    fun dayIndex(label: String): Int =
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").indexOf(label).let { if (it < 0) 8 else it }
    val merged = mutableMapOf<String, Float>()
    map.forEach { (k, v) ->
        val lab = dayLabel(k)
        merged[lab] = (merged[lab] ?: 0f) + v.toFloat()
    }
    return merged.entries.sortedBy { dayIndex(it.key) }.map { it.key to it.value }
}

fun scheduleWeekSeriesForChart(map: Map<String, Long>?): List<Pair<String, Float>> {
    if (map.isNullOrEmpty()) return emptyList()
    return map.entries
        .sortedBy { it.key.toIntOrNull() ?: 0 }
        .map { (k, v) -> "Н$k" to v.toFloat() }
}
