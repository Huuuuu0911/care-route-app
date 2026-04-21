package com.example.cs501_final_project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CareRouteDao {

    @Query("SELECT * FROM self_profile WHERE id = 1 LIMIT 1")
    fun observeSelfProfile(): Flow<SelfProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSelfProfile(profile: SelfProfileEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM family_members ORDER BY name COLLATE NOCASE ASC")
    fun observeFamilyMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFamilyMember(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFamilyMembers(members: List<FamilyMemberEntity>)

    @Query("DELETE FROM family_members WHERE id = :memberId")
    suspend fun deleteFamilyMember(memberId: String)

    @Query("SELECT * FROM history_records ORDER BY createdAt DESC")
    fun observeHistoryRecords(): Flow<List<SavedCheckRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistoryRecord(record: SavedCheckRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistoryRecords(records: List<SavedCheckRecordEntity>)

    @Query("DELETE FROM history_records WHERE id = :recordId")
    suspend fun deleteHistoryRecord(recordId: String)

    @Query("DELETE FROM history_records WHERE personId = :personId")
    suspend fun deleteHistoryRecordsForPerson(personId: String)

    @Query("DELETE FROM history_records")
    suspend fun clearHistoryRecords()

    @Query("SELECT * FROM imported_medical_records ORDER BY createdAt DESC")
    fun observeImportedMedicalRecords(): Flow<List<ImportedMedicalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImportedMedicalRecord(record: ImportedMedicalRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImportedMedicalRecords(records: List<ImportedMedicalRecordEntity>)

    @Query("DELETE FROM imported_medical_records WHERE id = :recordId")
    suspend fun deleteImportedMedicalRecord(recordId: String)

    @Query("DELETE FROM imported_medical_records WHERE personId = :personId")
    suspend fun deleteImportedMedicalRecordsForPerson(personId: String)

    @Query("DELETE FROM imported_medical_records")
    suspend fun clearImportedMedicalRecords()

    @Query("SELECT * FROM daily_health_tips")
    fun observeDailyHealthTips(): Flow<List<DailyHealthTipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyHealthTip(tip: DailyHealthTipEntity)

    @Query("DELETE FROM daily_health_tips WHERE personId = :personId")
    suspend fun deleteDailyHealthTipForPerson(personId: String)

    @Query("SELECT * FROM checkup_suggestions ORDER BY generatedDate DESC, priority DESC")
    fun observeCheckupSuggestions(): Flow<List<CheckupSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckupSuggestions(suggestions: List<CheckupSuggestionEntity>)

    @Query("DELETE FROM checkup_suggestions WHERE personId = :personId")
    suspend fun deleteCheckupSuggestionsForPerson(personId: String)

    @Transaction
    suspend fun replaceCheckupSuggestionsForPerson(
        personId: String,
        suggestions: List<CheckupSuggestionEntity>
    ) {
        deleteCheckupSuggestionsForPerson(personId)
        if (suggestions.isNotEmpty()) {
            upsertCheckupSuggestions(suggestions)
        }
    }
}