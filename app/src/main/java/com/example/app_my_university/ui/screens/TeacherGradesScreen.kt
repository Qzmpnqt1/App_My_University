package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.GradeRequest
import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.PracticeGradeRequest
import com.example.app_my_university.data.api.model.PracticeGradeResponse
import com.example.app_my_university.data.api.model.SubjectPracticeResponse
import com.example.app_my_university.ui.viewmodel.GradeBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradesScreen(
    onNavigateBack: () -> Unit,
    viewModel: GradeBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }
    var selectedPracticeId by remember { mutableStateOf<Long?>(null) }

    var showPracticeDialog by remember { mutableStateOf(false) }
    var editingPracticeGradeId by remember { mutableStateOf<Long?>(null) }
    var editingPracticeId by remember { mutableStateOf<Long?>(null) }
    var editingStudentId by remember { mutableStateOf<Long?>(null) }
    var editingStudentName by remember { mutableStateOf("") }
    var practiceStudentIdInput by remember { mutableStateOf("") }
    var gradeValue by remember { mutableStateOf("") }
    var creditStatus by remember { mutableStateOf<Boolean?>(null) }

    var showFinalDialog by remember { mutableStateOf(false) }
    var editingFinalGrade by remember { mutableStateOf<GradeResponse?>(null) }
    var newFinalStudentId by remember { mutableStateOf("") }
    var finalGradeNum by remember { mutableStateOf<String?>(null) }
    var finalCredit by remember { mutableStateOf<Boolean?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var subjectExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSubjectsInDirection()
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Сохранено")
            viewModel.clearSaveSuccess()
            selectedPracticeId?.let { viewModel.loadPracticeGradesByPractice(it) }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выставление оценок") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.subjects.find { it.id == uiState.selectedSubjectDirectionId }
                        ?.let { "${it.subjectName ?: "Предмет"} (${it.directionName ?: ""})" }
                        ?: "Выберите предмет",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Предмет") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    uiState.subjects.forEach { subj ->
                        DropdownMenuItem(
                            text = {
                                Text("${subj.subjectName ?: "Предмет"} (${subj.directionName ?: ""})")
                            },
                            onClick = {
                                viewModel.selectSubjectDirection(subj.id)
                                selectedPracticeId = null
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.selectedSubjectDirectionId == null) {
                Text(
                    text = "Выберите предмет, чтобы выставлять оценки",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SegmentedButton(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Практики", maxLines = 1)
                    }
                    SegmentedButton(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Итог", maxLines = 1)
                    }
                }

                when {
                    uiState.isLoading && tabIndex == 0 && uiState.practices.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    tabIndex == 0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.practices) { practice ->
                                PracticeCard(
                                    practice = practice,
                                    selected = practice.id == selectedPracticeId,
                                    onClick = {
                                        selectedPracticeId = practice.id
                                        viewModel.loadPracticeGradesByPractice(practice.id)
                                    }
                                )
                            }
                            if (uiState.practices.isEmpty()) {
                                item {
                                    Text(
                                        "Для этого предмета нет практических работ в справочнике",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (selectedPracticeId != null) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                editingPracticeGradeId = null
                                                editingPracticeId = selectedPracticeId
                                                editingStudentId = null
                                                editingStudentName = "Новая оценка"
                                                practiceStudentIdInput = ""
                                                gradeValue = ""
                                                creditStatus = null
                                                showPracticeDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Text("Добавить", modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                }
                            }
                            if (selectedPracticeId != null && uiState.gradesByPractice.isNotEmpty()) {
                                item {
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Text(
                                        "Студенты",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                items(uiState.gradesByPractice) { pg ->
                                    StudentPracticeGradeRow(
                                        practiceGrade = pg,
                                        onEdit = {
                                            editingPracticeGradeId = pg.id
                                            editingPracticeId = pg.practiceId
                                            editingStudentId = pg.studentId
                                            editingStudentName = pg.studentName ?: "Студент"
                                            practiceStudentIdInput = pg.studentId.toString()
                                            val creditPractice =
                                                uiState.practices.find { it.id == pg.practiceId }?.isCredit == true
                                            gradeValue =
                                                if (creditPractice) "" else (pg.grade?.toString() ?: "")
                                            creditStatus = pg.creditStatus
                                            showPracticeDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.finalGradesBySubject) { g ->
                                FinalGradeRow(
                                    grade = g,
                                    onEdit = {
                                        editingFinalGrade = g
                                        newFinalStudentId = g.studentId.toString()
                                        finalGradeNum = g.grade?.toString()
                                        finalCredit = g.creditStatus
                                        showFinalDialog = true
                                    }
                                )
                            }
                            item {
                                Button(
                                    onClick = {
                                        editingFinalGrade = null
                                        newFinalStudentId = ""
                                        finalGradeNum = null
                                        finalCredit = null
                                        showFinalDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Добавить итоговую оценку")
                                }
                            }
                            if (uiState.finalGradesBySubject.isEmpty()) {
                                item {
                                    Text(
                                        "Итоговых оценок пока нет. Нажмите кнопку выше, чтобы добавить.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPracticeDialog) {
        val practiceIsCredit =
            uiState.practices.find { it.id == editingPracticeId }?.isCredit == true
        AlertDialog(
            onDismissRequest = { showPracticeDialog = false },
            title = { Text(editingStudentName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (editingStudentId == null) {
                        OutlinedTextField(
                            value = practiceStudentIdInput,
                            onValueChange = { practiceStudentIdInput = it },
                            label = { Text("ID студента *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        if (practiceIsCredit) {
                            "Зачётная практика: укажите зачёт / незачёт (без числовой оценки)."
                        } else {
                            "Оценочная практика: выберите оценку 2–5."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!practiceIsCredit) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("2", "3", "4", "5").forEach { g ->
                                FilterChip(
                                    selected = gradeValue == g,
                                    onClick = { gradeValue = g },
                                    label = { Text(g) }
                                )
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = creditStatus == true,
                                onClick = { creditStatus = if (creditStatus == true) null else true },
                                label = { Text("Зачтено") }
                            )
                            FilterChip(
                                selected = creditStatus == false,
                                onClick = { creditStatus = if (creditStatus == false) null else false },
                                label = { Text("Не зачтено") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val practiceId = editingPracticeId ?: return@Button
                    val studentId = editingStudentId ?: practiceStudentIdInput.toLongOrNull() ?: return@Button
                    val req = if (practiceIsCredit) {
                        PracticeGradeRequest(
                            studentId = studentId,
                            practiceId = practiceId,
                            grade = null,
                            creditStatus = creditStatus
                        )
                    } else {
                        val g = gradeValue.toIntOrNull()
                        if (g == null || g !in 2..5) return@Button
                        PracticeGradeRequest(
                            studentId = studentId,
                            practiceId = practiceId,
                            grade = g,
                            creditStatus = null
                        )
                    }
                    if (editingPracticeGradeId != null) {
                        viewModel.updatePracticeGrade(editingPracticeGradeId!!, req)
                    } else {
                        viewModel.createPracticeGrade(req)
                    }
                    showPracticeDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPracticeDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showFinalDialog) {
        val subjectDirectionId = uiState.selectedSubjectDirectionId
        val finalType = uiState.subjects
            .find { it.id == subjectDirectionId }
            ?.finalAssessmentType
            ?.uppercase()
            ?: "EXAM"
        val isFinalCreditSubject = finalType == "CREDIT"
        AlertDialog(
            onDismissRequest = { showFinalDialog = false },
            title = {
                Text(
                    if (editingFinalGrade != null) {
                        "Итог: ${editingFinalGrade!!.studentName ?: "Студент"}"
                    } else {
                        "Новая итоговая оценка"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (editingFinalGrade == null) {
                        OutlinedTextField(
                            value = newFinalStudentId,
                            onValueChange = { newFinalStudentId = it },
                            label = { Text("ID студента *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        if (isFinalCreditSubject) {
                            "Дисциплина зачётная: укажите зачёт или незачёт (оценка 2–5 не используется)."
                        } else {
                            "Дисциплина экзаменационная: укажите итоговую оценку 2–5."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isFinalCreditSubject) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("2", "3", "4", "5").forEach { g ->
                                FilterChip(
                                    selected = finalGradeNum == g,
                                    onClick = { finalGradeNum = g },
                                    label = { Text(g) }
                                )
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = finalCredit == true,
                                onClick = { finalCredit = true },
                                label = { Text("Зачёт") }
                            )
                            FilterChip(
                                selected = finalCredit == false,
                                onClick = { finalCredit = false },
                                label = { Text("Незачёт") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectDirectionId == null) return@Button
                        val studentId = editingFinalGrade?.studentId ?: newFinalStudentId.toLongOrNull()
                            ?: return@Button
                        val req = if (isFinalCreditSubject) {
                            if (finalCredit == null) return@Button
                            GradeRequest(
                                studentId = studentId,
                                subjectDirectionId = subjectDirectionId,
                                grade = null,
                                creditStatus = finalCredit
                            )
                        } else {
                            val g = finalGradeNum?.toIntOrNull()
                            if (g == null || g !in 2..5) return@Button
                            GradeRequest(
                                studentId = studentId,
                                subjectDirectionId = subjectDirectionId,
                                grade = g,
                                creditStatus = null
                            )
                        }
                        if (editingFinalGrade != null) {
                            viewModel.updateGrade(editingFinalGrade!!.id, req)
                        } else {
                            viewModel.createGrade(req)
                        }
                        showFinalDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun PracticeCard(
    practice: SubjectPracticeResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = practice.practiceTitle ?: "Практика #${practice.practiceNumber ?: practice.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (practice.isCredit == true) {
                    Text(
                        text = "Зачётная практика",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    practice.maxGrade?.let {
                        Text(
                            text = "Макс. балл: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Оценочная (2–5)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentPracticeGradeRow(
    practiceGrade: PracticeGradeResponse,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = practiceGrade.studentName ?: "Студент #${practiceGrade.studentId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (practiceGrade.practiceIsCredit) {
                        true -> {
                            practiceGrade.creditStatus?.let {
                                Text(
                                    if (it) "Зачтено" else "Не зачтено",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            } ?: Text("—", style = MaterialTheme.typography.bodySmall)
                        }
                        false -> {
                            practiceGrade.grade?.let {
                                Text(
                                    "Оценка: $it" + (practiceGrade.maxGrade?.let { m -> "/$m" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } ?: Text("—", style = MaterialTheme.typography.bodySmall)
                        }
                        null -> {
                            practiceGrade.grade?.let {
                                Text(
                                    "Оценка: $it" + (practiceGrade.maxGrade?.let { m -> "/$m" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            practiceGrade.creditStatus?.let {
                                Text(
                                    if (it) "Зачтено" else "Не зачтено",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            if (practiceGrade.grade == null && practiceGrade.creditStatus == null) {
                                Text("—", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Изменить")
            }
        }
    }
}

@Composable
private fun FinalGradeRow(
    grade: GradeResponse,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grade.studentName ?: "Студент #${grade.studentId}",
                    fontWeight = FontWeight.Medium
                )
                val creditDiscipline = grade.finalAssessmentType.equals("CREDIT", ignoreCase = true) ||
                    (grade.finalAssessmentType.isNullOrBlank() && grade.creditStatus != null && grade.grade == null)
                if (creditDiscipline) {
                    grade.creditStatus?.let {
                        Text(
                            if (it) "Зачёт" else "Незачёт",
                            color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    } ?: Text("—", style = MaterialTheme.typography.bodySmall)
                } else {
                    grade.grade?.let {
                        Text("Оценка: $it", color = MaterialTheme.colorScheme.primary)
                    } ?: Text("—", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Изменить")
            }
        }
    }
}
