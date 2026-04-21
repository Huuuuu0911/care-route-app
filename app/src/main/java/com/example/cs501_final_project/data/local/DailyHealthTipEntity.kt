package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_health_tips",
    indices = [Index(value = ["generatedDate"])]
)
data class DailyHealthTipEntity(
    @PrimaryKey
    val personId: String,
    val title: String,
    val message: String,
    val focusArea: String,
    val caution: String,
    val generatedDate: String,
    val source: String = "Gemini"
)