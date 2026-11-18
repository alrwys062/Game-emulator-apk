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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.RetroSettings
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    settings: RetroSettings,
    connectedGamepadName: String?,
    onRefreshGamepads: () -> Unit,
    onPickBiosFile: (systemTag: String) -> Unit,
    onDownloadBiosUrl: (systemTag: String, url: String) -> Unit,
    onOpenBiosDocs: () -> Unit,
    onUpdateFilter: (String) -> Unit,
    onUpdateScanlines: (Boolean) -> Unit,
    onUpdateAspectRatio: (String) -> Unit,
    onUpdateResolutionScale: (Int) -> Unit,
    onUpdateShowFps: (Boolean) -> Unit,
    onUpdateFrameSkip: (Int) -> Unit,
    onUpdateVsync: (Boolean) -> Unit,
    onUpdateSoundVolume: (Float) -> Unit,
    onUpdateAudioLatency: (String) -> Unit,
    onUpdateControlsOpacity: (Float) -> Unit,
    onUpdateControlsScale: (Float) -> Unit,
    onUpdateMoveControls: (Boolean) -> Unit,
    onUpdateHaptics: (Boolean) -> Unit,
    onUpdateSelectedCore: (String) -> Unit,
    onUpdateWasmThreads: (Boolean) -> Unit,
    onUpdateRewindEnabled: (Boolean) -> Unit,
    onUpdateFastForwardSpeed: (Int) -> Unit,
    onUpdateAutoSaveOnExit: (Boolean) -> Unit,
    onUpdateCheatsEnabled: (Boolean) -> Unit,
    onUpdateCheatCodes: (String) -> Unit,
    onUpdatePspWarning: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateUserName: (String) -> Unit,
    onReplayOnboarding: () -> Unit,
    onClearCache: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showCheatsDialog by remember { mutableStateOf(false) }
    var draftCheats by remember(settings.cheatCodesText) { mutableStateOf(settings.cheatCodesText) }
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember(settings.userName) { mutableStateOf(settings.userName) }
    var showShortcutsDialog by remember { mutableStateOf(false) }
    var biosDownloadTarget by remember { mutableStateOf<String?>(null) }
    var draftBiosUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onRefreshGamepads()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
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

        // Section 0: BIOS Manager (PS1 / PS2 / Systems)
        item {
            SettingsGroupHeader(strings.sectionBios)
            SettingsGroupCard {
                BiosItemRow(
                    title = strings.ps1BiosTitle,
                    installedName = settings.ps1BiosName,
                    pickLabel = strings.pickBiosFile,
                    downloadLabel = strings.downloadBiosBtn,
                    installedFormatter = strings.biosInstalled,
                    notInstalledText = strings.biosNotInstalled,
                    accent = PrimaryPurple,
                    testTagPrefix = "bios_ps1",
                    onPick = { onPickBiosFile("ps1") },
                    onDownload = {
                        draftBiosUrl = ""
                        biosDownloadTarget = "ps1"
                    }
                )
                SettingsDivider()
                BiosItemRow(
                    title = strings.ps2BiosTitle,
                    installedName = settings.ps2BiosName,
                    pickLabel = strings.pickBiosFile,
                    downloadLabel = strings.downloadBiosBtn,
                    installedFormatter = strings.biosInstalled,
                    notInstalledText = strings.biosNotInstalled,
                    accent = SecondaryCyan,
                    testTagPrefix = "bios_ps2",
                    onPick = { onPickBiosFile("ps2") },
                    onDownload = {
                        draftBiosUrl = ""
                        biosDownloadTarget = "ps2"
                    }
                )
                SettingsDivider()
                BiosItemRow(
                    title = strings.extraBiosTitle,
                    installedName = settings.extraBiosName,
                    pickLabel = strings.pickBiosFile,
                    downloadLabel = strings.downloadBiosBtn,
                    installedFormatter = strings.biosInstalled,
                    notInstalledText = strings.biosNotInstalled,
                    accent = SuccessGreen,
                    testTagPrefix = "bios_extra",
                    onPick = { onPickBiosFile("extra") },
                    onDownload = {
                        draftBiosUrl = ""
                        biosDownloadTarget = "extra"
                    }
                )
            }
        }

        // Section 1: Graphics & Display
        item {
            SettingsGroupHeader(strings.sectionGraphics)
            SettingsGroupCard {
                val filters = listOf("CRT", "Scanlines", "LCD", "Smooth", "Sharp")
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
                val aspects = listOf("4:3", "16:9", "Original", "Full")
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
                val scales = listOf(1, 2, 3)
                SettingsCycleRow(
                    icon = Icons.Rounded.Hd,
                    iconBg = SuccessGreen,
                    title = strings.settingResolutionScale,
                    valueLabel = "${settings.resolutionScale}x",
                    onClick = {
                        val next = scales[(scales.indexOf(settings.resolutionScale) + 1) % scales.size]
                        onUpdateResolutionScale(next)
                    }
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
                val skips = listOf(0, 1, 2)
                SettingsCycleRow(
                    icon = Icons.Rounded.FastForward,
                    iconBg = Color(0xFFEC4899),
                    title = strings.settingFrameSkip,
                    valueLabel = if (settings.frameSkip == 0) strings.coreAuto else settings.frameSkip.toString(),
                    onClick = {
                        val next = skips[(skips.indexOf(settings.frameSkip) + 1) % skips.size]
                        onUpdateFrameSkip(next)
                    }
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
                    title = strings.settingGamepad,
                    valueLabel = connectedGamepadName ?: strings.notConnected,
                    onClick = onRefreshGamepads
                )
                SettingsDivider()
                SettingsCycleRow(
                    icon = Icons.Rounded.Bolt,
                    iconBg = WarningAmber,
                    title = strings.settingShortcuts,
                    valueLabel = "L1+Start",
                    onClick = { showShortcutsDialog = true }
                )
            }
        }

        // Section 3: Engine & Performance
        item {
            SettingsGroupHeader(strings.sectionEngine)
            SettingsGroupCard {
                val cores = listOf(
                    "auto", "pcsx_rearmed", "mednafen_psx_hw", "psp",
                    "nes", "snes", "gba", "nds", "n64", "segaMD", "mame2003"
                )
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
                    icon = Icons.Rounded.DeveloperBoard,
                    iconBg = SecondaryCyan,
                    title = strings.settingWasmThreads,
                    checked = settings.wasmThreadsEnabled,
                    testTag = "setting_wasm_threads_switch",
                    onCheckedChange = onUpdateWasmThreads
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

        // Section 4: App & Storage
        item {
            SettingsGroupHeader(strings.sectionApp)
            SettingsGroupCard {
                SettingsCycleRow(
                    icon = Icons.Rounded.Language,
                    iconBg = SecondaryCyan,
                    title = strings.settingLanguage,
                    valueLabel = if (settings.language == "ar") strings.langArabic else strings.langEnglish,
                    testTag = "setting_language_row",
                    onClick = {
                        onUpdateLanguage(if (settings.language == "ar") "en" else "ar")
                    }
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
                SettingsCycleRow(
                    icon = Icons.Rounded.Person,
                    iconBg = SuccessGreen,
                    title = strings.settingUsername,
                    valueLabel = settings.userName,
                    onClick = { showNameDialog = true }
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
    }

    biosDownloadTarget?.let { sysTag ->
        AlertDialog(
            onDismissRequest = { biosDownloadTarget = null },
            containerColor = SurfaceDark,
            title = { Text("${strings.downloadBiosUrlTitle} (${sysTag.uppercase()})", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.downloadBiosUrlHint, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    OutlinedTextField(
                        value = draftBiosUrl,
                        onValueChange = { draftBiosUrl = it },
                        placeholder = { Text("https://.../scph5501.bin", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .clickable {
                                biosDownloadTarget = null
                                onOpenBiosDocs()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.biosOpenDocs, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = draftBiosUrl.isNotBlank(),
                    onClick = {
                        onDownloadBiosUrl(sysTag, draftBiosUrl)
                        biosDownloadTarget = null
                    }
                ) {
                    Text(strings.downloadBiosBtn, color = SecondaryCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { biosDownloadTarget = null }) {
                    Text(strings.cancelAction, color = TextSecondary)
                }
            }
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
                }) {
                    Text(strings.saveAction, color = SecondaryCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheatsDialog = false }) {
                    Text(strings.cancelAction, color = TextSecondary)
                }
            }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = SurfaceDark,
            title = { Text(strings.editNameTitle, color = Color.White) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateUserName(draftName)
                    showNameDialog = false
                }) {
                    Text(strings.saveAction, color = SecondaryCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(strings.cancelAction, color = TextSecondary)
                }
            }
        )
    }

    if (showShortcutsDialog) {
        AlertDialog(
            onDismissRequest = { showShortcutsDialog = false },
            containerColor = SurfaceDark,
            title = { Text(strings.settingShortcuts, color = Color.White) },
            text = { Text(strings.shortcutsSummary, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showShortcutsDialog = false }) {
                    Text(strings.close, color = SecondaryCyan)
                }
            }
        )
    }
}

@Composable
private fun BiosItemRow(
    title: String,
    installedName: String,
    pickLabel: String,
    downloadLabel: String,
    installedFormatter: (String) -> String,
    notInstalledText: String,
    accent: Color,
    testTagPrefix: String,
    onPick: () -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIconBadge(Icons.Rounded.DeveloperBoard, accent)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (installedName.isNotBlank()) installedFormatter(installedName) else notInstalledText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (installedName.isNotBlank()) SuccessGreen else WarningAmber
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("${testTagPrefix}_pick")
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .clickable(onClick = onPick)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(pickLabel, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("${testTagPrefix}_download")
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                    .clickable(onClick = onDownload)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(downloadLabel, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
            }
        }
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
    HorizontalDivider(
        color = BorderSubtle,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = bgColor,
            modifier = Modifier.size(19.dp)
        )
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (valueLabel.isNotEmpty()) {
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
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
