package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.local.CheatEntity
import com.example.data.local.GameEntity
import com.example.data.local.RetroDao
import com.example.data.local.SaveStateEntity
import com.example.data.preferences.BiosFileEntry
import com.example.model.RetroSystem
import com.example.service.CheatParser
import com.example.service.CoverFetcher
import com.example.service.RomBundler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class GameRepository(
    private val context: Context,
    private val dao: RetroDao
) {
    val allGames: Flow<List<GameEntity>> = dao.observeAllGames()
    val totalSaveCount: Flow<Int> = dao.observeTotalSaveCount()

    fun observeSaveStates(gameId: Long): Flow<List<SaveStateEntity>> = dao.observeSaveStates(gameId)
    fun observeCheats(gameId: Long): Flow<List<CheatEntity>> = dao.observeCheats(gameId)

    suspend fun getCheatsForGame(gameId: Long): List<CheatEntity> = dao.getCheatsForGame(gameId)

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

    suspend fun importRomUris(uris: List<Uri>): List<GameEntity> = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext emptyList()
        val romsDir = File(context.filesDir, "roms").apply { mkdirs() }
        val metas = uris.map { it to RomBundler.queryFileMeta(context, it) }

        val hasCue = metas.any { it.second.first.endsWith(".cue", ignoreCase = true) }
        val hasBin = metas.any { it.second.first.endsWith(".bin", ignoreCase = true) }

        if (uris.size >= 2 && hasCue && hasBin) {
            val (zipFile, zipName) = RomBundler.bundleCueBinToZip(context, uris, romsDir)
            val cleanTitle = zipName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
                .ifEmpty { "PS1 Disc Game" }
            val game = GameEntity(
                title = cleanTitle,
                systemId = RetroSystem.PS1.id,
                coreOverride = "psx",
                fileName = zipName,
                localFilePath = zipFile.absolutePath,
                cartridgeMode = "racer",
                coverStyleIndex = kotlin.math.abs(cleanTitle.hashCode()) % 8,
                genreAr = "قرص مدمج (BIN+CUE)",
                genreEn = "Disc Bundle",
                fileSizeBytes = zipFile.length(),
                playTimeMinutes = 0,
                progressPercent = 0.05f,
                isFavorite = false,
                lastPlayedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertGame(game)
            return@withContext listOf(game.copy(id = id))
        }

        val importedList = mutableListOf<GameEntity>()
        for ((uri, meta) in metas) {
            val (fileName, reportedSize) = meta
            val safeName = "${System.currentTimeMillis()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
            val destFile = File(romsDir, safeName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output, 32768)
                }
            }
            val actualSize = if (destFile.exists() && destFile.length() > 0) destFile.length() else reportedSize
            val (detectedSystem, detectedCore) = RetroSystem.detectFromFileName(fileName)
            val cleanTitle = fileName.substringBeforeLast('.')
                .replace('_', ' ')
                .replace('-', ' ')
                .replace(Regex("\\s+"), " ")
                .trim()
                .ifEmpty { "Retro ROM" }

            val serial = if (detectedSystem == RetroSystem.PSP) {
                CheatParser.extractPspGameSerial(destFile, fileName)
            } else null

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
                createdAt = System.currentTimeMillis(),
                gameSerial = serial
            )
            val id = dao.insertGame(game)
            importedList.add(game.copy(id = id))
        }
        importedList
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
        game.localFilePath?.let { path -> runCatching { File(path).delete() } }
        game.coverUrl?.takeIf { it.startsWith("file:") }?.let { uri ->
            runCatching { File(Uri.parse(uri).path ?: "").delete() }
        }
        dao.deleteGameById(game.id)
    }

    suspend fun updateTitle(gameId: Long, newTitle: String) {
        val clean = newTitle.trim()
        if (clean.isNotEmpty()) dao.updateTitle(gameId, clean)
    }

    suspend fun updateSystemAndCore(gameId: Long, systemId: String, coreOverride: String) {
        dao.updateSystemAndCore(gameId, systemId, coreOverride)
    }

    suspend fun updateAiNotes(gameId: Long, notes: String?) {
        dao.updateAiNotes(gameId, notes)
    }

    suspend fun updateCoverFromUrlOrCache(gameId: Long, rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank()) {
            dao.updateCoverUrl(gameId, null)
            return true
        }
        val normalized = CoverFetcher.normalizeCoverUrl(rawUrl)
        val cached = CoverFetcher.downloadAndCacheImage(context, gameId, normalized)
        dao.updateCoverUrl(gameId, cached ?: normalized)
        return cached != null || normalized.startsWith("http")
    }

    suspend fun updateCoverFromDeviceUri(gameId: Long, uri: Uri): Boolean {
        val cached = CoverFetcher.savePickedCoverUri(context, gameId, uri) ?: return false
        dao.updateCoverUrl(gameId, cached)
        return true
    }

    suspend fun autoFetchCoverForGame(game: GameEntity, customTitleOverride: String? = null): Boolean {
        val sys = RetroSystem.fromId(game.systemId)
        val titleToUse = customTitleOverride?.takeIf { it.isNotBlank() } ?: game.title
        val fileToUse = customTitleOverride?.takeIf { it.isNotBlank() } ?: game.fileName
        val cached = CoverFetcher.autoFetchAndCacheCover(
            context = context,
            gameId = game.id,
            title = titleToUse,
            fileName = fileToUse,
            system = sys
        ) ?: return false
        dao.updateCoverUrl(game.id, cached)
        return true
    }

    // Cheats
    suspend fun addCheat(gameId: Long, name: String, code: String, cheatType: String) {
        dao.insertCheat(
            CheatEntity(
                gameId = gameId,
                name = name.trim().ifEmpty { "Cheat" },
                code = code.trim(),
                cheatType = cheatType,
                enabled = true
            )
        )
    }

    suspend fun toggleCheat(cheatId: Long, enabled: Boolean) {
        dao.setCheatEnabled(cheatId, enabled)
    }

    suspend fun deleteCheat(cheatId: Long) {
        dao.deleteCheatById(cheatId)
    }

    suspend fun importCheatFileForGame(game: GameEntity, uri: Uri): Int = withContext(Dispatchers.IO) {
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return@withContext 0
        val sys = RetroSystem.fromId(game.systemId)
        val parsed = if (content.contains("_S ") || content.contains("_C0 ") || content.contains("_C1 ")) {
            CheatParser.parsePpssppCheatDb(content, game.gameSerial, game.title)
        } else {
            CheatParser.parseRetroArchCht(content, sys.cheatFormatLabel)
        }
        if (parsed.isEmpty()) return@withContext 0
        val entities = parsed.map {
            CheatEntity(
                gameId = game.id,
                name = it.name,
                code = it.code,
                cheatType = it.cheatType,
                enabled = it.enabled
            )
        }
        dao.insertCheats(entities)
        entities.size
    }

    // BIOS
    suspend fun importBiosFromUri(slotKey: String, uri: Uri): BiosFileEntry = withContext(Dispatchers.IO) {
        val (fileName, _) = RomBundler.queryFileMeta(context, uri)
        val biosDir = File(context.filesDir, "bios").apply { mkdirs() }
        val safeName = "${slotKey.lowercase()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val destFile = File(biosDir, safeName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output, 32768)
            }
        }
        val md5 = RomBundler.calculateMd5(destFile)
        BiosFileEntry(
            slotKey = slotKey,
            path = destFile.absolutePath,
            name = fileName,
            sizeBytes = destFile.length(),
            md5 = md5
        )
    }

    suspend fun downloadBiosFromUrl(slotKey: String, url: String): BiosFileEntry = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.instanceFollowRedirects = true
        val fileName = Uri.parse(url).lastPathSegment?.takeIf { it.isNotBlank() } ?: "${slotKey}_bios.bin"
        val biosDir = File(context.filesDir, "bios").apply { mkdirs() }
        val safeName = "${slotKey.lowercase()}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val destFile = File(biosDir, safeName)
        conn.inputStream.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output, 32768)
            }
        }
        val md5 = RomBundler.calculateMd5(destFile)
        BiosFileEntry(
            slotKey = slotKey,
            path = destFile.absolutePath,
            name = fileName,
            sizeBytes = destFile.length(),
            md5 = md5
        )
    }

    fun calculateStorageUsageBytes(): Long {
        val romsSize = File(context.filesDir, "roms").walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val biosSize = File(context.filesDir, "bios").walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val coversSize = File(context.filesDir, "covers").walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return romsSize + biosSize + coversSize
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
