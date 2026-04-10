package com.example.app_my_university.ui.viewmodel

import com.example.app_my_university.data.api.model.InAppNotificationResponse
import com.example.app_my_university.data.repository.NotificationsRepository
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
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: NotificationsRepository
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        viewModel = NotificationsViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_success() = runTest {
        val items = listOf(
            InAppNotificationResponse(1L, "K", "T", "B", null, null),
        )
        coEvery { repo.getMyNotifications() } returns Result.success(items)

        viewModel.load()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals(1, viewModel.uiState.value.items.size)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun load_failure_setsError() = runTest {
        coEvery { repo.getMyNotifications() } returns Result.failure(Exception("offline"))

        viewModel.load()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals("offline", viewModel.uiState.value.error)
    }

    @Test
    fun markRead_onSuccess_reloads() = runTest {
        val items = listOf(InAppNotificationResponse(1L, "K", "T", null, null, null))
        coEvery { repo.getMyNotifications() } returns Result.success(items)
        coEvery { repo.markRead(1L) } returns Result.success(Unit)

        viewModel.load()
        advanceUntilIdle()
        viewModel.markRead(1L)
        advanceUntilIdle()

        coVerify(atLeast = 2) { repo.getMyNotifications() }
        coVerify(exactly = 1) { repo.markRead(1L) }
        assertFalse(viewModel.uiState.value.busy)
    }

    @Test
    fun clearError() = runTest {
        coEvery { repo.getMyNotifications() } returns Result.failure(Exception("e"))
        viewModel.load()
        advanceUntilIdle()
        assertEquals("e", viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
