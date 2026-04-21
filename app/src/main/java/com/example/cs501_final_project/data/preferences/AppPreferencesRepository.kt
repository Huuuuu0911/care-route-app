package com.example.cs501_final_project.data.preferences

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.careRoutePreferencesDataStore by preferencesDataStore(name = "care_route_preferences")

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val selectedPersonId = stringPreferencesKey("selected_person_id")
        val legacyMigrationCompleted = booleanPreferencesKey("legacy_migration_completed")
    }

    val selectedPersonIdFlow: Flow<String> = context.careRoutePreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[Keys.selectedPersonId] ?: "self" }

    suspend fun setSelectedPersonId(personId: String) {
        context.careRoutePreferencesDataStore.edit { preferences ->
            preferences[Keys.selectedPersonId] = personId
        }
    }

    suspend fun isLegacyMigrationCompleted(): Boolean {
        return context.careRoutePreferencesDataStore.data.first()[Keys.legacyMigrationCompleted] ?: false
    }

    suspend fun markLegacyMigrationCompleted() {
        context.careRoutePreferencesDataStore.edit { preferences ->
            preferences[Keys.legacyMigrationCompleted] = true
        }
    }
}