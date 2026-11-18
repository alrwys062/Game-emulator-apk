package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val systemId: String,
    val coreOverride: String,
    val fileName: String,
    val localFilePath: String?,
    val cartridgeMode: String,
    val coverStyleIndex: Int,
    val genreAr: String,
    val genreEn: String,
    val fileSizeBytes: Long,
    val playTimeMinutes: Int,
    val progressPercent: Float,
    val isFavorite: Boolean,
    val lastPlayedAt: Long,
    val createdAt: Long,
    val coverUrl: String? = null
)
