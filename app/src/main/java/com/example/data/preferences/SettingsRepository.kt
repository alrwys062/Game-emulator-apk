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
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "retroverse_settings")

data class BiosFileEntry(
    val slotKey: String,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val md5: String
)

data class ControlButtonPlacement(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val visible: Boolean = true
)

data class RetroSettings(
    val userName: String = "هلا",
    val language: String = "ar",
    val themeMode: String = "dark",
    val accentPreset: String = "purple_cyan",
    val uiScale: Float = 1.0f,

    // Video & Display
    val filterMode: String = "Sharp",
    val scanlinesEnabled: Boolean = false,
    val aspectRatio: String = "Original",
    val resolutionScale: Int = 1,
    val renderScalePercent: Int = 100,
    val showFps: Boolean = true,
    val orientationLock: String = "auto", // auto | portrait | landscape
    val wakeLockEnabled: Boolean = true,

    // Performance & Acceleration
    val performancePreset: String = "balanced", // saver | balanced | performance | custom
    val frameSkip: Int = 0,
    val vsyncEnabled: Boolean = true,
    val fpsCap: Int = 60,
    val smartPerfEnabled: Boolean = false,
    val preloadRomsEnabled: Boolean = true,
    val selectedCore: String = "auto",
    val wasmThreadsMode: String = "auto", // auto | on | off
    val rewindEnabled: Boolean = true,
    val fastForwardSpeed: Int = 2,
    val cdnMode: String = "official", // official | jsdelivr | custom
    val customDataPath: String = "",

    // Audio
    val soundVolume: Float = 0.8f,
    val audioMuted: Boolean = false,
    val audioLatency: String = "Balanced",

    // Controls & Gamepad
    val controlsOpacity: Float = 0.85f,
    val controlsScale: Float = 1.0f,
    val moveControlsEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val autoHideTouchOnGamepad: Boolean = true,
    val gamepadDeadzone: Float = 0.15f,
    val controlLayoutsJson: String = "{}",
    val gamepadBindingsJson: String = "{}",

    // Saves & Cheats
    val autoSaveOnExit: Boolean = true,
    val cheatsEnabled: Boolean = true,
    val cheatCodesText: String = "",
    val pspWarningEnabled: Boolean = true,

    // BIOS (legacy fields kept synced + full map JSON)
    val ps1BiosPath: String = "",
    val ps1BiosName: String = "",
    val ps2BiosPath: String = "",
    val ps2BiosName: String = "",
    val extraBiosPath: String = "",
    val extraBiosName: String = "",
    val biosMapJson: String = "{}",

    // Gemini AI (stored locally only)
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",

    val onboardingCompleted: Boolean = false
) {
    fun getBiosEntry(slotKey: String): BiosFileEntry? {
        return runCatching {
            val root = JSONObject(biosMapJson)
            if (!root.has(slotKey)) return null
            val obj = root.getJSONObject(slotKey)
            val path = obj.optString("path", "")
            if (path.isBlank()) return null
            BiosFileEntry(
                slotKey = slotKey,
                path = path,
                name = obj.optString("name", ""),
                sizeBytes = obj.optLong("sizeBytes", 0L),
                md5 = obj.optString("md5", "")
            )
        }.getOrNull()
    }

    fun getControlPlacement(systemProfile: String, isLandscape: Boolean, controlId: String): ControlButtonPlacement {
        return runCatching {
            val root = JSONObject(controlLayoutsJson)
            val key = "${systemProfile}_${if (isLandscape) "land" else "port"}"
            val sysObj = root.optJSONObject(key) ?: return ControlButtonPlacement()
            val btnObj = sysObj.optJSONObject(controlId) ?: return ControlButtonPlacement()
            ControlButtonPlacement(
                offsetX = btnObj.optDouble("x", 0.0).toFloat(),
                offsetY = btnObj.optDouble("y", 0.0).toFloat(),
                scale = btnObj.optDouble("s", 1.0).toFloat().coerceIn(0.6f, 1.6f),
                visible = btnObj.optBoolean("v", true)
            )
        }.getOrDefault(ControlButtonPlacement())
    }

    fun getKeyBindings(playerIndex: Int = 0): Map<String, Int> {
        return runCatching {
            val root = JSONObject(gamepadBindingsJson)
            val pObj = root.optJSONObject("p$playerIndex") ?: return emptyMap()
            val out = mutableMapOf<String, Int>()
            val keys = pObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                out[k] = pObj.getInt(k)
            }
            out
        }.getOrDefault(emptyMap())
    }
}

class SettingsRepository(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_PRESET = stringPreferencesKey("accent_preset")
        val UI_SCALE = floatPreferencesKey("ui_scale")

        val FILTER_MODE = stringPreferencesKey("filter_mode")
        val SCANLINES = booleanPreferencesKey("scanlines_enabled")
        val ASPECT_RATIO = stringPreferencesKey("aspect_ratio")
        val RESOLUTION_SCALE = intPreferencesKey("resolution_scale")
        val RENDER_SCALE_PERCENT = intPreferencesKey("render_scale_percent")
        val SHOW_FPS = booleanPreferencesKey("show_fps")
        val ORIENTATION_LOCK = stringPreferencesKey("orientation_lock")
        val WAKE_LOCK = booleanPreferencesKey("wake_lock")

        val PERF_PRESET = stringPreferencesKey("perf_preset")
        val FRAME_SKIP = intPreferencesKey("frame_skip")
        val VSYNC_ENABLED = booleanPreferencesKey("vsync_enabled")
        val FPS_CAP = intPreferencesKey("fps_cap")
        val SMART_PERF = booleanPreferencesKey("smart_perf")
        val PRELOAD_ROMS = booleanPreferencesKey("preload_roms")
        val SELECTED_CORE = stringPreferencesKey("selected_core")
        val WASM_THREADS_MODE = stringPreferencesKey("wasm_threads_mode")
        val REWIND_ENABLED = booleanPreferencesKey("rewind_enabled")
        val FF_SPEED = intPreferencesKey("ff_speed")
        val CDN_MODE = stringPreferencesKey("cdn_mode")
        val CUSTOM_DATA_PATH = stringPreferencesKey("custom_data_path")

        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val AUDIO_MUTED = booleanPreferencesKey("audio_muted")
        val AUDIO_LATENCY = stringPreferencesKey("audio_latency")

        val CONTROLS_OPACITY = floatPreferencesKey("controls_opacity")
        val CONTROLS_SCALE = floatPreferencesKey("controls_scale")
        val MOVE_CONTROLS = booleanPreferencesKey("move_controls")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val AUTO_HIDE_TOUCH = booleanPreferencesKey("auto_hide_touch")
        val GAMEPAD_DEADZONE = floatPreferencesKey("gamepad_deadzone")
        val CONTROL_LAYOUTS_JSON = stringPreferencesKey("control_layouts_json")
        val GAMEPAD_BINDINGS_JSON = stringPreferencesKey("gamepad_bindings_json")

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
        val BIOS_MAP_JSON = stringPreferencesKey("bios_map_json")

        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")

        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val settingsFlow: Flow<RetroSettings> = context.dataStore.data.map { prefs ->
        RetroSettings(
            userName = prefs[Keys.USER_NAME] ?: "هلا",
            language = prefs[Keys.LANGUAGE] ?: "ar",
            themeMode = prefs[Keys.THEME_MODE] ?: "dark",
            accentPreset = prefs[Keys.ACCENT_PRESET] ?: "purple_cyan",
            uiScale = prefs[Keys.UI_SCALE] ?: 1.0f,
            filterMode = prefs[Keys.FILTER_MODE] ?: "Sharp",
            scanlinesEnabled = prefs[Keys.SCANLINES] ?: false,
            aspectRatio = prefs[Keys.ASPECT_RATIO] ?: "Original",
            resolutionScale = prefs[Keys.RESOLUTION_SCALE] ?: 1,
            renderScalePercent = prefs[Keys.RENDER_SCALE_PERCENT] ?: 100,
            showFps = prefs[Keys.SHOW_FPS] ?: true,
            orientationLock = prefs[Keys.ORIENTATION_LOCK] ?: "auto",
            wakeLockEnabled = prefs[Keys.WAKE_LOCK] ?: true,
            performancePreset = prefs[Keys.PERF_PRESET] ?: "balanced",
            frameSkip = prefs[Keys.FRAME_SKIP] ?: 0,
            vsyncEnabled = prefs[Keys.VSYNC_ENABLED] ?: true,
            fpsCap = prefs[Keys.FPS_CAP] ?: 60,
            smartPerfEnabled = prefs[Keys.SMART_PERF] ?: false,
            preloadRomsEnabled = prefs[Keys.PRELOAD_ROMS] ?: true,
            selectedCore = prefs[Keys.SELECTED_CORE] ?: "auto",
            wasmThreadsMode = prefs[Keys.WASM_THREADS_MODE] ?: "auto",
            rewindEnabled = prefs[Keys.REWIND_ENABLED] ?: true,
            fastForwardSpeed = prefs[Keys.FF_SPEED] ?: 2,
            cdnMode = prefs[Keys.CDN_MODE] ?: "official",
            customDataPath = prefs[Keys.CUSTOM_DATA_PATH] ?: "",
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.8f,
            audioMuted = prefs[Keys.AUDIO_MUTED] ?: false,
            audioLatency = prefs[Keys.AUDIO_LATENCY] ?: "Balanced",
            controlsOpacity = prefs[Keys.CONTROLS_OPACITY] ?: 0.85f,
            controlsScale = prefs[Keys.CONTROLS_SCALE] ?: 1.0f,
            moveControlsEnabled = prefs[Keys.MOVE_CONTROLS] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            autoHideTouchOnGamepad = prefs[Keys.AUTO_HIDE_TOUCH] ?: true,
            gamepadDeadzone = prefs[Keys.GAMEPAD_DEADZONE] ?: 0.15f,
            controlLayoutsJson = prefs[Keys.CONTROL_LAYOUTS_JSON] ?: "{}",
            gamepadBindingsJson = prefs[Keys.GAMEPAD_BINDINGS_JSON] ?: "{}",
            autoSaveOnExit = prefs[Keys.AUTO_SAVE] ?: true,
            cheatsEnabled = prefs[Keys.CHEATS_ENABLED] ?: true,
            cheatCodesText = prefs[Keys.CHEAT_CODES] ?: "",
            pspWarningEnabled = prefs[Keys.PSP_WARNING] ?: true,
            ps1BiosPath = prefs[Keys.PS1_BIOS_PATH] ?: "",
            ps1BiosName = prefs[Keys.PS1_BIOS_NAME] ?: "",
            ps2BiosPath = prefs[Keys.PS2_BIOS_PATH] ?: "",
            ps2BiosName = prefs[Keys.PS2_BIOS_NAME] ?: "",
            extraBiosPath = prefs[Keys.EXTRA_BIOS_PATH] ?: "",
            extraBiosName = prefs[Keys.EXTRA_BIOS_NAME] ?: "",
            biosMapJson = prefs[Keys.BIOS_MAP_JSON] ?: "{}",
            geminiApiKey = prefs[Keys.GEMINI_API_KEY] ?: "",
            geminiModel = prefs[Keys.GEMINI_MODEL] ?: "gemini-2.5-flash",
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false
        )
    }

    suspend fun updateUserName(name: String) = context.dataStore.edit { it[Keys.USER_NAME] = name.trim().ifEmpty { "هلا" } }
    suspend fun updateLanguage(lang: String) = context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    suspend fun updateThemeMode(theme: String) = context.dataStore.edit { it[Keys.THEME_MODE] = theme }
    suspend fun updateAccentPreset(preset: String) = context.dataStore.edit { it[Keys.ACCENT_PRESET] = preset }
    suspend fun updateUiScale(scale: Float) = context.dataStore.edit { it[Keys.UI_SCALE] = scale.coerceIn(0.85f, 1.2f) }
    suspend fun updateFilterMode(filter: String) = context.dataStore.edit { it[Keys.FILTER_MODE] = filter }
    suspend fun updateScanlines(enabled: Boolean) = context.dataStore.edit { it[Keys.SCANLINES] = enabled }
    suspend fun updateAspectRatio(ratio: String) = context.dataStore.edit { it[Keys.ASPECT_RATIO] = ratio }
    suspend fun updateResolutionScale(scale: Int) = context.dataStore.edit { it[Keys.RESOLUTION_SCALE] = scale.coerceIn(1, 5) }
    suspend fun updateRenderScalePercent(percent: Int) = context.dataStore.edit { it[Keys.RENDER_SCALE_PERCENT] = percent.coerceIn(50, 100) }
    suspend fun updateShowFps(show: Boolean) = context.dataStore.edit { it[Keys.SHOW_FPS] = show }
    suspend fun updateOrientationLock(lock: String) = context.dataStore.edit { it[Keys.ORIENTATION_LOCK] = lock }
    suspend fun updateWakeLock(enabled: Boolean) = context.dataStore.edit { it[Keys.WAKE_LOCK] = enabled }

    suspend fun applyPerformancePreset(preset: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PERF_PRESET] = preset
            when (preset) {
                "saver" -> {
                    prefs[Keys.FRAME_SKIP] = 2
                    prefs[Keys.RESOLUTION_SCALE] = 1
                    prefs[Keys.RENDER_SCALE_PERCENT] = 75
                    prefs[Keys.FPS_CAP] = 30
                    prefs[Keys.VSYNC_ENABLED] = false
                    prefs[Keys.REWIND_ENABLED] = false
                }
                "balanced" -> {
                    prefs[Keys.FRAME_SKIP] = 0
                    prefs[Keys.RESOLUTION_SCALE] = 1
                    prefs[Keys.RENDER_SCALE_PERCENT] = 100
                    prefs[Keys.FPS_CAP] = 60
                    prefs[Keys.VSYNC_ENABLED] = true
                    prefs[Keys.REWIND_ENABLED] = true
                }
                "performance" -> {
                    prefs[Keys.FRAME_SKIP] = 0
                    prefs[Keys.RESOLUTION_SCALE] = 2
                    prefs[Keys.RENDER_SCALE_PERCENT] = 100
                    prefs[Keys.FPS_CAP] = 60
                    prefs[Keys.VSYNC_ENABLED] = true
                    prefs[Keys.WASM_THREADS_MODE] = "auto"
                }
            }
        }
    }

    suspend fun updateFrameSkip(skip: Int) = context.dataStore.edit {
        it[Keys.FRAME_SKIP] = skip.coerceIn(0, 5)
        it[Keys.PERF_PRESET] = "custom"
    }
    suspend fun updateVsync(enabled: Boolean) = context.dataStore.edit { it[Keys.VSYNC_ENABLED] = enabled }
    suspend fun updateFpsCap(cap: Int) = context.dataStore.edit { it[Keys.FPS_CAP] = cap }
    suspend fun updateSmartPerf(enabled: Boolean) = context.dataStore.edit { it[Keys.SMART_PERF] = enabled }
    suspend fun updatePreloadRoms(enabled: Boolean) = context.dataStore.edit { it[Keys.PRELOAD_ROMS] = enabled }
    suspend fun updateSelectedCore(core: String) = context.dataStore.edit { it[Keys.SELECTED_CORE] = core }
    suspend fun updateWasmThreadsMode(mode: String) = context.dataStore.edit { it[Keys.WASM_THREADS_MODE] = mode }
    suspend fun updateRewindEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.REWIND_ENABLED] = enabled }
    suspend fun updateFastForwardSpeed(speed: Int) = context.dataStore.edit { it[Keys.FF_SPEED] = speed.coerceIn(1, 4) }
    suspend fun updateCdnMode(mode: String) = context.dataStore.edit { it[Keys.CDN_MODE] = mode }
    suspend fun updateCustomDataPath(path: String) = context.dataStore.edit { it[Keys.CUSTOM_DATA_PATH] = path.trim() }

    suspend fun updateSoundVolume(vol: Float) = context.dataStore.edit { it[Keys.SOUND_VOLUME] = vol.coerceIn(0f, 1f) }
    suspend fun updateAudioMuted(muted: Boolean) = context.dataStore.edit { it[Keys.AUDIO_MUTED] = muted }
    suspend fun updateAudioLatency(lat: String) = context.dataStore.edit { it[Keys.AUDIO_LATENCY] = lat }
    suspend fun updateControlsOpacity(opacity: Float) = context.dataStore.edit { it[Keys.CONTROLS_OPACITY] = opacity.coerceIn(0.2f, 1f) }
    suspend fun updateControlsScale(scale: Float) = context.dataStore.edit { it[Keys.CONTROLS_SCALE] = scale.coerceIn(0.7f, 1.4f) }
    suspend fun updateMoveControls(enabled: Boolean) = context.dataStore.edit { it[Keys.MOVE_CONTROLS] = enabled }
    suspend fun updateHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    suspend fun updateAutoHideTouchOnGamepad(enabled: Boolean) = context.dataStore.edit { it[Keys.AUTO_HIDE_TOUCH] = enabled }
    suspend fun updateGamepadDeadzone(dz: Float) = context.dataStore.edit { it[Keys.GAMEPAD_DEADZONE] = dz.coerceIn(0.05f, 0.45f) }

    suspend fun updateAutoSaveOnExit(enabled: Boolean) = context.dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    suspend fun updateCheatsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.CHEATS_ENABLED] = enabled }
    suspend fun updateCheatCodes(codes: String) = context.dataStore.edit { it[Keys.CHEAT_CODES] = codes }
    suspend fun updatePspWarning(enabled: Boolean) = context.dataStore.edit { it[Keys.PSP_WARNING] = enabled }
    suspend fun setOnboardingCompleted(done: Boolean) = context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }

    suspend fun updateGeminiConfig(apiKey: String, model: String) {
        context.dataStore.edit {
            it[Keys.GEMINI_API_KEY] = apiKey.trim()
            it[Keys.GEMINI_MODEL] = model.trim().ifEmpty { "gemini-2.5-flash" }
        }
    }

    suspend fun saveControlPlacement(
        systemProfile: String,
        isLandscape: Boolean,
        controlId: String,
        placement: ControlButtonPlacement
    ) {
        context.dataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[Keys.CONTROL_LAYOUTS_JSON] ?: "{}") }.getOrElse { JSONObject() }
            val key = "${systemProfile}_${if (isLandscape) "land" else "port"}"
            val sysObj = root.optJSONObject(key) ?: JSONObject()
            val btnObj = JSONObject().apply {
                put("x", placement.offsetX.toDouble())
                put("y", placement.offsetY.toDouble())
                put("s", placement.scale.toDouble())
                put("v", placement.visible)
            }
            sysObj.put(controlId, btnObj)
            root.put(key, sysObj)
            prefs[Keys.CONTROL_LAYOUTS_JSON] = root.toString()
        }
    }

    suspend fun resetControlLayout(systemProfile: String, isLandscape: Boolean) {
        context.dataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[Keys.CONTROL_LAYOUTS_JSON] ?: "{}") }.getOrElse { JSONObject() }
            val key = "${systemProfile}_${if (isLandscape) "land" else "port"}"
            root.remove(key)
            prefs[Keys.CONTROL_LAYOUTS_JSON] = root.toString()
        }
    }

    suspend fun saveKeyBinding(playerIndex: Int, actionId: String, keyCode: Int) {
        context.dataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[Keys.GAMEPAD_BINDINGS_JSON] ?: "{}") }.getOrElse { JSONObject() }
            val pKey = "p$playerIndex"
            val pObj = root.optJSONObject(pKey) ?: JSONObject()
            pObj.put(actionId, keyCode)
            root.put(pKey, pObj)
            prefs[Keys.GAMEPAD_BINDINGS_JSON] = root.toString()
        }
    }

    suspend fun resetKeyBindings(playerIndex: Int) {
        context.dataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[Keys.GAMEPAD_BINDINGS_JSON] ?: "{}") }.getOrElse { JSONObject() }
            root.remove("p$playerIndex")
            prefs[Keys.GAMEPAD_BINDINGS_JSON] = root.toString()
        }
    }

    suspend fun updateBiosEntry(entry: BiosFileEntry?) {
        context.dataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[Keys.BIOS_MAP_JSON] ?: "{}") }.getOrElse { JSONObject() }
            if (entry != null) {
                val obj = JSONObject().apply {
                    put("path", entry.path)
                    put("name", entry.name)
                    put("sizeBytes", entry.sizeBytes)
                    put("md5", entry.md5)
                }
                root.put(entry.slotKey, obj)
                when (entry.slotKey) {
                    "ps1" -> {
                        prefs[Keys.PS1_BIOS_PATH] = entry.path
                        prefs[Keys.PS1_BIOS_NAME] = entry.name
                    }
                    "ps2" -> {
                        prefs[Keys.PS2_BIOS_PATH] = entry.path
                        prefs[Keys.PS2_BIOS_NAME] = entry.name
                    }
                    else -> {
                        prefs[Keys.EXTRA_BIOS_PATH] = entry.path
                        prefs[Keys.EXTRA_BIOS_NAME] = entry.name
                    }
                }
            }
            prefs[Keys.BIOS_MAP_JSON] = root.toString()
        }
    }

    suspend fun removeBiosEntry(slotKey: String) {
        context.dataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[Keys.BIOS_MAP_JSON] ?: "{}") }.getOrElse { JSONObject() }
            root.remove(slotKey)
            prefs[Keys.BIOS_MAP_JSON] = root.toString()
            when (slotKey) {
                "ps1" -> {
                    prefs[Keys.PS1_BIOS_PATH] = ""
                    prefs[Keys.PS1_BIOS_NAME] = ""
                }
                "ps2" -> {
                    prefs[Keys.PS2_BIOS_PATH] = ""
                    prefs[Keys.PS2_BIOS_NAME] = ""
                }
            }
        }
    }

    suspend fun exportSettingsJson(): String {
        var snapshot = JSONObject()
        context.dataStore.edit { prefs ->
            snapshot = JSONObject().apply {
                put("userName", prefs[Keys.USER_NAME] ?: "هلا")
                put("language", prefs[Keys.LANGUAGE] ?: "ar")
                put("themeMode", prefs[Keys.THEME_MODE] ?: "dark")
                put("accentPreset", prefs[Keys.ACCENT_PRESET] ?: "purple_cyan")
                put("filterMode", prefs[Keys.FILTER_MODE] ?: "Sharp")
                put("aspectRatio", prefs[Keys.ASPECT_RATIO] ?: "Original")
                put("frameSkip", prefs[Keys.FRAME_SKIP] ?: 0)
                put("controlLayoutsJson", prefs[Keys.CONTROL_LAYOUTS_JSON] ?: "{}")
                put("gamepadBindingsJson", prefs[Keys.GAMEPAD_BINDINGS_JSON] ?: "{}")
                put("cdnMode", prefs[Keys.CDN_MODE] ?: "official")
                put("customDataPath", prefs[Keys.CUSTOM_DATA_PATH] ?: "")
            }
        }
        return snapshot.toString(2)
    }

    suspend fun importSettingsJson(jsonStr: String) {
        val obj = JSONObject(jsonStr)
        context.dataStore.edit { prefs ->
            if (obj.has("userName")) prefs[Keys.USER_NAME] = obj.getString("userName")
            if (obj.has("language")) prefs[Keys.LANGUAGE] = obj.getString("language")
            if (obj.has("themeMode")) prefs[Keys.THEME_MODE] = obj.getString("themeMode")
            if (obj.has("accentPreset")) prefs[Keys.ACCENT_PRESET] = obj.getString("accentPreset")
            if (obj.has("filterMode")) prefs[Keys.FILTER_MODE] = obj.getString("filterMode")
            if (obj.has("aspectRatio")) prefs[Keys.ASPECT_RATIO] = obj.getString("aspectRatio")
            if (obj.has("frameSkip")) prefs[Keys.FRAME_SKIP] = obj.getInt("frameSkip")
            if (obj.has("controlLayoutsJson")) prefs[Keys.CONTROL_LAYOUTS_JSON] = obj.getString("controlLayoutsJson")
            if (obj.has("gamepadBindingsJson")) prefs[Keys.GAMEPAD_BINDINGS_JSON] = obj.getString("gamepadBindingsJson")
            if (obj.has("cdnMode")) prefs[Keys.CDN_MODE] = obj.getString("cdnMode")
            if (obj.has("customDataPath")) prefs[Keys.CUSTOM_DATA_PATH] = obj.getString("customDataPath")
        }
    }
}
