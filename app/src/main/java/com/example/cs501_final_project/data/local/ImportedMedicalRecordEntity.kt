package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.cs501_final_project.data.MedicalRecordSourceType

@Entity(
    tableName = "imported_medical_records",
    indices = [
        Index(value = ["personId"]),
        Index(value = ["createdAt"]),
        Index(value = ["sourceType"])
    ]
)
data class ImportedMedicalRecordEntity(
    @PrimaryKey
    val id: String,
    val personId: String,
    val personName: String,
    val sourceType: MedicalRecordSourceType,
    val sourceLabel: String,
    val title: String,
    val summary: String,
    val findings: List<String>,
    val recommendedFollowUp: List<String>,
    val rawText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)