package com.example.app_my_university.ui.viewmodel

import com.example.app_my_university.data.repository.ProfileRepository
import com.example.app_my_university.data.theme.AppThemePreference
import com.example.app_my_university.data.theme.ThemePreferenceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelPasswordTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ProfileRepository
    private lateinit var themePreferenceRepository: ThemePreferenceRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        themePreferenceRepository = mockk(relaxed = true)
        every { themePreferenceRepository.observeThemePreference() } returns flowOf(AppThemePreference.SYSTEM)
        coEvery { repository.getProfile() } returns Result.success(
            com.example.app_my_university.data.api.model.UserProfileResponse(
                id = 1L,
                email = "a@b.ru",
                firstName = "A",
                lastName = "B",
                middleName = null,
                userType = "STUDENT",
                isActive = true,
                createdAt = null,
                studentProfile = null,
                teacherProfile = null,
                adminProfile = null
            )
        )
        viewModel = ProfileViewModel(repository, themePreferenceRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `changePassword mismatch shows error without calling repository`() = runTest {
        viewModel.changePassword("old", "new123", "other")
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("не совпадают") == true)
    }
}
