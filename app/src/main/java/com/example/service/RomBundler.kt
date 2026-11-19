package com.example.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object RomBundler {

    fun queryFileMeta(context: Context, uri: Uri): Pair<String, Long> {
        var fileName = "retro_file.bin"
        var fileSize = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) fileName = cursor.getString(nameIdx) ?: fileName
                if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
            }
        }
        return fileName to fileSize
    }

    /**
     * Bundles a .cue + .bin (or multiple disc tracks) into a single .zip file so EmulatorJS
     * receives all referenced files together cleanly.
     */
    suspend fun bundleCueBinToZip(
        context: Context,
        uris: List<Uri>,
        destDir: File
    ): Pair<File, String> = withContext(Dispatchers.IO) {
        destDir.mkdirs()
        val metas = uris.map { it to queryFileMeta(context, it).first }
        val cueEntry = metas.firstOrNull { it.second.endsWith(".cue", ignoreCase = true) }
        val baseTitle = (cueEntry?.second ?: metas.first().second).substringBeforeLast('.')
        val safeZipName = "${System.currentTimeMillis()}_${baseTitle.replace(Regex("[^a-zA-Z0-9._-]"), "_")}.zip"
        val zipFile = File(destDir, safeZipName)

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            metas.forEach { (uri, name) ->
                zos.putNextEntry(ZipEntry(name))
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(zos, 32768)
                }
                zos.closeEntry()
            }
        }
        zipFile to "$baseTitle.zip"
    }

    fun calculateMd5(file: File): String {
        return runCatching {
            val md = MessageDigest.getInstance("MD5")
            val buf = ByteArray(16384)
            file.inputStream().use { input ->
                var read: Int
                while (input.read(buf).also { read = it } != -1) {
                    md.update(buf, 0, read)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        }.getOrDefault("")
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> String.format("%.2f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576L -> String.format("%.1f MB", bytes / 1_048_576.0)
            bytes >= 1024L -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }
}
