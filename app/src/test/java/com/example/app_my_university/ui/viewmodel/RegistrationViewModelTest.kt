package com.example.app_my_university.ui.viewmodel

import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.repository.AuthRepository
import com.example.app_my_university.data.repository.EducationRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var educationRepository: EducationRepository
    private lateinit var viewModel: RegistrationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        educationRepository = mockk(relaxed = true)
        coEvery { educationRepository.getUniversities() } returns Result.success(
            listOf(UniversityResponse(id = 1L, name = "U", shortName = null, city = null)),
        )
        coEvery { educationRepository.getInstitutes(1L) } returns Result.success(emptyList())
        coEvery { educationRepository.getDirections(1L) } returns Result.success(emptyList())
        coEvery { educationRepository.getGroups(1L) } returns Result.success(emptyList())
        viewModel = RegistrationViewModel(authRepository, educationRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `register with blank email sets validation error`() = runTest {
        viewModel.register(
            email = " ",
            password = "secret12",
            firstName = "Имя",
            lastName = "Фамилия",
            middleName = null,
        )
        advanceUntilIdle()
        assertEquals("Заполните все обязательные поля", viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.register(any()) }
    }

    @Test
    fun `register without selected university sets error`() = runTest {
        viewModel.register(
            email = "a@b.ru",
            password = "secret12",
            firstName = "Имя",
            lastName = "Фамилия",
            middleName = null,
        )
        advanceUntilIdle()
        assertEquals("Выберите университет", viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.register(any()) }
    }

    @Test
    fun `register student without group sets error`() = runTest {
        viewModel.selectUniversity(1L)
        advanceUntilIdle()
        viewModel.register(
            email = "a@b.ru",
            password = "secret12",
            firstName = "Имя",
            lastName = "Фамилия",
            middleName = null,
        )
        advanceUntilIdle()
        assertEquals("Выберите группу", viewModel.uiState.value.error)
    }

    @Test
    fun `register success sets isRegistered`() = runTest {
        viewModel.selectUserType("TEACHER")
        viewModel.selectUniversity(1L)
        advanceUntilIdle()
        coEvery { authRepository.register(any()) } returns Result.success(Unit)

        viewModel.register(
            email = "a@b.ru",
            password = "secret12",
            firstName = "Имя",
            lastName = "Фамилия",
            middleName = null,
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isRegistered)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify { authRepository.register(any()) }
    }
}
