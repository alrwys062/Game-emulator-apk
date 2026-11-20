package com.example.service

import android.content.Context
import android.webkit.WebResourceResponse
import com.example.model.RetroSystem
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class OfflineCoreBundleState(
    val systemId: String,
    val shortName: String,
    val coreId: String,
    val approxSizeLabel: String,
    val isReady: Boolean,
    val cachedBytes: Long,
    val threadsPrepared: Boolean,
    val fileCount: Int,
    val updatedAt: Long
)

data class OfflinePrepProgress(
    val systemId: String,
    val systemName: String,
    val coreId: String,
    val threadsEnabled: Boolean,
    val percent: Int,
    val statusLabel: String,
    val isBulk: Boolean = false,
    val bulkIndex: Int = 0,
    val bulkTotal: Int = 0
)

data class OfflinePrepRequest(
    val systemId: String,
    val systemName: String,
    val coreId: String,
    val cdnMode: String,
    val customDataPath: String,
    val threadsMode: String,
    val isBulk: Boolean = false,
    val bulkIndex: Int = 0,
    val bulkTotal: Int = 0,
    val requestSeq: Long = System.currentTimeMillis()
)

data class OfflineDiagnosticsReport(
    val swStatus: String = "checking",
    val isolated: Boolean = false,
    val sharedArrayBuffer: Boolean = false,
    val androidCacheBytes: Long = 0L,
    val webStorageUsageBytes: Long = 0L,
    val webStorageQuotaBytes: Long = 0L,
    val webCacheEntries: Int = 0,
    val readySystems: List<String> = emptyList(),
    val androidGamepads: List<String> = emptyList(),
    val webGamepads: List<String> = emptyList(),
    val isOnline: Boolean = true
)

class OfflineCoreManager(private val context: Context) {

    private val cacheRoot: File
        get() = File(context.filesDir, "ejs_cache").apply { mkdirs() }

    private val assetsDir: File
        get() = File(cacheRoot, "files").apply { mkdirs() }

    private val manifestFile: File
        get() = File(cacheRoot, "bundles_manifest.json")

    private val urlIndexFile: File
        get() = File(cacheRoot, "url_index.json")

    companion object {
        val supportedSystemOrder = listOf(
            RetroSystem.NES,
            RetroSystem.SNES,
            RetroSystem.GBA,
            RetroSystem.GBC,
            RetroSystem.GB,
            RetroSystem.NDS,
            RetroSystem.N64,
            RetroSystem.SEGA_MD,
            RetroSystem.SEGA_MS,
            RetroSystem.SEGA_GG,
            RetroSystem.PS1,
            RetroSystem.PSP,
            RetroSystem.PCE,
            RetroSystem.ARCADE
        )

        fun approxSizeForSystem(sys: RetroSystem): String = when (sys) {
            RetroSystem.NES, RetroSystem.GB, RetroSystem.GBC, RetroSystem.SEGA_MS, RetroSystem.SEGA_GG -> "~2.2 MB"
            RetroSystem.SNES, RetroSystem.GBA, RetroSystem.SEGA_MD, RetroSystem.PCE -> "~3.5 MB"
            RetroSystem.NDS -> "~7.5 MB"
            RetroSystem.N64 -> "~14 MB"
            RetroSystem.PS1 -> "~8.5 MB"
            RetroSystem.PSP -> "~28 MB"
            RetroSystem.ARCADE -> "~12 MB"
            else -> "~4.0 MB"
        }

        fun resolveContentType(url: String): String {
            val clean = url.substringBefore('?').substringBefore('#').lowercase()
            return when {
                clean.endsWith(".js") || clean.endsWith(".mjs") -> "application/javascript"
                clean.endsWith(".css") -> "text/css"
                clean.endsWith(".wasm") -> "application/wasm"
                clean.endsWith(".json") -> "application/json"
                clean.endsWith(".html") -> "text/html"
                clean.endsWith(".svg") -> "image/svg+xml"
                clean.endsWith(".png") -> "image/png"
                clean.endsWith(".woff2") -> "font/woff2"
                clean.endsWith(".ttf") -> "font/ttf"
                else -> "application/octet-stream"
            }
        }

        fun isEjsCdnUrl(url: String): Boolean {
            return url.startsWith("https://cdn.emulatorjs.org/") ||
                url.startsWith("https://cdn.jsdelivr.net/gh/EmulatorJS/")
        }

        fun shouldCacheCdnUrl(url: String): Boolean {
            if (!isEjsCdnUrl(url)) return false
            val low = url.lowercase()
            if (low.contains("cores/reports/") || low.endsWith(".map")) return false
            return true
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    @Synchronized
    private fun readManifest(): JSONObject {
        if (!manifestFile.exists()) return JSONObject()
        return runCatching { JSONObject(manifestFile.readText()) }.getOrElse { JSONObject() }
    }

    @Synchronized
    private fun writeManifest(obj: JSONObject) {
        runCatching { manifestFile.writeText(obj.toString(2)) }
    }

    @Synchronized
    private fun readUrlIndex(): JSONObject {
        if (!urlIndexFile.exists()) return JSONObject()
        return runCatching { JSONObject(urlIndexFile.readText()) }.getOrElse { JSONObject() }
    }

    @Synchronized
    private fun writeUrlIndex(obj: JSONObject) {
        runCatching { urlIndexFile.writeText(obj.toString()) }
    }

    fun hasEmbeddedLocalEjs(): Boolean {
        return runCatching {
            context.assets.open("ejs/data/loader.js").use { true }
        }.getOrDefault(false)
    }

    fun getEmbeddedAssetResponse(assetSubPath: String, method: String): WebResourceResponse? {
        val clean = assetSubPath.trimStart('/').substringBefore('?').substringBefore('#')
        val fullAssetPath = "ejs/data/$clean"
        val headers = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Cross-Origin-Opener-Policy" to "same-origin",
            "Cross-Origin-Embedder-Policy" to "credentialless",
            "Cross-Origin-Resource-Policy" to "cross-origin",
            "Cache-Control" to "public, max-age=31536000"
        )
        return try {
            val stream = context.assets.open(fullAssetPath)
            if (method.equals("HEAD", ignoreCase = true)) {
                stream.close()
                WebResourceResponse(resolveContentType(clean), "UTF-8", 200, "OK", headers, ByteArrayInputStream(ByteArray(0)))
            } else {
                WebResourceResponse(resolveContentType(clean), "UTF-8", 200, "OK", headers, stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Serves cached CDN assets (or fetches & persists when online) so EmulatorJS works 100% offline in Android WebView.
     */
    fun interceptOrFetchCdnAsset(url: String, method: String, isOnline: Boolean): WebResourceResponse? {
        if (!isEjsCdnUrl(url)) return null
        if (!method.equals("GET", ignoreCase = true) && !method.equals("HEAD", ignoreCase = true)) return null

        val key = sha256(url)
        val cachedFile = File(assetsDir, "$key.bin")
        val metaFile = File(assetsDir, "$key.meta")
        val headers = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Methods" to "GET, HEAD, OPTIONS",
            "Cross-Origin-Opener-Policy" to "same-origin",
            "Cross-Origin-Embedder-Policy" to "credentialless",
            "Cross-Origin-Resource-Policy" to "cross-origin",
            "Cache-Control" to "public, max-age=31536000"
        )

        if (cachedFile.exists() && cachedFile.length() > 0L) {
            val mime = runCatching { metaFile.takeIf { it.exists() }?.readText()?.trim() }
                .getOrNull()?.takeIf { it.isNotBlank() } ?: resolveContentType(url)
            val stream = if (method.equals("HEAD", ignoreCase = true)) {
                ByteArrayInputStream(ByteArray(0))
            } else {
                cachedFile.inputStream()
            }
            return WebResourceResponse(mime, "UTF-8", 200, "OK", headers, stream)
        }

        if (!isOnline) {
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                504,
                "Offline Cache Miss",
                headers,
                ByteArrayInputStream("Offline: core asset not cached ($url)".toByteArray())
            )
        }

        if (!shouldCacheCdnUrl(url)) {
            return null // Let WebView load optional reports normally when online
        }

        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 45_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "RetroVerse/1.0")
            }
            val code = conn.responseCode
            val contentType = conn.contentType?.substringBefore(';')?.trim()?.takeIf { it.isNotBlank() }
                ?: resolveContentType(url)
            if (code in 200..299) {
                if (method.equals("HEAD", ignoreCase = true)) {
                    conn.disconnect()
                    return WebResourceResponse(contentType, "UTF-8", 200, "OK", headers, ByteArrayInputStream(ByteArray(0)))
                }
                val bytes = conn.inputStream.use { it.readBytes() }
                conn.disconnect()
                if (bytes.isNotEmpty()) {
                    val tmpFile = File(assetsDir, "$key.tmp")
                    tmpFile.writeBytes(bytes)
                    tmpFile.renameTo(cachedFile)
                    metaFile.writeText(contentType)
                    recordUrlKey(url, key, bytes.size.toLong())
                }
                WebResourceResponse(contentType, "UTF-8", 200, "OK", headers, ByteArrayInputStream(bytes))
            } else {
                conn.disconnect()
                WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    code,
                    "HTTP $code",
                    headers,
                    ByteArrayInputStream(ByteArray(0))
                )
            }
        } catch (_: Exception) {
            if (!isOnline) {
                WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    504,
                    "Offline",
                    headers,
                    ByteArrayInputStream(ByteArray(0))
                )
            } else {
                null
            }
        }
    }

    @Synchronized
    private fun recordUrlKey(url: String, key: String, sizeBytes: Long) {
        val idx = readUrlIndex()
        idx.put(url, JSONObject().apply {
            put("key", key)
            put("size", sizeBytes)
        })
        writeUrlIndex(idx)
    }

    fun markCorePrepared(
        systemId: String,
        coreId: String,
        threadsPrepared: Boolean,
        totalBytes: Long,
        fileCount: Int,
        urls: List<String>
    ) {
        val manifest = readManifest()
        val urlKeys = JSONArray()
        urls.forEach { u -> urlKeys.put(sha256(u)) }
        manifest.put(systemId.lowercase(), JSONObject().apply {
            put("systemId", systemId.lowercase())
            put("coreId", coreId)
            put("ready", true)
            put("threadsPrepared", threadsPrepared)
            put("cachedBytes", totalBytes)
            put("fileCount", fileCount)
            put("updatedAt", System.currentTimeMillis())
            put("urlKeys", urlKeys)
        })
        writeManifest(manifest)
    }

    fun isSystemReadyOffline(systemId: String, resolvedCore: String, requiredThreads: Boolean? = null): Boolean {
        if (hasEmbeddedLocalEjs()) return true
        val manifest = readManifest()
        val bySys = manifest.optJSONObject(systemId.lowercase())
        if (bySys != null && bySys.optBoolean("ready", false)) {
            if (requiredThreads == null || bySys.optBoolean("threadsPrepared", false) == requiredThreads) {
                return true
            }
        }
        // Fallback check if another system sharing the same coreId is ready (e.g. fceumm / genesis_plus_gx)
        val keys = manifest.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val entry = manifest.optJSONObject(k) ?: continue
            if (entry.optBoolean("ready", false) &&
                entry.optString("coreId", "").equals(resolvedCore, ignoreCase = true)
            ) {
                if (requiredThreads == null || entry.optBoolean("threadsPrepared", false) == requiredThreads) {
                    return true
                }
            }
        }
        return false
    }

    fun getAllBundleStates(): List<OfflineCoreBundleState> {
        val manifest = readManifest()
        val hasEmbedded = hasEmbeddedLocalEjs()
        return supportedSystemOrder.map { sys ->
            val obj = manifest.optJSONObject(sys.id.lowercase())
            val ready = hasEmbedded || (obj?.optBoolean("ready", false) == true)
            OfflineCoreBundleState(
                systemId = sys.id,
                shortName = sys.shortName,
                coreId = obj?.optString("coreId", sys.ejsCore)?.ifBlank { sys.ejsCore } ?: sys.ejsCore,
                approxSizeLabel = approxSizeForSystem(sys),
                isReady = ready,
                cachedBytes = obj?.optLong("cachedBytes", 0L) ?: 0L,
                threadsPrepared = obj?.optBoolean("threadsPrepared", false) ?: false,
                fileCount = obj?.optInt("fileCount", 0) ?: 0,
                updatedAt = obj?.optLong("updatedAt", 0L) ?: 0L
            )
        }
    }

    fun deleteBundle(systemId: String) {
        val manifest = readManifest()
        val entry = manifest.optJSONObject(systemId.lowercase())
        if (entry != null) {
            val coreToRemove = entry.optString("coreId", "")
            val urlKeys = entry.optJSONArray("urlKeys")
            if (urlKeys != null) {
                // Keep shared loader/emulator files if any other system is still prepared
                var hasOtherReady = false
                val keys = manifest.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (!k.equals(systemId, ignoreCase = true) && manifest.optJSONObject(k)?.optBoolean("ready", false) == true) {
                        hasOtherReady = true
                        break
                    }
                }
                val idx = readUrlIndex()
                val idxKeys = idx.keys().asSequence().toList()
                for (i in 0 until urlKeys.length()) {
                    val hash = urlKeys.optString(i)
                    val matchingUrl = idxKeys.firstOrNull { idx.optJSONObject(it)?.optString("key") == hash }
                    val isCoreSpecific = matchingUrl?.contains("/cores/", ignoreCase = true) == true ||
                        matchingUrl?.contains(coreToRemove, ignoreCase = true) == true
                    if (!hasOtherReady || isCoreSpecific) {
                        runCatching { File(assetsDir, "$hash.bin").delete() }
                        runCatching { File(assetsDir, "$hash.meta").delete() }
                    }
                }
            }
            manifest.remove(systemId.lowercase())
            writeManifest(manifest)
        }
    }

    fun clearAllBundles() {
        runCatching { assetsDir.deleteRecursively() }
        assetsDir.mkdirs()
        runCatching { manifestFile.delete() }
        runCatching { urlIndexFile.delete() }
    }

    fun getTotalCacheSizeBytes(): Long {
        return assetsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun getReadySystemNames(): List<String> {
        return getAllBundleStates().filter { it.isReady }.map { it.shortName }
    }

    /**
     * Shared WebView request interceptor for emulator_host.html, coi-serviceworker.js,
     * embedded ./ejs/data/ bundle, and cached CDN assets (with COOP/COEP headers for SharedArrayBuffer).
     */
    fun interceptRetroLocalOrCdn(url: String, method: String, isOnline: Boolean): WebResourceResponse? {
        val isolationHeaders = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Methods" to "GET, HEAD, OPTIONS",
            "Access-Control-Allow-Headers" to "*",
            "Cross-Origin-Opener-Policy" to "same-origin",
            "Cross-Origin-Embedder-Policy" to "credentialless",
            "Cross-Origin-Resource-Policy" to "cross-origin"
        )

        if (url.startsWith("https://retroverse.local/") && method.equals("OPTIONS", ignoreCase = true)) {
            return WebResourceResponse("text/plain", "UTF-8", 200, "OK", isolationHeaders, ByteArrayInputStream(ByteArray(0)))
        }

        if (url.startsWith("https://retroverse.local/coi-serviceworker.js")) {
            val coiStream = runCatching { context.assets.open("coi-serviceworker.js") }.getOrElse { ByteArrayInputStream(ByteArray(0)) }
            return WebResourceResponse("application/javascript", "UTF-8", 200, "OK", isolationHeaders, coiStream)
        }

        if (url.startsWith("https://retroverse.local/emulator_host.html")) {
            val htmlStream = context.assets.open("emulator_host.html")
            return WebResourceResponse("text/html", "UTF-8", 200, "OK", isolationHeaders, htmlStream)
        }

        if (url.startsWith("https://retroverse.local/ejs/data/")) {
            val subPath = url.removePrefix("https://retroverse.local/ejs/data/")
            return getEmbeddedAssetResponse(subPath, method)
                ?: WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", isolationHeaders, ByteArrayInputStream(ByteArray(0)))
        }

        if (isEjsCdnUrl(url)) {
            return interceptOrFetchCdnAsset(url, method, isOnline)
        }

        return null
    }
}
