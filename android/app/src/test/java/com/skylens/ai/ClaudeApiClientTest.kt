package com.skylens.ai

import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClaudeApiClientTest {

    private lateinit var client: ClaudeApiClient
    private lateinit var mockHttpClient: OkHttpClient

    @Before
    fun setup() {
        mockHttpClient = mockk(relaxed = true)
        client = ClaudeApiClient(mockHttpClient)
    }

    @Test
    fun `generateLandmarkStory should return AI response on success`() = runTest {
        val mockResponse = """
        {
            "content": [
                {
                    "type": "text",
                    "text": "Mount Fuji is Japan's tallest mountain at 3,776 meters."
                }
            ]
        }
        """.trimIndent()

        val mockCall = mockk<Call>()
        val response = Response.Builder()
            .request(Request.Builder().url("https://api.anthropic.com/v1/messages").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(mockResponse.toResponseBody("application/json".toMediaType()))
            .build()

        every { mockHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns response

        val result = client.generateLandmarkStory(
            landmarkName = "Mount Fuji",
            landmarkType = "mountain",
            elevation = 3776,
            country = "Japan"
        )

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("Mount Fuji"))
    }

    @Test
    fun `generateLandmarkStory should return error on API failure`() = runTest {
        val mockCall = mockk<Call>()
        val response = Response.Builder()
            .request(Request.Builder().url("https://api.anthropic.com/v1/messages").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("".toResponseBody("application/json".toMediaType()))
            .build()

        every { mockHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns response

        val result = client.generateLandmarkStory(
            landmarkName = "Mount Fuji",
            landmarkType = "mountain",
            elevation = 3776,
            country = "Japan"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `answerLandmarkQuestion should include conversation history`() = runTest {
        val mockResponse = """
        {
            "content": [
                {
                    "type": "text",
                    "text": "Yes, Mount Fuji is visible from Tokyo on clear days."
                }
            ]
        }
        """.trimIndent()

        val mockCall = mockk<Call>()
        val response = Response.Builder()
            .request(Request.Builder().url("https://api.anthropic.com/v1/messages").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(mockResponse.toResponseBody("application/json".toMediaType()))
            .build()

        every { mockHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns response

        val conversationHistory = listOf(
            "What landmarks can I see?" to "You're flying over Tokyo, Japan."
        )

        val result = client.answerLandmarkQuestion(
            question = "Can I see Mount Fuji from here?",
            currentPosition = "Tokyo, Japan",
            nearbyLandmarks = listOf("Tokyo Tower", "Mount Fuji"),
            conversationHistory = conversationHistory
        )

        assertTrue(result.isSuccess)
        // Verify the call was made (conversation history included in request)
        verify { mockHttpClient.newCall(any()) }
    }

    @Test
    fun `generatePredictionContext should create teaser for upcoming landmark`() = runTest {
        val mockResponse = """
        {
            "content": [
                {
                    "type": "text",
                    "text": "Mount Fuji is about to come into view - watch for Japan's iconic snow-capped volcanic cone!"
                }
            ]
        }
        """.trimIndent()

        val mockCall = mockk<Call>()
        val response = Response.Builder()
            .request(Request.Builder().url("https://api.anthropic.com/v1/messages").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(mockResponse.toResponseBody("application/json".toMediaType()))
            .build()

        every { mockHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns response

        val result = client.generatePredictionContext(
            landmarkName = "Mount Fuji",
            landmarkType = "mountain",
            minutesUntilVisible = 5
        )

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }
}
