package com.example.app_my_university.data.repository

import com.example.app_my_university.data.network.TestApiServiceFactory
import com.example.app_my_university.domain.session.SessionManager
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AuthRepositoryMockWebServerTest {

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
    fun loginSuccess_parsesBodyAndSavesSession() = runBlocking {
        val body =
            """{"token":"tok","userId":7,"email":"u@x.ru","firstName":"Иван","lastName":"Петров","middleName":null,"userType":"STUDENT"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"),
        )
        val session = mockk<SessionManager>(relaxed = true)
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = AuthRepository(api, session)
        val r = repo.login("u@x.ru", "secret")
        assertTrue(r.isSuccess)
        coVerify(exactly = 1) {
            session.saveAuthData("tok", 7L, "STUDENT", "u@x.ru", "Петров Иван")
        }
        val req = server.takeRequest()
        assertEquals("/api/v1/auth/login", req.path)
    }

    @Test
    fun loginFailure_propagatesServerMessage() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"message":"Неверные учётные данные"}""")
                .addHeader("Content-Type", "application/json"),
        )
        val session = mockk<SessionManager>(relaxed = true)
        val api = TestApiServiceFactory.create(server.url("/").toString())
        val repo = AuthRepository(api, session)
        val r = repo.login("u@x.ru", "bad")
        assertTrue(r.isFailure)
        val msg = r.exceptionOrNull()?.message
        assertNotNull(msg)
        assertTrue(
            "unexpected: $msg",
            "Неверные" in msg!! || "401" in msg,
        )
    }
}
