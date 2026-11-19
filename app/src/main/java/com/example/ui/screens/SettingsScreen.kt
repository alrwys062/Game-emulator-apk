package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.RetroSettings
import com.example.model.RetroSystem
import com.example.service.AiPerfRecommendation
import com.example.service.DeviceSpecSummary
import com.example.service.RomBundler
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalIsArabic
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AiChatMessage

@Composable
fun SettingsScreen(
    settings: RetroSettings,
    deviceSpec: DeviceSpecSummary,
    connectedGamepadName: String?,
    storageUsedBytes: Long,
    lastDiagnosticLogs: String,
    aiBusy: Boolean,
    aiPerfRec: AiPerfRecommendation?,
    aiChatMessages: List<AiChatMessage>,
    onRefreshGamepads: () -> Unit,
    onRefreshStorage: () -> Unit,
    onPickBiosFile: (slotKey: String) -> Unit,
    onDownloadBiosUrl: (slotKey: String, url: String) -> Unit,
    onRemoveBios: (slotKey: String) -> Unit,
    onOpenBiosDocs: () -> Unit,
    onRunEngineTest: () -> Unit,
    onUpdateFilter: (String) -> Unit,
    onUpdateScanlines: (Boolean) -> Unit,
    onUpdateAspectRatio: (String) -> Unit,
    onUpdateResolutionScale: (Int) -> Unit,
    onUpdateRenderScalePercent: (Int) -> Unit,
    onUpdateShowFps: (Boolean) -> Unit,
    onUpdateOrientationLock: (String) -> Unit,
    onUpdateWakeLock: (Boolean) -> Unit,
    onApplyPerfPreset: (String) -> Unit,
    onUpdateFrameSkip: (Int) -> Unit,
    onUpdateVsync: (Boolean) -> Unit,
    onUpdateFpsCap: (Int) -> Unit,
    onUpdateSmartPerf: (Boolean) -> Unit,
    onUpdatePreloadRoms: (Boolean) -> Unit,
    onUpdateSelectedCore: (String) -> Unit,
    onUpdateThreadsMode: (String) -> Unit,
    onUpdateRewindEnabled: (Boolean) -> Unit,
    onUpdateFastForwardSpeed: (Int) -> Unit,
    onUpdateCdnMode: (String) -> Unit,
    onUpdateCustomDataPath: (String) -> Unit,
    onUpdateSoundVolume: (Float) -> Unit,
    onUpdateAudioMuted: (Boolean) -> Unit,
    onUpdateAudioLatency: (String) -> Unit,
    onUpdateControlsOpacity: (Float) -> Unit,
    onUpdateControlsScale: (Float) -> Unit,
    onUpdateMoveControls: (Boolean) -> Unit,
    onUpdateHaptics: (Boolean) -> Unit,
    onUpdateAutoHideTouch: (Boolean) -> Unit,
    onUpdateGamepadDeadzone: (Float) -> Unit,
    onSaveKeyBinding: (playerIndex: Int, actionId: String, keyCode: Int) -> Unit,
    onResetKeyBindings: (playerIndex: Int) -> Unit,
    onUpdateAutoSaveOnExit: (Boolean) -> Unit,
    onUpdateCheatsEnabled: (Boolean) -> Unit,
    onUpdateCheatCodes: (String) -> Unit,
    onUpdatePspWarning: (Boolean) -> Unit,
    onSaveGeminiConfig: (apiKey: String, model: String) -> Unit,
    onRequestAiPerfRec: () -> Unit,
    onApplyAiPerfRec: (AiPerfRecommendation) -> Unit,
    onSendAiChat: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateAccentPreset: (String) -> Unit,
    onUpdateUserName: (String) -> Unit,
    onReplayOnboarding: () -> Unit,
    onClearCache: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current

    var showBiosDialog by remember { mutableStateOf(false) }
    var showPerfDialog by remember { mutableStateOf(false) }
    var showGamepadDialog by remember { mutableStateOf(false) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showCheatsDialog by remember { mutableStateOf(false) }
    var draftCheats by remember(settings.cheatCodesText) { mutableStateOf(settings.cheatCodesText) }
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember(settings.userName) { mutableStateOf(settings.userName) }

    val installedBiosCount = remember(settings.biosMapJson, settings.ps1BiosName, settings.ps2BiosName) {
        RetroSystem.biosSpecs.count { spec ->
            val entry = settings.getBiosEntry(spec.key)
            val legacy = when (spec.key) {
                "ps1" -> settings.ps1BiosName
                "ps2" -> settings.ps2BiosName
                else -> ""
            }
            (entry?.name?.isNotBlank() == true) || legacy.isNotBlank()
        }
    }

    LaunchedEffect(Unit) {
        onRefreshGamepads()
        onRefreshStorage()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Section 0: BIOS, Performance Engine & Gemini AI Hubs
        item {
            SettingsGroupHeader(strings.sectionBios)
            SettingsGroupCard {
                SettingsCycleRow(
                    icon = Icons.Rounded.DeveloperBoard,
                    iconBg = PrimaryPurple,
                    title = strings.sectionBios,
                    valueLabel = "$installedBiosCount / ${RetroSystem.biosSpecs.size}",
                    testTag = "settings_bios_hub",
                    onClick = { showBiosDialog = true }
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.Speed,
                    iconBg = SecondaryCyan,
                    title = strings.sectionEngine,
                    valueLabel = "${settings.performancePreset.uppercase()} • ${settings.cdnMode}",
                    testTag = "settings_perf_hub",
                    onClick = { showPerfDialog = true }
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.PlayCircleOutline,
                    iconBg = SuccessGreen,
                    title = strings.engineTestBtn,
                    valueLabel = "GBA",
                    testTag = "settings_engine_test",
                    onClick = onRunEngineTest
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.AutoAwesome,
                    iconBg = Color(0xFFEC4899),
                    title = strings.aiAssistantTitle,
                    valueLabel = if (settings.geminiApiKey.isNotBlank()) settings.geminiModel else (if (isArabic) "غير مُعد" else "Setup"),
                    testTag = "settings_gemini_hub",
                    onClick = { showGeminiDialog = true }
                )
            }
        }

        // Section 1: Graphics & Display
        item {
            SettingsGroupHeader(strings.sectionGraphics)
            SettingsGroupCard {
                val filters = listOf("Sharp", "Smooth", "CRT", "Scanlines", "LCD")
                SettingsCycleRow(
                    icon = Icons.Rounded.FilterVintage,
                    iconBg = PrimaryPurple,
                    title = strings.settingFilter,
                    valueLabel = settings.filterMode,
                    onClick = {
                        val next = filters[(filters.indexOf(settings.filterMode) + 1) % filters.size]
                        onUpdateFilter(next)
                    }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.GridOn,
                    iconBg = SecondaryCyan,
                    title = strings.settingScanlines,
                    checked = settings.scanlinesEnabled,
                    testTag = "setting_scanlines_switch",
                    onCheckedChange = onUpdateScanlines
                )
                SettingsDivider()
                val aspects = listOf("Original", "4:3", "16:9", "Full")
                SettingsCycleRow(
                    icon = Icons.Rounded.AspectRatio,
                    iconBg = WarningAmber,
                    title = strings.settingAspectRatio,
                    valueLabel = settings.aspectRatio,
                    onClick = {
                        val next = aspects[(aspects.indexOf(settings.aspectRatio) + 1) % aspects.size]
                        onUpdateAspectRatio(next)
                    }
                )
                SettingsDivider()
                val orientations = listOf("auto", "portrait", "landscape")
                SettingsCycleRow(
                    icon = Icons.Rounded.ScreenLockRotation,
                    iconBg = PrimaryPurple,
                    title = if (isArabic) "اتجاه الشاشة أثناء اللعب" else "Play Orientation Lock",
                    valueLabel = settings.orientationLock.uppercase(),
                    onClick = {
                        val next = orientations[(orientations.indexOf(settings.orientationLock) + 1) % orientations.size]
                        onUpdateOrientationLock(next)
                    }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.WbSunny,
                    iconBg = WarningAmber,
                    title = if (isArabic) "إبقاء الشاشة مضاءة أثناء اللعب" else "Keep Screen Awake",
                    checked = settings.wakeLockEnabled,
                    testTag = "setting_wakelock_switch",
                    onCheckedChange = onUpdateWakeLock
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.Speed,
                    iconBg = Color(0xFF3B82F6),
                    title = strings.settingShowFps,
                    checked = settings.showFps,
                    testTag = "setting_show_fps_switch",
                    onCheckedChange = onUpdateShowFps
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.Sync,
                    iconBg = PrimaryPurple,
                    title = strings.settingVsync,
                    checked = settings.vsyncEnabled,
                    testTag = "setting_vsync_switch",
                    onCheckedChange = onUpdateVsync
                )
            }
        }

        // Section 2: Audio & Controls
        item {
            SettingsGroupHeader(strings.sectionAudioControls)
            SettingsGroupCard {
                SettingsSliderRow(
                    icon = Icons.Rounded.VolumeUp,
                    iconBg = SuccessGreen,
                    title = strings.settingSound,
                    value = settings.soundVolume,
                    valueRange = 0f..1f,
                    onValueChange = onUpdateSoundVolume
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.VolumeOff,
                    iconBg = ErrorRose,
                    title = if (isArabic) "كتم الصوت" else "Mute Audio",
                    checked = settings.audioMuted,
                    testTag = "setting_mute_switch",
                    onCheckedChange = onUpdateAudioMuted
                )
                SettingsDivider()
                val latencies = listOf("Low", "Balanced", "High")
                SettingsCycleRow(
                    icon = Icons.Rounded.GraphicEq,
                    iconBg = SecondaryCyan,
                    title = strings.settingAudioLatency,
                    valueLabel = settings.audioLatency,
                    onClick = {
                        val next = latencies[(latencies.indexOf(settings.audioLatency) + 1) % latencies.size]
                        onUpdateAudioLatency(next)
                    }
                )
                SettingsDivider()
                SettingsSliderRow(
                    icon = Icons.Rounded.SettingsBrightness,
                    iconBg = Color(0xFFEC4899),
                    title = strings.settingControlsOpacity,
                    value = settings.controlsOpacity,
                    valueRange = 0.25f..1f,
                    onValueChange = onUpdateControlsOpacity
                )
                SettingsDivider()
                SettingsSliderRow(
                    icon = Icons.Rounded.TouchApp,
                    iconBg = PrimaryPurple,
                    title = strings.settingControlsSize,
                    value = settings.controlsScale,
                    valueRange = 0.75f..1.35f,
                    onValueChange = onUpdateControlsScale
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.OpenWith,
                    iconBg = Color(0xFF3B82F6),
                    title = strings.settingMoveControls,
                    checked = settings.moveControlsEnabled,
                    testTag = "setting_move_controls_switch",
                    onCheckedChange = onUpdateMoveControls
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.Vibration,
                    iconBg = WarningAmber,
                    title = strings.settingHaptics,
                    checked = settings.hapticsEnabled,
                    testTag = "setting_haptics_switch",
                    onCheckedChange = onUpdateHaptics
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.Gamepad,
                    iconBg = SuccessGreen,
                    title = strings.gamepadMappingTitle,
                    valueLabel = connectedGamepadName ?: strings.notConnected,
                    testTag = "settings_gamepad_mapping",
                    onClick = { showGamepadDialog = true }
                )
            }
        }

        // Section 3: Engine Defaults & Cheats
        item {
            SettingsGroupHeader(strings.playSaveSlots)
            SettingsGroupCard {
                val cores = listOf("auto", "psx", "pcsx_rearmed", "psp", "nes", "snes", "gba", "nds", "n64", "segaMD", "mame2003", "fbneo")
                SettingsCycleRow(
                    icon = Icons.Rounded.Memory,
                    iconBg = ErrorRose,
                    title = strings.settingCore,
                    valueLabel = if (settings.selectedCore == "auto") strings.coreAuto else settings.selectedCore.uppercase(),
                    onClick = {
                        val next = cores[(cores.indexOf(settings.selectedCore) + 1) % cores.size]
                        onUpdateSelectedCore(next)
                    }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.History,
                    iconBg = PrimaryPurple,
                    title = strings.settingRewind,
                    checked = settings.rewindEnabled,
                    testTag = "setting_rewind_switch",
                    onCheckedChange = onUpdateRewindEnabled
                )
                SettingsDivider()
                val speeds = listOf(2, 3, 4)
                SettingsCycleRow(
                    icon = Icons.Rounded.FastForward,
                    iconBg = SuccessGreen,
                    title = strings.settingFastForwardSpeed,
                    valueLabel = "${settings.fastForwardSpeed}x",
                    onClick = {
                        val next = speeds[(speeds.indexOf(settings.fastForwardSpeed) + 1) % speeds.size]
                        onUpdateFastForwardSpeed(next)
                    }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.Save,
                    iconBg = Color(0xFF3B82F6),
                    title = strings.settingAutoSaveExit,
                    checked = settings.autoSaveOnExit,
                    testTag = "setting_autosave_switch",
                    onCheckedChange = onUpdateAutoSaveOnExit
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.Code,
                    iconBg = PrimaryPurple,
                    title = strings.settingCheats,
                    checked = settings.cheatsEnabled,
                    testTag = "setting_cheats_switch",
                    onCheckedChange = {
                        onUpdateCheatsEnabled(it)
                        if (it) showCheatsDialog = true
                    }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Rounded.Bolt,
                    iconBg = WarningAmber,
                    title = strings.settingPspWarning,
                    checked = settings.pspWarningEnabled,
                    testTag = "setting_psp_warning_switch",
                    onCheckedChange = onUpdatePspWarning
                )
            }
        }

        // Section 4: App, Storage & Backup
        item {
            SettingsGroupHeader(strings.sectionApp)
            SettingsGroupCard {
                SettingsCycleRow(
                    icon = Icons.Rounded.Language,
                    iconBg = SecondaryCyan,
                    title = strings.settingLanguage,
                    valueLabel = if (settings.language == "ar") strings.langArabic else strings.langEnglish,
                    testTag = "setting_language_row",
                    onClick = { onUpdateLanguage(if (settings.language == "ar") "en" else "ar") }
                )
                SettingsDivider()
                val themes = listOf("dark", "oled", "neon")
                val themeLabel = when (settings.themeMode) {
                    "oled" -> strings.themeOled
                    "neon" -> strings.themeNeon
                    else -> strings.themeDark
                }
                SettingsCycleRow(
                    icon = Icons.Rounded.Palette,
                    iconBg = PrimaryPurple,
                    title = strings.settingTheme,
                    valueLabel = themeLabel,
                    testTag = "setting_theme_row",
                    onClick = {
                        val next = themes[(themes.indexOf(settings.themeMode) + 1) % themes.size]
                        onUpdateThemeMode(next)
                    }
                )
                SettingsDivider()
                val accents = AccentPreset.entries
                val currentAccent = AccentPreset.fromId(settings.accentPreset)
                SettingsCycleRow(
                    icon = Icons.Rounded.ColorLens,
                    iconBg = currentAccent.primary,
                    title = if (isArabic) "تدرج الألوان" else "Accent Gradient",
                    valueLabel = if (isArabic) currentAccent.labelAr else currentAccent.labelEn,
                    onClick = {
                        val next = accents[(accents.indexOf(currentAccent) + 1) % accents.size]
                        onUpdateAccentPreset(next.id)
                    }
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.Person,
                    iconBg = SuccessGreen,
                    title = strings.settingUsername,
                    valueLabel = settings.userName,
                    onClick = { showNameDialog = true }
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.Storage,
                    iconBg = SecondaryCyan,
                    title = if (isArabic) "المساحة المستخدمة" else "Storage Used",
                    valueLabel = RomBundler.formatFileSize(storageUsedBytes),
                    onClick = onRefreshStorage
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.BugReport,
                    iconBg = WarningAmber,
                    title = if (isArabic) "سجل التشخيص والأخطاء" else "Diagnostic & Error Logs",
                    valueLabel = "",
                    onClick = { showLogsDialog = true }
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.PlayCircleOutline,
                    iconBg = Color(0xFF3B82F6),
                    title = strings.settingReplayOnboarding,
                    valueLabel = "",
                    onClick = onReplayOnboarding
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.CleaningServices,
                    iconBg = ErrorRose,
                    title = strings.settingClearCache,
                    valueLabel = "",
                    onClick = onClearCache
                )
            }
        }

        // Section 5: Backup & Restore (Settings + Covers + Cheats JSON)
        item {
            SettingsGroupHeader(if (isArabic) "نسخة احتياطية" else "Backup & Restore")
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WarningAmber.copy(alpha = 0.14f))
                    .border(1.dp, WarningAmber.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isArabic) {
                        "خذ نسخة احتياطية قبل التحديث — يتم حفظ الإعدادات والأغلفة وأكواد الغش بصيغة JSON."
                    } else {
                        "Back up before updating — saves settings, covers, and cheat codes as JSON."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WarningAmber
                )
            }
            SettingsGroupCard {
                SettingsCycleRow(
                    icon = Icons.Rounded.Backup,
                    iconBg = PrimaryPurple,
                    title = if (isArabic) "تصدير نسخة احتياطية (الإعدادات + الأغلفة + الغش JSON)" else "Export Backup (Settings + Covers + Cheats JSON)",
                    valueLabel = "JSON",
                    testTag = "settings_export_backup",
                    onClick = onExportBackup
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.Download,
                    iconBg = SuccessGreen,
                    title = if (isArabic) "استيراد نسخة احتياطية (JSON)" else "Import Backup (JSON)",
                    valueLabel = "",
                    testTag = "settings_import_backup",
                    onClick = onImportBackup
                )
            }
        }
    }

    if (showBiosDialog) {
        BiosManagerDialog(
            settings = settings,
            onDismiss = { showBiosDialog = false },
            onPickBiosFile = onPickBiosFile,
            onDownloadBiosUrl = onDownloadBiosUrl,
            onRemoveBios = onRemoveBios,
            onOpenBiosDocs = onOpenBiosDocs
        )
    }

    if (showPerfDialog) {
        PerformanceEngineDialog(
            settings = settings,
            deviceSpec = deviceSpec,
            onDismiss = { showPerfDialog = false },
            onApplyPreset = onApplyPerfPreset,
            onUpdateFrameSkip = onUpdateFrameSkip,
            onUpdateResolutionScale = onUpdateResolutionScale,
            onUpdateRenderScalePercent = onUpdateRenderScalePercent,
            onUpdateFpsCap = onUpdateFpsCap,
            onUpdateThreadsMode = onUpdateThreadsMode,
            onUpdateSmartPerf = onUpdateSmartPerf,
            onUpdatePreloadRoms = onUpdatePreloadRoms,
            onUpdateCdnMode = onUpdateCdnMode,
            onUpdateCustomDataPath = onUpdateCustomDataPath,
            onRunEngineTest = onRunEngineTest
        )
    }

    if (showGamepadDialog) {
        GamepadMappingDialog(
            settings = settings,
            connectedGamepadName = connectedGamepadName,
            onDismiss = { showGamepadDialog = false },
            onUpdateAutoHideTouch = onUpdateAutoHideTouch,
            onUpdateDeadzone = onUpdateGamepadDeadzone,
            onSaveKeyBinding = onSaveKeyBinding,
            onResetKeyBindings = onResetKeyBindings
        )
    }

    if (showGeminiDialog) {
        GeminiAiDialog(
            settings = settings,
            aiBusy = aiBusy,
            aiPerfRec = aiPerfRec,
            aiChatMessages = aiChatMessages,
            onDismiss = { showGeminiDialog = false },
            onSaveGeminiConfig = onSaveGeminiConfig,
            onRequestPerfRec = onRequestAiPerfRec,
            onApplyPerfRec = onApplyAiPerfRec,
            onSendChat = onSendAiChat
        )
    }

    if (showLogsDialog) {
        DiagnosticLogDialog(
            logs = lastDiagnosticLogs,
            onDismiss = { showLogsDialog = false },
            onShowMessage = onShowMessage
        )
    }

    if (showCheatsDialog) {
        AlertDialog(
            onDismissRequest = { showCheatsDialog = false },
            containerColor = SurfaceDark,
            title = { Text(strings.cheatsTitle, color = Color.White) },
            text = {
                OutlinedTextField(
                    value = draftCheats,
                    onValueChange = { draftCheats = it },
                    placeholder = { Text(strings.cheatsPlaceholder, color = TextSecondary) },
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateCheatCodes(draftCheats)
                    showCheatsDialog = false
                }) { Text(strings.saveAction, color = SecondaryCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showCheatsDialog = false }) { Text(strings.cancelAction, color = TextSecondary) }
            }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = SurfaceDark,
            title = { Text(strings.editNameTitle, color = Color.White) },
            text = {
                OutlinedTextField(value = draftName, onValueChange = { draftName = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateUserName(draftName)
                    showNameDialog = false
                }) { Text(strings.saveAction, color = SecondaryCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text(strings.cancelAction, color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Row(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingsIconBadge(icon: ImageVector, bgColor: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor.copy(alpha = 0.20f))
            .border(1.dp, bgColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = bgColor, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun SettingsCycleRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    valueLabel: String,
    testTag: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            SettingsIconBadge(icon, iconBg)
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (valueLabel.isNotEmpty()) {
                Text(text = valueLabel, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.width(4.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            SettingsIconBadge(icon, iconBg)
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SecondaryCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}

@Composable
private fun SettingsSliderRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.95f)) {
            SettingsIconBadge(icon, iconBg)
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1.05f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = SecondaryCyan,
                inactiveTrackColor = SurfaceElevated
            )
        )
    }
}
