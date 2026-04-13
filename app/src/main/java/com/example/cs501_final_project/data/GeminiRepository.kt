package com.example.cs501_final_project.data

import com.example.cs501_final_project.BuildConfig
import com.example.cs501_final_project.network.Content
import com.example.cs501_final_project.network.GeminiApiService
import com.example.cs501_final_project.network.GeminiRequest
import com.example.cs501_final_project.network.Part
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiRepository {

    private val api: GeminiApiService

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        api = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getFollowUpQuestions(
        bodyPart: String,
        symptomText: String,
        painLevel: Int,
        age: String = "",
        gender: String = "",
        height: String = "",
        weight: String = "",
        address: String = ""
    ): List<String> {
        val prompt = """
            You are helping a student demo medical app.
            The app needs exactly 3 short follow-up questions before showing a final triage-style response.
            Do not give the final assessment yet.
            Do not say you are an AI.
            Keep the questions short, practical, and easy for a user to answer.

            Prefer questions that can be answered with:
            - yes/no
            - better/same/worse
            - today / 1-3 days / more than a week / not sure
            - mild / moderate / severe

            Body area: $bodyPart
            Symptom: $symptomText
            Pain level from 0 to 10: $painLevel
            Age: $age
            Gender: $gender
            Height: $height
            Weight: $weight
            Address: $address

            Return exactly this format:

            QUESTION_1: ...
            QUESTION_2: ...
            QUESTION_3: ...
        """.trimIndent()

        val text = requestText(prompt)
        if (text.isBlank()) {
            return listOf(
                "Has it been getting worse?",
                "When did it start?",
                "Do you have any other symptoms?"
            )
        }

        val q1 = extractSingleLine(text, "QUESTION_1").ifBlank { "Has it been getting worse?" }
        val q2 = extractSingleLine(text, "QUESTION_2").ifBlank { "When did it start?" }
        val q3 = extractSingleLine(text, "QUESTION_3").ifBlank { "Do you have any other symptoms?" }

        return listOf(q1, q2, q3)
    }

    suspend fun askGeminiFinal(
        bodyPart: String,
        symptomText: String,
        painLevel: Int,
        followUpAnswers: List<String>,
        age: String = "",
        gender: String = "",
        height: String = "",
        weight: String = "",
        address: String = ""
    ): String {
        val answersText = followUpAnswers.joinToString("\n") { "- $it" }

        val prompt = """
            You are a symptom triage assistant for a student demo medical app.
            Do not give a final diagnosis.
            Do not say you are an AI.
            Write in a clean, app-friendly style.
            Keep each item short and practical.

            User selected body area: $bodyPart
            Symptom description: $symptomText
            Pain level from 0 to 10: $painLevel
            Follow-up answers:
            $answersText

            Age: $age
            Gender: $gender
            Height: $height
            Weight: $weight
            Address: $address

            Return the answer in EXACTLY this format:

            URGENCY: one of [Emergency | Urgent Care | Primary Care | Self Care]

            SUMMARY:
            one short sentence

            KEY_POINTS:
            - point 1
            - point 2
            - point 3

            NEXT_STEPS:
            - step 1
            - step 2
            - step 3

            WARNING_SIGNS:
            - warning 1
            - warning 2
            - warning 3

            NOTES:
            one short paragraph only

            Rules:
            - Never skip a section
            - Use short bullet points
            - Keep the tone calm and practical
            - Warning signs should be specific
            - Do not add extra headings outside the required format
        """.trimIndent()

        val text = requestText(prompt)
        return if (text.isBlank()) {
            structuredFallback(
                summary = "No response was received from the AI service.",
                keyPoints = listOf(
                    "The request completed without usable content",
                    "The service may be busy",
                    "Please try again"
                ),
                nextSteps = listOf(
                    "Retry in a moment",
                    "Check the API key",
                    "Review network connection"
                ),
                notes = "The response body did not contain usable text."
            )
        } else {
            text
        }
    }

    private suspend fun requestText(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            )
        )

        repeat(2) { attempt ->
            try {
                val response = api.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val text = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                if (!text.isNullOrBlank()) {
                    return text
                }

            } catch (e: HttpException) {
                val statusCode = e.code()

                if (statusCode == 503 && attempt == 0) {
                    delay(1500)
                    return@repeat
                }

                return when (statusCode) {
                    400 -> structuredFallback(
                        summary = "The request format was not accepted.",
                        keyPoints = listOf(
                            "The API request may be malformed",
                            "Please review the input or prompt",
                            "Try again after checking setup"
                        ),
                        nextSteps = listOf(
                            "Check request format",
                            "Review API settings",
                            "Try again"
                        ),
                        notes = "The server returned HTTP 400."
                    )

                    401, 403 -> structuredFallback(
                        summary = "The API key or access setting may be incorrect.",
                        keyPoints = listOf(
                            "Authentication failed",
                            "The API key may be invalid",
                            "Access may not be enabled"
                        ),
                        nextSteps = listOf(
                            "Check your Gemini API key",
                            "Confirm the API is enabled",
                            "Try again after updating the key"
                        ),
                        notes = "The server returned HTTP $statusCode."
                    )

                    429 -> structuredFallback(
                        summary = "Too many requests were sent in a short time.",
                        keyPoints = listOf(
                            "Rate limit was reached",
                            "The service asked the app to slow down",
                            "This is usually temporary"
                        ),
                        nextSteps = listOf(
                            "Wait a moment",
                            "Try again later",
                            "Reduce repeated requests"
                        ),
                        notes = "The server returned HTTP 429."
                    )

                    503 -> structuredFallback(
                        summary = "The AI service is temporarily busy.",
                        keyPoints = listOf(
                            "The Gemini service is under high demand",
                            "Your request did reach the server",
                            "This is usually temporary"
                        ),
                        nextSteps = listOf(
                            "Try again in a moment",
                            "Keep your symptom text short and clear",
                            "Retry after a short pause"
                        ),
                        notes = "The server returned HTTP 503. This is usually a temporary service issue, not an app UI problem."
                    )

                    else -> structuredFallback(
                        summary = "The request could not be completed.",
                        keyPoints = listOf(
                            "The server returned an unexpected response",
                            "This may be temporary",
                            "Please try again"
                        ),
                        nextSteps = listOf(
                            "Try again later",
                            "Check network connection",
                            "Review API setup if the issue continues"
                        ),
                        notes = "The server returned HTTP $statusCode."
                    )
                }

            } catch (e: Exception) {
                if (attempt == 0) {
                    delay(1000)
                    return@repeat
                }

                return structuredFallback(
                    summary = "The app could not reach the AI service.",
                    keyPoints = listOf(
                        "A network or runtime error happened",
                        "The request did not complete normally",
                        "Please try again"
                    ),
                    nextSteps = listOf(
                        "Check internet connection",
                        "Try again later",
                        "Review app setup if it keeps happening"
                    ),
                    notes = "${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }

        return structuredFallback(
            summary = "No response was received from the AI service.",
            keyPoints = listOf(
                "The request completed without usable content",
                "The service may be busy",
                "Please try again"
            ),
            nextSteps = listOf(
                "Retry in a moment",
                "Check the API key",
                "Review network connection"
            ),
            notes = "The response body did not contain usable text."
        )
    }

    private fun extractSingleLine(text: String, key: String): String {
        return text.lines()
            .firstOrNull { it.trim().startsWith("$key:") }
            ?.substringAfter("$key:")
            ?.trim()
            .orEmpty()
    }

    private fun structuredFallback(
        urgency: String = "Primary Care",
        summary: String,
        keyPoints: List<String>,
        nextSteps: List<String>,
        warningSigns: List<String> = listOf(
            "Severe pain",
            "Trouble breathing",
            "Fainting"
        ),
        notes: String
    ): String {
        return """
            URGENCY: $urgency

            SUMMARY:
            $summary

            KEY_POINTS:
            - ${keyPoints.getOrElse(0) { "No key point" }}
            - ${keyPoints.getOrElse(1) { "No key point" }}
            - ${keyPoints.getOrElse(2) { "No key point" }}

            NEXT_STEPS:
            - ${nextSteps.getOrElse(0) { "Try again later" }}
            - ${nextSteps.getOrElse(1) { "Check setup" }}
            - ${nextSteps.getOrElse(2) { "Review connection" }}

            WARNING_SIGNS:
            - ${warningSigns.getOrElse(0) { "Severe pain" }}
            - ${warningSigns.getOrElse(1) { "Trouble breathing" }}
            - ${warningSigns.getOrElse(2) { "Fainting" }}

            NOTES:
            $notes
        """.trimIndent()
    }
}