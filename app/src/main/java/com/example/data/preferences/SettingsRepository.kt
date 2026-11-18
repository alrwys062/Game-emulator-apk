package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
    val soundVolume: Float = 0.75f,
    val controlsOpacity: Float = 0.85f,
    val controlsScale: Float = 1.0f,
    val moveControlsEnabled: Boolean = false,
    val selectedCore: String = "auto",
    val cheatsEnabled: Boolean = false,
    val cheatCodesText: String = "",
    val pspWarningEnabled: Boolean = true,
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
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val CONTROLS_OPACITY = floatPreferencesKey("controls_opacity")
        val CONTROLS_SCALE = floatPreferencesKey("controls_scale")
        val MOVE_CONTROLS = booleanPreferencesKey("move_controls")
        val SELECTED_CORE = stringPreferencesKey("selected_core")
        val CHEATS_ENABLED = booleanPreferencesKey("cheats_enabled")
        val CHEAT_CODES = stringPreferencesKey("cheat_codes")
        val PSP_WARNING = booleanPreferencesKey("psp_warning")
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
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.75f,
            controlsOpacity = prefs[Keys.CONTROLS_OPACITY] ?: 0.85f,
            controlsScale = prefs[Keys.CONTROLS_SCALE] ?: 1.0f,
            moveControlsEnabled = prefs[Keys.MOVE_CONTROLS] ?: false,
            selectedCore = prefs[Keys.SELECTED_CORE] ?: "auto",
            cheatsEnabled = prefs[Keys.CHEATS_ENABLED] ?: false,
            cheatCodesText = prefs[Keys.CHEAT_CODES] ?: "",
            pspWarningEnabled = prefs[Keys.PSP_WARNING] ?: true,
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false
        )
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name.trim().ifEmpty { "هلا" } }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun updateThemeMode(theme: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = theme }
    }

    suspend fun updateFilterMode(filter: String) {
        context.dataStore.edit { it[Keys.FILTER_MODE] = filter }
    }

    suspend fun updateScanlines(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SCANLINES] = enabled }
    }

    suspend fun updateAspectRatio(ratio: String) {
        context.dataStore.edit { it[Keys.ASPECT_RATIO] = ratio }
    }

    suspend fun updateSoundVolume(vol: Float) {
        context.dataStore.edit { it[Keys.SOUND_VOLUME] = vol.coerceIn(0f, 1f) }
    }

    suspend fun updateControlsOpacity(opacity: Float) {
        context.dataStore.edit { it[Keys.CONTROLS_OPACITY] = opacity.coerceIn(0.25f, 1f) }
    }

    suspend fun updateControlsScale(scale: Float) {
        context.dataStore.edit { it[Keys.CONTROLS_SCALE] = scale.coerceIn(0.75f, 1.35f) }
    }

    suspend fun updateMoveControls(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MOVE_CONTROLS] = enabled }
    }

    suspend fun updateSelectedCore(core: String) {
        context.dataStore.edit { it[Keys.SELECTED_CORE] = core }
    }

    suspend fun updateCheatsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CHEATS_ENABLED] = enabled }
    }

    suspend fun updateCheatCodes(codes: String) {
        context.dataStore.edit { it[Keys.CHEAT_CODES] = codes }
    }

    suspend fun updatePspWarning(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PSP_WARNING] = enabled }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }
}
