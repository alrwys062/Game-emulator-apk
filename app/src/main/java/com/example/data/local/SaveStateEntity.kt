package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "save_states",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class SaveStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val slotIndex: Int, // 1..5
    val savedAt: Long,
    val playTimeMinutesAtSave: Int,
    val screenshotPath: String?,
    val stateDataJson: String
)
