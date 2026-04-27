package com.lockwitness.app.data.incident

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SecurityIncident::class],
    version = 1,
    exportSchema = false
)
abstract class LockWitnessDatabase : RoomDatabase() {
    abstract fun securityIncidentDao(): SecurityIncidentDao

    companion object {
        @Volatile
        private var instance: LockWitnessDatabase? = null

        fun getInstance(context: Context): LockWitnessDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LockWitnessDatabase::class.java,
                    "lockwitness.db"
                ).build().also { instance = it }
            }
    }
}
