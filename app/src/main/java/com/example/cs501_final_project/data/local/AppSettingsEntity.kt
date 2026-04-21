package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cs501_final_project.data.AccentThemeOption

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val accentTheme: AccentThemeOption = AccentThemeOption.BLUE
)