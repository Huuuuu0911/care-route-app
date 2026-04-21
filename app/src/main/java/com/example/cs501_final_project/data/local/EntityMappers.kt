package com.example.cs501_final_project.data.local

import com.example.cs501_final_project.data.AppSettings
import com.example.cs501_final_project.data.DailyHealthTip
import com.example.cs501_final_project.data.FamilyMember
import com.example.cs501_final_project.data.ImportedMedicalRecord
import com.example.cs501_final_project.data.PatientProfile
import com.example.cs501_final_project.data.PersonalizedCheckupSuggestion
import com.example.cs501_final_project.data.SavedCheckRecord

fun PatientProfile.toEntity(): SelfProfileEntity {
    return SelfProfileEntity(
        name = name,
        birthDate = birthDate,
        age = age,
        gender = gender,
        phone = phone,
        height = height,
        weight = weight,
        address = address,
        allergies = allergies,
        medications = medications,
        conditions = conditions,
        emergencyContact = emergencyContact
    )
}

fun SelfProfileEntity.toModel(): PatientProfile {
    return PatientProfile(
        name = name,
        birthDate = birthDate,
        age = age,
        gender = gender,
        phone = phone,
        height = height,
        weight = weight,
        address = address,
        allergies = allergies,
        medications = medications,
        conditions = conditions,
        emergencyContact = emergencyContact
    )
}

fun FamilyMember.toEntity(): FamilyMemberEntity {
    return FamilyMemberEntity(
        id = id,
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
}

fun FamilyMemberEntity.toModel(): FamilyMember {
    return FamilyMember(
        id = id,
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
}

fun SavedCheckRecord.toEntity(): SavedCheckRecordEntity {
    return SavedCheckRecordEntity(
        id = id,
        personId = personId,
        personName = personName,
        personGroup = personGroup,
        bodyPart = bodyPart,
        symptomText = symptomText,
        painLevel = painLevel,
        urgency = urgency,
        careLevel = careLevel,
        summary = summary,
        mapQuery = mapQuery,
        createdAt = createdAt
    )
}

fun SavedCheckRecordEntity.toModel(): SavedCheckRecord {
    return SavedCheckRecord(
        id = id,
        personId = personId,
        personName = personName,
        personGroup = personGroup,
        bodyPart = bodyPart,
        symptomText = symptomText,
        painLevel = painLevel,
        urgency = urgency,
        careLevel = careLevel,
        summary = summary,
        mapQuery = mapQuery,
        createdAt = createdAt
    )
}

fun ImportedMedicalRecord.toEntity(): ImportedMedicalRecordEntity {
    return ImportedMedicalRecordEntity(
        id = id,
        personId = personId,
        personName = personName,
        sourceType = sourceType,
        sourceLabel = sourceLabel,
        title = title,
        summary = summary,
        findings = findings,
        recommendedFollowUp = recommendedFollowUp,
        rawText = rawText,
        createdAt = createdAt
    )
}

fun ImportedMedicalRecordEntity.toModel(): ImportedMedicalRecord {
    return ImportedMedicalRecord(
        id = id,
        personId = personId,
        personName = personName,
        sourceType = sourceType,
        sourceLabel = sourceLabel,
        title = title,
        summary = summary,
        findings = findings,
        recommendedFollowUp = recommendedFollowUp,
        rawText = rawText,
        createdAt = createdAt
    )
}

fun AppSettings.toEntity(): AppSettingsEntity {
    return AppSettingsEntity(
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled,
        accentTheme = accentTheme
    )
}

fun AppSettingsEntity.toModel(): AppSettings {
    return AppSettings(
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled,
        accentTheme = accentTheme
    )
}

fun DailyHealthTip.toEntity(): DailyHealthTipEntity {
    return DailyHealthTipEntity(
        personId = personId,
        title = title,
        message = message,
        focusArea = focusArea,
        caution = caution,
        generatedDate = generatedDate,
        source = source
    )
}

fun DailyHealthTipEntity.toModel(): DailyHealthTip {
    return DailyHealthTip(
        title = title,
        message = message,
        focusArea = focusArea,
        caution = caution,
        generatedDate = generatedDate,
        personId = personId,
        source = source
    )
}

fun PersonalizedCheckupSuggestion.toEntity(): CheckupSuggestionEntity {
    return CheckupSuggestionEntity(
        id = id,
        title = title,
        reason = reason,
        timeframe = timeframe,
        priority = priority,
        personId = personId,
        generatedDate = generatedDate
    )
}

fun CheckupSuggestionEntity.toModel(): PersonalizedCheckupSuggestion {
    return PersonalizedCheckupSuggestion(
        id = id,
        title = title,
        reason = reason,
        timeframe = timeframe,
        priority = priority,
        personId = personId,
        generatedDate = generatedDate
    )
}