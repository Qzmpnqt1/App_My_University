package com.example.app_my_university.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_my_university.data.repository.AuthRepository
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.viewmodel.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsUniversityTitle() {
        runBlocking {
            val repo = mockk<AuthRepository>(relaxed = true)
            coEvery { repo.isLoggedIn() } returns false
            val vm = LoginViewModel(repo)
            composeRule.setContent {
                MaterialTheme {
                    LoginScreen(
                        viewModel = vm,
                        onNavigateToRegistration = {},
                        onNavigateToRegistrationStatus = {},
                        onLoginSuccess = {},
                    )
                }
            }
            composeRule.onNodeWithText("Мой Университет").assertIsDisplayed()
            composeRule.onNodeWithTag(UiTestTags.Screen.LOGIN).assertIsDisplayed()
        }
    }

    @Test
    fun blankLoginShowsValidation() {
        runBlocking {
            val repo = mockk<AuthRepository>(relaxed = true)
            coEvery { repo.isLoggedIn() } returns false
            val vm = LoginViewModel(repo)
            composeRule.setContent {
                MaterialTheme {
                    LoginScreen(
                        viewModel = vm,
                        onNavigateToRegistration = {},
                        onNavigateToRegistrationStatus = {},
                        onLoginSuccess = {},
                    )
                }
            }
            composeRule.onNodeWithText("Войти").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Заполните все поля").assertIsDisplayed()
        }
    }
}
