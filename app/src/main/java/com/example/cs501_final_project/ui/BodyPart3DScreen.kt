package com.example.cs501_final_project.ui

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs501_final_project.data.CareRouteViewModel
import com.example.cs501_final_project.data.GeminiRepository
import com.example.cs501_final_project.data.SavedCheckRecord
import io.github.sceneview.SceneView
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.min
import kotlin.math.roundToInt

private data class BodyFrameBounds(
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float
)

data class BodyHotspot(
    val name: String,
    val xRatio: Float,
    val yRatio: Float,
    val sizeRatio: Float = 0.065f
)

private data class BodyOverlaySpec(
    val bounds: BodyFrameBounds,
    val hotspots: List<BodyHotspot>
)

data class ParsedGeminiResponse(
    val urgency: String,
    val careLevel: String,
    val placeType: String,
    val mapQuery: String,
    val recommendationScore: Int,
    val summary: String,
    val keyPoints: List<String>,
    val selfCare: List<String>,
    val otcOptions: List<String>,
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape && configuration.screenWidthDp >= 720

    var rotationIndex by rememberSaveable { mutableIntStateOf(0) }
    val rotationY = rotationIndex * 90f

    val currentSide = when (rotationIndex) {
        0 -> "Front"
        1 -> "Left"
        2 -> "Back"
        3 -> "Right"
        else -> "Front"
    }

    val overlaySpec = remember(currentSide, useTwoPane) {
        getBodyOverlaySpec(currentSide, useTwoPane)
    }

    val bgColor = MaterialTheme.colorScheme.background
    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            Color(0xFF7B61FF)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        if (useTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.05f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderCard(
                        title = "Body Area Check",
                        subtitle = "Rotate the model and tap the hotspot closest to the painful area.",
                        gradient = headerGradient
                    )

                    ModelViewerCard(
                        currentSide = currentSide,
                        rotationY = rotationY,
                        overlaySpec = overlaySpec,
                        navController = navController,
                        modelHeight = 420.dp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.95f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ControlsCard(
                        onRotateLeft = {
                            rotationIndex = (rotationIndex - 1 + 4) % 4
                        },
                        onRotateRight = {
                            rotationIndex = (rotationIndex + 1) % 4
                        }
                    )

                    VisibleAreasCard(
                        hotspots = overlaySpec.hotspots,
                        navController = navController
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderCard(
                    title = "Body Area Check",
                    subtitle = "Rotate the model and tap the hotspot closest to the painful area.",
                    gradient = headerGradient
                )

                ModelViewerCard(
                    currentSide = currentSide,
                    rotationY = rotationY,
                    overlaySpec = overlaySpec,
                    navController = navController,
                    modelHeight = 380.dp
                )

                ControlsCard(
                    onRotateLeft = {
                        rotationIndex = (rotationIndex - 1 + 4) % 4
                    },
                    onRotateRight = {
                        rotationIndex = (rotationIndex + 1) % 4
                    }
                )

                VisibleAreasCard(
                    hotspots = overlaySpec.hotspots,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(
    title: String,
    subtitle: String,
    gradient: Brush
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradient,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(22.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun ModelViewerCard(
    currentSide: String,
    rotationY: Float,
    overlaySpec: BodyOverlaySpec,
    navController: NavController,
    modelHeight: Dp
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                text = "Tap the exact area that hurts.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(modelHeight),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))
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
                                scaleToUnits = 0.72f,
                                rotation = Rotation(y = rotationY)
                            )
                        }
                    }

                    // 衣服叠加层(在 3D 模型之上、热点之下,不拦截点击)
                    ClothingOverlay(
                        overlaySpec = overlaySpec,
                        side = currentSide
                    )

                    HotspotOverlay(
                        overlaySpec = overlaySpec,
                        onTap = { part ->
                            navController.navigate("detail/${Uri.encode(part)}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsCard(
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    onClick = onRotateLeft,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Rotate Left")
                }

                Button(
                    onClick = onRotateRight,
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
}

@Composable
private fun VisibleAreasCard(
    hotspots: List<BodyHotspot>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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



// 正/背面短裤:外侧竖直,两条明显的长方形裤腿,胯下 V 字
private fun DrawScope.drawShorts(
    bodyColor: Color,
    waistColor: Color,
    centerX: Float,
    topY: Float,
    width: Float,
    height: Float
) {
    val x = centerX - width / 2f
    val y = topY
    val w = width
    val h = height
    val cr = w * 0.10f                     // 顶部圆角
    val br = w * 0.06f                     // 底部圆角(裤脚口)

    val path = Path().apply {
        // 顶边(裤腰),从左上圆角开始
        moveTo(x + cr, y)
        lineTo(x + w - cr, y)
        // 右上圆角
        quadraticBezierTo(x + w, y, x + w, y + cr)

        // 右侧外边: 一路直线下到底
        lineTo(x + w, y + h - br)
        // 右下圆角(裤脚口外侧)
        quadraticBezierTo(x + w, y + h, x + w - br, y + h)
        // 右腿裤脚底边(向左)
        lineTo(x + w * 0.56f, y + h)
        // 右腿内缝直线上到胯部
        lineTo(x + w * 0.56f, y + h * 0.55f)
        // 胯下 V 字弧线(连接两腿内缝)
        quadraticBezierTo(
            x + w * 0.50f, y + h * 0.42f,
            x + w * 0.44f, y + h * 0.55f
        )
        // 左腿内缝直线下到底
        lineTo(x + w * 0.44f, y + h)
        // 左腿裤脚底边(向左)
        lineTo(x + br, y + h)
        // 左下圆角
        quadraticBezierTo(x, y + h, x, y + h - br)
        // 左侧外边: 直线上到顶
        lineTo(x, y + cr)
        quadraticBezierTo(x, y, x + cr, y)
        close()
    }
    drawPath(path, bodyColor)

    // 裤腰高光带(只在裤腰区域,不会跨到 V 字下面)
    drawRect(
        color = waistColor,
        topLeft = Offset(x, y),
        size = Size(w, h * 0.12f)
    )
}

// 侧视图短裤:简化为带圆角的矩形(看不到 V 字)

@Composable
private fun HotspotOverlay(
    overlaySpec: BodyOverlaySpec,
    onTap: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseValue"
    )
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@Box

        val bodyFrameLeft = canvasSize.width * overlaySpec.bounds.leftRatio
        val bodyFrameTop = canvasSize.height * overlaySpec.bounds.topRatio
        val bodyFrameWidth = canvasSize.width * overlaySpec.bounds.widthRatio
        val bodyFrameHeight = canvasSize.height * overlaySpec.bounds.heightRatio
        val referenceSize = min(bodyFrameWidth, bodyFrameHeight)

        overlaySpec.hotspots.forEach { hotspot ->
            val baseSizePx = referenceSize * hotspot.sizeRatio
            val animatedSizePx = baseSizePx * pulse
            val xPx = bodyFrameLeft + (bodyFrameWidth * hotspot.xRatio) - (animatedSizePx / 2f)
            val yPx = bodyFrameTop + (bodyFrameHeight * hotspot.yRatio) - (animatedSizePx / 2f)
            val ringSizeDp = with(density) { animatedSizePx.toDp() }
            val dotSizeDp = with(density) { (baseSizePx * 0.42f).toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
                    .size(ringSizeDp)
                    .clip(CircleShape)
                    .background(Color(0xFF7B61FF).copy(alpha = 0.12f))
                    .border(
                        width = 2.dp,
                        color = Color(0xFF7B61FF).copy(alpha = 0.75f),
                        shape = CircleShape
                    )
                    .clickable { onTap(hotspot.name) }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(dotSizeDp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape && configuration.screenWidthDp >= 720

    var selectedDetail by rememberSaveable(part) { mutableStateOf("") }
    var symptomText by rememberSaveable(part) { mutableStateOf("") }
    var painLevel by rememberSaveable(part) { mutableFloatStateOf(5f) }

    val bgColor = MaterialTheme.colorScheme.background
    val topGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            Color(0xFF7B61FF)
        )
    )

    val detailOptions = getDetailOptions(part)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        if (useTwoPane) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderCard(
                    title = "Symptom Check",
                    subtitle = selectedDetail.ifBlank { part },
                    gradient = topGradient
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailAreaCard(
                            detailOptions = detailOptions,
                            selectedDetail = selectedDetail,
                            onSelect = { selectedDetail = it }
                        )

                        PainLevelCard(
                            painLevel = painLevel,
                            onValueChange = { painLevel = it }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SymptomInputCard(
                            symptomText = symptomText,
                            onSymptomChange = { symptomText = it },
                            onContinue = {
                                val partEncoded = Uri.encode(selectedDetail.ifBlank { part })
                                val symptomEncoded = Uri.encode(symptomText)
                                navController.navigate("follow_up/$partEncoded/$symptomEncoded/${painLevel.toInt()}")
                            }
                        )

                        TextButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderCard(
                    title = "Symptom Check",
                    subtitle = selectedDetail.ifBlank { part },
                    gradient = topGradient
                )

                DetailAreaCard(
                    detailOptions = detailOptions,
                    selectedDetail = selectedDetail,
                    onSelect = { selectedDetail = it }
                )

                PainLevelCard(
                    painLevel = painLevel,
                    onValueChange = { painLevel = it }
                )

                SymptomInputCard(
                    symptomText = symptomText,
                    onSymptomChange = { symptomText = it },
                    onContinue = {
                        val partEncoded = Uri.encode(selectedDetail.ifBlank { part })
                        val symptomEncoded = Uri.encode(symptomText)
                        navController.navigate("follow_up/$partEncoded/$symptomEncoded/${painLevel.toInt()}")
                    }
                )

                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
private fun DetailAreaCard(
    detailOptions: List<String>,
    selectedDetail: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                onItemClick = onSelect
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
                        text = selectedDetail.ifBlank { "Nothing selected yet" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2937)
                    )
                }
            }
        }
    }
}

@Composable
private fun PainLevelCard(
    painLevel: Float,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                    onValueChange = onValueChange,
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Text("10", color = Color(0xFF667085))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Current pain level: ${painLevel.toInt()}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SymptomInputCard(
    symptomText: String,
    onSymptomChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                onValueChange = onSymptomChange,
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
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = symptomText.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}




@Composable
private fun ClothingOverlay(
    overlaySpec: BodyOverlaySpec,
    side: String
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val mainFabric = Color(0xFF3A3F46).copy(alpha = 0.96f)
    val darkPanel = Color(0xFF111827).copy(alpha = 0.98f)
    val waistBand = Color(0xFF0F172A).copy(alpha = 0.98f)
    val seamColor = Color(0xFF6B7280).copy(alpha = 0.70f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@Box

        Canvas(modifier = Modifier.fillMaxSize()) {
            val bfL = canvasSize.width * overlaySpec.bounds.leftRatio
            val bfT = canvasSize.height * overlaySpec.bounds.topRatio
            val bfW = canvasSize.width * overlaySpec.bounds.widthRatio
            val bfH = canvasSize.height * overlaySpec.bounds.heightRatio

            when (side) {
                "Front", "Back" -> {
                    drawFittedBoxerBriefs(
                        mainColor = mainFabric,
                        panelColor = darkPanel,
                        waistColor = waistBand,
                        seamColor = seamColor,
                        centerX = bfL + bfW * 0.50f,
                        topY = bfT + bfH * 0.425f,
                        width = bfW * 0.34f,
                        height = bfH * 0.205f
                    )
                }

                "Left", "Right" -> {
                    val sideCenter = if (side == "Left") 0.55f else 0.45f

                    drawFittedBoxerBriefsSide(
                        mainColor = mainFabric,
                        panelColor = darkPanel,
                        waistColor = waistBand,
                        centerX = bfL + bfW * sideCenter,
                        topY = bfT + bfH * 0.425f,
                        width = bfW * 0.17f,
                        height = bfH * 0.205f
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFittedBoxerBriefs(
    mainColor: Color,
    panelColor: Color,
    waistColor: Color,
    seamColor: Color,
    centerX: Float,
    topY: Float,
    width: Float,
    height: Float
) {
    val x = centerX - width / 2f
    val y = topY
    val w = width
    val h = height

    val waistH = h * 0.16f
    val bottomY = y + h
    val bodyTop = y + waistH

    val bodyPath = Path().apply {
        moveTo(x + w * 0.06f, bodyTop)

        cubicTo(
            x + w * 0.20f, y + h * 0.15f,
            x + w * 0.80f, y + h * 0.15f,
            x + w * 0.94f, bodyTop
        )

        cubicTo(
            x + w * 0.98f, y + h * 0.38f,
            x + w * 0.91f, y + h * 0.76f,
            x + w * 0.82f, y + h * 0.96f
        )

        lineTo(x + w * 0.58f, y + h * 0.96f)

        cubicTo(
            x + w * 0.56f, y + h * 0.79f,
            x + w * 0.54f, y + h * 0.66f,
            x + w * 0.50f, y + h * 0.58f
        )

        cubicTo(
            x + w * 0.46f, y + h * 0.66f,
            x + w * 0.44f, y + h * 0.79f,
            x + w * 0.42f, y + h * 0.96f
        )

        lineTo(x + w * 0.18f, y + h * 0.96f)

        cubicTo(
            x + w * 0.09f, y + h * 0.76f,
            x + w * 0.02f, y + h * 0.38f,
            x + w * 0.06f, bodyTop
        )

        close()
    }

    drawPath(bodyPath, mainColor)

    val leftPanel = Path().apply {
        moveTo(x + w * 0.06f, bodyTop)
        cubicTo(
            x + w * 0.12f, y + h * 0.40f,
            x + w * 0.13f, y + h * 0.73f,
            x + w * 0.18f, y + h * 0.96f
        )
        lineTo(x + w * 0.36f, y + h * 0.96f)
        cubicTo(
            x + w * 0.31f, y + h * 0.72f,
            x + w * 0.28f, y + h * 0.42f,
            x + w * 0.29f, bodyTop
        )
        close()
    }

    val rightPanel = Path().apply {
        moveTo(x + w * 0.94f, bodyTop)
        cubicTo(
            x + w * 0.88f, y + h * 0.40f,
            x + w * 0.87f, y + h * 0.73f,
            x + w * 0.82f, y + h * 0.96f
        )
        lineTo(x + w * 0.64f, y + h * 0.96f)
        cubicTo(
            x + w * 0.69f, y + h * 0.72f,
            x + w * 0.72f, y + h * 0.42f,
            x + w * 0.71f, bodyTop
        )
        close()
    }

    drawPath(leftPanel, panelColor.copy(alpha = 0.72f))
    drawPath(rightPanel, panelColor.copy(alpha = 0.72f))

    val pouchPath = Path().apply {
        moveTo(x + w * 0.43f, bodyTop + h * 0.03f)
        cubicTo(
            x + w * 0.46f, y + h * 0.45f,
            x + w * 0.46f, y + h * 0.62f,
            x + w * 0.50f, y + h * 0.72f
        )
        cubicTo(
            x + w * 0.54f, y + h * 0.62f,
            x + w * 0.54f, y + h * 0.45f,
            x + w * 0.57f, bodyTop + h * 0.03f
        )
        close()
    }

    drawPath(pouchPath, Color(0xFF4B5563).copy(alpha = 0.45f))

    drawRoundRect(
        color = waistColor,
        topLeft = Offset(x, y),
        size = Size(w, waistH),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )

    drawRect(
        color = Color.White.copy(alpha = 0.10f),
        topLeft = Offset(x, y + waistH * 0.18f),
        size = Size(w, waistH * 0.18f)
    )

    drawLine(
        color = seamColor,
        start = Offset(x + w * 0.50f, bodyTop + h * 0.03f),
        end = Offset(x + w * 0.50f, y + h * 0.88f),
        strokeWidth = w * 0.012f
    )

    drawLine(
        color = Color.Black.copy(alpha = 0.25f),
        start = Offset(x + w * 0.42f, y + h * 0.96f),
        end = Offset(x + w * 0.50f, y + h * 0.58f),
        strokeWidth = w * 0.010f
    )

    drawLine(
        color = Color.Black.copy(alpha = 0.25f),
        start = Offset(x + w * 0.58f, y + h * 0.96f),
        end = Offset(x + w * 0.50f, y + h * 0.58f),
        strokeWidth = w * 0.010f
    )
}

private fun DrawScope.drawFittedBoxerBriefsSide(
    mainColor: Color,
    panelColor: Color,
    waistColor: Color,
    centerX: Float,
    topY: Float,
    width: Float,
    height: Float
) {
    val x = centerX - width / 2f
    val y = topY
    val w = width
    val h = height

    val waistH = h * 0.16f

    val bodyPath = Path().apply {
        moveTo(x + w * 0.10f, y + waistH)
        quadraticBezierTo(x + w * 0.50f, y + h * 0.08f, x + w * 0.90f, y + waistH)
        cubicTo(x + w * 0.94f, y + h * 0.38f, x + w * 0.86f, y + h * 0.80f, x + w * 0.78f, y + h * 0.96f)
        lineTo(x + w * 0.22f, y + h * 0.96f)
        cubicTo(x + w * 0.14f, y + h * 0.80f, x + w * 0.06f, y + h * 0.38f, x + w * 0.10f, y + waistH)
        close()
    }

    drawPath(bodyPath, mainColor)

    drawPath(
        Path().apply {
            moveTo(x + w * 0.08f, y + waistH)
            cubicTo(x + w * 0.18f, y + h * 0.35f, x + w * 0.20f, y + h * 0.75f, x + w * 0.24f, y + h * 0.96f)
            lineTo(x + w * 0.42f, y + h * 0.96f)
            cubicTo(x + w * 0.35f, y + h * 0.68f, x + w * 0.33f, y + h * 0.36f, x + w * 0.34f, y + waistH)
            close()
        },
        panelColor.copy(alpha = 0.70f)
    )

    drawRoundRect(
        color = waistColor,
        topLeft = Offset(x, y),
        size = Size(w, waistH),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
    )

    drawRect(
        color = Color.White.copy(alpha = 0.10f),
        topLeft = Offset(x, y + waistH * 0.18f),
        size = Size(w, waistH * 0.18f)
    )
}

@Composable
fun FollowUpScreen(
    part: String,
    symptomText: String,
    painLevel: Int,
    navController: NavController,
    viewModel: CareRouteViewModel
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape && configuration.screenWidthDp >= 720

    val repository = remember { GeminiRepository() }
    val scope = rememberCoroutineScope()
    val patient = viewModel.activePatientContext()
    val recentHistory = remember(viewModel.historyRecords.size, patient.id) {
        viewModel.recentHistorySummariesFor(patient.id)
    }

    val bgColor = MaterialTheme.colorScheme.background
    val topGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            Color(0xFF7B61FF)
        )
    )

    var questionsLoaded by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf(false) }
    var loadingQuestions by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf(false) }
    var loadingFinal by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf(false) }
    var rawQuestionsSerialized by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf("") }
    var rawResponse by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf("") }
    var answer1 by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf("") }
    var answer2 by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf("") }
    var answer3 by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf("") }
    var optionalNote by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf("") }
    var recordSaved by rememberSaveable(part, symptomText, painLevel, patient.id) { mutableStateOf(false) }

    val rawQuestions = remember(rawQuestionsSerialized) {
        rawQuestionsSerialized
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    val parsed = remember(rawResponse) { parseGeminiResponse(rawResponse) }

    val uiQuestions = remember(rawQuestions) {
        rawQuestions.map { question ->
            FollowUpUiQuestion(
                text = question,
                type = detectFollowUpType(question)
            )
        }
    }

    LaunchedEffect(part, symptomText, painLevel, patient.id) {
        if (questionsLoaded || rawQuestionsSerialized.isNotBlank()) return@LaunchedEffect

        loadingQuestions = true
        try {
            val fetchedQuestions = repository.getFollowUpQuestions(
                bodyPart = part,
                symptomText = symptomText,
                painLevel = painLevel,
                patient = patient,
                recentHistory = recentHistory
            )
            rawQuestionsSerialized = fetchedQuestions.joinToString("\n")
        } catch (_: Exception) {
            rawQuestionsSerialized = listOf(
                "Has it been getting worse?",
                "When did it start?",
                "Do you have any other symptoms?"
            ).joinToString("\n")
        }
        questionsLoaded = true
        loadingQuestions = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        if (useTwoPane) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderCard(
                    title = "Follow-up Questions",
                    subtitle = "Step 2 of 3 • $part • ${patient.displayName}",
                    gradient = topGradient
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InitialSymptomCard(
                            symptomText = symptomText,
                            painLevel = painLevel,
                            patientName = patient.displayName
                        )

                        if (loadingQuestions && rawQuestions.isEmpty()) {
                            LoadingCard("Preparing personalized follow-up questions...")
                        } else {
                            FollowUpFormCard(
                                uiQuestions = uiQuestions,
                                answer1 = answer1,
                                answer2 = answer2,
                                answer3 = answer3,
                                optionalNote = optionalNote,
                                onAnswer1 = { answer1 = it },
                                onAnswer2 = { answer2 = it },
                                onAnswer3 = { answer3 = it },
                                onOptionalNote = { optionalNote = it },
                                onSubmit = {
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
                                            patient = patient,
                                            recentHistory = recentHistory
                                        )
                                        loadingFinal = false

                                        if (!recordSaved) {
                                            val parsedResponse = parseGeminiResponse(rawResponse)
                                            viewModel.addHistoryRecord(
                                                SavedCheckRecord(
                                                    id = UUID.randomUUID().toString(),
                                                    personId = patient.id,
                                                    personName = patient.displayName,
                                                    personGroup = patient.group,
                                                    bodyPart = part,
                                                    symptomText = symptomText,
                                                    painLevel = painLevel,
                                                    urgency = parsedResponse.urgency,
                                                    careLevel = parsedResponse.careLevel,
                                                    summary = parsedResponse.summary,
                                                    mapQuery = parsedResponse.mapQuery
                                                )
                                            )
                                            recordSaved = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (loadingFinal) {
                            LoadingCard("Generating final assessment...")
                        }

                        if (rawResponse.isNotBlank()) {
                            ResultCards(
                                parsed = parsed,
                                onOpenMap = {
                                    if (parsed.mapQuery.isNotBlank() && parsed.mapQuery.lowercase() != "none") {
                                        navController.navigate("map?query=${Uri.encode(parsed.mapQuery)}")
                                    }
                                }
                            )
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderCard(
                    title = "Follow-up Questions",
                    subtitle = "Step 2 of 3 • $part • ${patient.displayName}",
                    gradient = topGradient
                )

                InitialSymptomCard(
                    symptomText = symptomText,
                    painLevel = painLevel,
                    patientName = patient.displayName
                )

                if (loadingQuestions && rawQuestions.isEmpty()) {
                    LoadingCard("Preparing personalized follow-up questions...")
                } else {
                    FollowUpFormCard(
                        uiQuestions = uiQuestions,
                        answer1 = answer1,
                        answer2 = answer2,
                        answer3 = answer3,
                        optionalNote = optionalNote,
                        onAnswer1 = { answer1 = it },
                        onAnswer2 = { answer2 = it },
                        onAnswer3 = { answer3 = it },
                        onOptionalNote = { optionalNote = it },
                        onSubmit = {
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
                                    patient = patient,
                                    recentHistory = recentHistory
                                )
                                loadingFinal = false

                                if (!recordSaved) {
                                    val parsedResponse = parseGeminiResponse(rawResponse)
                                    viewModel.addHistoryRecord(
                                        SavedCheckRecord(
                                            id = UUID.randomUUID().toString(),
                                            personId = patient.id,
                                            personName = patient.displayName,
                                            personGroup = patient.group,
                                            bodyPart = part,
                                            symptomText = symptomText,
                                            painLevel = painLevel,
                                            urgency = parsedResponse.urgency,
                                            careLevel = parsedResponse.careLevel,
                                            summary = parsedResponse.summary,
                                            mapQuery = parsedResponse.mapQuery
                                        )
                                    )
                                    recordSaved = true
                                }
                            }
                        }
                    )
                }

                if (loadingFinal) {
                    LoadingCard("Generating final assessment...")
                }

                if (rawResponse.isNotBlank()) {
                    ResultCards(
                        parsed = parsed,
                        onOpenMap = {
                            if (parsed.mapQuery.isNotBlank() && parsed.mapQuery.lowercase() != "none") {
                                navController.navigate("map?query=${Uri.encode(parsed.mapQuery)}")
                            }
                        }
                    )
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
}

@Composable
private fun InitialSymptomCard(
    symptomText: String,
    painLevel: Int,
    patientName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
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
                text = "Patient: $patientName",
                color = Color(0xFF667085)
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
}

@Composable
private fun FollowUpFormCard(
    uiQuestions: List<FollowUpUiQuestion>,
    answer1: String,
    answer2: String,
    answer3: String,
    optionalNote: String,
    onAnswer1: (String) -> Unit,
    onAnswer2: (String) -> Unit,
    onAnswer3: (String) -> Unit,
    onOptionalNote: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
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
                onValueChange = onAnswer1
            )

            QuestionCard(
                index = 2,
                question = uiQuestions.getOrNull(1)?.text ?: "Question 2",
                type = uiQuestions.getOrNull(1)?.type ?: FollowUpType.TEXT,
                value = answer2,
                onValueChange = onAnswer2
            )

            QuestionCard(
                index = 3,
                question = uiQuestions.getOrNull(2)?.text ?: "Question 3",
                type = uiQuestions.getOrNull(2)?.type ?: FollowUpType.TEXT,
                value = answer3,
                onValueChange = onAnswer3
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
                        onValueChange = onOptionalNote,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Anything else you want to mention?") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = answer1.isNotBlank() && answer2.isNotBlank() && answer3.isNotBlank(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Get Final Assessment")
            }
        }
    }
}

@Composable
private fun LoadingCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
            Text(text)
        }
    }
}

@Composable
private fun ResultCards(
    parsed: ParsedGeminiResponse,
    onOpenMap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RecommendationSummaryCard(parsed = parsed, onOpenMap = onOpenMap)

        SimpleSectionCard(
            title = "Key Points",
            items = parsed.keyPoints,
            bulletColor = Color(0xFF7B61FF)
        )

        SimpleSectionCard(
            title = "At Home / Self Care",
            items = parsed.selfCare,
            bulletColor = Color(0xFF12B76A)
        )

        SimpleSectionCard(
            title = "OTC Options",
            items = parsed.otcOptions,
            bulletColor = Color(0xFF4F8EEB)
        )

        SimpleSectionCard(
            title = "Next Steps",
            items = parsed.nextSteps,
            bulletColor = Color(0xFF7B61FF)
        )

        WarningCard(parsed.warningSigns)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
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
}

@Composable
private fun RecommendationSummaryCard(
    parsed: ParsedGeminiResponse,
    onOpenMap: () -> Unit
) {
    val urgencyColor = when (parsed.urgency.trim()) {
        "Emergency" -> Color(0xFFD92D20)
        "Urgent Care" -> Color(0xFFF79009)
        "Primary Care" -> Color(0xFF2E90FA)
        else -> Color(0xFF12B76A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Assessment",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(urgencyColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = parsed.urgency,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Text(
                text = parsed.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF344054)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Confidence",
                    value = "${parsed.recommendationScore}/5",
                    icon = Icons.Default.Star,
                    tint = Color(0xFFF79009)
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Care level",
                    value = parsed.careLevel.prettyCareLevel(),
                    icon = if (parsed.placeType == "pharmacy") Icons.Default.LocalPharmacy else Icons.Default.Map,
                    tint = urgencyColor
                )
            }

            if (parsed.mapQuery.isNotBlank() && parsed.mapQuery.lowercase() != "none") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Suggested Place")
                    }

                    OutlinedButton(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (parsed.placeType == "pharmacy") Icons.Default.LocalPharmacy else Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (parsed.placeType == "pharmacy") "Find Pharmacy" else "Find Care",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF667085)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF1F2937)
            )
        }
    }
}

@Composable
private fun SimpleSectionCard(
    title: String,
    items: List<String>,
    bulletColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
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
                            .background(bulletColor)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD92D20)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Seek Care Now If",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFB42318)
                )
            }

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
                    color = Color(0xFF1F2937),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
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
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current
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
        q.contains("when did it start") || q.contains("how long") || q.contains("how many days") || q.contains("when did this start") || q.contains("how long have you") -> FollowUpType.DURATION
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

    fun singleLineValue(name: String): String {
        return text.lines()
            .firstOrNull { it.trim().startsWith("$name:") }
            ?.substringAfter("$name:")
            ?.trim()
            .orEmpty()
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

    val urgency = singleLineValue("URGENCY").ifBlank { "Primary Care" }
    val careLevel = singleLineValue("CARE_LEVEL").ifBlank { "PRIMARY_CARE" }
    val placeType = singleLineValue("PLACE_TYPE").ifBlank { "primary care" }
    val mapQuery = singleLineValue("MAP_QUERY").ifBlank { "primary care clinic near me" }
    val recommendationScore = singleLineValue("RECOMMENDATION_SCORE").toIntOrNull() ?: 3

    val summary = sectionValue(
        "SUMMARY",
        listOf("KEY_POINTS", "SELF_CARE", "OTC_OPTIONS", "NEXT_STEPS", "WARNING_SIGNS", "NOTES")
    ).ifBlank { "No summary available." }

    val keyPoints = bulletList(
        sectionValue(
            "KEY_POINTS",
            listOf("SELF_CARE", "OTC_OPTIONS", "NEXT_STEPS", "WARNING_SIGNS", "NOTES")
        )
    )

    val selfCare = bulletList(
        sectionValue(
            "SELF_CARE",
            listOf("OTC_OPTIONS", "NEXT_STEPS", "WARNING_SIGNS", "NOTES")
        )
    )

    val otcOptions = bulletList(
        sectionValue(
            "OTC_OPTIONS",
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
        careLevel = careLevel,
        placeType = placeType,
        mapQuery = mapQuery,
        recommendationScore = recommendationScore,
        summary = summary,
        keyPoints = if (keyPoints.isEmpty()) listOf("No key points available.") else keyPoints,
        selfCare = if (selfCare.isEmpty()) listOf("Rest and monitor symptoms.") else selfCare,
        otcOptions = if (otcOptions.isEmpty()) listOf("Not appropriate without clinician or pharmacist guidance.") else otcOptions,
        nextSteps = if (nextSteps.isEmpty()) listOf("No next steps available.") else nextSteps,
        warningSigns = if (warningSigns.isEmpty()) listOf("No urgent warning signs listed.") else warningSigns,
        notes = notes
    )
}

private fun getBodyOverlaySpec(side: String, useTwoPane: Boolean): BodyOverlaySpec {
    val bounds = when (side) {
        "Front" -> if (useTwoPane) {
            BodyFrameBounds(0.11f, 0.08f, 0.78f, 0.84f)
        } else {
            BodyFrameBounds(0.10f, 0.07f, 0.80f, 0.86f)
        }

        "Back" -> if (useTwoPane) {
            BodyFrameBounds(0.11f, 0.08f, 0.78f, 0.84f)
        } else {
            BodyFrameBounds(0.10f, 0.07f, 0.80f, 0.86f)
        }

        else -> if (useTwoPane) {
            BodyFrameBounds(0.20f, 0.08f, 0.60f, 0.84f)
        } else {
            BodyFrameBounds(0.18f, 0.07f, 0.64f, 0.86f)
        }
    }

    val hotspots = when (side) {
        "Front" -> listOf(
            BodyHotspot("Forehead", 0.50f, 0.0001f, 0.052f),

            BodyHotspot("Neck", 0.50f, 0.17f, 0.050f),

            BodyHotspot("Left Shoulder", 0.30f, 0.15f, 0.052f),
            BodyHotspot("Right Shoulder", 0.70f, 0.15f, 0.052f),

            BodyHotspot("Left Chest", 0.43f, 0.33f, 0.052f),
            BodyHotspot("Center Chest", 0.50f, 0.34f, 0.052f),
            BodyHotspot("Right Chest", 0.57f, 0.33f, 0.052f),

            BodyHotspot(" Abdomen", 0.50f, 0.2f, 0.052f),


            BodyHotspot("Left Arm", 0.06f, 0.15f, 0.050f),
            BodyHotspot("Right Arm", 0.94f, 0.15f, 0.050f),



            BodyHotspot("Left Thigh", 0.38f, 0.5f, 0.048f),
            BodyHotspot("Right Thigh", 0.60f, 0.5f, 0.048f),

            BodyHotspot("Left Knee", 0.40f, 0.65f, 0.046f),
            BodyHotspot("Right Knee", 0.60f, 0.65f, 0.046f),

            BodyHotspot("Left Shin", 0.40f, 0.80f, 0.044f),
            BodyHotspot("Right Shin", 0.60f, 0.80f, 0.044f)
        )

        "Back" -> listOf(
            BodyHotspot("Back Head", 0.50f, 0.10f, 0.060f),
            BodyHotspot("Back Neck", 0.50f, 0.23f, 0.055f),
            BodyHotspot("Left Upper Back", 0.43f, 0.37f, 0.060f),
            BodyHotspot("Right Upper Back", 0.57f, 0.37f, 0.060f),
            BodyHotspot("Mid Back", 0.50f, 0.50f, 0.060f),
            BodyHotspot("Lower Back", 0.50f, 0.62f, 0.060f),
            BodyHotspot("Left Elbow Back", 0.10f, 0.51f, 0.055f),
            BodyHotspot("Right Elbow Back", 0.90f, 0.51f, 0.055f),
            BodyHotspot("Left Glute", 0.46f, 0.72f, 0.056f),
            BodyHotspot("Right Glute", 0.54f, 0.72f, 0.056f),
            BodyHotspot("Left Hamstring", 0.45f, 0.82f, 0.055f),
            BodyHotspot("Right Hamstring", 0.55f, 0.82f, 0.055f),
            BodyHotspot("Left Calf", 0.45f, 0.94f, 0.050f),
            BodyHotspot("Right Calf", 0.55f, 0.94f, 0.050f)
        )

        "Left" -> listOf(
            BodyHotspot("Left Temple", 0.55f, 0.10f, 0.055f),
            BodyHotspot("Left Jaw", 0.56f, 0.17f, 0.052f),
            BodyHotspot("Left Neck", 0.55f, 0.24f, 0.052f),
            BodyHotspot("Left Shoulder Side", 0.61f, 0.32f, 0.056f),
            BodyHotspot("Left Rib", 0.59f, 0.45f, 0.056f),
            BodyHotspot("Left Waist", 0.57f, 0.56f, 0.055f),
            BodyHotspot("Left Hip", 0.55f, 0.67f, 0.055f),
            BodyHotspot("Left Thigh Side", 0.53f, 0.81f, 0.055f),
            BodyHotspot("Left Knee Side", 0.53f, 0.92f, 0.050f),
            BodyHotspot("Left Lower Leg", 0.53f, 0.99f, 0.048f)
        )

        else -> listOf(
            BodyHotspot("Right Temple", 0.45f, 0.10f, 0.055f),
            BodyHotspot("Right Jaw", 0.44f, 0.17f, 0.052f),
            BodyHotspot("Right Neck", 0.45f, 0.24f, 0.052f),
            BodyHotspot("Right Shoulder Side", 0.39f, 0.32f, 0.056f),
            BodyHotspot("Right Rib", 0.41f, 0.45f, 0.056f),
            BodyHotspot("Right Waist", 0.43f, 0.56f, 0.055f),
            BodyHotspot("Right Hip", 0.45f, 0.67f, 0.055f),
            BodyHotspot("Right Thigh Side", 0.47f, 0.81f, 0.055f),
            BodyHotspot("Right Knee Side", 0.47f, 0.92f, 0.050f),
            BodyHotspot("Right Lower Leg", 0.47f, 0.99f, 0.048f)
        )
    }

    return BodyOverlaySpec(bounds = bounds, hotspots = hotspots)
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

private fun String.prettyCareLevel(): String {
    return lowercase()
        .replace('_', ' ')
        .split(' ')
        .joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}