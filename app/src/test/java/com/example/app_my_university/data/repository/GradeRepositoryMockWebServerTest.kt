package com.example.app_my_university.data.repository

import com.example.app_my_university.data.network.TestApiServiceFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GradeRepositoryMockWebServerTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getMyGrades_emptyList() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"),
        )
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = GradeRepository(api)
        val r = repo.getMyGrades()
        assertTrue(r.isSuccess)
        assertTrue(r.getOrNull().isNullOrEmpty())
        assertEquals("/api/v1/grades/my", server.takeRequest().path)
    }

    @Test
    fun getMyGrades_parsesItems() = runBlocking {
        val json =
            """[{"id":1,"studentId":10,"studentName":null,"subjectDirectionId":99,"subjectName":"Математика","grade":5,"creditStatus":null,"finalAssessmentType":"EXAM","course":1,"semester":1,"directionName":null,"practiceCount":0}]"""
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .addHeader("Content-Type", "application/json"),
        )
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = GradeRepository(api)
        val r = repo.getMyGrades()
        assertTrue(r.isSuccess)
        val list = r.getOrNull()!!
        assertEquals(1, list.size)
        assertEquals(99L, list[0].subjectDirectionId)
        assertEquals("Математика", list[0].subjectName)
    }

    @Test
    fun getMyGrades_httpError_propagatesMessage() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"message":"Внутренняя ошибка"}""")
                .addHeader("Content-Type", "application/json"),
        )
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = GradeRepository(api)
        val r = repo.getMyGrades()
        assertTrue(r.isFailure)
        assertEquals("Внутренняя ошибка", r.exceptionOrNull()?.message)
    }
}
