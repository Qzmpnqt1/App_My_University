package com.example.app_my_university.ui.components.analytics

import androidx.compose.material3.ColorScheme
import com.example.app_my_university.data.api.model.GradeResponse

private fun GradeResponse.isExamFinal(): Boolean =
    finalAssessmentType.equals("EXAM", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && grade != null)

private fun GradeResponse.isCreditFinal(): Boolean =
    finalAssessmentType.equals("CREDIT", ignoreCase = true) ||
        (finalAssessmentType.isNullOrBlank() && creditStatus != null && grade == null)

/** Счётчики оценок 2–5 по дисциплинам с экзаменом. */
fun examGradeCounts(grades: List<GradeResponse>): Map<Int, Int> {
    val exams = grades.filter { it.isExamFinal() }
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
    val rows = grades.filter { it.isCreditFinal() }
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
