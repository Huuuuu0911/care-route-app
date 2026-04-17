package com.example.cs501_final_project.data

import android.util.Base64
import com.example.cs501_final_project.BuildConfig
import com.example.cs501_final_project.network.Content
import com.example.cs501_final_project.network.GeminiApiService
import com.example.cs501_final_project.network.GeminiRequest
import com.example.cs501_final_project.network.InlineData
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
        logging.level = HttpLoggingInterceptor.Level.BASIC

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
        patient: PatientContext,
        recentHistory: List<String>
    ): List<String> {
        val historyText = if (recentHistory.isEmpty()) {
            "No recent records."
        } else {
            recentHistory.joinToString("\n") { "- $it" }
        }

        val prompt = """
            You are generating intake questions for a polished symptom-support app.
            Goal: ask exactly 3 short, personalized follow-up questions before the final recommendation.
            Do not give the final assessment yet.
            Do not mention being an AI.
            Keep the wording simple, direct, and app-friendly.

            Answer style preferences:
            - yes/no
            - better / same / worse
            - today / 1-3 days / more than a week / not sure
            - mild / moderate / severe

            Patient:
            Name: ${patient.displayName}
            Group: ${patient.group}
            Age: ${patient.age}
            Gender: ${patient.gender}
            Height: ${patient.height}
            Weight: ${patient.weight}
            Address: ${patient.address}
            Allergies: ${patient.allergies}
            Current medications: ${patient.medications}
            Conditions: ${patient.conditions}

            Symptom input:
            Body area: $bodyPart
            Symptom text: $symptomText
            Pain level 0-10: $painLevel

            Recent symptom history:
            $historyText

            Rules:
            - Tailor the 3 questions to the body area, age, conditions, medications, and allergies
            - Prioritize red flags relevant to the case
            - Make the questions easy to answer in a mobile app
            - Avoid repeating the same idea

            Return exactly this format:
            QUESTION_1: ...
            QUESTION_2: ...
            QUESTION_3: ...
        """.trimIndent()

        val text = requestText(listOf(Part(text = prompt)))
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
        patient: PatientContext,
        recentHistory: List<String>
    ): String {
        val answersText = followUpAnswers.joinToString("\n") { "- $it" }
        val historyText = if (recentHistory.isEmpty()) {
            "No recent records."
        } else {
            recentHistory.joinToString("\n") { "- $it" }
        }

        val prompt = """
            You are a conservative symptom-support assistant for a mobile demo app.
            This is NOT a diagnosis tool.
            Do not say you are an AI.
            Write in a clear, practical, calm style.
            Keep bullets short.
            Only give general low-risk OTC suggestions. Do not give dosing.
            If there is any chance the person needs urgent evaluation, say so clearly.

            Patient:
            Name: ${patient.displayName}
            Group: ${patient.group}
            Age: ${patient.age}
            Gender: ${patient.gender}
            Height: ${patient.height}
            Weight: ${patient.weight}
            Address: ${patient.address}
            Allergies: ${patient.allergies}
            Current medications: ${patient.medications}
            Conditions: ${patient.conditions}

            Symptom input:
            Body area: $bodyPart
            Symptom description: $symptomText
            Pain level 0-10: $painLevel

            Follow-up answers:
            $answersText

            Recent symptom history:
            $historyText

            Return EXACTLY this format:

            URGENCY: one of [Emergency | Urgent Care | Primary Care | Self Care]
            CARE_LEVEL: one of [ER_NOW | URGENT_CARE | PRIMARY_CARE | BUY_OTC | REST_AT_HOME]
            PLACE_TYPE: one of [hospital | urgent care | primary care | pharmacy | none]
            MAP_QUERY: short Google Maps search query or none
            RECOMMENDATION_SCORE: integer from 1 to 5

            SUMMARY:
            one short sentence

            KEY_POINTS:
            - point 1
            - point 2
            - point 3

            SELF_CARE:
            - step 1
            - step 2
            - step 3

            OTC_OPTIONS:
            - option 1
            - option 2
            - option 3

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
            - Never claim a diagnosis
            - OTC suggestions must be cautious and generic
            - If OTC options are not appropriate, write "Not appropriate without clinician or pharmacist guidance"
            - MAP_QUERY must match PLACE_TYPE
            - Recommendation score 5 means the app is very confident in the place/action category, not a medical certainty
        """.trimIndent()

        val text = requestText(listOf(Part(text = prompt)))
        return if (text.isBlank()) {
            structuredFallback(
                summary = "No response was received from the AI service.",
                keyPoints = listOf(
                    "The request completed without usable content",
                    "The service may be busy",
                    "Please try again"
                ),
                selfCare = listOf(
                    "Rest while you retry the request",
                    "Track any change in symptoms",
                    "Seek care sooner if symptoms become severe"
                ),
                otcOptions = listOf(
                    "Not appropriate without clinician or pharmacist guidance",
                    "Not appropriate without clinician or pharmacist guidance",
                    "Not appropriate without clinician or pharmacist guidance"
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

    suspend fun generateDailyHealthTip(
        patient: PatientContext,
        recentHistory: List<String>,
        importedMedicalNotes: List<String>
    ): DailyHealthTip {
        val historyText = buildArchiveContext(recentHistory, importedMedicalNotes)
        val today = todayKey()
        val prompt = """
            You are writing one daily health tip for a mobile health support app.
            Keep it practical, calm, and general.
            Do not diagnose.
            Tailor the tip lightly using the profile and recent history.
            Keep the main message under 35 words.
            Keep the caution under 18 words.

            Patient:
            Name: ${patient.displayName}
            Age: ${patient.age}
            Birth date: ${patient.birthDate}
            Gender: ${patient.gender}
            Conditions: ${patient.conditions}
            Allergies: ${patient.allergies}
            Medications: ${patient.medications}

            Recent records:
            $historyText

            Return exactly this format:
            TITLE: ...
            FOCUS: ...
            TIP: ...
            CAUTION: ...
        """.trimIndent()

        val text = requestText(listOf(Part(text = prompt)))
        if (text.isBlank()) {
            return fallbackDailyTip(patient, today)
        }

        return DailyHealthTip(
            title = extractSingleLine(text, "TITLE").ifBlank { fallbackDailyTip(patient, today).title },
            focusArea = extractSingleLine(text, "FOCUS").ifBlank { "Prevention" },
            message = extractSingleLine(text, "TIP").ifBlank { fallbackDailyTip(patient, today).message },
            caution = extractSingleLine(text, "CAUTION").ifBlank { "Seek urgent care if symptoms escalate quickly." },
            generatedDate = today,
            personId = patient.id,
            source = "Gemini"
        )
    }

    suspend fun generateCheckupSuggestions(
        patient: PatientContext,
        recentHistory: List<String>,
        importedMedicalNotes: List<String>
    ): List<PersonalizedCheckupSuggestion> {
        val historyText = buildArchiveContext(recentHistory, importedMedicalNotes)
        val today = todayKey()
        val prompt = """
            You are creating 3 concise personalized checkup suggestions for a health support app.
            This is preventive guidance only, not a diagnosis.
            Tailor the suggestions using age, birth date, gender, conditions, medications, allergies, and recent health history.
            Each suggestion should feel practical for a mobile app.

            Patient:
            Name: ${patient.displayName}
            Age: ${patient.age}
            Birth date: ${patient.birthDate}
            Gender: ${patient.gender}
            Conditions: ${patient.conditions}
            Allergies: ${patient.allergies}
            Medications: ${patient.medications}

            Recent records:
            $historyText

            Return exactly this format:
            ITEM_1: title | reason | timeframe | priority(1-5)
            ITEM_2: title | reason | timeframe | priority(1-5)
            ITEM_3: title | reason | timeframe | priority(1-5)
        """.trimIndent()

        val text = requestText(listOf(Part(text = prompt)))
        if (text.isBlank()) {
            return fallbackCheckupSuggestions(patient, today)
        }

        val parsed = listOfNotNull(
            parseSuggestionLine(extractSingleLine(text, "ITEM_1"), patient.id, today, "General wellness visit"),
            parseSuggestionLine(extractSingleLine(text, "ITEM_2"), patient.id, today, "Medication and allergy review"),
            parseSuggestionLine(extractSingleLine(text, "ITEM_3"), patient.id, today, "Symptom trend follow-up")
        )

        return if (parsed.isEmpty()) fallbackCheckupSuggestions(patient, today) else parsed
    }

    suspend fun structureManualMedicalRecord(
        patient: PatientContext,
        title: String,
        rawText: String
    ): ImportedMedicalRecordDraft {
        val safeTitle = title.ifBlank { "Past medical note" }
        val prompt = """
            You are structuring a user's past medical note for a mobile health archive.
            Keep the output conservative and app-friendly.
            Do not diagnose.
            Summarize only what is reasonably described.

            Patient: ${patient.displayName}, age ${patient.age}, gender ${patient.gender}
            Record title: $safeTitle
            Raw note:
            $rawText

            Return exactly this format:
            TITLE: ...
            SUMMARY: ...
            FINDINGS:
            - item 1
            - item 2
            - item 3
            FOLLOW_UP:
            - item 1
            - item 2
            - item 3
        """.trimIndent()

        val text = requestText(listOf(Part(text = prompt)))
        if (text.isBlank()) {
            return ImportedMedicalRecordDraft(
                title = safeTitle,
                summary = rawText.take(180).ifBlank { "Manual medical record saved." },
                findings = listOf("Manual note added to the archive."),
                recommendedFollowUp = listOf("Review this note during your next visit."),
                rawText = rawText
            )
        }

        return ImportedMedicalRecordDraft(
            title = extractSingleLine(text, "TITLE").ifBlank { safeTitle },
            summary = extractSingleLine(text, "SUMMARY").ifBlank { rawText.take(180) },
            findings = extractBullets(text, "FINDINGS", listOf("Manual record added to the archive.")),
            recommendedFollowUp = extractBullets(text, "FOLLOW_UP", listOf("Bring this note to a clinician if symptoms return.")),
            rawText = rawText
        )
    }

    suspend fun summarizeMedicalRecordImage(
        patient: PatientContext,
        imageBytes: ByteArray,
        mimeType: String
    ): ImportedMedicalRecordDraft {
        val encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val prompt = """
            You are reviewing a patient-uploaded medical note or document image for a mobile health archive.
            Summarize only what is visible or strongly implied.
            Do not diagnose.
            If the image is unclear, say so briefly in the summary.
            Keep the output concise and structured.

            Patient: ${patient.displayName}, age ${patient.age}, gender ${patient.gender}

            Return exactly this format:
            TITLE: ...
            SUMMARY: ...
            FINDINGS:
            - item 1
            - item 2
            - item 3
            FOLLOW_UP:
            - item 1
            - item 2
            - item 3
        """.trimIndent()

        val text = requestText(
            listOf(
                Part(text = prompt),
                Part(
                    inlineData = InlineData(
                        mimeType = mimeType,
                        data = encoded
                    )
                )
            )
        )

        if (text.isBlank()) {
            return ImportedMedicalRecordDraft(
                title = "Uploaded medical record",
                summary = "The document was saved, but the image could not be summarized right now.",
                findings = listOf("Try uploading a clearer image.", "Keep the original record available."),
                recommendedFollowUp = listOf("Review it later with a clinician or pharmacist."),
                rawText = ""
            )
        }

        return ImportedMedicalRecordDraft(
            title = extractSingleLine(text, "TITLE").ifBlank { "Uploaded medical record" },
            summary = extractSingleLine(text, "SUMMARY").ifBlank { "Document uploaded to the health archive." },
            findings = extractBullets(text, "FINDINGS", listOf("No clear findings extracted from the document.")),
            recommendedFollowUp = extractBullets(text, "FOLLOW_UP", listOf("Review this document during the next visit if needed.")),
            rawText = ""
        )
    }

    private suspend fun requestText(parts: List<Part>): String {
        val request = GeminiRequest(
            contents = listOf(
                Content(parts = parts)
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
                    ?.firstOrNull { !it.text.isNullOrBlank() }
                    ?.text

                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (e: HttpException) {
                if (e.code() == 503 && attempt == 0) {
                    delay(1200)
                    return@repeat
                }
                return ""
            } catch (_: Exception) {
                if (attempt == 0) {
                    delay(900)
                    return@repeat
                }
                return ""
            }
        }

        return ""
    }

    private fun extractSingleLine(text: String, key: String): String {
        return text.lines()
            .firstOrNull { it.trim().startsWith("$key:") }
            ?.substringAfter("$key:")
            ?.trim()
            .orEmpty()
    }

    private fun extractBullets(text: String, key: String, fallback: List<String>): List<String> {
        val lines = text.lines()
        val startIndex = lines.indexOfFirst { it.trim().startsWith("$key:") }
        if (startIndex == -1) return fallback

        val collected = mutableListOf<String>()
        for (index in startIndex + 1 until lines.size) {
            val line = lines[index].trim()
            if (line.endsWith(":") && !line.startsWith("-") && !line.startsWith("•")) break
            if (line.startsWith("- ")) collected.add(line.removePrefix("- ").trim())
            if (line.startsWith("• ")) collected.add(line.removePrefix("• ").trim())
        }
        return if (collected.isEmpty()) fallback else collected
    }

    private fun parseSuggestionLine(
        line: String,
        personId: String,
        generatedDate: String,
        fallbackTitle: String
    ): PersonalizedCheckupSuggestion? {
        if (line.isBlank()) return null
        val pieces = line.split("|").map { it.trim() }
        val title = pieces.getOrNull(0).orEmpty().ifBlank { fallbackTitle }
        val reason = pieces.getOrNull(1).orEmpty().ifBlank { "Useful based on your profile and recent records." }
        val timeframe = pieces.getOrNull(2).orEmpty().ifBlank { "In the next routine visit" }
        val priority = pieces.getOrNull(3)?.filter { it.isDigit() }?.toIntOrNull()?.coerceIn(1, 5) ?: 3
        return PersonalizedCheckupSuggestion(
            id = title + generatedDate + personId,
            title = title,
            reason = reason,
            timeframe = timeframe,
            priority = priority,
            personId = personId,
            generatedDate = generatedDate
        )
    }

    private fun buildArchiveContext(recentHistory: List<String>, importedMedicalNotes: List<String>): String {
        val checks = if (recentHistory.isEmpty()) {
            "No recent symptom checks."
        } else {
            recentHistory.joinToString("\n") { "- $it" }
        }
        val records = if (importedMedicalNotes.isEmpty()) {
            "No saved medical notes."
        } else {
            importedMedicalNotes.joinToString("\n") { "- $it" }
        }
        return "Symptom checks:\n$checks\n\nSaved records:\n$records"
    }

    private fun fallbackDailyTip(patient: PatientContext, today: String): DailyHealthTip {
        val message = when {
            patient.conditions.contains("asthma", ignoreCase = true) -> "Keep rescue medicines easy to access and note any breathing trigger that appears today."
            patient.conditions.contains("diabetes", ignoreCase = true) -> "Pair symptom checks with a quick note about meals, energy, and any glucose-related concerns."
            patient.allergies.isNotBlank() -> "Check labels carefully before trying any over-the-counter medicine or new supplement."
            else -> "Track start time, severity changes, and anything that makes the symptom better or worse."
        }
        return DailyHealthTip(
            title = "Today's health note",
            message = message,
            focusArea = "Self-monitoring",
            caution = "Get urgent help for severe breathing trouble, fainting, or heavy bleeding.",
            generatedDate = today,
            personId = patient.id
        )
    }

    private fun fallbackCheckupSuggestions(
        patient: PatientContext,
        today: String
    ): List<PersonalizedCheckupSuggestion> {
        val items = mutableListOf(
            PersonalizedCheckupSuggestion(
                id = "wellness-$today-${patient.id}",
                title = "General wellness visit",
                reason = "A routine visit keeps baseline health information current for future symptom checks.",
                timeframe = "Within the next 3 to 6 months",
                priority = 3,
                personId = patient.id,
                generatedDate = today
            ),
            PersonalizedCheckupSuggestion(
                id = "meds-$today-${patient.id}",
                title = "Medication and allergy review",
                reason = "Keeping a current medication and allergy list reduces risks when symptoms flare up.",
                timeframe = "At your next routine appointment",
                priority = 4,
                personId = patient.id,
                generatedDate = today
            )
        )

        val ageValue = patient.age.toIntOrNull()
        val ageSpecific = if (ageValue != null && ageValue >= 40) {
            PersonalizedCheckupSuggestion(
                id = "preventive-$today-${patient.id}",
                title = "Preventive screening review",
                reason = "Age-related preventive screening becomes more useful as general health risks gradually increase.",
                timeframe = "Discuss in the next routine visit",
                priority = 5,
                personId = patient.id,
                generatedDate = today
            )
        } else {
            PersonalizedCheckupSuggestion(
                id = "baseline-$today-${patient.id}",
                title = "Baseline preventive check",
                reason = "A baseline exam helps future symptom visits feel faster and more personalized.",
                timeframe = "During your next annual visit",
                priority = 3,
                personId = patient.id,
                generatedDate = today
            )
        }

        items.add(ageSpecific)
        return items.take(3)
    }

    private fun structuredFallback(
        urgency: String = "Primary Care",
        careLevel: String = "PRIMARY_CARE",
        placeType: String = "primary care",
        mapQuery: String = "primary care clinic near me",
        recommendationScore: Int = 3,
        summary: String,
        keyPoints: List<String>,
        selfCare: List<String>,
        otcOptions: List<String>,
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
            CARE_LEVEL: $careLevel
            PLACE_TYPE: $placeType
            MAP_QUERY: $mapQuery
            RECOMMENDATION_SCORE: $recommendationScore

            SUMMARY:
            $summary

            KEY_POINTS:
            - ${keyPoints.getOrElse(0) { "No key point" }}
            - ${keyPoints.getOrElse(1) { "No key point" }}
            - ${keyPoints.getOrElse(2) { "No key point" }}

            SELF_CARE:
            - ${selfCare.getOrElse(0) { "Rest and monitor symptoms" }}
            - ${selfCare.getOrElse(1) { "Track symptom changes" }}
            - ${selfCare.getOrElse(2) { "Seek help if symptoms worsen" }}

            OTC_OPTIONS:
            - ${otcOptions.getOrElse(0) { "Not appropriate without clinician or pharmacist guidance" }}
            - ${otcOptions.getOrElse(1) { "Not appropriate without clinician or pharmacist guidance" }}
            - ${otcOptions.getOrElse(2) { "Not appropriate without clinician or pharmacist guidance" }}

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

    private fun todayKey(): String {
        val now = java.util.Calendar.getInstance()
        val month = (now.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = now.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val year = now.get(java.util.Calendar.YEAR)
        return "$year-$month-$day"
    }
}
