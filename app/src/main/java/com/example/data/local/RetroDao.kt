package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RetroDao {
    @Query("SELECT * FROM games ORDER BY lastPlayedAt DESC, createdAt DESC")
    fun observeAllGames(): Flow<List<GameEntity>>

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGamesCount(): Int

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Long): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("UPDATE games SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE games SET coverUrl = :coverUrl WHERE id = :id")
    suspend fun updateCoverUrl(id: Long, coverUrl: String?)

    @Query("UPDATE games SET lastPlayedAt = :timestamp, playTimeMinutes = playTimeMinutes + :addedMinutes, progressPercent = :progress WHERE id = :id")
    suspend fun recordPlaySession(id: Long, timestamp: Long, addedMinutes: Int, progress: Float)

    @Query("SELECT * FROM save_states WHERE gameId = :gameId ORDER BY slotIndex ASC")
    fun observeSaveStates(gameId: Long): Flow<List<SaveStateEntity>>

    @Query("SELECT COUNT(*) FROM save_states")
    fun observeTotalSaveCount(): Flow<Int>

    @Query("SELECT * FROM save_states WHERE gameId = :gameId AND slotIndex = :slotIndex LIMIT 1")
    suspend fun getSaveStateBySlot(gameId: Long, slotIndex: Int): SaveStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSaveState(saveState: SaveStateEntity)
}
