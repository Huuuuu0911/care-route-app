package com.example.cs501_final_project.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.UUID

class CareRouteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("care_route_data", Application.MODE_PRIVATE)
    private val gson = Gson()

    var selfProfile by mutableStateOf(loadSelfProfile())
        private set

    var settings by mutableStateOf(loadSettings())
        private set

    var selectedPersonId by mutableStateOf(loadSelectedPersonId())
        private set

    var dailyHealthTip by mutableStateOf(loadDailyHealthTip())
        private set

    private val _familyMembers = mutableStateListOf<FamilyMember>().apply { addAll(loadFamilyMembers()) }
    val familyMembers: List<FamilyMember> get() = _familyMembers

    private val _historyRecords = mutableStateListOf<SavedCheckRecord>().apply { addAll(loadHistoryRecords()) }
    val historyRecords: List<SavedCheckRecord> get() = _historyRecords

    private val _importedMedicalRecords = mutableStateListOf<ImportedMedicalRecord>().apply { addAll(loadImportedMedicalRecords()) }
    val importedMedicalRecords: List<ImportedMedicalRecord> get() = _importedMedicalRecords

    private val _checkupSuggestions = mutableStateListOf<PersonalizedCheckupSuggestion>().apply { addAll(loadCheckupSuggestions()) }
    val checkupSuggestions: List<PersonalizedCheckupSuggestion> get() = _checkupSuggestions

    fun updateSelfProfile(profile: PatientProfile) {
        selfProfile = profile
        saveSelfProfile()
    }

    fun updateSettings(updated: AppSettings) {
        settings = updated
        saveSettings()
    }

    fun toggleNotifications(enabled: Boolean) {
        settings = settings.copy(notificationsEnabled = enabled)
        saveSettings()
    }

    fun toggleDarkMode(enabled: Boolean) {
        settings = settings.copy(darkModeEnabled = enabled)
        saveSettings()
    }

    fun updateAccentTheme(accentThemeOption: AccentThemeOption) {
        settings = settings.copy(accentTheme = accentThemeOption)
        saveSettings()
    }

    fun selectPerson(personId: String) {
        selectedPersonId = personId
        saveSelectedPersonId()
    }

    fun activePatientContext(): PatientContext {
        val member = _familyMembers.firstOrNull { it.id == selectedPersonId }
        return if (selectedPersonId == "self" || member == null) {
            PatientContext(
                id = "self",
                displayName = selfProfile.name.ifBlank { "You" },
                group = "Mine",
                age = selfProfile.age.ifBlank { calculateAgeFromBirthDate(selfProfile.birthDate) },
                birthDate = selfProfile.birthDate,
                gender = selfProfile.gender,
                phone = selfProfile.phone,
                height = selfProfile.height,
                weight = selfProfile.weight,
                address = selfProfile.address,
                allergies = selfProfile.allergies,
                medications = selfProfile.medications,
                conditions = selfProfile.conditions,
                emergencyContact = selfProfile.emergencyContact
            )
        } else {
            PatientContext(
                id = member.id,
                displayName = member.name,
                group = "Family",
                age = member.age.ifBlank { calculateAgeFromBirthDate(member.birthDate) },
                birthDate = member.birthDate,
                gender = member.gender,
                phone = "",
                height = "",
                weight = "",
                address = selfProfile.address,
                allergies = member.allergies,
                medications = member.medications,
                conditions = member.conditions,
                emergencyContact = selfProfile.emergencyContact
            )
        }
    }

    fun upsertFamilyMember(member: FamilyMember) {
        val index = _familyMembers.indexOfFirst { it.id == member.id }
        if (index >= 0) {
            _familyMembers[index] = member
        } else {
            _familyMembers.add(member)
        }
        saveFamilyMembers()
    }

    fun createFamilyMember(
        name: String,
        relation: String,
        birthDate: String,
        age: String,
        gender: String,
        allergies: String,
        medications: String,
        conditions: String,
        notes: String
    ) {
        _familyMembers.add(
            FamilyMember(
                id = UUID.randomUUID().toString(),
                name = name,
                relation = relation,
                birthDate = birthDate,
                age = age,
                gender = gender,
                allergies = allergies,
                medications = medications,
                conditions = conditions,
                notes = notes
            )
        )
        saveFamilyMembers()
    }

    fun deleteFamilyMember(memberId: String) {
        _familyMembers.removeAll { it.id == memberId }
        if (selectedPersonId == memberId) {
            selectedPersonId = "self"
            saveSelectedPersonId()
        }
        _historyRecords.removeAll { it.personId == memberId }
        _importedMedicalRecords.removeAll { it.personId == memberId }
        _checkupSuggestions.removeAll { it.personId == memberId }
        if (dailyHealthTip?.personId == memberId) {
            dailyHealthTip = null
            saveDailyHealthTip()
        }
        saveFamilyMembers()
        saveHistoryRecords()
        saveImportedMedicalRecords()
        saveCheckupSuggestions()
    }

    fun addHistoryRecord(record: SavedCheckRecord) {
        _historyRecords.add(0, record)
        saveHistoryRecords()
    }

    fun deleteHistoryRecord(recordId: String) {
        _historyRecords.removeAll { it.id == recordId }
        saveHistoryRecords()
    }

    fun addImportedMedicalRecord(
        person: PatientContext,
        sourceType: MedicalRecordSourceType,
        sourceLabel: String,
        title: String,
        summary: String,
        findings: List<String>,
        recommendedFollowUp: List<String>,
        rawText: String = ""
    ) {
        _importedMedicalRecords.add(
            0,
            ImportedMedicalRecord(
                id = UUID.randomUUID().toString(),
                personId = person.id,
                personName = person.displayName,
                sourceType = sourceType,
                sourceLabel = sourceLabel,
                title = title,
                summary = summary,
                findings = findings,
                recommendedFollowUp = recommendedFollowUp,
                rawText = rawText
            )
        )
        saveImportedMedicalRecords()
    }

    fun deleteImportedMedicalRecord(recordId: String) {
        _importedMedicalRecords.removeAll { it.id == recordId }
        saveImportedMedicalRecords()
    }

    fun clearHistoryArchive() {
        _historyRecords.clear()
        _importedMedicalRecords.clear()
        saveHistoryRecords()
        saveImportedMedicalRecords()
    }

    fun recentHistorySummariesFor(personId: String): List<String> {
        return _historyRecords
            .filter { it.personId == personId }
            .sortedByDescending { it.createdAt }
            .take(4)
            .map { "${it.bodyPart}: ${it.symptomText} (${it.urgency})" }
    }

    fun recentImportedRecordSummariesFor(personId: String): List<String> {
        return _importedMedicalRecords
            .filter { it.personId == personId }
            .sortedByDescending { it.createdAt }
            .take(4)
            .map { "${it.title}: ${it.summary}" }
    }

    fun latestHistoryRecordFor(personId: String): SavedCheckRecord? {
        return _historyRecords
            .filter { it.personId == personId }
            .maxByOrNull { it.createdAt }
    }

    fun latestImportedRecordFor(personId: String): ImportedMedicalRecord? {
        return _importedMedicalRecords
            .filter { it.personId == personId }
            .maxByOrNull { it.createdAt }
    }

    fun updateDailyHealthTip(tip: DailyHealthTip) {
        dailyHealthTip = tip
        saveDailyHealthTip()
    }

    fun shouldRefreshDailyTip(personId: String): Boolean {
        val today = todayKey()
        return dailyHealthTip == null || dailyHealthTip?.generatedDate != today || dailyHealthTip?.personId != personId
    }

    fun updateCheckupSuggestions(personId: String, suggestions: List<PersonalizedCheckupSuggestion>) {
        _checkupSuggestions.removeAll { it.personId == personId }
        _checkupSuggestions.addAll(0, suggestions)
        saveCheckupSuggestions()
    }

    fun getCheckupSuggestionsFor(personId: String): List<PersonalizedCheckupSuggestion> {
        return _checkupSuggestions
            .filter { it.personId == personId }
            .sortedByDescending { it.priority }
            .take(3)
    }

    fun shouldRefreshCheckupSuggestions(personId: String): Boolean {
        val suggestions = getCheckupSuggestionsFor(personId)
        return suggestions.isEmpty() || suggestions.first().generatedDate != todayKey()
    }

    fun suggestedCheckupFocus(): List<String> {
        val active = activePatientContext()
        val cached = getCheckupSuggestionsFor(active.id)
        if (cached.isNotEmpty()) {
            return cached.map { "${it.title} · ${it.timeframe}" }
        }

        val fallback = mutableListOf<String>()
        val ageNumber = active.age.toIntOrNull()
        if (ageNumber != null && ageNumber >= 40) {
            fallback.add("Routine blood pressure and cholesterol check")
        } else {
            fallback.add("General preventive wellness visit")
        }
        if (active.conditions.contains("asthma", ignoreCase = true)) {
            fallback.add("Breathing review and trigger plan")
        }
        if (active.conditions.contains("diabetes", ignoreCase = true)) {
            fallback.add("Glucose review and foot check")
        }
        if (_historyRecords.any { it.personId == active.id && it.bodyPart.contains("Chest", ignoreCase = true) }) {
            fallback.add("Review repeat chest symptoms with a clinician")
        }
        return fallback.take(3)
    }

    fun profileCompletionScore(): Int {
        var score = 0
        if (selfProfile.name.isNotBlank()) score += 20
        if (selfProfile.birthDate.isNotBlank()) score += 15
        if (selfProfile.gender.isNotBlank()) score += 10
        if (selfProfile.phone.isNotBlank()) score += 10
        if (selfProfile.conditions.isNotBlank()) score += 15
        if (selfProfile.allergies.isNotBlank()) score += 15
        if (selfProfile.medications.isNotBlank()) score += 10
        if (selfProfile.emergencyContact.isNotBlank()) score += 5
        return score.coerceAtMost(100)
    }

    fun archiveRecordCountFor(personId: String): Int {
        return _historyRecords.count { it.personId == personId } +
                _importedMedicalRecords.count { it.personId == personId }
    }

    private fun todayKey(): String {
        val calendar = Calendar.getInstance()
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val year = calendar.get(Calendar.YEAR)
        return "$year-$month-$day"
    }

    private fun calculateAgeFromBirthDate(birthDate: String): String {
        val digits = birthDate.filter { it.isDigit() }
        if (digits.length != 8) return ""

        val month = digits.substring(0, 2).toIntOrNull() ?: return ""
        val day = digits.substring(2, 4).toIntOrNull() ?: return ""
        val year = digits.substring(4, 8).toIntOrNull() ?: return ""

        if (month !in 1..12 || day !in 1..31 || year !in 1900..2100) return ""

        val today = Calendar.getInstance()
        val age = today.get(Calendar.YEAR) - year - if (
            today.get(Calendar.MONTH) + 1 < month ||
            (today.get(Calendar.MONTH) + 1 == month && today.get(Calendar.DAY_OF_MONTH) < day)
        ) 1 else 0

        return if (age >= 0) age.toString() else ""
    }

    private fun loadSelfProfile(): PatientProfile {
        val json = prefs.getString(KEY_SELF_PROFILE, null) ?: return PatientProfile(name = "You")
        return runCatching { gson.fromJson(json, PatientProfile::class.java) }
            .getOrDefault(PatientProfile(name = "You"))
    }

    private fun loadSettings(): AppSettings {
        val json = prefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return runCatching { gson.fromJson(json, AppSettings::class.java) }.getOrDefault(AppSettings())
    }

    private fun loadSelectedPersonId(): String {
        return prefs.getString(KEY_SELECTED_PERSON, "self") ?: "self"
    }

    private fun loadDailyHealthTip(): DailyHealthTip? {
        val json = prefs.getString(KEY_DAILY_TIP, null) ?: return null
        return runCatching { gson.fromJson(json, DailyHealthTip::class.java) }.getOrNull()
    }

    private fun loadFamilyMembers(): List<FamilyMember> {
        val json = prefs.getString(KEY_FAMILY_MEMBERS, null) ?: return emptyList()
        val type = object : TypeToken<List<FamilyMember>>() {}.type
        return runCatching { gson.fromJson<List<FamilyMember>>(json, type) }.getOrDefault(emptyList())
    }

    private fun loadHistoryRecords(): List<SavedCheckRecord> {
        val json = prefs.getString(KEY_HISTORY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedCheckRecord>>() {}.type
        return runCatching { gson.fromJson<List<SavedCheckRecord>>(json, type) }.getOrDefault(emptyList())
    }

    private fun loadImportedMedicalRecords(): List<ImportedMedicalRecord> {
        val json = prefs.getString(KEY_IMPORTED_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<ImportedMedicalRecord>>() {}.type
        return runCatching { gson.fromJson<List<ImportedMedicalRecord>>(json, type) }.getOrDefault(emptyList())
    }

    private fun loadCheckupSuggestions(): List<PersonalizedCheckupSuggestion> {
        val json = prefs.getString(KEY_CHECKUP_SUGGESTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<PersonalizedCheckupSuggestion>>() {}.type
        return runCatching { gson.fromJson<List<PersonalizedCheckupSuggestion>>(json, type) }.getOrDefault(emptyList())
    }

    private fun saveSelfProfile() {
        prefs.edit().putString(KEY_SELF_PROFILE, gson.toJson(selfProfile)).apply()
    }

    private fun saveSettings() {
        prefs.edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply()
    }

    private fun saveSelectedPersonId() {
        prefs.edit().putString(KEY_SELECTED_PERSON, selectedPersonId).apply()
    }

    private fun saveDailyHealthTip() {
        if (dailyHealthTip == null) {
            prefs.edit().remove(KEY_DAILY_TIP).apply()
        } else {
            prefs.edit().putString(KEY_DAILY_TIP, gson.toJson(dailyHealthTip)).apply()
        }
    }

    private fun saveFamilyMembers() {
        prefs.edit().putString(KEY_FAMILY_MEMBERS, gson.toJson(_familyMembers)).apply()
    }

    private fun saveHistoryRecords() {
        prefs.edit().putString(KEY_HISTORY_RECORDS, gson.toJson(_historyRecords)).apply()
    }

    private fun saveImportedMedicalRecords() {
        prefs.edit().putString(KEY_IMPORTED_RECORDS, gson.toJson(_importedMedicalRecords)).apply()
    }

    private fun saveCheckupSuggestions() {
        prefs.edit().putString(KEY_CHECKUP_SUGGESTIONS, gson.toJson(_checkupSuggestions)).apply()
    }

    private companion object {
        const val KEY_SELF_PROFILE = "self_profile"
        const val KEY_SETTINGS = "app_settings"
        const val KEY_SELECTED_PERSON = "selected_person_id"
        const val KEY_DAILY_TIP = "daily_health_tip"
        const val KEY_FAMILY_MEMBERS = "family_members"
        const val KEY_HISTORY_RECORDS = "history_records"
        const val KEY_IMPORTED_RECORDS = "imported_medical_records"
        const val KEY_CHECKUP_SUGGESTIONS = "checkup_suggestions"
    }
}
