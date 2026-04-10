package com.example.app_my_university.ui.viewmodel

import com.example.app_my_university.data.auth.TokenManager
import com.example.app_my_university.data.repository.EducationRepository
import com.example.app_my_university.data.repository.ScheduleRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var educationRepository: EducationRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: ScheduleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        scheduleRepository = mockk(relaxed = true)
        educationRepository = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        coEvery { scheduleRepository.getMySchedule(any(), any()) } returns Result.success(emptyList())
        coEvery { scheduleRepository.getGroupSchedule(any(), any(), any()) } returns Result.success(emptyList())
        viewModel = ScheduleViewModel(scheduleRepository, educationRepository, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setViewingGroup null clears schedule and stops loading`() = runTest {
        viewModel.setViewingGroup(1L)
        advanceUntilIdle()

        viewModel.setViewingGroup(null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.viewingGroupId)
        assertTrue(state.scheduleByDay.isEmpty())
        assertEquals(false, state.isLoading)
    }
}
