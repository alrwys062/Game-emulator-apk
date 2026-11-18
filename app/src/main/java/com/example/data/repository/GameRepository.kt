package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.example.data.local.GameEntity
import com.example.data.local.RetroDao
import com.example.data.local.SaveStateEntity
import com.example.model.RetroSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class GameRepository(
    private val context: Context,
    private val dao: RetroDao
) {
    val allGames: Flow<List<GameEntity>> = dao.observeAllGames()
    val totalSaveCount: Flow<Int> = dao.observeTotalSaveCount()

    fun observeSaveStates(gameId: Long): Flow<List<SaveStateEntity>> = dao.observeSaveStates(gameId)

    suspend fun ensureSeededHomebrew() {
        if (dao.getGamesCount() > 0) return
        val now = System.currentTimeMillis()
        val starterHomebrew = listOf(
            GameEntity(
                title = "Crash Racer",
                systemId = RetroSystem.PS1.id,
                coreOverride = "psx",
                fileName = "crash_racer_hb.bin",
                localFilePath = null,
                cartridgeMode = "racer",
                coverStyleIndex = 0,
                genreAr = "سباق",
                genreEn = "Racing",
                fileSizeBytes = 14_200_000L,
                playTimeMinutes = 134,
                progressPercent = 0.72f,
                isFavorite = true,
                lastPlayedAt = now - 60_000L,
                createdAt = now - 500_000L
            ),
            GameEntity(
                title = "Sky Pilot",
                systemId = RetroSystem.PSP.id,
                coreOverride = "psp",
                fileName = "sky_pilot_hb.iso",
                localFilePath = null,
                cartridgeMode = "pilot",
                coverStyleIndex = 1,
                genreAr = "طيران",
                genreEn = "Flight",
                fileSizeBytes = 32_500_000L,
                playTimeMinutes = 38,
                progressPercent = 0.45f,
                isFavorite = false,
                lastPlayedAt = now - 1_800_000L,
                createdAt = now - 600_000L
            ),
            GameEntity(
                title = "Brick Jump",
                systemId = RetroSystem.NES.id,
                coreOverride = "nes",
                fileName = "brick_jump_hb.nes",
                localFilePath = null,
                cartridgeMode = "platform",
                coverStyleIndex = 2,
                genreAr = "منصات",
                genreEn = "Platform",
                fileSizeBytes = 512_000L,
                playTimeMinutes = 72,
                progressPercent = 0.60f,
                isFavorite = true,
                lastPlayedAt = now - 7_200_000L,
                createdAt = now - 700_000L
            ),
            GameEntity(
                title = "Neon Ring",
                systemId = RetroSystem.GBA.id,
                coreOverride = "gba",
                fileName = "neon_ring_hb.gba",
                localFilePath = null,
                cartridgeMode = "ring",
                coverStyleIndex = 3,
                genreAr = "أركيد",
                genreEn = "Arcade",
                fileSizeBytes = 4_096_000L,
                playTimeMinutes = 65,
                progressPercent = 0.35f,
                isFavorite = false,
                lastPlayedAt = now - 14_400_000L,
                createdAt = now - 800_000L
            ),
            GameEntity(
                title = "Star Quest",
                systemId = RetroSystem.SNES.id,
                coreOverride = "snes",
                fileName = "star_quest_hb.sfc",
                localFilePath = null,
                cartridgeMode = "space",
                coverStyleIndex = 4,
                genreAr = "مغامرة",
                genreEn = "Adventure",
                fileSizeBytes = 2_048_000L,
                playTimeMinutes = 540,
                progressPercent = 0.85f,
                isFavorite = true,
                lastPlayedAt = now - 86_400_000L,
                createdAt = now - 900_000L
            ),
            GameEntity(
                title = "Kart Dash",
                systemId = RetroSystem.N64.id,
                coreOverride = "n64",
                fileName = "kart_dash_hb.n64",
                localFilePath = null,
                cartridgeMode = "kart",
                coverStyleIndex = 5,
                genreAr = "سباق",
                genreEn = "Racing",
                fileSizeBytes = 16_777_216L,
                playTimeMinutes = 300,
                progressPercent = 0.55f,
                isFavorite = false,
                lastPlayedAt = now - 172_800_000L,
                createdAt = now - 1_000_000L
            )
        )
        dao.insertGames(starterHomebrew)
    }

    suspend fun importRomFromUri(uri: Uri): GameEntity = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var fileName = "imported_game.nes"
        var fileSize = 0L
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) fileName = cursor.getString(nameIdx) ?: fileName
                if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
            }
        }

        val romsDir = File(context.filesDir, "roms").apply { mkdirs() }
        val safeName = "${System.currentTimeMillis()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val destFile = File(romsDir, safeName)
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        val actualSize = if (destFile.exists() && destFile.length() > 0) destFile.length() else fileSize
        val (detectedSystem, detectedCore) = RetroSystem.detectFromFileName(fileName)
        val cleanTitle = fileName.substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .ifEmpty { "Retro ROM" }

        val game = GameEntity(
            title = cleanTitle,
            systemId = detectedSystem.id,
            coreOverride = detectedCore,
            fileName = fileName,
            localFilePath = destFile.absolutePath,
            cartridgeMode = "space",
            coverStyleIndex = kotlin.math.abs(cleanTitle.hashCode()) % 8,
            genreAr = "ROM مستورد",
            genreEn = "Imported ROM",
            fileSizeBytes = actualSize,
            playTimeMinutes = 0,
            progressPercent = 0.05f,
            isFavorite = false,
            lastPlayedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insertGame(game)
        game.copy(id = id)
    }

    suspend fun importDownloadedBytes(fileName: String, bytes: ByteArray): GameEntity = withContext(Dispatchers.IO) {
        val romsDir = File(context.filesDir, "roms").apply { mkdirs() }
        val safeName = "${System.currentTimeMillis()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val destFile = File(romsDir, safeName)
        destFile.writeBytes(bytes)
        val (detectedSystem, detectedCore) = RetroSystem.detectFromFileName(fileName)
        val cleanTitle = fileName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
            .ifEmpty { "Homebrew ROM" }
        val game = GameEntity(
            title = cleanTitle,
            systemId = detectedSystem.id,
            coreOverride = detectedCore,
            fileName = fileName,
            localFilePath = destFile.absolutePath,
            cartridgeMode = "space",
            coverStyleIndex = kotlin.math.abs(cleanTitle.hashCode()) % 8,
            genreAr = "من المتجر",
            genreEn = "Store Homebrew",
            fileSizeBytes = bytes.size.toLong(),
            playTimeMinutes = 0,
            progressPercent = 0.05f,
            isFavorite = false,
            lastPlayedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insertGame(game)
        game.copy(id = id)
    }

    suspend fun toggleFavorite(game: GameEntity) {
        dao.setFavorite(game.id, !game.isFavorite)
    }

    suspend fun deleteGame(game: GameEntity) = withContext(Dispatchers.IO) {
        game.localFilePath?.let { path ->
            runCatching { File(path).delete() }
        }
        dao.deleteGameById(game.id)
    }

    suspend fun updateCoverUrl(gameId: Long, coverUrl: String?) {
        dao.updateCoverUrl(gameId, coverUrl?.trim()?.takeIf { it.isNotEmpty() })
    }

    suspend fun importBiosFromUri(systemTag: String, uri: Uri): Pair<String, String> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var fileName = "${systemTag}_bios.bin"
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIdx >= 0) {
                fileName = cursor.getString(nameIdx) ?: fileName
            }
        }
        val biosDir = File(context.filesDir, "bios").apply { mkdirs() }
        val safeName = "${systemTag.lowercase()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val destFile = File(biosDir, safeName)
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath to fileName
    }

    suspend fun downloadBiosFromUrl(systemTag: String, url: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.instanceFollowRedirects = true
        val fileName = Uri.parse(url).lastPathSegment?.takeIf { it.isNotBlank() } ?: "${systemTag}_bios.bin"
        val biosDir = File(context.filesDir, "bios").apply { mkdirs() }
        val safeName = "${systemTag.lowercase()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val destFile = File(biosDir, safeName)
        conn.inputStream.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath to fileName
    }

    suspend fun clearTempCache() = withContext(Dispatchers.IO) {
        runCatching { context.cacheDir.deleteRecursively() }
    }

    suspend fun recordPlaySession(gameId: Long, addedMinutes: Int, newProgress: Float) {
        dao.recordPlaySession(
            id = gameId,
            timestamp = System.currentTimeMillis(),
            addedMinutes = addedMinutes.coerceAtLeast(1),
            progress = newProgress.coerceIn(0.05f, 1.0f)
        )
    }

    suspend fun saveStateToSlot(
        game: GameEntity,
        slotIndex: Int,
        stateDataJson: String,
        screenshotBase64: String?
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getSaveStateBySlot(game.id, slotIndex)
        var screenshotPath: String? = existing?.screenshotPath
        if (!screenshotBase64.isNullOrBlank()) {
            val cleanB64 = screenshotBase64.substringAfter("base64,", screenshotBase64)
            runCatching {
                val bytes = Base64.decode(cleanB64, Base64.DEFAULT)
                val dir = File(context.filesDir, "screenshots").apply { mkdirs() }
                val file = File(dir, "slot_${game.id}_$slotIndex.png")
                file.writeBytes(bytes)
                screenshotPath = file.absolutePath
            }
        }
        dao.upsertSaveState(
            SaveStateEntity(
                id = existing?.id ?: 0L,
                gameId = game.id,
                slotIndex = slotIndex,
                savedAt = System.currentTimeMillis(),
                playTimeMinutesAtSave = game.playTimeMinutes,
                screenshotPath = screenshotPath,
                stateDataJson = stateDataJson
            )
        )
    }

    suspend fun saveGalleryScreenshot(gameId: Long, screenshotBase64: String): String? = withContext(Dispatchers.IO) {
        val cleanB64 = screenshotBase64.substringAfter("base64,", screenshotBase64)
        runCatching {
            val bytes = Base64.decode(cleanB64, Base64.DEFAULT)
            val dir = File(context.filesDir, "captures").apply { mkdirs() }
            val file = File(dir, "snap_${gameId}_${System.currentTimeMillis()}.png")
            file.writeBytes(bytes)
            file.absolutePath
        }.getOrNull()
    }
}
