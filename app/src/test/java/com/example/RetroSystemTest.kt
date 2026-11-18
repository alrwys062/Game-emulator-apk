package com.example

import com.example.model.RetroSystem
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
        assertEquals(RetroSystem.SEGA to "segaMD", RetroSystem.detectFromFileName("sonic.md"))
        assertEquals(RetroSystem.SEGA to "segaMS", RetroSystem.detectFromFileName("alex.sms"))
        assertEquals(RetroSystem.SEGA to "segaGG", RetroSystem.detectFromFileName("shinobi.gg"))
        assertEquals(RetroSystem.ATARI to "atari2600", RetroSystem.detectFromFileName("pitfall.a26"))
        assertEquals(RetroSystem.ATARI to "atari7800", RetroSystem.detectFromFileName("asteroids.a78"))
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
    }
}
