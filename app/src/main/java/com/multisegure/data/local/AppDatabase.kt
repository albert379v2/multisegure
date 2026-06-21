package com.multisegure.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.multisegure.data.model.BrowserProfile

@Database(entities = [BrowserProfile::class], version = 2, exportSchema = false)  // ← version 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multisegure_database"
                )
                .fallbackToDestructiveMigration()  // ← AGREGADO: borra y recrea si cambia schema
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
