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

class NotificationsRepositoryMockWebServerTest {

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
    fun getMyNotifications_parsesList() = runBlocking {
        val json =
            """[{"id":1,"kind":"GRADE","title":"Оценка","body":"Тест","readAt":null,"createdAt":"2026-01-01T00:00:00Z"}]"""
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .addHeader("Content-Type", "application/json"),
        )
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = NotificationsRepository(api)
        val r = repo.getMyNotifications()
        assertTrue(r.isSuccess)
        assertEquals(1, r.getOrNull()!!.size)
        assertEquals("Оценка", r.getOrNull()!![0].title)
    }

    @Test
    fun markRead_noContent_success() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = NotificationsRepository(api)
        val r = repo.markRead(5L)
        assertTrue(r.isSuccess)
        assertEquals("/api/v1/notifications/5/read", server.takeRequest().path)
    }
}
