package com.example.cs501_final_project.data

enum class AccentThemeOption {
    BLUE,
    PURPLE,
    GREEN,
    ORANGE
}

enum class MedicalRecordSourceType {
    MANUAL_ENTRY,
    GALLERY_UPLOAD,
    CAMERA_CAPTURE
}

data class PatientProfile(
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

data class FamilyMember(
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

data class SavedCheckRecord(
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

data class DailyHealthTip(
    val title: String,
    val message: String,
    val focusArea: String,
    val caution: String,
    val generatedDate: String,
    val personId: String,
    val source: String = "Gemini"
)

data class PersonalizedCheckupSuggestion(
    val id: String,
    val title: String,
    val reason: String,
    val timeframe: String,
    val priority: Int,
    val personId: String,
    val generatedDate: String
)

data class ImportedMedicalRecord(
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

data class ImportedMedicalRecordDraft(
    val title: String,
    val summary: String,
    val findings: List<String>,
    val recommendedFollowUp: List<String>,
    val rawText: String = ""
)

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val accentTheme: AccentThemeOption = AccentThemeOption.BLUE
)

data class PatientContext(
    val id: String,
    val displayName: String,
    val group: String,
    val age: String,
    val birthDate: String = "",
    val gender: String,
    val phone: String = "",
    val height: String,
    val weight: String,
    val address: String,
    val allergies: String,
    val medications: String,
    val conditions: String,
    val emergencyContact: String = ""
)
