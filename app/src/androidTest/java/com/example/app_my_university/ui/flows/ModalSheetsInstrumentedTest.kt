package com.example.app_my_university.ui.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_my_university.ui.muSetContent
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.screens.adminassignment.AdminTeacherAssignmentSheet
import com.example.app_my_university.ui.screens.teachergrading.TeacherGradingAssessmentSheet
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.viewmodel.AdminTeacherAssignmentUiState
import com.example.app_my_university.ui.viewmodel.AdminTeacherAssignmentViewModel
import com.example.app_my_university.ui.TestModelFixtures
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModalSheetsInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun teacherGradingAssessmentSheet_showsTag() {
        muSetContent(composeRule, AppRole.Teacher) {
            TeacherGradingAssessmentSheet(
                data = TestModelFixtures.numericTeacherAssessment(),
                saving = false,
                onDismiss = {},
                onSave = { _, _, _ -> },
            )
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Sheet.TEACHER_GRADING_ASSESSMENT).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Sheet.TEACHER_GRADING_ASSESSMENT).assertIsDisplayed()
        composeRule.onNodeWithText("Программирование", substring = true).assertIsDisplayed()
    }

    @Test
    fun adminTeacherAssignmentSheet_showsTag() {
        val vm = mockk<AdminTeacherAssignmentViewModel>(relaxed = true)
        val teacher = TestModelFixtures.teacherUserProfile()
        muSetContent(composeRule, AppRole.Admin) {
            AdminTeacherAssignmentSheet(
                uiState = AdminTeacherAssignmentUiState(
                    selectedTeacher = teacher,
                    institutes = listOf(TestModelFixtures.institute()),
                    catalogsLoading = false,
                ),
                viewModel = vm,
                onDismiss = {},
            )
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Sheet.ADMIN_TEACHER_ASSIGNMENT).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Sheet.ADMIN_TEACHER_ASSIGNMENT).assertIsDisplayed()
        composeRule.onNodeWithText("Назначение дисциплин").assertIsDisplayed()
    }
}
