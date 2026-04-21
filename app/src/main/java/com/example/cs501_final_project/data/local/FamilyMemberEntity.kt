package com.example.cs501_final_project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "family_members",
    indices = [Index(value = ["name"])]
)
data class FamilyMemberEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val relation: String,
    val birthDate: String = "",
    val age: String = "",
    val gender: String = "",
    val allergies: String = "",
    val medications: String = "",
    val conditions: String = "",
    val notes: String = ""
)