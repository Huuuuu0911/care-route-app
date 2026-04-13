package com.example.cs501_final_project.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs501_final_project.ui.*

object Routes {
    const val HOME = "home"
    const val BODY_PART = "body_part"
    const val BODY_PART_3D = "body_part_3d"
    const val DETAIL = "detail"
    const val FOLLOW_UP = "follow_up"
    const val TRIAGE = "triage"
    const val RESULT = "result"
    const val MAP = "map"
    const val HISTORY = "history"
}

@Composable
fun AppNav() {

    val navController = rememberNavController()

    var symptom by rememberSaveable { mutableStateOf("") }
    var painLevel by rememberSaveable { mutableStateOf(5f) }
    var duration by rememberSaveable { mutableStateOf("") }

    var urgency by rememberSaveable { mutableStateOf("") }
    var recommendation by rememberSaveable { mutableStateOf("") }

    var submittedSymptom by rememberSaveable { mutableStateOf("") }
    var submittedPainLevel by rememberSaveable { mutableStateOf(5) }
    var submittedDuration by rememberSaveable { mutableStateOf("") }

    var history by rememberSaveable(
        stateSaver = listSaver(
            save = { it },
            restore = { it.toMutableList() }
        )
    ) {
        mutableStateOf(mutableListOf<String>())
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onStartClick = {
                    navController.navigate(Routes.BODY_PART_3D)
                },
                onHistoryClick = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        composable(Routes.BODY_PART_3D) {
            BodyPart3DScreen(navController)
        }

        composable("detail/{part}") {
            val part = Uri.decode(it.arguments?.getString("part") ?: "")

            DetailScreen(
                part = part,
                navController = navController
            )
        }

        composable("follow_up/{part}/{symptom}/{pain}") {
            val part = Uri.decode(it.arguments?.getString("part") ?: "")
            val symptomArg = Uri.decode(it.arguments?.getString("symptom") ?: "")
            val pain = it.arguments?.getString("pain")?.toIntOrNull() ?: 0

            FollowUpScreen(
                part = part,
                symptomText = symptomArg,
                painLevel = pain,
                navController = navController
            )
        }

        composable(Routes.TRIAGE) {
            TriageScreen(
                symptom = symptom,
                onSymptomChange = { symptom = it },
                painLevel = painLevel,
                onPainLevelChange = { painLevel = it },
                duration = duration,
                onDurationChange = { duration = it },
                onVoiceResult = { spokenText ->
                    symptom = spokenText
                },
                onSubmitClick = {

                    if (symptom.isBlank()) {
                        urgency = "Input Needed"
                        recommendation = "Please enter your symptom first."
                        submittedSymptom = ""
                        submittedPainLevel = painLevel.toInt()
                        submittedDuration = duration
                    } else {

                        submittedSymptom = symptom
                        submittedPainLevel = painLevel.toInt()
                        submittedDuration = duration

                        val result = getTriageResult(
                            symptom = symptom,
                            painLevel = painLevel.toInt(),
                            duration = duration
                        )

                        urgency = result.first
                        recommendation = result.second

                        val item =
                            "Symptom: $symptom | Pain: ${painLevel.toInt()} | Duration: $duration | Result: $urgency"
                        history = (history + item).toMutableList()

                        symptom = ""
                        painLevel = 5f
                        duration = ""
                    }

                    navController.navigate(Routes.RESULT)
                }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                urgency = urgency,
                recommendation = recommendation,
                symptom = submittedSymptom,
                painLevel = submittedPainLevel,
                duration = submittedDuration,
                onFindCareClick = {
                    navController.navigate(Routes.MAP)
                },
                onViewHistoryClick = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        composable(Routes.MAP) {
            MapScreen(
                urgency = urgency,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                history = history,
                onHistoryItemClick = { },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

fun getTriageResult(
    symptom: String,
    painLevel: Int,
    duration: String
): Pair<String, String> {

    val text = symptom.lowercase()
    val timeText = duration.lowercase()

    return when {
        text.contains("chest") ||
                text.contains("shortness of breath") ||
                text.contains("can't breathe") ||
                painLevel >= 8 -> {
            "Emergency" to "Go to ER immediately."
        }

        text.contains("fever") ||
                text.contains("vomit") ||
                painLevel >= 5 ||
                timeText.contains("day") -> {
            "Urgent Care" to "Visit urgent care."
        }

        else -> {
            "Primary Care" to "Schedule a doctor visit."
        }
    }
}