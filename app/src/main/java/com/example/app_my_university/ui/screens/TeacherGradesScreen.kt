package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.app_my_university.core.logging.AppLogger
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.screens.teachergrading.DirectionPickerBlock
import com.example.app_my_university.ui.screens.teachergrading.GroupPickerBlock
import com.example.app_my_university.ui.screens.teachergrading.InstitutePickerBlock
import com.example.app_my_university.ui.screens.teachergrading.StudentPickerBlock
import com.example.app_my_university.ui.screens.teachergrading.SubjectDirectionPickerBlock
import com.example.app_my_university.ui.screens.teachergrading.TeacherGradingAssessmentSheet
import com.example.app_my_university.ui.screens.teachergrading.TeacherGradingPathSummary
import com.example.app_my_university.ui.screens.teachergrading.WizardSectionCard
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.TeacherGradingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradesScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: TeacherGradingViewModel,
) {
    val ui by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(ui.error, ui.assessment) {
        ui.error?.let { msg ->
            if (ui.assessment != null) {
                snackbar.showSnackbar(msg)
                viewModel.clearError()
            }
        }
    }
    LaunchedEffect(ui.saveSuccess) {
        if (ui.saveSuccess) {
            snackbar.showSnackbar("Сохранено")
            viewModel.clearSaveSuccess()
        }
    }

    val filteredStudents = remember(ui.students, ui.studentSearchQuery) {
        viewModel.filteredStudents()
    }

    RoleShellScaffold(
        role = AppRole.Teacher,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Выставление оценок",
                onBackPressed = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            ui.loading && ui.institutes.isEmpty() -> {
                MuLoadingState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    message = "Загружаем ваши институты…",
                )
            }
            ui.institutes.isEmpty() && !ui.loading && ui.error != null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                ) {
                    MuErrorState(
                        message = ui.error ?: "Не удалось загрузить данные",
                        onRetry = { viewModel.refreshInstitutes() },
                    )
                }
            }
            ui.institutes.isEmpty() && !ui.loading -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                ) {
                    MuEmptyState(
                        title = "Нет назначенных дисциплин",
                        subtitle = "Администратор ещё не назначил вам предметы в этом вузе.",
                    )
                }
            }
            else -> {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                ) {
                    item {
                        Text(
                            "Пошагово выберите контекст: институт и направление задают учебный план, затем дисциплина, группа и студент.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        TeacherGradingPathSummary(ui = ui)
                    }
                    item {
                        WizardSectionCard(
                            title = "1. Институт",
                            subtitle = "Подразделение, где ведёте занятия",
                        ) {
                            InstitutePickerBlock(ui = ui, onSelect = {
                                AppLogger.userAction("TeacherGrading", "institute=$it")
                                viewModel.selectInstitute(it)
                            })
                        }
                    }
                    if (ui.selectedInstituteId != null) {
                        item {
                            WizardSectionCard(
                                title = "2. Направление подготовки",
                                subtitle = "Группы на следующих шагах соответствуют только этому направлению",
                            ) {
                                DirectionPickerBlock(
                                    ui = ui,
                                    onSelect = {
                                        AppLogger.userAction("TeacherGrading", "direction=$it")
                                        viewModel.selectDirection(it)
                                    },
                                    onRetry = {
                                        ui.selectedInstituteId?.let { viewModel.selectInstitute(it) }
                                    },
                                )
                            }
                        }
                    }
                    if (ui.selectedDirectionId != null) {
                        item {
                            WizardSectionCard(
                                title = "3. Дисциплина в учебном плане",
                                subtitle = "Конкретная связка предмета с курсом и семестром",
                            ) {
                                SubjectDirectionPickerBlock(
                                    ui = ui,
                                    onSelect = {
                                        AppLogger.userAction("TeacherGrading", "subjectDirection=$it")
                                        viewModel.selectSubjectDirection(it)
                                    },
                                    onRetry = {
                                        ui.selectedDirectionId?.let { viewModel.selectDirection(it) }
                                    },
                                )
                            }
                        }
                    }
                    if (ui.selectedSubjectDirectionId != null) {
                        item {
                            WizardSectionCard(
                                title = "4. Группа",
                                subtitle = "Учебная группа по выбранному направлению",
                            ) {
                                GroupPickerBlock(
                                    ui = ui,
                                    onSelect = {
                                        AppLogger.userAction("TeacherGrading", "group=$it")
                                        viewModel.selectGroup(it)
                                    },
                                    onRetry = {
                                        val sid = ui.selectedSubjectDirectionId
                                        if (sid != null) viewModel.selectSubjectDirection(sid)
                                    },
                                )
                            }
                        }
                    }
                    if (ui.selectedGroupId != null && ui.selectedSubjectDirectionId != null) {
                        item {
                            WizardSectionCard(
                                title = "5. Студент",
                                subtitle = "Выберите человека для выставления оценки",
                            ) {
                                StudentPickerBlock(
                                    ui = ui,
                                    filteredStudents = filteredStudents,
                                    onSearchChange = {
                                        AppLogger.userAction("TeacherGrading", "studentSearch")
                                        viewModel.onStudentSearchChange(it)
                                    },
                                    onSelect = {
                                        AppLogger.userAction("TeacherGrading", "student=$it")
                                        viewModel.selectStudent(it)
                                    },
                                    onRetry = {
                                        val sid = ui.selectedSubjectDirectionId
                                        val gid = ui.selectedGroupId
                                        if (sid != null && gid != null) viewModel.selectGroup(gid)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (ui.assessment != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissAssessment() },
            sheetState = sheetState,
        ) {
            TeacherGradingAssessmentSheet(
                data = ui.assessment!!,
                saving = ui.saving || ui.assessmentLoading,
                onDismiss = { viewModel.dismissAssessment() },
                onSave = { finalG, finalC, drafts ->
                    viewModel.saveAssessment(finalG, finalC, drafts)
                },
            )
        }
    }
}
