package com.example

import com.example.model.RetroSystem
import com.example.service.CheatParser
import com.example.service.CoverFetcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetroSystemTest {
    @Test
    fun detectsAllSupportedRomExtensions() {
        assertEquals(RetroSystem.NES to "nes", RetroSystem.detectFromFileName("mario.nes"))
        assertEquals(RetroSystem.SNES to "snes", RetroSystem.detectFromFileName("zelda.sfc"))
        assertEquals(RetroSystem.SNES to "snes", RetroSystem.detectFromFileName("chrono.smc"))
        assertEquals(RetroSystem.GB to "gb", RetroSystem.detectFromFileName("tetris.gb"))
        assertEquals(RetroSystem.GBC to "gbc", RetroSystem.detectFromFileName("link.gbc"))
        assertEquals(RetroSystem.GBA to "gba", RetroSystem.detectFromFileName("metroid.gba"))
        assertEquals(RetroSystem.NDS to "nds", RetroSystem.detectFromFileName("kart.nds"))
        assertEquals(RetroSystem.N64 to "n64", RetroSystem.detectFromFileName("oot.n64"))
        assertEquals(RetroSystem.N64 to "n64", RetroSystem.detectFromFileName("sm64.z64"))
        assertEquals(RetroSystem.SEGA_MD to "segaMD", RetroSystem.detectFromFileName("sonic.md"))
        assertEquals(RetroSystem.SEGA_MS to "segaMS", RetroSystem.detectFromFileName("alex.sms"))
        assertEquals(RetroSystem.SEGA_GG to "segaGG", RetroSystem.detectFromFileName("shinobi.gg"))
        assertEquals(RetroSystem.ATARI_2600 to "atari2600", RetroSystem.detectFromFileName("pitfall.a26"))
        assertEquals(RetroSystem.ATARI_7800 to "atari7800", RetroSystem.detectFromFileName("asteroids.a78"))
        assertEquals(RetroSystem.PS1 to "psx", RetroSystem.detectFromFileName("crash.bin"))
        assertEquals(RetroSystem.PSP to "psp", RetroSystem.detectFromFileName("ridge.iso"))
        assertEquals(RetroSystem.PSP to "psp", RetroSystem.detectFromFileName("lumines.cso"))
        assertEquals(RetroSystem.ARCADE to "mame2003", RetroSystem.detectFromFileName("mslug.zip"))
        assertEquals(RetroSystem.PCE to "pce", RetroSystem.detectFromFileName("bonk.pce"))
        assertEquals(RetroSystem.NEOGEO to "neogeo", RetroSystem.detectFromFileName("kof.neo"))
    }

    @Test
    fun ps2IsMarkedComingSoon() {
        assertTrue(RetroSystem.PS2.isComingSoon)
        assertTrue(RetroSystem.playableEntries.none { it == RetroSystem.PS2 })
    }

    @Test
    fun convertsGithubBlobUrlToRawAndBuildsLibretroCandidates() {
        val blobUrl = "https://github.com/libretro-thumbnails/Nintendo_-_Game_Boy_Advance/blob/master/Named_Boxarts/Super_Mario.png"
        val expected = "https://raw.githubusercontent.com/libretro-thumbnails/Nintendo_-_Game_Boy_Advance/master/Named_Boxarts/Super_Mario.png"
        assertEquals(expected, CoverFetcher.normalizeCoverUrl(blobUrl))

        val candidates = CoverFetcher.buildLibretroCandidates(
            title = "Super Mario Advance 2",
            fileName = "Super Mario Advance 2 (USA, Australia).gba",
            system = RetroSystem.GBA
        )
        assertTrue(candidates.any { it.contains("Named_Boxarts/Super%20Mario%20Advance%202%20%28USA%2C%20Australia%29.png") })
        assertTrue(candidates.any { it.contains("Named_Titles/") })
        assertTrue(candidates.any { it.contains("Named_Snaps/") })
    }

    @Test
    fun parsesPpssppCheatDbAndRetroArchCht() {
        val db = """
            _S ULUS-10041
            _G Grand Theft Auto: Liberty City Stories
            _C1 Max Health
            _L 0x20123456 0x00000064
            _C0 Unlimited Money
            _L 0x20654321 0x000F423F
            _S ULES-00502
            _G Other Game
            _C1 Other Cheat
            _L 0x20000000 0x00000001
        """.trimIndent()

        val pspCheats = CheatParser.parsePpssppCheatDb(db, targetSerial = "ULUS-10041")
        assertEquals(2, pspCheats.size)
        assertTrue(pspCheats[0].enabled)
        assertEquals("Max Health", pspCheats[0].name)
        assertEquals("_L 0x20123456 0x00000064", pspCheats[0].code)

        val cht = """
            cheats = 1
            cheat0_desc = "Infinite Lives"
            cheat0_code = "SXIOPO"
            cheat0_enable = true
        """.trimIndent()
        val parsedCht = CheatParser.parseRetroArchCht(cht)
        assertEquals(1, parsedCht.size)
        assertEquals("Infinite Lives", parsedCht[0].name)
        assertEquals("SXIOPO", parsedCht[0].code)
        assertTrue(parsedCht[0].enabled)
    }
}
