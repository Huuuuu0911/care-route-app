package com.example.cs501_final_project.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.cs501_final_project.data.CareRouteViewModel
import com.example.cs501_final_project.data.GeminiRepository
import com.example.cs501_final_project.data.ImportedMedicalRecord
import com.example.cs501_final_project.data.MedicalRecordSourceType
import com.example.cs501_final_project.data.SavedCheckRecord
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.input.KeyboardType

private enum class HistoryFilterOption {
    ALL,
    CHECKS,
    RECORDS
}

private sealed interface HistoryFeedItem {
    val id: String
    val createdAt: Long

    data class Check(val record: SavedCheckRecord) : HistoryFeedItem {
        override val id: String = record.id
        override val createdAt: Long = record.createdAt
    }

    data class Record(val document: ImportedMedicalRecord) : HistoryFeedItem {
        override val id: String = document.id
        override val createdAt: Long = document.createdAt
    }
}

@Composable
fun HistoryScreen(
    viewModel: CareRouteViewModel,
    onOpenMap: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { GeminiRepository() }
    val scope = rememberCoroutineScope()
    val bgColor = MaterialTheme.colorScheme.background

    var filter by rememberSaveable { mutableStateOf(HistoryFilterOption.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showManualDialog by rememberSaveable { mutableStateOf(false) }
    var processingMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val patient = viewModel.activePatientContext()
    val checkRecords = viewModel.historyRecords.toList()
    val importedRecords = viewModel.importedMedicalRecords.toList()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            processingMessage = "Summarizing uploaded record..."
            val bytes = readImageBytesFromUri(context, uri)
            if (bytes == null) {
                statusMessage = "Could not read that image. Please try a clearer file."
                processingMessage = null
                return@launch
            }

            val draft = repository.summarizeMedicalRecordImage(
                patient = patient,
                imageBytes = bytes,
                mimeType = "image/jpeg"
            )

            viewModel.addImportedMedicalRecord(
                person = patient,
                sourceType = MedicalRecordSourceType.GALLERY_UPLOAD,
                sourceLabel = "Gallery upload",
                title = draft.title,
                summary = draft.summary,
                findings = draft.findings,
                recommendedFollowUp = draft.recommendedFollowUp,
                rawText = draft.rawText
            )

            statusMessage = "Medical record added to your archive."
            processingMessage = null
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult

        scope.launch {
            processingMessage = "Analyzing captured photo..."
            val bytes = bitmapToJpegBytes(bitmap)
            val draft = repository.summarizeMedicalRecordImage(
                patient = patient,
                imageBytes = bytes,
                mimeType = "image/jpeg"
            )

            viewModel.addImportedMedicalRecord(
                person = patient,
                sourceType = MedicalRecordSourceType.CAMERA_CAPTURE,
                sourceLabel = "Camera capture",
                title = draft.title,
                summary = draft.summary,
                findings = draft.findings,
                recommendedFollowUp = draft.recommendedFollowUp,
                rawText = draft.rawText
            )

            statusMessage = "Photo-based medical note saved."
            processingMessage = null
        }
    }

    val feedItems = remember(checkRecords, importedRecords, filter, searchQuery) {
        val normalizedQuery = searchQuery.trim().lowercase()
        val baseItems = buildList {
            if (filter == HistoryFilterOption.ALL || filter == HistoryFilterOption.CHECKS) {
                addAll(checkRecords.map { HistoryFeedItem.Check(it) })
            }
            if (filter == HistoryFilterOption.ALL || filter == HistoryFilterOption.RECORDS) {
                addAll(importedRecords.map { HistoryFeedItem.Record(it) })
            }
        }

        baseItems
            .filter { item ->
                if (normalizedQuery.isBlank()) return@filter true
                when (item) {
                    is HistoryFeedItem.Check -> {
                        val text = listOf(
                            item.record.personName,
                            item.record.bodyPart,
                            item.record.symptomText,
                            item.record.summary,
                            item.record.urgency
                        ).joinToString(" ")
                        text.lowercase().contains(normalizedQuery)
                    }
                    is HistoryFeedItem.Record -> {
                        val text = buildString {
                            append(item.document.personName)
                            append(' ')
                            append(item.document.title)
                            append(' ')
                            append(item.document.summary)
                            append(' ')
                            append(item.document.findings.joinToString(" "))
                        }
                        text.lowercase().contains(normalizedQuery)
                    }
                }
            }
            .sortedByDescending { it.createdAt }
    }

    if (showManualDialog) {
        ManualRecordDialog(
            onDismiss = { showManualDialog = false },
            onSave = { title, noteText ->
                scope.launch {
                    processingMessage = "Structuring manual medical record..."
                    val draft = repository.structureManualMedicalRecord(
                        patient = patient,
                        title = title,
                        rawText = noteText
                    )
                    viewModel.addImportedMedicalRecord(
                        person = patient,
                        sourceType = MedicalRecordSourceType.MANUAL_ENTRY,
                        sourceLabel = "Manual entry",
                        title = draft.title,
                        summary = draft.summary,
                        findings = draft.findings,
                        recommendedFollowUp = draft.recommendedFollowUp,
                        rawText = draft.rawText
                    )
                    showManualDialog = false
                    statusMessage = "Manual history note added."
                    processingMessage = null
                }
            }
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Build a long-term archive with symptom checks, manual history notes, and uploaded medical documents.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667085)
                )
            }

            SummaryRow(
                checkCount = checkRecords.size,
                recordCount = importedRecords.size,
                currentProfile = patient.displayName
            )

            if (processingMessage != null) {
                ProcessingCard(text = processingMessage.orEmpty())
            }

            if (statusMessage != null) {
                StatusCard(
                    text = statusMessage.orEmpty(),
                    onDismiss = { statusMessage = null }
                )
            }

            SearchAndUploadCard(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                filter = filter,
                onFilterChange = { filter = it },
                onManualEntryClick = { showManualDialog = true },
                onGalleryClick = {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onCameraClick = { takePhotoLauncher.launch(null) }
            )

            if (feedItems.isEmpty()) {
                EmptyHistoryCard()
            } else {
                feedItems.forEach { item ->
                    when (item) {
                        is HistoryFeedItem.Check -> HistoryRecordCard(
                            record = item.record,
                            onDelete = { viewModel.deleteHistoryRecord(item.record.id) },
                            onOpenMap = {
                                onOpenMap(
                                    item.record.mapQuery.ifBlank {
                                        "${item.record.urgency} care near me"
                                    }
                                )
                            }
                        )

                        is HistoryFeedItem.Record -> ImportedMedicalRecordCard(
                            record = item.document,
                            onDelete = { viewModel.deleteImportedMedicalRecord(item.document.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    checkCount: Int,
    recordCount: Int,
    currentProfile: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCountCard(
            modifier = Modifier.weight(1f),
            title = "Checks",
            value = checkCount.toString(),
            icon = Icons.Default.History,
            accent = Color(0xFF4F8EEB)
        )
        SummaryCountCard(
            modifier = Modifier.weight(1f),
            title = "Records",
            value = recordCount.toString(),
            icon = Icons.Default.Description,
            accent = Color(0xFF12B76A)
        )
        SummaryCountCard(
            modifier = Modifier.weight(1f),
            title = "Profile",
            value = if (currentProfile.length <= 6) currentProfile else currentProfile.take(6),
            icon = Icons.Default.Person,
            accent = Color(0xFF7B61FF)
        )
    }
}

@Composable
private fun SearchAndUploadCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filter: HistoryFilterOption,
    onFilterChange: (HistoryFilterOption) -> Unit,
    onManualEntryClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search history and uploaded records") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == HistoryFilterOption.ALL,
                    onClick = { onFilterChange(HistoryFilterOption.ALL) },
                    label = { Text("All") },
                    leadingIcon = { Icon(Icons.Default.FilterAlt, contentDescription = null) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEAE6FF),
                        selectedLabelColor = Color(0xFF4B3BC8)
                    )
                )
                FilterChip(
                    selected = filter == HistoryFilterOption.CHECKS,
                    onClick = { onFilterChange(HistoryFilterOption.CHECKS) },
                    label = { Text("Checks") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEFF8FF),
                        selectedLabelColor = Color(0xFF175CD3)
                    )
                )
                FilterChip(
                    selected = filter == HistoryFilterOption.RECORDS,
                    onClick = { onFilterChange(HistoryFilterOption.RECORDS) },
                    label = { Text("Records") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F7EF),
                        selectedLabelColor = Color(0xFF067647)
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UploadActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Manual",
                    icon = Icons.Default.Add,
                    accent = Color(0xFF7B61FF),
                    onClick = onManualEntryClick
                )
                UploadActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Upload",
                    icon = Icons.Default.Image,
                    accent = Color(0xFF12B76A),
                    onClick = onGalleryClick
                )
                UploadActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Camera",
                    icon = Icons.Default.CameraAlt,
                    accent = Color(0xFFF79009),
                    onClick = onCameraClick
                )
            }
        }
    }
}

@Composable
private fun UploadActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        }
    }
}

@Composable
private fun ProcessingCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text)
        }
    }
}

@Composable
private fun StatusCard(
    text: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color(0xFF175CD3),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun ManualRecordDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, noteText: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onSave(title, noteText)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = "Add Medical History Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    placeholder = { Text("Example: Shoulder MRI result") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    label = { Text("Summary or note") },
                    placeholder = { Text("Write your past diagnosis, note from a clinician, or key details from a previous visit...") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No history yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Complete a symptom check, add a manual medical history note, or upload a past document. Everything you save here helps future suggestions feel more personalized.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )
        }
    }
}

@Composable
private fun SummaryCountCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent
            )
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
private fun HistoryRecordCard(
    record: SavedCheckRecord,
    onDelete: () -> Unit,
    onOpenMap: () -> Unit
) {
    val urgencyColor = when (record.urgency) {
        "Emergency" -> Color(0xFFD92D20)
        "Urgent Care" -> Color(0xFFF79009)
        "Primary Care" -> Color(0xFF2E90FA)
        else -> Color(0xFF12B76A)
    }

    val formattedTime = SimpleDateFormat("MMM dd · h:mm a", Locale.getDefault())
        .format(record.createdAt)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.personName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$formattedTime · ${record.personGroup}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(urgencyColor.copy(alpha = 0.14f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = record.urgency,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                }
            }

            Text(
                text = "${record.bodyPart} · Pain ${record.painLevel}/10",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = record.symptomText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF344054)
            )

            Text(
                text = record.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onOpenMap) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Open map",
                        tint = Color(0xFF12B76A)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = Color(0xFFD92D20)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportedMedicalRecordCard(
    record: ImportedMedicalRecord,
    onDelete: () -> Unit
) {
    val accent = when (record.sourceType) {
        MedicalRecordSourceType.MANUAL_ENTRY -> Color(0xFF7B61FF)
        MedicalRecordSourceType.GALLERY_UPLOAD -> Color(0xFF12B76A)
        MedicalRecordSourceType.CAMERA_CAPTURE -> Color(0xFFF79009)
    }

    val formattedTime = SimpleDateFormat("MMM dd · h:mm a", Locale.getDefault())
        .format(record.createdAt)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$formattedTime · ${record.personName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = record.sourceLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            Text(
                text = record.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF344054)
            )

            if (record.findings.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Key findings",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF475467)
                    )
                    record.findings.take(3).forEach { item ->
                        BulletLine(item = item, bulletColor = accent)
                    }
                }
            }

            if (record.recommendedFollowUp.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Recommended follow-up",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF475467)
                    )
                    record.recommendedFollowUp.take(2).forEach { item ->
                        BulletLine(item = item, bulletColor = Color(0xFF2F6BFF))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete imported record",
                        tint = Color(0xFFD92D20)
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletLine(
    item: String,
    bulletColor: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            color = Color(0xFF475467),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun readImageBytesFromUri(context: android.content.Context, uri: Uri): ByteArray? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input) ?: return null
            bitmapToJpegBytes(bitmap)
        }
    }.getOrNull()
}

private fun bitmapToJpegBytes(bitmap: Bitmap): ByteArray {
    return ByteArrayOutputStream().use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    }
}
