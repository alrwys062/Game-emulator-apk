package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class, SaveStateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RetroDatabase : RoomDatabase() {
    abstract fun retroDao(): RetroDao

    companion object {
        @Volatile
        private var INSTANCE: RetroDatabase? = null

        fun getInstance(context: Context): RetroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RetroDatabase::class.java,
                    "retroverse_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
