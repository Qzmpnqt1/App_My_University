package com.example.app_my_university.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val localeRu = Locale("ru", "RU")

/** Например: «5 марта 2025 г., 14:30». */
private val dateTimeRu: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy 'г.', HH:mm", localeRu)

/**
 * Разбор типичных строк из Spring/Jackson (ISO_LOCAL_DATE_TIME, с офсетом, Zoned, Instant, только дата)
 * и вывод в понятном для пользователя виде на русском.
 * Если разобрать не удалось — возвращается исходная строка (обрезанная по краям).
 */
fun formatApiDateTimeForDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val s = raw.trim()
    val ldt = parseToLocalDateTimeBestEffort(s) ?: return s
    return ldt.format(dateTimeRu)
}

private fun parseToLocalDateTimeBestEffort(s: String): LocalDateTime? {
    try {
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (_: DateTimeParseException) {
    }
    try {
        return OffsetDateTime.parse(s).toLocalDateTime()
    } catch (_: DateTimeParseException) {
    }
    try {
        return ZonedDateTime.parse(s).toLocalDateTime()
    } catch (_: DateTimeParseException) {
    }
    try {
        return Instant.parse(s).atZone(ZoneId.systemDefault()).toLocalDateTime()
    } catch (_: DateTimeParseException) {
    }
    try {
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
    } catch (_: DateTimeParseException) {
    }
    return null
}
