package com.example.model

enum class ControllerProfile {
    PSX,   // D-Pad, Cross/Circle/Square/Triangle, L1/L2/R1/R2, L3/R3, Left & Right Analog Sticks, Start/Select
    PSP,   // D-Pad, Cross/Circle/Square/Triangle, L/R, Left Analog Stick, Start/Select
    N64,   // D-Pad, Analog Stick, A/B, C-Up/C-Down/C-Left/C-Right, L/R/Z, Start
    SNES,  // D-Pad, A/B/X/Y, L/R, Start/Select
    GBA,   // D-Pad, A/B, L/R, Start/Select
    SEGA6, // D-Pad, A/B/C/X/Y/Z, Start/Mode
    NES    // D-Pad, A/B, Start/Select
}

data class BiosSlotSpec(
    val key: String,
    val systemId: String,
    val titleAr: String,
    val titleEn: String,
    val expectedFiles: String,
    val isRequired: Boolean,
    val isComingSoon: Boolean = false
)

enum class RetroSystem(
    val id: String,
    val shortName: String,
    val fullNameAr: String,
    val fullNameEn: String,
    val ejsCore: String,
    val availableCores: List<String>,
    val extensions: List<String>,
    val libretroRepo: String,
    val controllerProfile: ControllerProfile,
    val requiresBios: Boolean = false,
    val biosSlotKey: String? = null,
    val cheatFormatLabel: String = "GameShark / Raw",
    val cheatExample: String = "80070000 0063",
    val isComingSoon: Boolean = false
) {
    PS1(
        id = "ps1",
        shortName = "PS1",
        fullNameAr = "بلايستيشن 1",
        fullNameEn = "PlayStation 1",
        ejsCore = "psx",
        availableCores = listOf("psx", "pcsx_rearmed", "mednafen_psx_hw"),
        extensions = listOf("bin", "cue", "chd", "pbp", "img", "iso", "psx"),
        libretroRepo = "Sony_-_PlayStation",
        controllerProfile = ControllerProfile.PSX,
        requiresBios = true,
        biosSlotKey = "ps1",
        cheatFormatLabel = "GameShark / Action Replay",
        cheatExample = "80071234 00FF"
    ),
    PSP(
        id = "psp",
        shortName = "PSP",
        fullNameAr = "بلايستيشن بورتابل",
        fullNameEn = "PlayStation Portable",
        ejsCore = "psp",
        availableCores = listOf("psp", "ppsspp"),
        extensions = listOf("iso", "cso", "pbp"),
        libretroRepo = "Sony_-_PlayStation_Portable",
        controllerProfile = ControllerProfile.PSP,
        requiresBios = false,
        cheatFormatLabel = "CWCheat (cheat.db)",
        cheatExample = "_C1 Max Money\n_L 0x20123456 0x000F423F"
    ),
    N64(
        id = "n64",
        shortName = "N64",
        fullNameAr = "نينتندو 64",
        fullNameEn = "Nintendo 64",
        ejsCore = "n64",
        availableCores = listOf("n64", "parallel_n64", "mupen64plus_next"),
        extensions = listOf("n64", "z64", "v64"),
        libretroRepo = "Nintendo_-_Nintendo_64",
        controllerProfile = ControllerProfile.N64,
        cheatFormatLabel = "GameShark N64",
        cheatExample = "8033B21E 0008"
    ),
    SNES(
        id = "snes",
        shortName = "SNES",
        fullNameAr = "سوبر نينتندو",
        fullNameEn = "Super Nintendo",
        ejsCore = "snes",
        availableCores = listOf("snes", "snes9x", "bsnes"),
        extensions = listOf("sfc", "smc", "fig", "swc"),
        libretroRepo = "Nintendo_-_Super_Nintendo_Entertainment_System",
        controllerProfile = ControllerProfile.SNES,
        cheatFormatLabel = "Game Genie / Pro Action Replay",
        cheatExample = "7E0DBA09"
    ),
    NES(
        id = "nes",
        shortName = "NES",
        fullNameAr = "نينتندو NES",
        fullNameEn = "Nintendo NES",
        ejsCore = "nes",
        availableCores = listOf("nes", "fceumm", "nestopia"),
        extensions = listOf("nes", "fds", "unif"),
        libretroRepo = "Nintendo_-_Nintendo_Entertainment_System",
        controllerProfile = ControllerProfile.NES,
        cheatFormatLabel = "Game Genie (6/8 chars)",
        cheatExample = "SXIOPO"
    ),
    GBA(
        id = "gba",
        shortName = "GBA",
        fullNameAr = "جيم بوي أدفانس",
        fullNameEn = "Game Boy Advance",
        ejsCore = "gba",
        availableCores = listOf("gba", "mgba", "vba_next"),
        extensions = listOf("gba"),
        libretroRepo = "Nintendo_-_Game_Boy_Advance",
        controllerProfile = ControllerProfile.GBA,
        biosSlotKey = "gba",
        cheatFormatLabel = "GameShark v3 / CodeBreaker",
        cheatExample = "82025840 0044"
    ),
    GBC(
        id = "gbc",
        shortName = "GBC",
        fullNameAr = "جيم بوي كولور",
        fullNameEn = "Game Boy Color",
        ejsCore = "gbc",
        availableCores = listOf("gbc", "gambatte"),
        extensions = listOf("gbc"),
        libretroRepo = "Nintendo_-_Game_Boy_Color",
        controllerProfile = ControllerProfile.NES,
        cheatFormatLabel = "Game Genie / GameShark",
        cheatExample = "010FF6C5"
    ),
    GB(
        id = "gb",
        shortName = "GB",
        fullNameAr = "جيم بوي",
        fullNameEn = "Game Boy",
        ejsCore = "gb",
        availableCores = listOf("gb", "gambatte"),
        extensions = listOf("gb"),
        libretroRepo = "Nintendo_-_Game_Boy",
        controllerProfile = ControllerProfile.NES,
        cheatFormatLabel = "Game Genie / GameShark",
        cheatExample = "00C-23E-E69"
    ),
    NDS(
        id = "nds",
        shortName = "NDS",
        fullNameAr = "نينتندو DS",
        fullNameEn = "Nintendo DS",
        ejsCore = "nds",
        availableCores = listOf("nds", "desmume", "melonds"),
        extensions = listOf("nds"),
        libretroRepo = "Nintendo_-_Nintendo_DS",
        controllerProfile = ControllerProfile.SNES,
        biosSlotKey = "nds",
        cheatFormatLabel = "Action Replay DS",
        cheatExample = "02001234 000003E7"
    ),
    SEGA_MD(
        id = "sega",
        shortName = "Genesis",
        fullNameAr = "سيجا جينيسيس",
        fullNameEn = "Sega Genesis / MD",
        ejsCore = "segaMD",
        availableCores = listOf("segaMD", "genesis_plus_gx", "picodrive"),
        extensions = listOf("md", "gen", "smd"),
        libretroRepo = "Sega_-_Mega_Drive_-_Genesis",
        controllerProfile = ControllerProfile.SEGA6,
        cheatFormatLabel = "Game Genie / Pro Action Replay",
        cheatExample = "AJBT-AA2G"
    ),
    SEGA_MS(
        id = "segams",
        shortName = "MasterSys",
        fullNameAr = "سيجا ماستر سيستم",
        fullNameEn = "Sega Master System",
        ejsCore = "segaMS",
        availableCores = listOf("segaMS", "genesis_plus_gx"),
        extensions = listOf("sms"),
        libretroRepo = "Sega_-_Master_System_-_Mark_III",
        controllerProfile = ControllerProfile.NES,
        cheatFormatLabel = "Pro Action Replay",
        cheatExample = "00C000:05"
    ),
    SEGA_GG(
        id = "segagg",
        shortName = "GameGear",
        fullNameAr = "سيجا جيم جير",
        fullNameEn = "Sega Game Gear",
        ejsCore = "segaGG",
        availableCores = listOf("segaGG", "genesis_plus_gx"),
        extensions = listOf("gg"),
        libretroRepo = "Sega_-_Game_Gear",
        controllerProfile = ControllerProfile.NES,
        cheatFormatLabel = "Game Genie",
        cheatExample = "00F-123-E69"
    ),
    SEGA_CD(
        id = "segacd",
        shortName = "SegaCD",
        fullNameAr = "سيجا CD",
        fullNameEn = "Sega CD",
        ejsCore = "segaCD",
        availableCores = listOf("segaCD", "genesis_plus_gx", "picodrive"),
        extensions = listOf("scd"),
        libretroRepo = "Sega_-_Mega-CD_-_Sega_CD",
        controllerProfile = ControllerProfile.SEGA6,
        requiresBios = true,
        biosSlotKey = "segacd",
        cheatFormatLabel = "Pro Action Replay",
        cheatExample = "FF0010:0009"
    ),
    SEGA_32X(
        id = "sega32x",
        shortName = "32X",
        fullNameAr = "سيجا 32X",
        fullNameEn = "Sega 32X",
        ejsCore = "sega32x",
        availableCores = listOf("sega32x", "picodrive"),
        extensions = listOf("32x"),
        libretroRepo = "Sega_-_32X",
        controllerProfile = ControllerProfile.SEGA6,
        cheatFormatLabel = "Pro Action Replay",
        cheatExample = "FF0010:0009"
    ),
    ATARI_2600(
        id = "atari",
        shortName = "Atari2600",
        fullNameAr = "أتاري 2600",
        fullNameEn = "Atari 2600",
        ejsCore = "atari2600",
        availableCores = listOf("atari2600", "stella"),
        extensions = listOf("a26"),
        libretroRepo = "Atari_-_2600",
        controllerProfile = ControllerProfile.NES,
        cheatFormatLabel = "RetroArch CHT",
        cheatExample = "0080:09"
    ),
    ATARI_7800(
        id = "atari7800",
        shortName = "Atari7800",
        fullNameAr = "أتاري 7800",
        fullNameEn = "Atari 7800",
        ejsCore = "atari7800",
        availableCores = listOf("atari7800", "prosystem"),
        extensions = listOf("a78"),
        libretroRepo = "Atari_-_7800",
        controllerProfile = ControllerProfile.NES,
        biosSlotKey = "atari7800",
        cheatFormatLabel = "RetroArch CHT",
        cheatExample = "0080:09"
    ),
    ATARI_LYNX(
        id = "lynx",
        shortName = "Lynx",
        fullNameAr = "أتاري لينكس",
        fullNameEn = "Atari Lynx",
        ejsCore = "lynx",
        availableCores = listOf("lynx", "handy"),
        extensions = listOf("lnx"),
        libretroRepo = "Atari_-_Lynx",
        controllerProfile = ControllerProfile.NES,
        requiresBios = true,
        biosSlotKey = "lynx"
    ),
    ATARI_JAGUAR(
        id = "jaguar",
        shortName = "Jaguar",
        fullNameAr = "أتاري جاغوار",
        fullNameEn = "Atari Jaguar",
        ejsCore = "jaguar",
        availableCores = listOf("jaguar", "virtualjaguar"),
        extensions = listOf("j64", "jag"),
        libretroRepo = "Atari_-_Jaguar",
        controllerProfile = ControllerProfile.SEGA6
    ),
    PCE(
        id = "pce",
        shortName = "PCE",
        fullNameAr = "PC Engine",
        fullNameEn = "PC Engine / TG-16",
        ejsCore = "pce",
        availableCores = listOf("pce", "mednafen_pce"),
        extensions = listOf("pce", "sgx"),
        libretroRepo = "NEC_-_PC_Engine_-_TurboGrafx_16",
        controllerProfile = ControllerProfile.NES,
        biosSlotKey = "pce"
    ),
    NEOGEO(
        id = "neogeo",
        shortName = "NeoGeo",
        fullNameAr = "نيو جيو",
        fullNameEn = "Neo Geo",
        ejsCore = "neogeo",
        availableCores = listOf("neogeo", "fbneo"),
        extensions = listOf("neo"),
        libretroRepo = "SNK_-_Neo_Geo",
        controllerProfile = ControllerProfile.SEGA6,
        requiresBios = true,
        biosSlotKey = "neogeo"
    ),
    WONDERSWAN(
        id = "ws",
        shortName = "WonderSwan",
        fullNameAr = "وندرسوان",
        fullNameEn = "WonderSwan / Color",
        ejsCore = "ws",
        availableCores = listOf("ws", "mednafen_wswan"),
        extensions = listOf("ws", "wsc"),
        libretroRepo = "Bandai_-_WonderSwan_Color",
        controllerProfile = ControllerProfile.NES
    ),
    THREEDO(
        id = "3do",
        shortName = "3DO",
        fullNameAr = "3DO",
        fullNameEn = "3DO Interactive",
        ejsCore = "3do",
        availableCores = listOf("3do", "opera"),
        extensions = listOf("3do"),
        libretroRepo = "The_3DO_Company_-_3DO",
        controllerProfile = ControllerProfile.SNES,
        requiresBios = true,
        biosSlotKey = "3do"
    ),
    ARCADE(
        id = "arcade",
        shortName = "Arcade",
        fullNameAr = "أركيد MAME/FBNeo",
        fullNameEn = "Arcade (MAME / FBNeo)",
        ejsCore = "mame2003",
        availableCores = listOf("mame2003", "fbneo", "mame2003_plus"),
        extensions = listOf("zip", "7z"),
        libretroRepo = "MAME",
        controllerProfile = ControllerProfile.SEGA6,
        cheatFormatLabel = "MAME / FBNeo Cheat",
        cheatExample = "001000:FF"
    ),
    PS2(
        id = "ps2",
        shortName = "PS2",
        fullNameAr = "بلايستيشن 2",
        fullNameEn = "PlayStation 2",
        ejsCore = "",
        availableCores = emptyList(),
        extensions = emptyList(),
        libretroRepo = "Sony_-_PlayStation_2",
        controllerProfile = ControllerProfile.PSX,
        requiresBios = true,
        biosSlotKey = "ps2",
        cheatFormatLabel = "PS2 RAW / CodeBreaker",
        cheatExample = "20123456 00000063",
        isComingSoon = true
    );

    companion object {
        val playableEntries: List<RetroSystem> get() = entries.filter { !it.isComingSoon }

        val biosSpecs = listOf(
            BiosSlotSpec("ps1", "ps1", "بيوس PlayStation 1", "PlayStation 1 BIOS", "scph5501.bin / scph5500.bin / scph1001.bin", isRequired = true),
            BiosSlotSpec("gba", "gba", "بيوس Game Boy Advance", "GBA BIOS (اختياري)", "gba_bios.bin", isRequired = false),
            BiosSlotSpec("nds", "nds", "بيوس Nintendo DS", "NDS BIOS / Firmware", "bios7.bin / bios9.bin / firmware.bin", isRequired = false),
            BiosSlotSpec("segacd", "segacd", "بيوس Sega CD", "Sega CD BIOS", "bios_CD_U.bin / bios_CD_E.bin", isRequired = true),
            BiosSlotSpec("atari7800", "atari7800", "بيوس Atari 7800", "Atari 7800 BIOS", "7800 BIOS (U).rom", isRequired = false),
            BiosSlotSpec("pce", "pce", "بيوس PC Engine CD", "PC Engine CD SysCard", "syscard3.pce", isRequired = false),
            BiosSlotSpec("neogeo", "neogeo", "بيوس Neo Geo", "Neo Geo BIOS Archive", "neogeo.zip", isRequired = true),
            BiosSlotSpec("3do", "3do", "بيوس 3DO", "3DO BIOS", "panafz10.bin / goldstar.bin", isRequired = true),
            BiosSlotSpec("ps2", "ps2", "بيوس PlayStation 2 (للمستقبل)", "PS2 BIOS (Future Ready)", "scph39001.bin / SCPH-70012.bin", isRequired = false, isComingSoon = true)
        )

        fun fromId(id: String): RetroSystem = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NES

        fun detectFromFileName(fileName: String): Pair<RetroSystem, String> {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "nes", "fds", "unif" -> NES to "nes"
                "sfc", "smc", "fig", "swc" -> SNES to "snes"
                "gb" -> GB to "gb"
                "gbc" -> GBC to "gbc"
                "gba" -> GBA to "gba"
                "nds" -> NDS to "nds"
                "n64", "z64", "v64" -> N64 to "n64"
                "sms" -> SEGA_MS to "segaMS"
                "md", "gen", "smd" -> SEGA_MD to "segaMD"
                "gg" -> SEGA_GG to "segaGG"
                "scd" -> SEGA_CD to "segaCD"
                "32x" -> SEGA_32X to "sega32x"
                "a26" -> ATARI_2600 to "atari2600"
                "a78" -> ATARI_7800 to "atari7800"
                "lnx" -> ATARI_LYNX to "lynx"
                "j64", "jag" -> ATARI_JAGUAR to "jaguar"
                "bin", "cue", "chd", "img", "mdf", "psx" -> PS1 to "psx"
                "pbp" -> PS1 to "psx"
                "iso", "cso" -> PSP to "psp"
                "zip", "7z" -> ARCADE to "mame2003"
                "pce", "sgx" -> PCE to "pce"
                "neo" -> NEOGEO to "neogeo"
                "ws", "wsc" -> WONDERSWAN to "ws"
                "3do" -> THREEDO to "3do"
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
