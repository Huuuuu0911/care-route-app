package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_records",
    indices = [
        Index(value = ["personId"]),
        Index(value = ["createdAt"]),
        Index(value = ["urgency"])
    ]
)
data class SavedCheckRecordEntity(
    @PrimaryKey
    val id: String,
    val personId: String,
    val personName: String,
    val personGroup: String,
    val bodyPart: String,
    val symptomText: String,
    val painLevel: Int,
    val urgency: String,
    val careLevel: String,
    val summary: String,
    val mapQuery: String,
    val createdAt: Long = System.currentTimeMillis()
)