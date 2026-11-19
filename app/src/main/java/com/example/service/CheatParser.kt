package com.example.service

import java.io.File
import java.io.RandomAccessFile

data class ParsedCheatItem(
    val gameIdSerial: String,
    val gameTitle: String,
    val name: String,
    val code: String,
    val cheatType: String,
    val enabled: Boolean
)

object CheatParser {

    /**
     * Parses PPSSPP cheat.db format:
     * _S ULUS-10041
     * _G Grand Theft Auto: Liberty City Stories
     * _C0 Infinite Health
     * _L 0x20123456 0x00000001
     */
    fun parsePpssppCheatDb(
        content: String,
        targetSerial: String? = null,
        targetGameTitle: String? = null
    ): List<ParsedCheatItem> {
        val allEntries = mutableListOf<ParsedCheatItem>()
        var currentSerial = ""
        var currentGameTitle = ""
        var currentCheatName: String? = null
        var currentEnabled = false
        val codeLines = mutableListOf<String>()

        fun flushCheat() {
            val name = currentCheatName
            if (!name.isNullOrBlank() && codeLines.isNotEmpty()) {
                allEntries.add(
                    ParsedCheatItem(
                        gameIdSerial = currentSerial,
                        gameTitle = currentGameTitle,
                        name = name,
                        code = codeLines.joinToString("\n"),
                        cheatType = "CWCheat",
                        enabled = currentEnabled
                    )
                )
            }
            currentCheatName = null
            codeLines.clear()
        }

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach
            when {
                line.startsWith("_S ") || line.startsWith("_S\t") -> {
                    flushCheat()
                    currentSerial = line.substring(2).trim().uppercase()
                }
                line.startsWith("_G ") || line.startsWith("_G\t") -> {
                    currentGameTitle = line.substring(2).trim()
                }
                line.startsWith("_C1 ") || line.startsWith("_C1\t") -> {
                    flushCheat()
                    currentCheatName = line.substring(3).trim()
                    currentEnabled = true
                }
                line.startsWith("_C0 ") || line.startsWith("_C0\t") -> {
                    flushCheat()
                    currentCheatName = line.substring(3).trim()
                    currentEnabled = false
                }
                line.startsWith("_C ") -> {
                    flushCheat()
                    currentCheatName = line.substring(2).trim()
                    currentEnabled = false
                }
                line.startsWith("_L ") || line.startsWith("_L\t") -> {
                    codeLines.add(line)
                }
            }
        }
        flushCheat()

        if (allEntries.isEmpty()) return emptyList()

        // Match by serial first if provided
        val cleanTargetSerial = targetSerial?.trim()?.uppercase()?.replace("-", "")
        if (!cleanTargetSerial.isNullOrBlank()) {
            val bySerial = allEntries.filter {
                it.gameIdSerial.replace("-", "").equals(cleanTargetSerial, ignoreCase = true)
            }
            if (bySerial.isNotEmpty()) return bySerial
        }

        // Match by normalized title words
        if (!targetGameTitle.isNullOrBlank()) {
            val tokens = normalizeTitle(targetGameTitle).split(" ").filter { it.length >= 3 }
            if (tokens.isNotEmpty()) {
                val byTitle = allEntries.filter { entry ->
                    val normEntry = normalizeTitle(entry.gameTitle)
                    tokens.all { normEntry.contains(it) } ||
                        (tokens.size >= 2 && tokens.count { normEntry.contains(it) } >= (tokens.size + 1) / 2)
                }
                if (byTitle.isNotEmpty()) return byTitle
            }
        }

        // Fallback: return first 150 entries if user imported a single-game db or wants to browse
        return allEntries.take(150)
    }

    /**
     * Parses RetroArch .cht format:
     * cheats = 2
     * cheat0_desc = "Infinite Lives"
     * cheat0_code = "SXIOPO"
     * cheat0_enable = false
     */
    fun parseRetroArchCht(content: String, defaultType: String = "RetroArch"): List<ParsedCheatItem> {
        val descs = mutableMapOf<Int, String>()
        val codes = mutableMapOf<Int, String>()
        val enabled = mutableMapOf<Int, Boolean>()

        val keyRegex = Regex("""^cheat(\d+)_(desc|code|enable)\s*=\s*(.+)$""")
        content.lineSequence().forEach { raw ->
            val line = raw.trim()
            val match = keyRegex.matchEntire(line) ?: return@forEach
            val idx = match.groupValues[1].toIntOrNull() ?: return@forEach
            val field = match.groupValues[2]
            val value = match.groupValues[3].trim().trim('"')
            when (field) {
                "desc" -> descs[idx] = value
                "code" -> codes[idx] = value
                "enable" -> enabled[idx] = value.equals("true", ignoreCase = true) || value == "1"
            }
        }

        if (codes.isNotEmpty()) {
            return codes.keys.sorted().mapNotNull { idx ->
                val code = codes[idx]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val desc = descs[idx]?.takeIf { it.isNotBlank() } ?: "Cheat #${idx + 1}"
                ParsedCheatItem(
                    gameIdSerial = "",
                    gameTitle = "",
                    name = desc,
                    code = code,
                    cheatType = defaultType,
                    enabled = enabled[idx] ?: false
                )
            }
        }

        // Fallback: plain text lines "Name: CODE" or raw codes line by line
        val out = mutableListOf<ParsedCheatItem>()
        content.lineSequence().forEachIndexed { i, raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) return@forEachIndexed
            val parts = line.split(":", "|", limit = 2)
            if (parts.size == 2 && parts[0].length < 40) {
                out.add(
                    ParsedCheatItem("", "", parts[0].trim(), parts[1].trim(), defaultType, true)
                )
            } else {
                out.add(
                    ParsedCheatItem("", "", "Code #${i + 1}", line, defaultType, true)
                )
            }
        }
        return out
    }

    /**
     * Attempts to extract a PSP Game ID (e.g. ULUS-10041, NPJH-50001) from filename or ISO header.
     */
    fun extractPspGameSerial(file: File?, fileName: String): String? {
        val regex = Regex("""([UNEP][LC][AEJKU][BCDMSX]-?\d{5})""", RegexOption.IGNORE_CASE)
        regex.find(fileName)?.let {
            val raw = it.groupValues[1].uppercase().replace("-", "")
            return "${raw.take(4)}-${raw.drop(4)}"
        }
        if (file == null || !file.exists() || file.length() < 65536) return null
        return runCatching {
            RandomAccessFile(file, "r").use { raf ->
                // Scan first 1.5MB in chunks for DISC_ID / PARAM.SFO serial signature
                val maxScan = minOf(file.length(), 1_572_864L).toInt()
                val buf = ByteArray(minOf(65536, maxScan))
                var pos = 0L
                while (pos < maxScan) {
                    raf.seek(pos)
                    val read = raf.read(buf)
                    if (read <= 0) break
                    val text = String(buf, 0, read, Charsets.ISO_8859_1)
                    regex.find(text)?.let { m ->
                        val raw = m.groupValues[1].uppercase().replace("-", "")
                        return "${raw.take(4)}-${raw.drop(4)}"
                    }
                    pos += read - 16
                }
            }
            null
        }.getOrNull()
    }

    private fun normalizeTitle(input: String): String {
        return input
            .replace(Regex("""\([^)]*\)"""), " ")
            .replace(Regex("""\[[^\]]*]"""), " ")
            .replace(Regex("""[^a-zA-Z0-9\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .lowercase()
    }
}
