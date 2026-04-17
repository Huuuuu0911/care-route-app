package com.example.cs501_final_project.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {


    fun buildCategoryQuery(category: String): String {
        return when (category) {
            "Hospital" -> "hospital near me"
            "Pharmacy" -> "pharmacy near me"
            "Urgent Care" -> "urgent care near me"
            "Checkup Center" -> "primary care clinic near me"
            else -> "$category near me"
        }
    }
    val context = LocalContext.current
    val bgColor = Color(0xFFF6F8FC)

    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Hospital") }

    val quickCategories = remember {
        listOf("Hospital", "Pharmacy", "Urgent Care", "Checkup Center")
    }

    val highlightGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFEEF9FF),
            Color(0xFFF3F0FF)
        )
    )

    val defaultCenter = remember {
        LatLng(42.3505, -71.1054)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 13f)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = true,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    val mapProperties = remember {
        MapProperties(isBuildingEnabled = true)
    }

    fun openMapQuery(query: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        )
        context.startActivity(intent)
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
                    text = "Map",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Search nearby pharmacies, hospitals, urgent care, and checkup centers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667085)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(highlightGradient)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF4F8EEB)
                        )
                        Text(
                            text = "Nearby Search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search keyword") },
                        placeholder = { Text("e.g. 24 hour pharmacy, urgent care, pediatric clinic") },
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickCategories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    searchText = ""
                                    openMapQuery(buildCategoryQuery(category))
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE6F4EA),
                                    selectedLabelColor = Color(0xFF067647),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF48556A)
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = mapProperties,
                            uiSettings = mapUiSettings
                        ) {
                            Marker(
                                state = MarkerState(position = defaultCenter),
                                title = "CareRoute Map",
                                snippet = if (searchText.isBlank()) {
                                    "$selectedCategory search area"
                                } else {
                                    searchText
                                }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val query = if (searchText.isBlank()) {
                                buildCategoryQuery(selectedCategory)
                            } else {
                                "$searchText near me"
                            }
                            openMapQuery(query)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF12B76A)
                        )
                    ) {
                        Text("Open Nearby Search")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickMapCard(
                    modifier = Modifier.weight(1f),
                    title = "Hospitals",
                    subtitle = "Emergency and general care",
                    icon = Icons.Default.LocalHospital,
                    accent = Color(0xFF4F8EEB),
                    onClick = {
                        selectedCategory = "Hospital"
                        searchText = "hospital"
                        openMapQuery("hospital near me")
                    }
                )

                QuickMapCard(
                    modifier = Modifier.weight(1f),
                    title = "Pharmacy",
                    subtitle = "Prescription and OTC help",
                    icon = Icons.Default.Medication,
                    accent = Color(0xFF7B61FF),
                    onClick = {
                        selectedCategory = "Pharmacy"
                        searchText = "pharmacy"
                        openMapQuery("pharmacy near me")
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickMapCard(
                    modifier = Modifier.weight(1f),
                    title = "Urgent Care",
                    subtitle = "Faster walk-in options",
                    icon = Icons.Default.MedicalServices,
                    accent = Color(0xFFF79009),
                    onClick = {
                        selectedCategory = "Urgent Care"
                        searchText = "urgent care"
                        openMapQuery("urgent care near me")
                    }
                )

                QuickMapCard(
                    modifier = Modifier.weight(1f),
                    title = "Checkup Center",
                    subtitle = "Routine screening visits",
                    icon = Icons.Default.Place,
                    accent = Color(0xFF12B76A),
                    onClick = {
                        selectedCategory = "Checkup Center"
                        searchText = "checkup center"
                        openMapQuery("checkup center near me")
                    }
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color(0xFF12B76A)
                        )
                        Text(
                            text = "Smart Ideas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "This page now shows an embedded map directly in the app, while the green button still opens Google Maps for a real nearby search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF667085)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickMapCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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