package com.example.cs501_final_project.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_final_project.data.local.CareRouteDatabase
import com.example.cs501_final_project.data.local.toEntity
import com.example.cs501_final_project.data.local.toModel
import com.example.cs501_final_project.data.preferences.AppPreferencesRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class CareRouteViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CareRouteDatabase.getInstance(application).careRouteDao()
    private val preferencesRepository = AppPreferencesRepository(application)
    private val legacyPrefs = application.getSharedPreferences(LEGACY_PREFS_NAME, Application.MODE_PRIVATE)
    private val gson = Gson()

    private var dailyTipsByPerson: Map<String, DailyHealthTip> = emptyMap()

    var selfProfile by mutableStateOf(PatientProfile(name = "You"))
        private set

    var settings by mutableStateOf(AppSettings())
        private set

    var selectedPersonId by mutableStateOf("self")
        private set

    var dailyHealthTip by mutableStateOf<DailyHealthTip?>(null)
        private set

    private val _familyMembers = mutableStateListOf<FamilyMember>()
    val familyMembers: List<FamilyMember> get() = _familyMembers

    private val _historyRecords = mutableStateListOf<SavedCheckRecord>()
    val historyRecords: List<SavedCheckRecord> get() = _historyRecords

    private val _importedMedicalRecords = mutableStateListOf<ImportedMedicalRecord>()
    val importedMedicalRecords: List<ImportedMedicalRecord> get() = _importedMedicalRecords

    private val _checkupSuggestions = mutableStateListOf<PersonalizedCheckupSuggestion>()
    val checkupSuggestions: List<PersonalizedCheckupSuggestion> get() = _checkupSuggestions

    init {
        observeLocalData()
        viewModelScope.launch(Dispatchers.IO) {
            migrateLegacyDataIfNeeded()
        }
    }

    fun updateSelfProfile(profile: PatientProfile) {
        selfProfile = profile
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertSelfProfile(profile.toEntity())
        }
    }

    fun updateSettings(updated: AppSettings) {
        settings = updated
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertSettings(updated.toEntity())
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        updateSettings(settings.copy(notificationsEnabled = enabled))
    }

    fun toggleDarkMode(enabled: Boolean) {
        updateSettings(settings.copy(darkModeEnabled = enabled))
    }

    fun updateAccentTheme(accentThemeOption: AccentThemeOption) {
        updateSettings(settings.copy(accentTheme = accentThemeOption))
    }

    fun selectPerson(personId: String) {
        selectedPersonId = personId
        dailyHealthTip = dailyTipsByPerson[personId]
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.setSelectedPersonId(personId)
        }
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
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertFamilyMember(member.toEntity())
        }
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
        upsertFamilyMember(
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
    }

    fun deleteFamilyMember(memberId: String) {
        _familyMembers.removeAll { it.id == memberId }
        _historyRecords.removeAll { it.personId == memberId }
        _importedMedicalRecords.removeAll { it.personId == memberId }
        _checkupSuggestions.removeAll { it.personId == memberId }
        if (dailyHealthTip?.personId == memberId) {
            dailyHealthTip = null
        }
        dailyTipsByPerson = dailyTipsByPerson - memberId

        if (selectedPersonId == memberId) {
            selectPerson("self")
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteFamilyMember(memberId)
            dao.deleteHistoryRecordsForPerson(memberId)
            dao.deleteImportedMedicalRecordsForPerson(memberId)
            dao.deleteCheckupSuggestionsForPerson(memberId)
            dao.deleteDailyHealthTipForPerson(memberId)
        }
    }

    fun addHistoryRecord(record: SavedCheckRecord) {
        _historyRecords.removeAll { it.id == record.id }
        _historyRecords.add(0, record)
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertHistoryRecord(record.toEntity())
        }
    }

    fun deleteHistoryRecord(recordId: String) {
        _historyRecords.removeAll { it.id == recordId }
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteHistoryRecord(recordId)
        }
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
        val record = ImportedMedicalRecord(
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
        _importedMedicalRecords.removeAll { it.id == record.id }
        _importedMedicalRecords.add(0, record)
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertImportedMedicalRecord(record.toEntity())
        }
    }

    fun deleteImportedMedicalRecord(recordId: String) {
        _importedMedicalRecords.removeAll { it.id == recordId }
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteImportedMedicalRecord(recordId)
        }
    }

    fun clearHistoryArchive() {
        _historyRecords.clear()
        _importedMedicalRecords.clear()
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearHistoryRecords()
            dao.clearImportedMedicalRecords()
        }
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
        dailyTipsByPerson = dailyTipsByPerson + (tip.personId to tip)
        if (tip.personId == selectedPersonId) {
            dailyHealthTip = tip
        }
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertDailyHealthTip(tip.toEntity())
        }
    }

    fun shouldRefreshDailyTip(personId: String): Boolean {
        val cachedTip = dailyTipsByPerson[personId]
        val today = todayKey()
        return cachedTip == null || cachedTip.generatedDate != today
    }

    fun updateCheckupSuggestions(personId: String, suggestions: List<PersonalizedCheckupSuggestion>) {
        _checkupSuggestions.removeAll { it.personId == personId }
        _checkupSuggestions.addAll(0, suggestions)
        viewModelScope.launch(Dispatchers.IO) {
            dao.replaceCheckupSuggestionsForPerson(
                personId = personId,
                suggestions = suggestions.map { it.toEntity() }
            )
        }
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

    private fun observeLocalData() {
        viewModelScope.launch {
            preferencesRepository.selectedPersonIdFlow.collect { personId ->
                selectedPersonId = personId.ifBlank { "self" }
                dailyHealthTip = dailyTipsByPerson[selectedPersonId]
            }
        }

        viewModelScope.launch {
            dao.observeSelfProfile().collect { entity ->
                selfProfile = entity?.toModel() ?: PatientProfile(name = "You")
            }
        }

        viewModelScope.launch {
            dao.observeSettings().collect { entity ->
                settings = entity?.toModel() ?: AppSettings()
            }
        }

        viewModelScope.launch {
            dao.observeFamilyMembers().collect { members ->
                _familyMembers.replaceAllWith(members.map { it.toModel() })
            }
        }

        viewModelScope.launch {
            dao.observeHistoryRecords().collect { records ->
                _historyRecords.replaceAllWith(records.map { it.toModel() })
            }
        }

        viewModelScope.launch {
            dao.observeImportedMedicalRecords().collect { records ->
                _importedMedicalRecords.replaceAllWith(records.map { it.toModel() })
            }
        }

        viewModelScope.launch {
            dao.observeDailyHealthTips().collect { tips ->
                dailyTipsByPerson = tips
                    .map { it.toModel() }
                    .associateBy { it.personId }
                dailyHealthTip = dailyTipsByPerson[selectedPersonId]
            }
        }

        viewModelScope.launch {
            dao.observeCheckupSuggestions().collect { suggestions ->
                _checkupSuggestions.replaceAllWith(
                    suggestions.map { it.toModel() }
                )
            }
        }
    }

    private suspend fun migrateLegacyDataIfNeeded() {
        if (preferencesRepository.isLegacyMigrationCompleted()) return

        val hasLegacyData = legacyPrefs.contains(KEY_SELF_PROFILE) ||
                legacyPrefs.contains(KEY_SETTINGS) ||
                legacyPrefs.contains(KEY_SELECTED_PERSON) ||
                legacyPrefs.contains(KEY_DAILY_TIP) ||
                legacyPrefs.contains(KEY_FAMILY_MEMBERS) ||
                legacyPrefs.contains(KEY_HISTORY_RECORDS) ||
                legacyPrefs.contains(KEY_IMPORTED_RECORDS) ||
                legacyPrefs.contains(KEY_CHECKUP_SUGGESTIONS)

        if (hasLegacyData) {
            dao.upsertSelfProfile(loadLegacySelfProfile().toEntity())
            dao.upsertSettings(loadLegacySettings().toEntity())

            val familyMembers = loadLegacyFamilyMembers()
            if (familyMembers.isNotEmpty()) {
                dao.upsertFamilyMembers(familyMembers.map { it.toEntity() })
            }

            val historyRecords = loadLegacyHistoryRecords()
            if (historyRecords.isNotEmpty()) {
                dao.upsertHistoryRecords(historyRecords.map { it.toEntity() })
            }

            val importedRecords = loadLegacyImportedMedicalRecords()
            if (importedRecords.isNotEmpty()) {
                dao.upsertImportedMedicalRecords(importedRecords.map { it.toEntity() })
            }

            loadLegacyDailyHealthTip()?.let { dao.upsertDailyHealthTip(it.toEntity()) }

            val suggestions = loadLegacyCheckupSuggestions()
            suggestions.groupBy { it.personId }.forEach { (personId, personSuggestions) ->
                dao.replaceCheckupSuggestionsForPerson(
                    personId = personId,
                    suggestions = personSuggestions.map { it.toEntity() }
                )
            }

            preferencesRepository.setSelectedPersonId(loadLegacySelectedPersonId())
        }

        preferencesRepository.markLegacyMigrationCompleted()
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

    private fun loadLegacySelfProfile(): PatientProfile {
        val json = legacyPrefs.getString(KEY_SELF_PROFILE, null) ?: return PatientProfile(name = "You")
        return runCatching { gson.fromJson(json, PatientProfile::class.java) }
            .getOrDefault(PatientProfile(name = "You"))
    }

    private fun loadLegacySettings(): AppSettings {
        val json = legacyPrefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return runCatching { gson.fromJson(json, AppSettings::class.java) }
            .getOrDefault(AppSettings())
    }

    private fun loadLegacySelectedPersonId(): String {
        return legacyPrefs.getString(KEY_SELECTED_PERSON, "self") ?: "self"
    }

    private fun loadLegacyDailyHealthTip(): DailyHealthTip? {
        val json = legacyPrefs.getString(KEY_DAILY_TIP, null) ?: return null
        return runCatching { gson.fromJson(json, DailyHealthTip::class.java) }.getOrNull()
    }

    private fun loadLegacyFamilyMembers(): List<FamilyMember> {
        val json = legacyPrefs.getString(KEY_FAMILY_MEMBERS, null) ?: return emptyList()
        val type = object : TypeToken<List<FamilyMember>>() {}.type
        return runCatching { gson.fromJson<List<FamilyMember>>(json, type) }.getOrDefault(emptyList())
    }

    private fun loadLegacyHistoryRecords(): List<SavedCheckRecord> {
        val json = legacyPrefs.getString(KEY_HISTORY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedCheckRecord>>() {}.type
        return runCatching { gson.fromJson<List<SavedCheckRecord>>(json, type) }.getOrDefault(emptyList())
    }

    private fun loadLegacyImportedMedicalRecords(): List<ImportedMedicalRecord> {
        val json = legacyPrefs.getString(KEY_IMPORTED_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<ImportedMedicalRecord>>() {}.type
        return runCatching { gson.fromJson<List<ImportedMedicalRecord>>(json, type) }.getOrDefault(emptyList())
    }

    private fun loadLegacyCheckupSuggestions(): List<PersonalizedCheckupSuggestion> {
        val json = legacyPrefs.getString(KEY_CHECKUP_SUGGESTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<PersonalizedCheckupSuggestion>>() {}.type
        return runCatching { gson.fromJson<List<PersonalizedCheckupSuggestion>>(json, type) }
            .getOrDefault(emptyList())
    }

    private fun <T> SnapshotStateList<T>.replaceAllWith(items: List<T>) {
        clear()
        addAll(items)
    }

    private companion object {
        const val LEGACY_PREFS_NAME = "care_route_data"
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