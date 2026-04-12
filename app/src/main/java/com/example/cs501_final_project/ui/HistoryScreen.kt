package com.example.cs501_final_project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val bgColor = Color(0xFFF6F8FC)

    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF5B8DEF),
            Color(0xFF7B61FF)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = headerGradient,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Review your previous symptom checks and results.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            if (history.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history yet.\nStart a symptom check to see results here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666A73)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(history) { index, item ->
                        val urgency = getUrgencyTag(item)
                        val tagColor = getUrgencyColor(urgency)
                        val fakeTime = getFakeTimeLabel(index)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHistoryItemClick(item) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Symptom Record",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = urgency,
                                                color = Color.White
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = tagColor,
                                            labelColor = Color.White
                                        )
                                    )
                                }

                                Text(
                                    text = fakeTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7A7F87)
                                )

                                HorizontalDivider(color = Color(0xFFE8EAF0))

                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF44474F)
                                )

                                Text(
                                    text = "Tap to view details",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5B8DEF)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B8DEF)
                    )
                ) {
                    Text("Back")
                }
            }
        }
    }
}

private fun getUrgencyTag(item: String): String {
    val text = item.lowercase()

    return when {
        "emergency" in text -> "Emergency"
        "urgent" in text -> "Urgent Care"
        else -> "Primary Care"
    }
}

private fun getUrgencyColor(tag: String): Color {
    return when (tag) {
        "Emergency" -> Color(0xFFE53935)
        "Urgent Care" -> Color(0xFFFB8C00)
        else -> Color(0xFF5B8DEF)
    }
}

private fun getFakeTimeLabel(index: Int): String {
    return when (index) {
        0 -> "Today · 3:20 PM"
        1 -> "Yesterday · 7:45 PM"
        2 -> "Apr 10 · 10:15 AM"
        3 -> "Apr 08 · 6:30 PM"
        else -> "Earlier record"
    }
}