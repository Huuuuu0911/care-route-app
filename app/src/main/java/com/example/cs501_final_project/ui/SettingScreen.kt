package com.example.cs501_final_project.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cs501_final_project.data.AccentThemeOption
import com.example.cs501_final_project.data.CareRouteViewModel

@Composable
fun SettingScreen(
    viewModel: CareRouteViewModel,
    onLogout: () -> Unit
) {
    val profile = viewModel.selfProfile
    val settings = viewModel.settings

    var showEditDialog by rememberSaveable { mutableStateOf(false) }

    if (showEditDialog) {
        var tempName by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
        var tempBirthDate by rememberSaveable(profile.birthDate) { mutableStateOf(profile.birthDate) }
        var tempGender by rememberSaveable(profile.gender) {
            mutableStateOf(
                if (profile.gender == "Male" || profile.gender == "Female") profile.gender else ""
            )
        }
        var tempHeight by rememberSaveable(profile.height) { mutableStateOf(profile.height) }
        var tempWeight by rememberSaveable(profile.weight) { mutableStateOf(profile.weight) }
        var tempAddress by rememberSaveable(profile.address) { mutableStateOf(profile.address) }
        var tempConditions by rememberSaveable(profile.conditions) { mutableStateOf(profile.conditions) }
        var tempAllergies by rememberSaveable(profile.allergies) { mutableStateOf(profile.allergies) }
        var tempMedications by rememberSaveable(profile.medications) { mutableStateOf(profile.medications) }
        var tempEmergencyContact by rememberSaveable(profile.emergencyContact) {
            mutableStateOf(profile.emergencyContact)
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateSelfProfile(
                            profile.copy(
                                name = tempName.trim(),
                                birthDate = formatBirthDateInput(tempBirthDate),
                                gender = tempGender,
                                height = tempHeight.trim(),
                                weight = tempWeight.trim(),
                                address = tempAddress.trim(),
                                conditions = tempConditions.trim(),
                                allergies = tempAllergies.trim(),
                                medications = tempMedications.trim(),
                                emergencyContact = tempEmergencyContact.trim()
                            )
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "Edit Main Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempBirthDate,
                        onValueChange = { tempBirthDate = formatBirthDateInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Birth Date") },
                        placeholder = { Text("MM / DD / YYYY") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF667085)
                            )
                        }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF667085)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Male", "Female").forEach { option ->
                                FilterChip(
                                    selected = tempGender == option,
                                    onClick = { tempGender = option },
                                    label = { Text(option) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFEAE6FF),
                                        selectedLabelColor = Color(0xFF4B3BC8)
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempHeight,
                        onValueChange = { tempHeight = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Height") },
                        placeholder = { Text("Example: 5'10 or 178 cm") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempWeight,
                        onValueChange = { tempWeight = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Weight") },
                        placeholder = { Text("Example: 160 lb or 72 kg") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempAddress,
                        onValueChange = { tempAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Address") },
                        placeholder = { Text("Home address or city") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempConditions,
                        onValueChange = { tempConditions = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Conditions") },
                        placeholder = { Text("Example: asthma, diabetes") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempAllergies,
                        onValueChange = { tempAllergies = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Allergies") },
                        placeholder = { Text("Example: penicillin, peanuts") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempMedications,
                        onValueChange = { tempMedications = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Medications") },
                        placeholder = { Text("Current medications") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = tempEmergencyContact,
                        onValueChange = { tempEmergencyContact = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Emergency Contact") },
                        placeholder = { Text("Name, email, or relationship") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ProfileHeader(
                displayName = profile.name.ifBlank { "User" },
                birthDate = profile.birthDate,
                onEditClick = { showEditDialog = true }
            )

            SectionTitle("App Preferences")

            PreferencesSection(
                notificationsOn = settings.notificationsEnabled,
                onNotificationsChange = { viewModel.toggleNotifications(it) },
                darkModeOn = settings.darkModeEnabled,
                onDarkModeChange = { viewModel.toggleDarkMode(it) },
                accentColor = settings.accentTheme,
                onAccentColorChange = { viewModel.updateAccentTheme(it) }
            )

            SectionTitle("More")

            UsefulLinksSection(
                profileCompletion = viewModel.profileCompletionScore(),
                archiveCount = viewModel.historyRecords.size + viewModel.importedMedicalRecords.size
            )

            AccountFooter(
                currentUser = profile.name.ifBlank { "User" },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String,
    birthDate: String,
    onEditClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2F6BFF),
            Color(0xFF6D4BFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(gradient)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Main Profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }

                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", color = Color.White)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.18f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoPill(
                    icon = Icons.Default.CalendarMonth,
                    text = birthDate.ifBlank { "MM / DD / YYYY" }
                )
            }
        }
    }
}

@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun PreferencesSection(
    notificationsOn: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    darkModeOn: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    accentColor: AccentThemeOption,
    onAccentColorChange: (AccentThemeOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PreferenceRow(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Medication and reminder alerts",
            trailing = {
                Switch(
                    checked = notificationsOn,
                    onCheckedChange = onNotificationsChange
                )
            }
        )

        PreferenceRow(
            icon = Icons.Default.SettingsSuggest,
            title = "Dark Mode",
            subtitle = "Reduce eye strain at night",
            trailing = {
                Switch(
                    checked = darkModeOn,
                    onCheckedChange = onDarkModeChange
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color(0xFFE7ECF3), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF4E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color(0xFFF79009)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Accent Theme",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Choose your preferred app color",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccentThemeOption.entries.forEach { option ->
                    FilterChip(
                        selected = accentColor == option,
                        onClick = { onAccentColorChange(option) },
                        label = { Text(option.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEAE6FF),
                            selectedLabelColor = Color(0xFF4B3BC8)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0xFFE7ECF3), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF5F7FC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667085)
            )
        }

        trailing()
    }
}

@Composable
private fun UsefulLinksSection(
    profileCompletion: Int,
    archiveCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        UsefulRow(
            icon = Icons.Default.Person,
            title = "Profile Completion",
            subtitle = "$profileCompletion% complete. More details improve triage accuracy."
        )

        UsefulRow(
            icon = Icons.Default.SettingsSuggest,
            title = "History Archive",
            subtitle = "You have $archiveCount saved check or medical record item(s)."
        )
    }
}

@Composable
private fun UsefulRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0xFFE7ECF3), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF5F7FC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2F6BFF)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
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
private fun AccountFooter(
    currentUser: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(color = Color(0xFFE4E7EC))

        Text(
            text = "Signed in as $currentUser",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF667085)
        )

        TextButton(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = Color(0xFFD92D20)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log out",
                color = Color(0xFFD92D20)
            )
        }
    }
}

private fun AccentThemeOption.displayName(): String {
    return when (this) {
        AccentThemeOption.BLUE -> "Blue"
        AccentThemeOption.PURPLE -> "Purple"
        AccentThemeOption.GREEN -> "Green"
        AccentThemeOption.ORANGE -> "Orange"
    }
}

private fun formatBirthDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)

    return buildString {
        digits.forEachIndexed { index, c ->
            append(c)
            if (index == 1 && index != digits.lastIndex) append(" / ")
            if (index == 3 && index != digits.lastIndex) append(" / ")
        }
    }
}
