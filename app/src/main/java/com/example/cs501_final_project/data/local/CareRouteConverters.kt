package com.example.cs501_final_project.data.local

import androidx.room.TypeConverter
import com.example.cs501_final_project.data.AccentThemeOption
import com.example.cs501_final_project.data.MedicalRecordSourceType
import org.json.JSONArray

class CareRouteConverters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return JSONArray(value ?: emptyList<String>()).toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val jsonArray = JSONArray(value)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                add(jsonArray.optString(index))
            }
        }.filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromMedicalRecordSourceType(value: MedicalRecordSourceType?): String {
        return value?.name ?: MedicalRecordSourceType.MANUAL_ENTRY.name
    }

    @TypeConverter
    fun toMedicalRecordSourceType(value: String?): MedicalRecordSourceType {
        return runCatching {
            MedicalRecordSourceType.valueOf(value.orEmpty())
        }.getOrDefault(MedicalRecordSourceType.MANUAL_ENTRY)
    }

    @TypeConverter
    fun fromAccentThemeOption(value: AccentThemeOption?): String {
        return value?.name ?: AccentThemeOption.BLUE.name
    }

    @TypeConverter
    fun toAccentThemeOption(value: String?): AccentThemeOption {
        return runCatching {
            AccentThemeOption.valueOf(value.orEmpty())
        }.getOrDefault(AccentThemeOption.BLUE)
    }
}