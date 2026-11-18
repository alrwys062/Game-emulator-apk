package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameEntity::class, SaveStateEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RetroDatabase : RoomDatabase() {
    abstract fun retroDao(): RetroDao

    companion object {
        @Volatile
        private var INSTANCE: RetroDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN coverUrl TEXT")
            }
        }

        fun getInstance(context: Context): RetroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RetroDatabase::class.java,
                    "retroverse_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
