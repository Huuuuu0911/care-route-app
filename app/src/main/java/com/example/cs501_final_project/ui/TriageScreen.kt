package com.example.cs501_final_project.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun TriageScreen(
    symptom: String,
    onSymptomChange: (String) -> Unit,
    painLevel: Float,
    onPainLevelChange: (Float) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    onVoiceResult: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    val context = LocalContext.current

    // local message for permission / recognition errors
    var voiceStatusText by remember { mutableStateOf("") }

    // launcher for Android speech recognition activity
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull().orEmpty()

            if (spokenText.isNotBlank()) {
                onVoiceResult(spokenText)
                voiceStatusText = "Voice input added."
            } else {
                voiceStatusText = "No speech recognized."
            }
        } else {
            voiceStatusText = "Voice input cancelled."
        }
    }

    // runtime permission request for microphone
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your symptom")
            }
            speechLauncher.launch(intent)
        } else {
            voiceStatusText = "Microphone permission is required for voice input."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // screen title
        Text(
            text = "Symptom Check",
            style = MaterialTheme.typography.headlineSmall
        )

        // symptom input
        OutlinedTextField(
            value = symptom,
            onValueChange = onSymptomChange,
            label = { Text("Describe your symptom") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        // voice input button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            ) {
                Text("Use Voice Input")
            }
        }

        // message under voice button
        if (voiceStatusText.isNotBlank()) {
            Text(
                text = voiceStatusText,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // pain level label
        Text(
            text = "Pain Level: ${painLevel.toInt()}",
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )

        // pain slider
        Slider(
            value = painLevel,
            onValueChange = onPainLevelChange,
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )

        // duration input
        OutlinedTextField(
            value = duration,
            onValueChange = onDurationChange,
            label = { Text("How long have you had this symptom?") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )

        // submit button
        Button(
            onClick = onSubmitClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("See Result")
        }
    }
}