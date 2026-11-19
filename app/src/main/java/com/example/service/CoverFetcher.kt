package com.example.service

import android.content.Context
import android.net.Uri
import com.example.model.RetroSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object CoverFetcher {

    /**
     * Converts github.com/user/repo/blob/branch/file.png -> raw.githubusercontent.com/user/repo/branch/file.png
     */
    fun normalizeCoverUrl(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.contains("github.com/") && trimmed.contains("/blob/")) {
            return trimmed
                .replace("https://github.com/", "https://raw.githubusercontent.com/")
                .replace("http://github.com/", "https://raw.githubusercontent.com/")
                .replace("/blob/", "/")
        }
        return trimmed
    }

    /**
     * Libretro filename sanitization: replaces & * / : ` < > ? \ | with _
     */
    fun sanitizeLibretroName(name: String): String {
        return name.replace(Regex("""[&*/:`<>?\\|]"""), "_").trim()
    }

    fun stripRegionTags(name: String): String {
        return name
            .replace(Regex("""\s*\([^)]*\)"""), "")
            .replace(Regex("""\s*\[[^\]]*]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    fun buildLibretroCandidates(
        title: String,
        fileName: String,
        system: RetroSystem
    ): List<String> {
        val repo = system.libretroRepo
        val rawBaseName = fileName.substringBeforeLast('.').trim()
        val namesToTry = linkedSetOf<String>()

        if (rawBaseName.isNotBlank()) namesToTry.add(sanitizeLibretroName(rawBaseName))
        if (title.isNotBlank()) namesToTry.add(sanitizeLibretroName(title))

        val strippedFile = sanitizeLibretroName(stripRegionTags(rawBaseName))
        val strippedTitle = sanitizeLibretroName(stripRegionTags(title))
        if (strippedFile.isNotBlank()) {
            namesToTry.add(strippedFile)
            namesToTry.add("$strippedFile (USA)")
            namesToTry.add("$strippedFile (USA, Europe)")
            namesToTry.add("$strippedFile (World)")
            namesToTry.add("$strippedFile (Europe)")
            namesToTry.add("$strippedFile (Japan)")
        }
        if (strippedTitle.isNotBlank()) {
            namesToTry.add(strippedTitle)
            namesToTry.add("$strippedTitle (USA)")
            namesToTry.add("$strippedTitle (USA, Europe)")
        }

        val folders = listOf("Named_Boxarts", "Named_Titles", "Named_Snaps")
        val urls = mutableListOf<String>()
        for (folder in folders) {
            for (name in namesToTry) {
                val encoded = URLEncoder.encode(name, "UTF-8").replace("+", "%20")
                urls.add("https://raw.githubusercontent.com/libretro-thumbnails/$repo/master/$folder/$encoded.png")
            }
        }
        return urls.distinct()
    }

    /**
     * Tries candidate URLs in order and caches the first valid PNG/JPEG locally for offline rendering.
     * Returns local file:// URI path or null if none found.
     */
    suspend fun autoFetchAndCacheCover(
        context: Context,
        gameId: Long,
        title: String,
        fileName: String,
        system: RetroSystem
    ): String? = withContext(Dispatchers.IO) {
        val candidates = buildLibretroCandidates(title, fileName, system)
        for (url in candidates) {
            val cached = downloadAndCacheImage(context, gameId, url)
            if (cached != null) return@withContext cached
        }
        null
    }

    suspend fun downloadAndCacheImage(
        context: Context,
        gameId: Long,
        rawUrl: String
    ): String? = withContext(Dispatchers.IO) {
        val urlStr = normalizeCoverUrl(rawUrl)
        runCatching {
            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.connectTimeout = 7_000
            conn.readTimeout = 10_000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "RetroVerse/1.0")
            if (conn.responseCode !in 200..299) {
                conn.disconnect()
                return@runCatching null
            }
            val bytes = conn.inputStream.use { it.readBytes() }
            conn.disconnect()
            if (bytes.size < 512) return@runCatching null
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            val destFile = File(coversDir, "cover_${gameId}_${System.currentTimeMillis()}.png")
            // Clean previous cached cover for this game
            coversDir.listFiles()?.filter { it.name.startsWith("cover_${gameId}_") }?.forEach { it.delete() }
            destFile.writeBytes(bytes)
            destFile.toURI().toString()
        }.getOrNull()
    }

    suspend fun savePickedCoverUri(
        context: Context,
        gameId: Long,
        uri: Uri
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            coversDir.listFiles()?.filter { it.name.startsWith("cover_${gameId}_") }?.forEach { it.delete() }
            val destFile = File(coversDir, "cover_${gameId}_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.toURI().toString()
        }.getOrNull()
    }
}
