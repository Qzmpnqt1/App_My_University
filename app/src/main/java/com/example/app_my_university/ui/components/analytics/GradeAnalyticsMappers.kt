package com.example.app_my_university.ui.components.analytics

import androidx.compose.material3.ColorScheme
import com.example.app_my_university.data.api.model.GradeResponse

/** Дисциплина с итоговым экзаменом (числовая оценка 2–5). */
fun GradeResponse.isDisciplineExamFinal(): Boolean =
    finalAssessmentType.equals("EXAM", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && grade != null)

/** Дисциплина с зачётом / без экзамена по типу из API или по заполненным полям. */
fun GradeResponse.isDisciplineCreditFinal(): Boolean =
    finalAssessmentType.equals("CREDIT", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && creditStatus != null && grade == null)

/**
 * Выставлен ли итог по дисциплине (согласовано с правилами статистики на backend: зачёт — есть creditStatus;
 * экзамен — оценка 2–5).
 */
fun GradeResponse.hasRecordedFinalResult(): Boolean =
    if (isDisciplineCreditFinal()) {
        creditStatus != null
    } else {
        grade != null && grade in 2..5
    }

/** Счётчики оценок 2–5 по дисциплинам с экзаменом. */
fun examGradeCounts(grades: List<GradeResponse>): Map<Int, Int> {
    val exams = grades.filter { it.isDisciplineExamFinal() }
    val m = mutableMapOf(2 to 0, 3 to 0, 4 to 0, 5 to 0)
    exams.forEach { g ->
        val n = g.grade ?: return@forEach
        if (n in 2..5) m[n] = (m[n] ?: 0) + 1
    }
    return m
}

fun examGradeBarEntries(grades: List<GradeResponse>): List<Pair<String, Float>> {
    val c = examGradeCounts(grades)
    return listOf("2" to c[2]!!.toFloat(), "3" to c[3]!!.toFloat(), "4" to c[4]!!.toFloat(), "5" to c[5]!!.toFloat())
}

data class CreditBreakdown(val passed: Int, val failed: Int, val pending: Int)

fun creditBreakdown(grades: List<GradeResponse>): CreditBreakdown {
    val rows = grades.filter { it.isDisciplineCreditFinal() }
    if (rows.isEmpty()) return CreditBreakdown(0, 0, 0)
    var p = 0
    var f = 0
    var pend = 0
    rows.forEach { g ->
        when (g.creditStatus) {
            true -> p++
            false -> f++
            null -> pend++
        }
    }
    return CreditBreakdown(p, f, pend)
}

fun creditDonutSegments(breakdown: CreditBreakdown, scheme: ColorScheme): List<MuDonutSegment> = buildList {
    if (breakdown.passed > 0) add(MuDonutSegment("Зачтено", breakdown.passed.toFloat(), scheme.primary))
    if (breakdown.failed > 0) add(MuDonutSegment("Незачёт", breakdown.failed.toFloat(), scheme.error))
    if (breakdown.pending > 0) add(MuDonutSegment("Нет оценки", breakdown.pending.toFloat(), scheme.outline.copy(alpha = 0.6f)))
}

fun examAverage(grades: List<GradeResponse>): Double? {
    val nums = grades.filter { it.isDisciplineExamFinal() }
        .mapNotNull { it.grade }
        .filter { it in 2..5 }
    return if (nums.isEmpty()) null else nums.average()
}

fun pendingFinalCount(grades: List<GradeResponse>): Int =
    grades.count { g ->
        when {
            g.isDisciplineCreditFinal() -> g.creditStatus == null
            g.isDisciplineExamFinal() -> g.grade == null
            else -> g.grade == null && g.creditStatus == null
        }
    }
