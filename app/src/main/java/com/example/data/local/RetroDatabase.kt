package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameEntity::class, SaveStateEntity::class, CheatEntity::class],
    version = 3,
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN gameSerial TEXT")
                db.execSQL("ALTER TABLE games ADD COLUMN aiNotes TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cheats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        gameId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        code TEXT NOT NULL,
                        cheatType TEXT NOT NULL,
                        enabled INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(gameId) REFERENCES games(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cheats_gameId ON cheats(gameId)")
            }
        }

        fun getInstance(context: Context): RetroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RetroDatabase::class.java,
                    "retroverse_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
