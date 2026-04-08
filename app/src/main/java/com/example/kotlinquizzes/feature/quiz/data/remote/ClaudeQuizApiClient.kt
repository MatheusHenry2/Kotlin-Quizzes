package com.example.kotlinquizzes.feature.quiz.data.remote

import android.util.Log
import com.example.kotlinquizzes.BuildConfig
import com.example.kotlinquizzes.core.utils.Constants.TAG
import com.example.kotlinquizzes.feature.quiz.data.model.QuizDto
import com.example.kotlinquizzes.feature.quiz.data.model.QuizzesPayloadDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal Claude API client for adaptive quiz generation.
 * Uses HttpURLConnection so no extra HTTP dependency is needed.
 *
 * The API key is read from BuildConfig.CLAUDE_API_KEY (set via local.properties).
 * If the key is missing/blank the caller should fall back to a local generator.
 */
@Singleton
class ClaudeQuizApiClient @Inject constructor(
    private val json: Json,
) {

    private companion object {
        const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        const val MODEL = "claude-sonnet-4-5"
        const val API_VERSION = "2023-06-01"
        const val MAX_TOKENS = 4000
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 60_000
    }

    val isConfigured: Boolean
        get() = BuildConfig.CLAUDE_API_KEY.isNotBlank()

    suspend fun generateQuizzes(weakTags: List<String>, quizCount: Int, questionsPerQuiz: Int): List<QuizDto> =
        withContext(Dispatchers.IO) {
            require(isConfigured) { "Claude API key is not configured" }
            val prompt = buildPrompt(weakTags, quizCount, questionsPerQuiz)
            val requestBody = json.encodeToString(
                ClaudeRequest.serializer(),
                ClaudeRequest(
                    model = MODEL,
                    maxTokens = MAX_TOKENS,
                    messages = listOf(ClaudeMessage(role = "user", content = prompt)),
                ),
            )

            val text = post(requestBody)
            val payloadJson = extractJsonObject(text)
                ?: error("Claude response did not contain a JSON object")

            val payload = json.decodeFromString(QuizzesPayloadDto.serializer(), payloadJson)
            payload.quizzes
        }

    private fun post(body: String): String {
        val url = URL(ENDPOINT)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-api-key", BuildConfig.CLAUDE_API_KEY)
            setRequestProperty("anthropic-version", API_VERSION)
        }
        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                Log.e(TAG, "Claude API error $code: $response")
                error("Claude API HTTP $code")
            }
            val parsed = json.decodeFromString(ClaudeResponse.serializer(), response)
            return parsed.content.firstOrNull { it.type == "text" }?.text.orEmpty()
        } finally {
            connection.disconnect()
        }
    }

    private fun buildPrompt(weakTags: List<String>, quizCount: Int, questionsPerQuiz: Int): String {
        val tagsLine = if (weakTags.isEmpty()) {
            "general Kotlin and Android fundamentals"
        } else {
            weakTags.joinToString(", ")
        }
        return """
            You are generating adaptive practice quizzes for an Android learner.
            Focus the questions on these weak topics/tags: $tagsLine.

            Produce $quizCount quizzes. Each quiz must contain exactly $questionsPerQuiz multiple-choice questions.
            Each question must have 4 options and exactly one correct option.
            Vary questions; do not repeat the same question across quizzes.

            Respond with ONLY valid JSON (no markdown, no commentary) matching this schema:
            {
              "quizzes": [
                {
                  "id": "<unique_id>",
                  "title": "<short title>",
                  "questions": [
                    {
                      "id": "<unique_q_id>",
                      "text": "<question text>",
                      "options": ["a","b","c","d"],
                      "correctIndex": 0,
                      "tags": ["kotlin","..."]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
    }

    private fun extractJsonObject(text: String): String? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) return null
        return text.substring(start, end + 1)
    }

    @Serializable
    private data class ClaudeRequest(
        val model: String,
        @kotlinx.serialization.SerialName("max_tokens") val maxTokens: Int,
        val messages: List<ClaudeMessage>,
    )

    @Serializable
    private data class ClaudeMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class ClaudeResponse(
        val content: List<ClaudeContent> = emptyList(),
    )

    @Serializable
    private data class ClaudeContent(
        val type: String,
        val text: String = "",
    )
}
