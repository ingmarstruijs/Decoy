package nl.decoy.privacy.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BannedIdentityInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = PrivacyHttpClient.create()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun allowsCleanRequest() {
        server.enqueue(MockResponse().setBody("ok"))
        val response = client.newCall(
            Request.Builder().url(server.url("/menu")).build(),
        ).execute()
        assertTrue(response.isSuccessful)
        response.close()
    }

    @Test
    fun rejectsBannedHeader() {
        server.enqueue(MockResponse().setBody("ok"))
        assertThrows(BannedIdentityException::class.java) {
            client.newCall(
                Request.Builder()
                    .url(server.url("/"))
                    .header("X-Advertising-Id", "deadbeef")
                    .build(),
            ).execute()
        }
    }

    @Test
    fun rejectsBannedQueryToken() {
        server.enqueue(MockResponse().setBody("ok"))
        assertThrows(BannedIdentityException::class.java) {
            client.newCall(
                Request.Builder()
                    .url(server.url("/x?gaid=abc"))
                    .build(),
            ).execute()
        }
    }

    @Test
    fun doesNotResendCookies() {
        server.enqueue(
            MockResponse()
                .addHeader("Set-Cookie", "tracker=1; Path=/")
                .setBody("ok"),
        )
        server.enqueue(MockResponse().setBody("ok"))
        client.newCall(Request.Builder().url(server.url("/a")).build()).execute().close()
        client.newCall(Request.Builder().url(server.url("/b")).build()).execute().close()
        server.takeRequest() // /a
        val second = server.takeRequest() // /b
        assertFalse(second.getHeader("Cookie")?.contains("tracker") == true)
    }
}
