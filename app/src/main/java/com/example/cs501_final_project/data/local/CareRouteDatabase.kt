package com.example.cs501_final_project.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SelfProfileEntity::class,
        FamilyMemberEntity::class,
        SavedCheckRecordEntity::class,
        ImportedMedicalRecordEntity::class,
        AppSettingsEntity::class,
        DailyHealthTipEntity::class,
        CheckupSuggestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(CareRouteConverters::class)
abstract class CareRouteDatabase : RoomDatabase() {

    abstract fun careRouteDao(): CareRouteDao

    companion object {
        @Volatile
        private var instance: CareRouteDatabase? = null

        fun getInstance(context: Context): CareRouteDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CareRouteDatabase::class.java,
                    "care_route_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}