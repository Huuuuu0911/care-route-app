package com.example.cs501_final_project.ui

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs501_final_project.data.GeminiRepository
import com.example.cs501_final_project.ui.components.AppCard
import io.github.sceneview.SceneView
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.launch

data class BodyHotspot(
    val name: String,
    val x: Dp,
    val y: Dp,
    val size: Dp = 34.dp
)

data class ParsedGeminiResponse(
    val urgency: String,
    val summary: String,
    val keyPoints: List<String>,
    val nextSteps: List<String>,
    val warningSigns: List<String>,
    val notes: String
)

data class FollowUpUiQuestion(
    val text: String,
    val type: FollowUpType
)

enum class FollowUpType {
    YES_NO,
    SEVERITY,
    DURATION,
    PROGRESS,
    TEXT
}

@Composable
fun BodyPart3DScreen(
    navController: NavController
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    var rotationIndex by remember { mutableStateOf(0) }
    val rotationY = rotationIndex * 90f

    val currentSide = when (rotationIndex) {
        0 -> "Front"
        1 -> "Left"
        2 -> "Back"
        3 -> "Right"
        else -> "Front"
    }

    val bgColor = Color(0xFFF5F7FC)
    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF5B8DEF),
            Color(0xFF7B61FF)
        )
    )

    val hotspots = remember(currentSide) { getHotspotsForSide(currentSide) }

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
                        text = "Body Area Check",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Text(
                        text = "Rotate the model and tap the glowing point where it hurts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3D Body Viewer",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFF0F3FA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentSide,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF44506A)
                            )
                        }
                    }

                    Text(
                        text = "Tap any glowing point. You can rotate to front, back, left, or right.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF667085)
                    )

                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(560.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            SceneView(
                                modifier = Modifier.fillMaxSize(),
                                engine = engine,
                                modelLoader = modelLoader
                            ) {
                                rememberModelInstance(
                                    modelLoader = modelLoader,
                                    assetFileLocation = "models/male_model.glb"
                                )?.let { modelInstance ->
                                    ModelNode(
                                        modelInstance = modelInstance,
                                        scaleToUnits = 0.65f,
                                        rotation = Rotation(y = rotationY)
                                    )
                                }
                            }

                            HotspotOverlay(
                                hotspots = hotspots,
                                onTap = { part ->
                                    navController.navigate("detail/${Uri.encode(part)}")
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 14.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.88f))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFE8ECF5),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Tip: rotate and tap a glowing point",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5F6B85)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Model Controls",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                rotationIndex = (rotationIndex - 1 + 4) % 4
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5B8DEF)
                            )
                        ) {
                            Text("Rotate Left")
                        }

                        Button(
                            onClick = {
                                rotationIndex = (rotationIndex + 1) % 4
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B61FF)
                            )
                        ) {
                            Text("Rotate Right")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Visible Areas",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ChipRows(
                        items = hotspots.map { it.name },
                        selectedItem = "",
                        onItemClick = { name ->
                            navController.navigate("detail/${Uri.encode(name)}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipRows(
    items: List<String>,
    selectedItem: String,
    onItemClick: (String) -> Unit
) {
    val rows = remember(items) { items.chunked(3) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    FilterChip(
                        selected = selectedItem == item,
                        onClick = { onItemClick(item) },
                        label = { Text(item) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEAE6FF),
                            selectedLabelColor = Color(0xFF4B3BC8),
                            containerColor = Color(0xFFF6F8FC),
                            labelColor = Color(0xFF48556A)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HotspotOverlay(
    hotspots: List<BodyHotspot>,
    onTap: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseValue"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        hotspots.forEach { hotspot ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = hotspot.x, y = hotspot.y)
                    .size(hotspot.size * pulse)
                    .clip(CircleShape)
                    .background(Color(0xFF7B61FF).copy(alpha = 0.14f))
                    .border(
                        width = 2.dp,
                        color = Color(0xFF7B61FF).copy(alpha = 0.65f),
                        shape = CircleShape
                    )
                    .clickable {
                        onTap(hotspot.name)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(hotspot.size * 0.42f)
                        .clip(CircleShape)
                        .background(Color(0xFF5B8DEF))
                )
            }
        }
    }
}

@Composable
fun DetailScreen(
    part: String,
    navController: NavController
) {
    var selectedDetail by remember { mutableStateOf("") }
    var symptomText by remember { mutableStateOf("") }
    var painLevel by remember { mutableFloatStateOf(5f) }

    val bgColor = Color(0xFFF5F7FC)
    val topGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF5B8DEF),
            Color(0xFF7B61FF)
        )
    )

    val detailOptions = getDetailOptions(part)

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topGradient, RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Symptom Check",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Text(
                        text = selectedDetail.ifBlank { part },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.94f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Choose a more specific area",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ChipRows(
                        items = detailOptions,
                        selectedItem = selectedDetail,
                        onItemClick = { option ->
                            selectedDetail = option
                        }
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8FAFD)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Selected Area",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF667085)
                            )

                            Text(
                                text = if (selectedDetail.isBlank()) "Nothing selected yet" else selectedDetail,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pain Level",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Move the slider from 0 to 10",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF667085)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("0", color = Color(0xFF667085))
                        Slider(
                            value = painLevel,
                            onValueChange = { painLevel = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF5B8DEF),
                                activeTrackColor = Color(0xFF5B8DEF)
                            )
                        )
                        Text("10", color = Color(0xFF667085))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEFF4FF))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Current pain level: ${painLevel.toInt()}",
                            color = Color(0xFF175CD3)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Describe your symptom",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = symptomText,
                        onValueChange = { symptomText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = {
                            Text("Example: sharp pain when breathing, soreness, swelling, numbness...")
                        },
                        shape = RoundedCornerShape(18.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        singleLine = false
                    )

                    Button(
                        onClick = {
                            val partEncoded = Uri.encode(selectedDetail.ifBlank { part })
                            val symptomEncoded = Uri.encode(symptomText)
                            navController.navigate("follow_up/$partEncoded/$symptomEncoded/${painLevel.toInt()}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = symptomText.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B8DEF)
                        )
                    ) {
                        Text("Continue")
                    }
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun FollowUpScreen(
    part: String,
    symptomText: String,
    painLevel: Int,
    navController: NavController
) {
    val repository = remember { GeminiRepository() }
    val scope = rememberCoroutineScope()

    val bgColor = Color(0xFFF5F7FC)
    val topGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF5B8DEF),
            Color(0xFF7B61FF)
        )
    )

    var loadingQuestions by remember { mutableStateOf(true) }
    var loadingFinal by remember { mutableStateOf(false) }

    var rawQuestions by remember { mutableStateOf(listOf<String>()) }
    var rawResponse by remember { mutableStateOf("") }

    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var answer3 by remember { mutableStateOf("") }
    var optionalNote by remember { mutableStateOf("") }

    val parsed = remember(rawResponse) { parseGeminiResponse(rawResponse) }

    val uiQuestions = remember(rawQuestions) {
        rawQuestions.map { question ->
            FollowUpUiQuestion(
                text = question,
                type = detectFollowUpType(question)
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            rawQuestions = repository.getFollowUpQuestions(
                bodyPart = part,
                symptomText = symptomText,
                painLevel = painLevel,
                age = "21",
                gender = "Female",
                height = "5'6\"",
                weight = "130",
                address = "Boston"
            )
        } catch (e: Exception) {
            rawQuestions = listOf(
                "Has it been getting worse?",
                "When did it start?",
                "Do you have any other symptoms?"
            )
        }
        loadingQuestions = false
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topGradient, RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Follow-up Questions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step 2 of 3",
                            color = Color.White.copy(alpha = 0.88f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = part,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Initial Symptom",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = symptomText,
                        color = Color(0xFF344054)
                    )

                    Text(
                        text = "Pain level: $painLevel / 10",
                        color = Color(0xFF667085)
                    )
                }
            }

            if (loadingQuestions) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Preparing follow-up questions...")
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "Please answer these questions",
                            style = MaterialTheme.typography.titleMedium
                        )

                        QuestionCard(
                            index = 1,
                            question = uiQuestions.getOrNull(0)?.text ?: "Question 1",
                            type = uiQuestions.getOrNull(0)?.type ?: FollowUpType.TEXT,
                            value = answer1,
                            onValueChange = { answer1 = it }
                        )

                        QuestionCard(
                            index = 2,
                            question = uiQuestions.getOrNull(1)?.text ?: "Question 2",
                            type = uiQuestions.getOrNull(1)?.type ?: FollowUpType.TEXT,
                            value = answer2,
                            onValueChange = { answer2 = it }
                        )

                        QuestionCard(
                            index = 3,
                            question = uiQuestions.getOrNull(2)?.text ?: "Question 3",
                            type = uiQuestions.getOrNull(2)?.type ?: FollowUpType.TEXT,
                            value = answer3,
                            onValueChange = { answer3 = it }
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Optional Note",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                OutlinedTextField(
                                    value = optionalNote,
                                    onValueChange = { optionalNote = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Anything else you want to mention?") },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    loadingFinal = true
                                    rawResponse = repository.askGeminiFinal(
                                        bodyPart = part,
                                        symptomText = symptomText,
                                        painLevel = painLevel,
                                        followUpAnswers = listOf(
                                            "${uiQuestions.getOrNull(0)?.text ?: "Question 1"} Answer: $answer1",
                                            "${uiQuestions.getOrNull(1)?.text ?: "Question 2"} Answer: $answer2",
                                            "${uiQuestions.getOrNull(2)?.text ?: "Question 3"} Answer: $answer3",
                                            "Optional note: $optionalNote"
                                        ),
                                        age = "21",
                                        gender = "Female",
                                        height = "5'6\"",
                                        weight = "130",
                                        address = "Boston"
                                    )
                                    loadingFinal = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = answer1.isNotBlank() && answer2.isNotBlank() && answer3.isNotBlank(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5B8DEF)
                            )
                        ) {
                            Text("Get Final Assessment")
                        }
                    }
                }
            }

            if (loadingFinal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Generating final assessment...")
                    }
                }
            }

            if (rawResponse.isNotBlank()) {
                UrgencyCard(parsed.urgency, parsed.summary)

                SimpleSectionCard(
                    title = "Key Points",
                    items = parsed.keyPoints,
                    cardColor = Color.White
                )

                SimpleSectionCard(
                    title = "Next Steps",
                    items = parsed.nextSteps,
                    cardColor = Color.White
                )

                WarningCard(parsed.warningSigns)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Detailed Note",
                            style = MaterialTheme.typography.titleMedium
                        )

                        HorizontalDivider(color = Color(0xFFE9EEF5))

                        Text(
                            text = parsed.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF344054)
                        )
                    }
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    question: String,
    type: FollowUpType,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFEAE6FF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = index.toString(),
                        color = Color(0xFF4B3BC8)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF1F2937)
                )
            }

            when (type) {
                FollowUpType.YES_NO -> {
                    OptionButtons(
                        options = listOf("Yes", "No"),
                        selected = value,
                        onSelect = onValueChange
                    )
                }

                FollowUpType.SEVERITY -> {
                    OptionButtons(
                        options = listOf("Mild", "Moderate", "Severe"),
                        selected = value,
                        onSelect = onValueChange
                    )
                }

                FollowUpType.DURATION -> {
                    OptionButtons(
                        options = listOf("Today", "1-3 days", "More than a week", "Not sure"),
                        selected = value,
                        onSelect = onValueChange
                    )
                }

                FollowUpType.PROGRESS -> {
                    OptionButtons(
                        options = listOf("Better", "Same", "Worse"),
                        selected = value,
                        onSelect = onValueChange
                    )
                }

                FollowUpType.TEXT -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your answer") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionButtons(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val rows = options.chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { option ->
                    val isSelected = selected == option

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelect(option) },
                        label = { Text(option) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEAE6FF),
                            selectedLabelColor = Color(0xFF4B3BC8),
                            containerColor = Color.White,
                            labelColor = Color(0xFF48556A)
                        )
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun detectFollowUpType(question: String): FollowUpType {
    val q = question.lowercase()

    return when {
        q.contains("yes or no") -> FollowUpType.YES_NO
        q.startsWith("do ") || q.startsWith("did ") || q.startsWith("is ") || q.startsWith("are ") || q.startsWith("has ") || q.startsWith("have ") -> FollowUpType.YES_NO
        q.contains("getting worse") || q.contains("better or worse") || q.contains("better, worse") || q.contains("worse, better") || q.contains("staying the same") -> FollowUpType.PROGRESS
        q.contains("when did it start") || q.contains("how long") || q.contains("how many days") || q.contains("when did this start") -> FollowUpType.DURATION
        q.contains("how severe") || q.contains("how strong") || q.contains("how intense") -> FollowUpType.SEVERITY
        else -> FollowUpType.TEXT
    }
}

private fun parseGeminiResponse(text: String): ParsedGeminiResponse {
    fun sectionValue(name: String, nextNames: List<String>): String {
        val startIndex = text.indexOf("$name:")
        if (startIndex == -1) return ""

        val contentStart = startIndex + name.length + 1
        val nextIndex = nextNames
            .map { next -> text.indexOf("$next:", contentStart) }
            .filter { it != -1 }
            .minOrNull() ?: text.length

        return text.substring(contentStart, nextIndex).trim()
    }

    fun bulletList(section: String): List<String> {
        return section.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map {
                when {
                    it.startsWith("- ") -> it.removePrefix("- ").trim()
                    it.startsWith("• ") -> it.removePrefix("• ").trim()
                    else -> it
                }
            }
    }

    val urgency = sectionValue(
        "URGENCY",
        listOf("SUMMARY", "KEY_POINTS", "NEXT_STEPS", "WARNING_SIGNS", "NOTES")
    ).ifBlank { "Primary Care" }

    val summary = sectionValue(
        "SUMMARY",
        listOf("KEY_POINTS", "NEXT_STEPS", "WARNING_SIGNS", "NOTES")
    ).ifBlank { "No summary available." }

    val keyPoints = bulletList(
        sectionValue(
            "KEY_POINTS",
            listOf("NEXT_STEPS", "WARNING_SIGNS", "NOTES")
        )
    )

    val nextSteps = bulletList(
        sectionValue(
            "NEXT_STEPS",
            listOf("WARNING_SIGNS", "NOTES")
        )
    )

    val warningSigns = bulletList(
        sectionValue(
            "WARNING_SIGNS",
            listOf("NOTES")
        )
    )

    val notes = sectionValue(
        "NOTES",
        emptyList()
    ).ifBlank { "No additional note." }

    return ParsedGeminiResponse(
        urgency = urgency,
        summary = summary,
        keyPoints = if (keyPoints.isEmpty()) listOf("No key points available.") else keyPoints,
        nextSteps = if (nextSteps.isEmpty()) listOf("No next steps available.") else nextSteps,
        warningSigns = if (warningSigns.isEmpty()) listOf("No urgent warning signs listed.") else warningSigns,
        notes = notes
    )
}

@Composable
private fun UrgencyCard(
    urgency: String,
    summary: String
) {
    val (bg, chipBg, chipText) = when (urgency.trim()) {
        "Emergency" -> Triple(Color(0xFFFFF1F1), Color(0xFFD92D20), Color.White)
        "Urgent Care" -> Triple(Color(0xFFFFF7ED), Color(0xFFF79009), Color.White)
        "Primary Care" -> Triple(Color(0xFFEFF8FF), Color(0xFF2E90FA), Color.White)
        else -> Triple(Color(0xFFF6FEF9), Color(0xFF12B76A), Color.White)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Assessment",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(chipBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = urgency,
                        color = chipText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF344054)
            )
        }
    }
}

@Composable
private fun SimpleSectionCard(
    title: String,
    items: List<String>,
    cardColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider(color = Color(0xFFE9EEF5))

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7B61FF))
                    )

                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF344054),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningCard(
    warningSigns: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Seek Care Now If",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB42318)
            )

            HorizontalDivider(color = Color(0xFFF4C7C3))

            warningSigns.forEach { item ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD92D20))
                    )

                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7A271A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun getHotspotsForSide(side: String): List<BodyHotspot> {
    return when (side) {
        "Front" -> listOf(
            BodyHotspot("Forehead", 0.dp, (-215).dp, 34.dp),
            BodyHotspot("Face", 0.dp, (-178).dp, 34.dp),
            BodyHotspot("Neck", 0.dp, (-138).dp, 30.dp),
            BodyHotspot("Left Shoulder", (-64).dp, (-108).dp, 32.dp),
            BodyHotspot("Right Shoulder", 64.dp, (-108).dp, 32.dp),
            BodyHotspot("Left Chest", (-36).dp, (-76).dp, 36.dp),
            BodyHotspot("Center Chest", 0.dp, (-70).dp, 36.dp),
            BodyHotspot("Right Chest", 36.dp, (-76).dp, 36.dp),
            BodyHotspot("Upper Abdomen", 0.dp, (-8).dp, 40.dp),
            BodyHotspot("Lower Abdomen", 0.dp, 42.dp, 40.dp),
            BodyHotspot("Left Arm", (-112).dp, (-18).dp, 34.dp),
            BodyHotspot("Right Arm", 112.dp, (-18).dp, 34.dp),
            BodyHotspot("Pelvis", 0.dp, 98.dp, 36.dp),
            BodyHotspot("Left Thigh", (-34).dp, 160.dp, 34.dp),
            BodyHotspot("Right Thigh", 34.dp, 160.dp, 34.dp),
            BodyHotspot("Left Knee", (-34).dp, 230.dp, 30.dp),
            BodyHotspot("Right Knee", 34.dp, 230.dp, 30.dp),
            BodyHotspot("Left Shin", (-34).dp, 298.dp, 30.dp),
            BodyHotspot("Right Shin", 34.dp, 298.dp, 30.dp)
        )

        "Back" -> listOf(
            BodyHotspot("Back Head", 0.dp, (-202).dp, 34.dp),
            BodyHotspot("Back Neck", 0.dp, (-150).dp, 30.dp),
            BodyHotspot("Left Upper Back", (-42).dp, (-96).dp, 36.dp),
            BodyHotspot("Right Upper Back", 42.dp, (-96).dp, 36.dp),
            BodyHotspot("Mid Back", 0.dp, (-26).dp, 40.dp),
            BodyHotspot("Lower Back", 0.dp, 48.dp, 40.dp),
            BodyHotspot("Left Elbow Back", (-106).dp, 4.dp, 30.dp),
            BodyHotspot("Right Elbow Back", 106.dp, 4.dp, 30.dp),
            BodyHotspot("Left Glute", (-30).dp, 114.dp, 34.dp),
            BodyHotspot("Right Glute", 30.dp, 114.dp, 34.dp),
            BodyHotspot("Left Hamstring", (-34).dp, 182.dp, 32.dp),
            BodyHotspot("Right Hamstring", 34.dp, 182.dp, 32.dp),
            BodyHotspot("Left Calf", (-34).dp, 278.dp, 30.dp),
            BodyHotspot("Right Calf", 34.dp, 278.dp, 30.dp)
        )

        "Left" -> listOf(
            BodyHotspot("Left Temple", 18.dp, (-188).dp, 30.dp),
            BodyHotspot("Left Jaw", 20.dp, (-154).dp, 28.dp),
            BodyHotspot("Left Neck", 14.dp, (-128).dp, 28.dp),
            BodyHotspot("Left Shoulder Side", 30.dp, (-98).dp, 32.dp),
            BodyHotspot("Left Rib", 24.dp, (-44).dp, 34.dp),
            BodyHotspot("Left Waist", 18.dp, 8.dp, 34.dp),
            BodyHotspot("Left Hip", 14.dp, 74.dp, 34.dp),
            BodyHotspot("Left Thigh Side", 8.dp, 154.dp, 34.dp),
            BodyHotspot("Left Knee Side", 6.dp, 232.dp, 30.dp),
            BodyHotspot("Left Lower Leg", 4.dp, 300.dp, 30.dp)
        )

        else -> listOf(
            BodyHotspot("Right Temple", (-18).dp, (-188).dp, 30.dp),
            BodyHotspot("Right Jaw", (-20).dp, (-154).dp, 28.dp),
            BodyHotspot("Right Neck", (-14).dp, (-128).dp, 28.dp),
            BodyHotspot("Right Shoulder Side", (-30).dp, (-98).dp, 32.dp),
            BodyHotspot("Right Rib", (-24).dp, (-44).dp, 34.dp),
            BodyHotspot("Right Waist", (-18).dp, 8.dp, 34.dp),
            BodyHotspot("Right Hip", (-14).dp, 74.dp, 34.dp),
            BodyHotspot("Right Thigh Side", (-8).dp, 154.dp, 34.dp),
            BodyHotspot("Right Knee Side", (-6).dp, 232.dp, 30.dp),
            BodyHotspot("Right Lower Leg", (-4).dp, 300.dp, 30.dp)
        )
    }
}

private fun getDetailOptions(part: String): List<String> {
    return when (part) {
        "Forehead", "Face", "Back Head", "Left Temple", "Right Temple", "Left Jaw", "Right Jaw" ->
            listOf(
                "$part - Left",
                "$part - Center",
                "$part - Right",
                "$part - Mild",
                "$part - Severe"
            )

        "Neck", "Back Neck", "Left Neck", "Right Neck" ->
            listOf(
                "$part - Front",
                "$part - Back",
                "$part - Left Side",
                "$part - Right Side",
                "$part - Stiffness"
            )

        "Left Chest", "Center Chest", "Right Chest", "Left Rib", "Right Rib" ->
            listOf(
                "$part - Sharp Pain",
                "$part - Pressure",
                "$part - Tightness",
                "$part - With Breathing",
                "$part - Tender"
            )

        "Upper Abdomen", "Lower Abdomen", "Left Waist", "Right Waist", "Pelvis" ->
            listOf(
                "$part - Cramping",
                "$part - Pressure",
                "$part - Burning",
                "$part - Swelling",
                "$part - Tender"
            )

        "Left Upper Back", "Right Upper Back", "Mid Back", "Lower Back" ->
            listOf(
                "$part - Left Side",
                "$part - Right Side",
                "$part - Soreness",
                "$part - Sharp Pain",
                "$part - Stiffness"
            )

        "Left Shoulder", "Right Shoulder", "Left Shoulder Side", "Right Shoulder Side" ->
            listOf(
                "$part - Front",
                "$part - Back",
                "$part - Joint",
                "$part - Movement Pain",
                "$part - Weakness"
            )

        "Left Arm", "Right Arm", "Left Elbow Back", "Right Elbow Back" ->
            listOf(
                "$part - Upper",
                "$part - Elbow",
                "$part - Forearm",
                "$part - Numbness",
                "$part - Weakness"
            )

        "Left Thigh", "Right Thigh", "Left Thigh Side", "Right Thigh Side", "Left Hamstring", "Right Hamstring" ->
            listOf(
                "$part - Front",
                "$part - Back",
                "$part - Muscle Soreness",
                "$part - Sharp Pain",
                "$part - Cramp"
            )

        "Left Knee", "Right Knee", "Left Knee Side", "Right Knee Side" ->
            listOf(
                "$part - Front",
                "$part - Side",
                "$part - Swelling",
                "$part - Clicking",
                "$part - Movement Pain"
            )

        "Left Shin", "Right Shin", "Left Calf", "Right Calf", "Left Lower Leg", "Right Lower Leg" ->
            listOf(
                "$part - Front",
                "$part - Back",
                "$part - Swelling",
                "$part - Cramp",
                "$part - Tender"
            )

        "Left Glute", "Right Glute", "Left Hip", "Right Hip" ->
            listOf(
                "$part - Joint",
                "$part - Muscle",
                "$part - Sitting Pain",
                "$part - Walking Pain",
                "$part - Tender"
            )

        else -> listOf(
            "$part - Left",
            "$part - Center",
            "$part - Right",
            "$part - Sharp Pain",
            "$part - Soreness"
        )
    }
}