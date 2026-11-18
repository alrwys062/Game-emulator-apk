package com.example.model

enum class RetroSystem(
    val id: String,
    val shortName: String,
    val fullNameAr: String,
    val fullNameEn: String,
    val ejsCore: String,
    val extensions: List<String>,
    val isComingSoon: Boolean = false
) {
    PS1("ps1", "PS1", "بلايستيشن 1", "PlayStation 1", "psx", listOf("bin", "cue", "chd", "pbp", "psx")),
    PSP("psp", "PSP", "بلايستيشن بورتابل", "PlayStation Portable", "psp", listOf("iso", "cso")),
    N64("n64", "N64", "نينتندو 64", "Nintendo 64", "n64", listOf("n64", "z64", "v64")),
    SNES("snes", "SNES", "سوبر نينتندو", "Super Nintendo", "snes", listOf("sfc", "smc")),
    NES("nes", "NES", "نينتندو الكلاسيكي", "Nintendo NES", "nes", listOf("nes")),
    GBA("gba", "GBA", "جيم بوي أدفانس", "Game Boy Advance", "gba", listOf("gba")),
    GBC("gbc", "GBC", "جيم بوي كولور", "Game Boy Color", "gbc", listOf("gbc")),
    GB("gb", "GB", "جيم بوي", "Game Boy", "gb", listOf("gb")),
    NDS("nds", "NDS", "نينتندو DS", "Nintendo DS", "nds", listOf("nds")),
    SEGA("sega", "Sega", "سيجا", "Sega MS/Genesis/GG", "segaMD", listOf("md", "gen", "sms", "gg")),
    ATARI("atari", "Atari", "أتاري 2600/7800", "Atari 2600/7800", "atari2600", listOf("a26", "a78")),
    ARCADE("arcade", "Arcade", "أركيد MAME", "Arcade (MAME)", "mame2003", listOf("zip")),
    PCE("pce", "PCE", "PC Engine", "PC Engine", "pce", listOf("pce")),
    NEOGEO("neogeo", "NeoGeo", "نيو جيو", "Neo Geo", "neogeo", listOf("neo")),
    PS2("ps2", "PS2", "بلايستيشن 2", "PlayStation 2", "", emptyList(), isComingSoon = true);

    companion object {
        fun fromId(id: String): RetroSystem = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NES

        fun detectFromFileName(fileName: String): Pair<RetroSystem, String> {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "nes" -> NES to "nes"
                "sfc", "smc" -> SNES to "snes"
                "gb" -> GB to "gb"
                "gbc" -> GBC to "gbc"
                "gba" -> GBA to "gba"
                "nds" -> NDS to "nds"
                "n64", "z64", "v64" -> N64 to "n64"
                "sms" -> SEGA to "segaMS"
                "md", "gen" -> SEGA to "segaMD"
                "gg" -> SEGA to "segaGG"
                "a26" -> ATARI to "atari2600"
                "a78" -> ATARI to "atari7800"
                "bin", "cue", "chd", "pbp", "img", "mdf" -> PS1 to "psx"
                "iso", "cso" -> PSP to "psp"
                "zip", "7z" -> ARCADE to "mame2003"
                "pce" -> PCE to "pce"
                "neo" -> NEOGEO to "neogeo"
                else -> NES to "nes"
            }
        }
    }
}

data class LegalSource(
    val id: String,
    val title: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val url: String,
    val badgeColorIndex: Int
)

object StoreCatalog {
    val legalSources = listOf(
        LegalSource(
            id = "archive",
            title = "Internet Archive",
            subtitleAr = "Homebrew والمجال العام",
            subtitleEn = "Homebrew & Public Domain",
            url = "https://archive.org/details/homebrew_roms",
            badgeColorIndex = 0
        ),
        LegalSource(
            id = "itch",
            title = "itch.io",
            subtitleAr = "ألعاب ريترو مجانية",
            subtitleEn = "Free Indie Retro ROMs",
            url = "https://itch.io/games/free/tag-retro",
            badgeColorIndex = 3
        ),
        LegalSource(
            id = "hh",
            title = "Homebrew Hub",
            subtitleAr = "ألعاب هواة لـ Game Boy وغيره",
            subtitleEn = "Game Boy & Retro Homebrew",
            url = "https://hh.gbdev.io/",
            badgeColorIndex = 5
        ),
        LegalSource(
            id = "pdroms",
            title = "PDRoms",
            subtitleAr = "ألعاب مجانية بإذن المطورين",
            subtitleEn = "Developer-Authorized Freeware",
            url = "https://pdroms.de/",
            badgeColorIndex = 2
        ),
        LegalSource(
            id = "libretro",
            title = "Libretro",
            subtitleAr = "ألعاب ومحتوى حر",
            subtitleEn = "Free Content & Cores",
            url = "https://www.libretro.com/",
            badgeColorIndex = 1
        )
    )

    val helpfulResources = listOf(
        LegalSource(
            id = "ejs_docs",
            title = "EmulatorJS",
            subtitleAr = "توثيق المحرك والأنظمة المدعومة",
            subtitleEn = "Engine Docs & Core Guide",
            url = "https://emulatorjs.org/",
            badgeColorIndex = 6
        ),
        LegalSource(
            id = "libretro_docs",
            title = "Libretro Docs",
            subtitleAr = "شرح الأنظمة وملفات BIOS",
            subtitleEn = "System Specs & Core Docs",
            url = "https://docs.libretro.com/",
            badgeColorIndex = 4
        ),
        LegalSource(
            id = "libretro_thumbs",
            title = "Libretro Thumbnails",
            subtitleAr = "مكتبة أغلفة الألعاب المفتوحة",
            subtitleEn = "Open Boxart Archive",
            url = "https://thumbnails.libretro.com/",
            badgeColorIndex = 0
        )
    )
}
