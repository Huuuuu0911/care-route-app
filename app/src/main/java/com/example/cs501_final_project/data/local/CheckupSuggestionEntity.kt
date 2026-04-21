package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkup_suggestions",
    indices = [
        Index(value = ["personId"]),
        Index(value = ["generatedDate"]),
        Index(value = ["priority"])
    ]
)
data class CheckupSuggestionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val reason: String,
    val timeframe: String,
    val priority: Int,
    val personId: String,
    val generatedDate: String
)