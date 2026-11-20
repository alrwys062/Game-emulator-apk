package com.example

import android.content.Context
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.SettingsRepository
import com.example.service.OfflineCoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineAndGamepadTest {

    @Test
    fun offlineCoreManager_urlFilteringAndIsolationHeadersWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = OfflineCoreManager(context)
        manager.clearAllBundles()

        assertTrue(OfflineCoreManager.isEjsCdnUrl("https://cdn.emulatorjs.org/stable/data/loader.js"))
        assertTrue(OfflineCoreManager.isEjsCdnUrl("https://cdn.jsdelivr.net/gh/EmulatorJS/EmulatorJS@main/data/cores/gba.data"))
        assertFalse(OfflineCoreManager.isEjsCdnUrl("https://example.com/game.zip"))

        assertTrue(OfflineCoreManager.shouldCacheCdnUrl("https://cdn.emulatorjs.org/stable/data/cores/ppsspp.data"))
        assertFalse(OfflineCoreManager.shouldCacheCdnUrl("https://cdn.emulatorjs.org/stable/data/cores/reports/ppsspp.json"))
        assertFalse(OfflineCoreManager.shouldCacheCdnUrl("https://cdn.emulatorjs.org/stable/data/emulator.min.js.map"))

        val hostResp = manager.interceptRetroLocalOrCdn(
            "https://retroverse.local/emulator_host.html",
            "GET",
            isOnline = false
        )
        assertNotNull(hostResp)
        assertEquals(200, hostResp!!.statusCode)
        assertEquals("same-origin", hostResp.responseHeaders["Cross-Origin-Opener-Policy"])
        assertEquals("credentialless", hostResp.responseHeaders["Cross-Origin-Embedder-Policy"])
        assertEquals("cross-origin", hostResp.responseHeaders["Cross-Origin-Resource-Policy"])

        val swResp = manager.interceptRetroLocalOrCdn(
            "https://retroverse.local/coi-serviceworker.js",
            "GET",
            isOnline = false
        )
        assertNotNull(swResp)
        assertEquals(200, swResp!!.statusCode)
        assertEquals("same-origin", swResp.responseHeaders["Cross-Origin-Opener-Policy"])

        val missResp = manager.interceptRetroLocalOrCdn(
            "https://cdn.emulatorjs.org/stable/data/cores/gba.data",
            "GET",
            isOnline = false
        )
        assertNotNull(missResp)
        assertEquals(504, missResp!!.statusCode)
    }

    @Test
    fun offlineCoreManager_bundleLifecycleAndThreadsParity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = OfflineCoreManager(context)
        manager.clearAllBundles()

        assertFalse(manager.isSystemReadyOffline("psp", "ppsspp", requiredThreads = true))

        manager.markCorePrepared(
            systemId = "psp",
            coreId = "ppsspp",
            threadsPrepared = true,
            totalBytes = 12_500_000L,
            fileCount = 5,
            urls = listOf(
                "https://cdn.emulatorjs.org/stable/data/loader.js",
                "https://cdn.emulatorjs.org/stable/data/cores/ppsspp-wasm.data"
            )
        )

        assertTrue(manager.isSystemReadyOffline("psp", "ppsspp", requiredThreads = true))
        assertFalse(manager.isSystemReadyOffline("psp", "ppsspp", requiredThreads = false))
        assertTrue(manager.getReadySystemNames().contains("PSP"))

        manager.deleteBundle("psp")
        assertFalse(manager.isSystemReadyOffline("psp", "ppsspp", requiredThreads = null))
    }

    @Test
    fun settingsRepository_perGamepadIdProfileBindingsPersist() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SettingsRepository(context)
        val padId = "DualSense Wireless Controller (054c:0ce6)"

        repo.resetGamepadProfileBindings(padId)
        assertTrue(repo.settingsFlow.first().getGamepadProfileBindings(padId).isEmpty())

        repo.saveGamepadProfileBinding(padId, "a", KeyEvent.KEYCODE_BUTTON_B)
        repo.saveGamepadProfileBinding(padId, "b", KeyEvent.KEYCODE_BUTTON_A)
        val saved = repo.settingsFlow.first().getGamepadProfileBindings(padId)
        assertEquals(KeyEvent.KEYCODE_BUTTON_B, saved["a"])
        assertEquals(KeyEvent.KEYCODE_BUTTON_A, saved["b"])

        repo.resetGamepadProfileBindings(padId)
        assertTrue(repo.settingsFlow.first().getGamepadProfileBindings(padId).isEmpty())
    }
}
