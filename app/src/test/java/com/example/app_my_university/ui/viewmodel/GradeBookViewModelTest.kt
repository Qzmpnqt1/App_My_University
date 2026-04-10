package com.example.app_my_university.ui.viewmodel

import com.example.app_my_university.data.api.model.GradeResponse
import com.example.app_my_university.data.api.model.StudentPerformanceSummaryResponse
import com.example.app_my_university.data.api.model.StudentPracticeSlotResponse
import com.example.app_my_university.data.repository.GradeRepository
import com.example.app_my_university.data.repository.StatisticsRepository
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
class GradeBookViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var gradeRepository: GradeRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var viewModel: GradeBookViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gradeRepository = mockk()
        statisticsRepository = mockk()
        viewModel = GradeBookViewModel(gradeRepository, statisticsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadInitial_success_setsGradesAndClearsLoading() = runTest {
        val grades = listOf(
            GradeResponse(
                id = 1L,
                studentId = 10L,
                studentName = null,
                subjectDirectionId = 2L,
                subjectName = "Предмет",
                grade = 4,
                creditStatus = null,
            ),
        )
        val summary = StudentPerformanceSummaryResponse(
            courseFilter = null,
            semesterFilter = null,
            plannedSubjects = 1,
            subjectsWithFinalResult = 1,
            subjectsCredited = 0,
            averageNumericGrade = 4.0,
            totalPractices = 0,
            practicesWithResult = 0,
            subjectCompletionPercent = 100.0,
            practiceCompletionPercent = 0.0,
        )
        coEvery { gradeRepository.getMyGrades() } returns Result.success(grades)
        coEvery { statisticsRepository.getMyStudentPerformance(null, null) } returns Result.success(summary)

        viewModel.loadInitial()
        advanceUntilIdle()

        val s = viewModel.uiState.value
        assertFalse(s.initialLoading)
        assertFalse(s.isRefreshing)
        assertNull(s.screenError)
        assertEquals(1, s.grades.size)
        assertEquals(100.0, s.planSummary?.subjectCompletionPercent ?: 0.0, 0.001)
    }

    @Test
    fun loadInitial_gradesFail_setsScreenError() = runTest {
        coEvery { gradeRepository.getMyGrades() } returns Result.failure(Exception("net"))
        coEvery { statisticsRepository.getMyStudentPerformance(null, null) } returns Result.success(
            StudentPerformanceSummaryResponse(
                courseFilter = null,
                semesterFilter = null,
                plannedSubjects = 0,
                subjectsWithFinalResult = 0,
                subjectsCredited = 0,
                averageNumericGrade = null,
                totalPractices = 0,
                practicesWithResult = 0,
                subjectCompletionPercent = 0.0,
                practiceCompletionPercent = 0.0,
            ),
        )

        viewModel.loadInitial()
        advanceUntilIdle()

        assertEquals("net", viewModel.uiState.value.screenError)
        assertTrue(viewModel.uiState.value.grades.isEmpty())
    }

    @Test
    fun refresh_secondLoad_usesRefreshingFlags() = runTest {
        val g = emptyList<GradeResponse>()
        val summary = StudentPerformanceSummaryResponse(
            courseFilter = null,
            semesterFilter = null,
            plannedSubjects = 0,
            subjectsWithFinalResult = 0,
            subjectsCredited = 0,
            averageNumericGrade = null,
            totalPractices = 0,
            practicesWithResult = 0,
            subjectCompletionPercent = 0.0,
            practiceCompletionPercent = 0.0,
        )
        coEvery { gradeRepository.getMyGrades() } returns Result.success(g)
        coEvery { statisticsRepository.getMyStudentPerformance(null, null) } returns Result.success(summary)

        viewModel.loadInitial()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(atLeast = 2) { gradeRepository.getMyGrades() }
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun expandSubject_loadsPracticeSlots() = runTest {
        coEvery { gradeRepository.getMyGrades() } returns Result.success(emptyList())
        coEvery { statisticsRepository.getMyStudentPerformance(null, null) } returns Result.success(
            StudentPerformanceSummaryResponse(
                courseFilter = null,
                semesterFilter = null,
                plannedSubjects = 0,
                subjectsWithFinalResult = 0,
                subjectsCredited = 0,
                averageNumericGrade = null,
                totalPractices = 0,
                practicesWithResult = 0,
                subjectCompletionPercent = 0.0,
                practiceCompletionPercent = 0.0,
            ),
        )
        val slot = StudentPracticeSlotResponse(
            practiceId = 9L,
            practiceNumber = 1,
            practiceTitle = "П1",
            maxGrade = 10,
            isCredit = false,
            grade = null,
            creditStatus = null,
        )
        coEvery { gradeRepository.getMyPracticeSlots(3L) } returns Result.success(listOf(slot))

        viewModel.loadInitial()
        advanceUntilIdle()
        viewModel.setSubjectExpanded(3L, expanded = true)
        advanceUntilIdle()

        val st = viewModel.uiState.value.practiceStates[3L]
        assertTrue(st!!.loaded)
        assertEquals(1, st.slots.size)
    }

    @Test
    fun onListFilterChange_updatesFilter() = runTest {
        coEvery { gradeRepository.getMyGrades() } returns Result.success(emptyList())
        coEvery { statisticsRepository.getMyStudentPerformance(null, null) } returns Result.success(
            StudentPerformanceSummaryResponse(
                courseFilter = null,
                semesterFilter = null,
                plannedSubjects = 0,
                subjectsWithFinalResult = 0,
                subjectsCredited = 0,
                averageNumericGrade = null,
                totalPractices = 0,
                practicesWithResult = 0,
                subjectCompletionPercent = 0.0,
                practiceCompletionPercent = 0.0,
            ),
        )
        viewModel.loadInitial()
        advanceUntilIdle()
        viewModel.onListFilterChange(GradeBookListFilter.EXAMS)
        assertEquals(GradeBookListFilter.EXAMS, viewModel.uiState.value.listFilter)
    }

    @Test
    fun dismissScreenError_clearsError() = runTest {
        coEvery { gradeRepository.getMyGrades() } returns Result.failure(Exception("x"))
        coEvery { statisticsRepository.getMyStudentPerformance(null, null) } returns Result.success(
            StudentPerformanceSummaryResponse(
                courseFilter = null,
                semesterFilter = null,
                plannedSubjects = 0,
                subjectsWithFinalResult = 0,
                subjectsCredited = 0,
                averageNumericGrade = null,
                totalPractices = 0,
                practicesWithResult = 0,
                subjectCompletionPercent = 0.0,
                practiceCompletionPercent = 0.0,
            ),
        )
        viewModel.loadInitial()
        advanceUntilIdle()
        assertEquals("x", viewModel.uiState.value.screenError)
        viewModel.dismissScreenError()
        assertNull(viewModel.uiState.value.screenError)
    }
}
