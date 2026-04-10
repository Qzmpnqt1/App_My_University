package com.example.app_my_university.ui.viewmodel

import com.example.app_my_university.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        coEvery { authRepository.isLoggedIn() } returns false
        viewModel = LoginViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank fields sets validation error without repository login`() = runTest {
        viewModel.login("   ", "secret")
        advanceUntilIdle()
        assertEquals("Заполните все поля", viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `successful login updates ui state`() = runTest {
        coEvery { authRepository.login("a@b.ru", "pwd") } returns Result.success(Unit)
        coEvery { authRepository.getUserType() } returns "TEACHER"

        viewModel.login("a@b.ru", "pwd")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("TEACHER", viewModel.uiState.value.userType)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `failed login exposes message`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("bad"))

        viewModel.login("a@b.ru", "pwd")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals("bad", viewModel.uiState.value.error)
    }

    @Test
    fun `logout clears ui synchronously`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.success(Unit)
        coEvery { authRepository.getUserType() } returns "STUDENT"
        viewModel.login("a@b.ru", "pwd")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.logout()
        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertNull(viewModel.uiState.value.userType)
    }
}
