package com.example.app_my_university.ui.screens.teachergrading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_my_university.data.api.model.TeacherPracticeSlotResponse
import com.example.app_my_university.data.api.model.TeacherStudentAssessmentResponse
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.PracticeGradeDraft

@Composable
fun TeacherGradingAssessmentSheet(
    data: TeacherStudentAssessmentResponse,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (finalGrade: Int?, finalCredit: Boolean?, drafts: Map<Long, PracticeGradeDraft>) -> Unit,
) {
    val isCredit = data.finalAssessmentType?.equals("CREDIT", ignoreCase = true) == true
    var finalNum by remember(data) { mutableStateOf(data.finalGrade?.grade?.toString() ?: "") }
    var finalCredit by remember(data) { mutableStateOf(data.finalGrade?.creditStatus) }
    val drafts = remember(data.subjectDirectionId, data.studentUserId) {
        mutableStateMapOf<Long, PracticeGradeDraft>().apply {
            data.practices.orEmpty().forEach { p ->
                put(p.practiceId, PracticeGradeDraft(p.grade, p.creditStatus))
            }
        }
    }

    val finalDirty = remember(data, finalNum, finalCredit, isCredit) {
        if (isCredit) {
            finalCredit != data.finalGrade?.creditStatus
        } else {
            finalNum.toIntOrNull() != data.finalGrade?.grade
        }
    }

    var showSaveConfirm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
            ) {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(Dimens.screenPadding)
                            .padding(top = Dimens.spaceS),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss, enabled = !saving) {
                            Text("Закрыть")
                        }
                        Button(
                            onClick = { showSaveConfirm = true },
                            enabled = !saving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            if (saving) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.height(22.dp),
                                )
                            } else {
                                Text("Сохранить всё", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = Dimens.spaceM,
                bottom = Dimens.spaceXL,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
        ) {
            item {
                Text(
                    "Оценивание",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Dimens.spaceS))
            }
            item {
                AssessmentContextCard(data)
            }
            item {
                Text(
                    "Итоговый контроль",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (finalDirty) {
                    Text(
                        "Черновик: изменения ещё не сохранены",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Text(
                    "Повторное нажатие на уже выбранный вариант отменяет выбор и возвращает ранее сохранённое значение.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.spaceS))
                if (isCredit) {
                    FinalCreditSegmented(
                        value = finalCredit,
                        onChange = { picked ->
                            finalCredit = if (finalCredit == picked) {
                                data.finalGrade?.creditStatus
                            } else {
                                picked
                            }
                        },
                    )
                } else {
                    FinalExamSegmented(
                        current = finalNum,
                        onPick = { g ->
                            finalNum = if (finalNum == g) {
                                data.finalGrade?.grade?.toString().orEmpty()
                            } else {
                                g
                            }
                        },
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Text(
                    "Практики",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (data.practices.isNullOrEmpty()) {
                item {
                    Text(
                        "Практик нет — сохраните только итоговый результат.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = data.practices,
                    key = { it.practiceId },
                ) { p ->
                    PracticeAssessmentCard(
                        p = p,
                        draft = drafts[p.practiceId] ?: PracticeGradeDraft(),
                        onDraftChange = { drafts[p.practiceId] = it },
                    )
                }
            }
        }
    }

    if (showSaveConfirm) {
        val summaryFinal = if (isCredit) {
            when (finalCredit) {
                true -> "итог: зачёт"
                false -> "итог: незачёт"
                null -> "итог: не выбран"
            }
        } else {
            finalNum.toIntOrNull()?.let { "итоговая оценка: $it" } ?: "итоговая оценка: не выбрана"
        }
        AlertDialog(
            onDismissRequest = { if (!saving) showSaveConfirm = false },
            title = { Text("Сохранить изменения?") },
            text = {
                Text(
                    "Будут отправлены на сервер: $summaryFinal, а также оценки по практикам (если менялись). " +
                        "Продолжить?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveConfirm = false
                        val fg = if (isCredit) null else finalNum.toIntOrNull()
                        onSave(fg, finalCredit, HashMap(drafts))
                    },
                    enabled = !saving,
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveConfirm = false },
                    enabled = !saving,
                ) {
                    Text("Отмена")
                }
            },
        )
    }
}

@Composable
private fun AssessmentContextCard(data: TeacherStudentAssessmentResponse) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
    ) {
        Column(Modifier.padding(Dimens.spaceM)) {
            Text(
                "Кому выставляется оценка",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(Dimens.spaceS))
            ContextLine("Студент", data.studentDisplayName)
            ContextLine("Группа", data.groupName)
            ContextLine("Дисциплина", data.subjectName)
            ContextLine("Направление", data.directionName)
            ContextLine("Институт", data.instituteName)
        }
    }
}

@Composable
private fun ContextLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column(Modifier.padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FinalCreditSegmented(
    value: Boolean?,
    onChange: (Boolean?) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
        listOf(
            Triple(true, "Зачёт", "Зачтено"),
            Triple(false, "Незачёт", "Не зачтено"),
        ).forEach { (v, short, long) ->
            val selected = value == v
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChange(v) },
            ) {
                Column(
                    Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (selected) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        short,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        long,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FinalExamSegmented(
    current: String,
    onPick: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS)) {
        listOf("2", "3", "4", "5").forEach { g ->
            val selected = current == g
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPick(g) },
            ) {
                Column(
                    Modifier.padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        g,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "балл",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeAssessmentCard(
    p: TeacherPracticeSlotResponse,
    draft: PracticeGradeDraft,
    onDraftChange: (PracticeGradeDraft) -> Unit,
) {
    val savedGrade = p.grade
    val savedCredit = p.creditStatus
    val dirty = if (p.creditPractice == true) {
        draft.credit != savedCredit
    } else {
        draft.grade != savedGrade
    }
    val hasSaved = savedGrade != null || savedCredit != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(Dimens.spaceM)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${p.practiceNumber ?: "—"}. ${p.practiceTitle ?: "Практика"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                ) {
                    Text(
                        if (p.creditPractice == true) "Зачётная" else "Оценочная",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            if (p.maxGrade != null) {
                Text(
                    "Максимум баллов: ${p.maxGrade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                hasSaved && !dirty -> {
                    Text(
                        "Сохранено: ${formatSavedPractice(p)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                hasSaved && dirty -> {
                    Text(
                        "Было: ${formatSavedPractice(p)} → изменено",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                !hasSaved -> {
                    Text(
                        "Ещё не оценено",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Spacer(Modifier.height(Dimens.spaceS))
            if (p.creditPractice == true) {
                FinalCreditSegmented(draft.credit) { onDraftChange(draft.copy(credit = it)) }
            } else {
                val hint = if (p.maxGrade != null) "от 2 до ${p.maxGrade}" else "от 2 до 5"
                Text(
                    "Допустимый диапазон: $hint",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = draft.grade?.toString() ?: "",
                    onValueChange = { v ->
                        val n = v.filter { it.isDigit() }.take(3).toIntOrNull()
                        onDraftChange(draft.copy(grade = n))
                    },
                    label = { Text("Оценка") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }
}

private fun formatSavedPractice(p: TeacherPracticeSlotResponse): String {
    return if (p.creditPractice == true) {
        when (p.creditStatus) {
            true -> "зачтено"
            false -> "не зачтено"
            null -> "—"
        }
    } else {
        p.grade?.toString() ?: "—"
    }
}
