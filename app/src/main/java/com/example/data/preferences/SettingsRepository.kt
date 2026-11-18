package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "retroverse_settings")

data class RetroSettings(
    val userName: String = "هلا",
    val language: String = "ar",
    val themeMode: String = "dark",
    val filterMode: String = "CRT",
    val scanlinesEnabled: Boolean = true,
    val aspectRatio: String = "4:3",
    val resolutionScale: Int = 1,
    val showFps: Boolean = true,
    val frameSkip: Int = 0,
    val vsyncEnabled: Boolean = true,
    val soundVolume: Float = 0.75f,
    val audioLatency: String = "Balanced",
    val controlsOpacity: Float = 0.85f,
    val controlsScale: Float = 1.0f,
    val moveControlsEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val selectedCore: String = "auto",
    val wasmThreadsEnabled: Boolean = true,
    val rewindEnabled: Boolean = true,
    val fastForwardSpeed: Int = 2,
    val autoSaveOnExit: Boolean = true,
    val cheatsEnabled: Boolean = false,
    val cheatCodesText: String = "",
    val pspWarningEnabled: Boolean = true,
    val ps1BiosPath: String = "",
    val ps1BiosName: String = "",
    val ps2BiosPath: String = "",
    val ps2BiosName: String = "",
    val extraBiosPath: String = "",
    val extraBiosName: String = "",
    val onboardingCompleted: Boolean = false
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FILTER_MODE = stringPreferencesKey("filter_mode")
        val SCANLINES = booleanPreferencesKey("scanlines_enabled")
        val ASPECT_RATIO = stringPreferencesKey("aspect_ratio")
        val RESOLUTION_SCALE = intPreferencesKey("resolution_scale")
        val SHOW_FPS = booleanPreferencesKey("show_fps")
        val FRAME_SKIP = intPreferencesKey("frame_skip")
        val VSYNC_ENABLED = booleanPreferencesKey("vsync_enabled")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val AUDIO_LATENCY = stringPreferencesKey("audio_latency")
        val CONTROLS_OPACITY = floatPreferencesKey("controls_opacity")
        val CONTROLS_SCALE = floatPreferencesKey("controls_scale")
        val MOVE_CONTROLS = booleanPreferencesKey("move_controls")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SELECTED_CORE = stringPreferencesKey("selected_core")
        val WASM_THREADS = booleanPreferencesKey("wasm_threads")
        val REWIND_ENABLED = booleanPreferencesKey("rewind_enabled")
        val FF_SPEED = intPreferencesKey("ff_speed")
        val AUTO_SAVE = booleanPreferencesKey("auto_save_exit")
        val CHEATS_ENABLED = booleanPreferencesKey("cheats_enabled")
        val CHEAT_CODES = stringPreferencesKey("cheat_codes")
        val PSP_WARNING = booleanPreferencesKey("psp_warning")
        val PS1_BIOS_PATH = stringPreferencesKey("ps1_bios_path")
        val PS1_BIOS_NAME = stringPreferencesKey("ps1_bios_name")
        val PS2_BIOS_PATH = stringPreferencesKey("ps2_bios_path")
        val PS2_BIOS_NAME = stringPreferencesKey("ps2_bios_name")
        val EXTRA_BIOS_PATH = stringPreferencesKey("extra_bios_path")
        val EXTRA_BIOS_NAME = stringPreferencesKey("extra_bios_name")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val settingsFlow: Flow<RetroSettings> = context.dataStore.data.map { prefs ->
        RetroSettings(
            userName = prefs[Keys.USER_NAME] ?: "هلا",
            language = prefs[Keys.LANGUAGE] ?: "ar",
            themeMode = prefs[Keys.THEME_MODE] ?: "dark",
            filterMode = prefs[Keys.FILTER_MODE] ?: "CRT",
            scanlinesEnabled = prefs[Keys.SCANLINES] ?: true,
            aspectRatio = prefs[Keys.ASPECT_RATIO] ?: "4:3",
            resolutionScale = prefs[Keys.RESOLUTION_SCALE] ?: 1,
            showFps = prefs[Keys.SHOW_FPS] ?: true,
            frameSkip = prefs[Keys.FRAME_SKIP] ?: 0,
            vsyncEnabled = prefs[Keys.VSYNC_ENABLED] ?: true,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.75f,
            audioLatency = prefs[Keys.AUDIO_LATENCY] ?: "Balanced",
            controlsOpacity = prefs[Keys.CONTROLS_OPACITY] ?: 0.85f,
            controlsScale = prefs[Keys.CONTROLS_SCALE] ?: 1.0f,
            moveControlsEnabled = prefs[Keys.MOVE_CONTROLS] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            selectedCore = prefs[Keys.SELECTED_CORE] ?: "auto",
            wasmThreadsEnabled = prefs[Keys.WASM_THREADS] ?: true,
            rewindEnabled = prefs[Keys.REWIND_ENABLED] ?: true,
            fastForwardSpeed = prefs[Keys.FF_SPEED] ?: 2,
            autoSaveOnExit = prefs[Keys.AUTO_SAVE] ?: true,
            cheatsEnabled = prefs[Keys.CHEATS_ENABLED] ?: false,
            cheatCodesText = prefs[Keys.CHEAT_CODES] ?: "",
            pspWarningEnabled = prefs[Keys.PSP_WARNING] ?: true,
            ps1BiosPath = prefs[Keys.PS1_BIOS_PATH] ?: "",
            ps1BiosName = prefs[Keys.PS1_BIOS_NAME] ?: "",
            ps2BiosPath = prefs[Keys.PS2_BIOS_PATH] ?: "",
            ps2BiosName = prefs[Keys.PS2_BIOS_NAME] ?: "",
            extraBiosPath = prefs[Keys.EXTRA_BIOS_PATH] ?: "",
            extraBiosName = prefs[Keys.EXTRA_BIOS_NAME] ?: "",
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false
        )
    }

    suspend fun updateUserName(name: String) = context.dataStore.edit { it[Keys.USER_NAME] = name.trim().ifEmpty { "هلا" } }
    suspend fun updateLanguage(lang: String) = context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    suspend fun updateThemeMode(theme: String) = context.dataStore.edit { it[Keys.THEME_MODE] = theme }
    suspend fun updateFilterMode(filter: String) = context.dataStore.edit { it[Keys.FILTER_MODE] = filter }
    suspend fun updateScanlines(enabled: Boolean) = context.dataStore.edit { it[Keys.SCANLINES] = enabled }
    suspend fun updateAspectRatio(ratio: String) = context.dataStore.edit { it[Keys.ASPECT_RATIO] = ratio }
    suspend fun updateResolutionScale(scale: Int) = context.dataStore.edit { it[Keys.RESOLUTION_SCALE] = scale }
    suspend fun updateShowFps(show: Boolean) = context.dataStore.edit { it[Keys.SHOW_FPS] = show }
    suspend fun updateFrameSkip(skip: Int) = context.dataStore.edit { it[Keys.FRAME_SKIP] = skip }
    suspend fun updateVsync(enabled: Boolean) = context.dataStore.edit { it[Keys.VSYNC_ENABLED] = enabled }
    suspend fun updateSoundVolume(vol: Float) = context.dataStore.edit { it[Keys.SOUND_VOLUME] = vol.coerceIn(0f, 1f) }
    suspend fun updateAudioLatency(lat: String) = context.dataStore.edit { it[Keys.AUDIO_LATENCY] = lat }
    suspend fun updateControlsOpacity(opacity: Float) = context.dataStore.edit { it[Keys.CONTROLS_OPACITY] = opacity.coerceIn(0.25f, 1f) }
    suspend fun updateControlsScale(scale: Float) = context.dataStore.edit { it[Keys.CONTROLS_SCALE] = scale.coerceIn(0.75f, 1.35f) }
    suspend fun updateMoveControls(enabled: Boolean) = context.dataStore.edit { it[Keys.MOVE_CONTROLS] = enabled }
    suspend fun updateHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    suspend fun updateSelectedCore(core: String) = context.dataStore.edit { it[Keys.SELECTED_CORE] = core }
    suspend fun updateWasmThreads(enabled: Boolean) = context.dataStore.edit { it[Keys.WASM_THREADS] = enabled }
    suspend fun updateRewindEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.REWIND_ENABLED] = enabled }
    suspend fun updateFastForwardSpeed(speed: Int) = context.dataStore.edit { it[Keys.FF_SPEED] = speed }
    suspend fun updateAutoSaveOnExit(enabled: Boolean) = context.dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    suspend fun updateCheatsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.CHEATS_ENABLED] = enabled }
    suspend fun updateCheatCodes(codes: String) = context.dataStore.edit { it[Keys.CHEAT_CODES] = codes }
    suspend fun updatePspWarning(enabled: Boolean) = context.dataStore.edit { it[Keys.PSP_WARNING] = enabled }
    suspend fun setOnboardingCompleted(done: Boolean) = context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }

    suspend fun updateBios(systemTag: String, path: String, name: String) {
        context.dataStore.edit { prefs ->
            when (systemTag.lowercase()) {
                "ps1" -> {
                    prefs[Keys.PS1_BIOS_PATH] = path
                    prefs[Keys.PS1_BIOS_NAME] = name
                }
                "ps2" -> {
                    prefs[Keys.PS2_BIOS_PATH] = path
                    prefs[Keys.PS2_BIOS_NAME] = name
                }
                else -> {
                    prefs[Keys.EXTRA_BIOS_PATH] = path
                    prefs[Keys.EXTRA_BIOS_NAME] = name
                }
            }
        }
    }
}
