package com.example.app_my_university.ui.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_my_university.data.api.model.GuestRegistrationStatusResponse
import com.example.app_my_university.data.repository.AuthRepository
import com.example.app_my_university.ui.mockEducationRepository
import com.example.app_my_university.ui.muSetContent
import com.example.app_my_university.ui.screens.OnboardingWelcomeScreen
import com.example.app_my_university.ui.screens.RegistrationScreen
import com.example.app_my_university.ui.screens.RegistrationStatusScreen
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.viewmodel.LoginViewModel
import com.example.app_my_university.ui.viewmodel.RegistrationStatusViewModel
import com.example.app_my_university.ui.viewmodel.RegistrationViewModel
import com.example.app_my_university.ui.screens.LoginScreen
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthFlowInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun welcome_showsTagAndLoginEntry() {
        muSetContent(composeRule, null) {
            OnboardingWelcomeScreen(onNavigateToLogin = {})
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.WELCOME).assertIsDisplayed()
        composeRule.onNodeWithText("Вход в аккаунт").assertIsDisplayed()
    }

    @Test
    fun login_showsScreenTag() {
        runBlocking {
            val repo = mockk<AuthRepository>(relaxed = true)
            coEvery { repo.isLoggedIn() } returns false
            muSetContent(composeRule, null) {
                LoginScreen(
                    viewModel = LoginViewModel(repo),
                    onNavigateToRegistration = {},
                    onNavigateToRegistrationStatus = {},
                    onLoginSuccess = {},
                )
            }
            composeRule.onNodeWithTag(UiTestTags.Screen.LOGIN).assertIsDisplayed()
            composeRule.onNodeWithText("Мой Университет").assertIsDisplayed()
        }
    }

    /**
     * Отдельный тест без длинной цепочки ввода после snackbar — иначе на части устройств/прогонов
     * встречалось «No compose hierarchies» при повторном взаимодействии в одном @Test.
     */
    @Test
    fun registration_emptySubmit_showsValidation() {
        val auth = mockk<AuthRepository>(relaxed = true)
        val edu = mockEducationRepository()
        muSetContent(composeRule, null) {
            RegistrationScreen(
                onNavigateBack = {},
                onRegistrationSuccess = {},
                viewModel = RegistrationViewModel(auth, edu),
            )
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.REGISTRATION).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.REGISTRATION).assertIsDisplayed()
        repeat(3) {
            composeRule.onNodeWithTag(UiTestTags.RegistrationForm.SCROLL_CONTAINER).performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag(UiTestTags.RegistrationForm.SUBMIT).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Заполните все обязательные поля", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNode(
            hasText("Заполните все обязательные поля", substring = true),
            useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    @Test
    fun registration_filledWithoutUniversity_showsUniversityError() {
        val auth = mockk<AuthRepository>(relaxed = true)
        val edu = mockEducationRepository()
        muSetContent(composeRule, null) {
            RegistrationScreen(
                onNavigateBack = {},
                onRegistrationSuccess = {},
                viewModel = RegistrationViewModel(auth, edu),
            )
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.REGISTRATION).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.waitForIdle()
        fun typeInto(tag: String, text: String) {
            composeRule.onNodeWithTag(tag).performScrollTo()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(tag).performTextInput(text)
        }
        typeInto(UiTestTags.RegistrationForm.LAST_NAME, "Тестов")
        typeInto(UiTestTags.RegistrationForm.FIRST_NAME, "Студент")
        typeInto(UiTestTags.RegistrationForm.EMAIL, "reg@test.ru")
        typeInto(UiTestTags.RegistrationForm.PASSWORD, "secret12")
        repeat(2) {
            composeRule.onNodeWithTag(UiTestTags.RegistrationForm.SCROLL_CONTAINER).performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag(UiTestTags.RegistrationForm.SUBMIT).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Выберите университет", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNode(
            hasText("Выберите университет", substring = true),
            useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    @Test
    fun registrationStatus_lookupSuccessShowsStatusCard() {
        val auth = mockk<AuthRepository>(relaxed = true)
        val edu = mockEducationRepository()
        coEvery {
            auth.lookupRegistrationStatus(any(), any())
        } returns Result.success(
            GuestRegistrationStatusResponse(
                id = 1L,
                status = "PENDING",
                userType = "STUDENT",
                universityId = 1L,
                groupId = null,
                instituteId = null,
                rejectionReason = null,
                createdAt = "2026-01-01T10:00:00",
            ),
        )
        muSetContent(composeRule, null) {
            RegistrationStatusScreen(
                onNavigateBack = {},
                viewModel = RegistrationStatusViewModel(auth, edu),
            )
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.REGISTRATION_STATUS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithText("Email").performTextInput("a@b.ru")
        composeRule.onNodeWithText("Пароль из заявки").performTextInput("secret")
        composeRule.onNodeWithText("Проверить статус", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithText("Статус:", substring = true).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithText("Статус:", substring = true).assertIsDisplayed()
    }
}
