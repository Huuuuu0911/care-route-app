package com.example.cs501_final_project.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs501_final_project.data.CareRouteViewModel
import com.example.cs501_final_project.data.DailyHealthTip
import com.example.cs501_final_project.data.GeminiRepository
import com.example.cs501_final_project.data.PersonalizedCheckupSuggestion

@Composable
fun HomeScreen(
    viewModel: CareRouteViewModel,
    onStartClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary
    val activePatient = viewModel.activePatientContext()
    val repository = remember { GeminiRepository() }

    var dailyTipLoading by rememberSaveable(activePatient.id) { mutableStateOf(false) }
    var suggestionsLoading by rememberSaveable(activePatient.id) { mutableStateOf(false) }
    var insightsError by rememberSaveable(activePatient.id) { mutableStateOf<String?>(null) }

    val tip = viewModel.dailyHealthTip
    val checkupSuggestions = viewModel.getCheckupSuggestionsFor(activePatient.id)
    val archiveCount = viewModel.archiveRecordCountFor(activePatient.id)
    val latestCheck = viewModel.latestHistoryRecordFor(activePatient.id)
    val latestImported = viewModel.latestImportedRecordFor(activePatient.id)

    LaunchedEffect(activePatient.id, viewModel.historyRecords.size, viewModel.importedMedicalRecords.size) {
        val recentChecks = viewModel.recentHistorySummariesFor(activePatient.id)
        val recentRecords = viewModel.recentImportedRecordSummariesFor(activePatient.id)

        if (viewModel.shouldRefreshDailyTip(activePatient.id)) {
            dailyTipLoading = true
            runCatching {
                repository.generateDailyHealthTip(
                    patient = activePatient,
                    recentHistory = recentChecks,
                    importedMedicalNotes = recentRecords
                )
            }.onSuccess {
                viewModel.updateDailyHealthTip(it)
                insightsError = null
            }.onFailure {
                insightsError = "Daily health tip could not be refreshed right now."
            }
            dailyTipLoading = false
        }

        if (viewModel.shouldRefreshCheckupSuggestions(activePatient.id)) {
            suggestionsLoading = true
            runCatching {
                repository.generateCheckupSuggestions(
                    patient = activePatient,
                    recentHistory = recentChecks,
                    importedMedicalNotes = recentRecords
                )
            }.onSuccess {
                viewModel.updateCheckupSuggestions(activePatient.id, it)
                insightsError = null
            }.onFailure {
                insightsError = "Checkup suggestions are using a local fallback for now."
            }
            suggestionsLoading = false
        }
    }

    fun openEmergencyDialer() {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
    }

    fun openNearestEmergencyRoom() {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=${Uri.encode("emergency room near me")}")
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "CareRoute",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Daily support, symptom checks, and preventive guidance in one place.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF667085)
                )
            }

            HeroCheckCard(
                activePatientName = activePatient.displayName,
                accent = accent,
                onStartClick = onStartClick
            )


            if (insightsError != null) {
                Text(
                    text = insightsError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB42318)
                )
            }

            InsightCard(
                title = "Daily Health Tip",
                icon = Icons.Default.TipsAndUpdates,
                accent = Color(0xFF7B61FF),
                loading = dailyTipLoading,
                content = {
                    DailyTipContent(tip = tip, activePatientName = activePatient.displayName)
                }
            )

            InsightCard(
                title = "Personalized Checkup Suggestions",
                icon = Icons.Default.LocalHospital,
                accent = Color(0xFF12B76A),
                loading = suggestionsLoading,
                content = {
                    CheckupSuggestionContent(suggestions = checkupSuggestions)
                }
            )

            ArchiveSummaryCard(
                archiveCount = archiveCount,
                latestCheckSummary = latestCheck?.let { "${it.bodyPart} · ${it.urgency}" },
                latestRecordSummary = latestImported?.title
            )

            QuickAccessCard(
                onHistoryClick = onHistoryClick,
                onMapClick = onMapClick,
                onSettingClick = onSettingClick
            )

            EmergencyCard(
                onCallClick = { openEmergencyDialer() },
                onNearestErClick = { openNearestEmergencyRoom() }
            )
        }
    }
}

@Composable
private fun HeroCheckCard(
    activePatientName: String,
    accent: Color,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent,
                            accent.copy(alpha = 0.82f),
                            Color(0xFF9C6BFF)
                        )
                    )
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Column {
                    Text(
                        text = "Start Symptom Check",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Main feature · 3D body viewer and personalized follow-up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            Text(
                text = "Selected profile: $activePatientName. Start from the body viewer, answer targeted questions, then jump to care or pharmacy recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.96f)
            )

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = accent
                )
            ) {
                Text(
                    text = "Begin 3D Symptom Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    loading: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Updating personalized content…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF667085)
                    )
                }
            } else {
                content()
            }
        }
    }
}

@Composable
private fun DailyTipContent(
    tip: DailyHealthTip?,
    activePatientName: String
) {
    if (tip == null) {
        Text(
            text = "Your Gemini-powered daily health tip will appear here once generated.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF667085)
        )
        return
    }

    Text(
        text = tip.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827)
    )

    Text(
        text = tip.message,
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF344054)
    )

    HorizontalDivider(color = Color(0xFFE9EEF5))

    Text(
        text = "Focus: ${tip.focusArea} · Built for $activePatientName",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF667085)
    )

    Text(
        text = tip.caution,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFB42318)
    )
}

@Composable
private fun CheckupSuggestionContent(
    suggestions: List<PersonalizedCheckupSuggestion>
) {
    if (suggestions.isEmpty()) {
        Text(
            text = "Personalized checkup suggestions will appear here after profile and history analysis.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF667085)
        )
        return
    }

    suggestions.forEachIndexed { index, suggestion ->
        if (index > 0) {
            HorizontalDivider(color = Color(0xFFE9EEF5))
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE8F7EF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Priority ${suggestion.priority}/5",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF067647)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = suggestion.timeframe,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF667085)
                )
            }

            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475467)
            )
        }
    }
}

@Composable
private fun ArchiveSummaryCard(
    archiveCount: Int,
    latestCheckSummary: String?,
    latestRecordSummary: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Health Archive",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArchiveMiniStat(
                    modifier = Modifier.weight(1f),
                    title = "Saved Items",
                    value = archiveCount.toString(),
                    icon = Icons.Default.History,
                    accent = Color(0xFF7B61FF)
                )
                ArchiveMiniStat(
                    modifier = Modifier.weight(1f),
                    title = "Imported Notes",
                    value = if (latestRecordSummary == null) "0" else "1+",
                    icon = Icons.Default.EventNote,
                    accent = Color(0xFF12B76A)
                )
            }

            if (latestCheckSummary != null) {
                Text(
                    text = "Latest symptom check: $latestCheckSummary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF344054)
                )
            }

            if (latestRecordSummary != null) {
                Text(
                    text = "Latest saved record: $latestRecordSummary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF344054)
                )
            }
        }
    }
}

@Composable
private fun ArchiveMiniStat(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667085)
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    onHistoryClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    title = "History",
                    subtitle = "Review checks and uploaded records",
                    accent = Color(0xFF7B61FF),
                    onClick = onHistoryClick
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Map,
                    title = "Map",
                    subtitle = "Find pharmacy, clinic, or hospital",
                    accent = Color(0xFF12B76A),
                    onClick = onMapClick
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    title = "Setting",
                    subtitle = "Profile and preferences",
                    accent = Color(0xFFF79009),
                    onClick = onSettingClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667085)
            )
        }
    }
}

@Composable
private fun EmergencyCard(
    onCallClick: () -> Unit,
    onNearestErClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Emergency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB42318)
            )

            Text(
                text = "For severe chest pain, breathing trouble, fainting, seizure, stroke symptoms, or heavy bleeding, skip the symptom checker and act immediately.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB42318)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92D20))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call 911")
                }

                Button(
                    onClick = onNearestErClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB42318))
                ) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nearest ER")
                }
            }
        }
    }
}
