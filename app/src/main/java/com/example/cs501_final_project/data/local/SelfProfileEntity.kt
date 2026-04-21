package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "self_profile")
data class SelfProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val birthDate: String = "",
    val age: String = "",
    val gender: String = "",
    val phone: String = "",
    val height: String = "",
    val weight: String = "",
    val address: String = "",
    val allergies: String = "",
    val medications: String = "",
    val conditions: String = "",
    val emergencyContact: String = ""
)