package com.example.cs501_final_project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs501_final_project.ui.HistoryScreen
import com.example.cs501_final_project.ui.HomeScreen
import com.example.cs501_final_project.ui.MapScreen
import com.example.cs501_final_project.ui.ResultScreen
import com.example.cs501_final_project.ui.TriageScreen
import com.example.cs501_final_project.ui.BodyPartScreen
import com.example.cs501_final_project.ui.BodyPart3DScreen

// app routes
object Routes {
    const val HOME = "home"
    const val BODY_PART = "body_part"
    const val BODY_PART_3D = "body_part_3d"
    const val TRIAGE = "triage"
    const val RESULT = "result"
    const val MAP = "map"
    const val HISTORY = "history"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    // user input state
    // rememberSaveable keeps small UI state across rotation/config changes
    var symptom by rememberSaveable { mutableStateOf("") }
    var painLevel by rememberSaveable { mutableStateOf(5f) }
    var duration by rememberSaveable { mutableStateOf("") }

    // result state
    var urgency by rememberSaveable { mutableStateOf("") }
    var recommendation by rememberSaveable { mutableStateOf("") }

    // values shown on result screen
    var submittedSymptom by rememberSaveable { mutableStateOf("") }
    var submittedPainLevel by rememberSaveable { mutableStateOf(5) }
    var submittedDuration by rememberSaveable { mutableStateOf("") }

    // history state
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

        composable(Routes.BODY_PART) {
            BodyPartScreen(
                onBodyPartSelected = { selected ->
                    navController.navigate(Routes.TRIAGE)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.BODY_PART_3D) {
            BodyPart3DScreen(
                onBodyPartSelected = { selected ->
                    navController.navigate(Routes.TRIAGE)
                },
                onBackClick = {
                    navController.popBackStack()
                }
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
                    // validation
                    if (symptom.isBlank()) {
                        urgency = "Input Needed"
                        recommendation = "Please enter your symptom first."
                        submittedSymptom = ""
                        submittedPainLevel = painLevel.toInt()
                        submittedDuration = duration
                    } else {
                        // keep a copy for result screen before clearing inputs
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

                        // save history
                        val item =
                            "Symptom: $symptom | Pain: ${painLevel.toInt()} | Duration: $duration | Result: $urgency"
                        history = (history + item).toMutableList()

                        // clear input back to initial values
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

        composable("history") {
            HistoryScreen(
                history = history,
                onHistoryItemClick = { },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// simple rule-based triage logic
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
                text.contains("can’t breathe") ||
                painLevel >= 8 -> {
            "Emergency" to "Your symptoms may need immediate attention. Please go to the ER or call emergency services if things get worse."
        }

        text.contains("fever") ||
                text.contains("vomit") ||
                text.contains("infection") ||
                painLevel >= 5 ||
                timeText.contains("day") -> {
            "Urgent Care" to "Your symptoms may need same-day care. Urgent care is a reasonable next step."
        }

        else -> {
            "Primary Care" to "Your symptoms seem less urgent. You can start with a regular doctor visit."
        }
    }
}