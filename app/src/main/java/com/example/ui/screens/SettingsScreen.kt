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
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.TouchApp
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
    onUpdateFilter: (String) -> Unit,
    onUpdateScanlines: (Boolean) -> Unit,
    onUpdateAspectRatio: (String) -> Unit,
    onUpdateSoundVolume: (Float) -> Unit,
    onUpdateControlsOpacity: (Float) -> Unit,
    onUpdateControlsScale: (Float) -> Unit,
    onUpdateMoveControls: (Boolean) -> Unit,
    onUpdateSelectedCore: (String) -> Unit,
    onUpdateCheatsEnabled: (Boolean) -> Unit,
    onUpdateCheatCodes: (String) -> Unit,
    onUpdatePspWarning: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateUserName: (String) -> Unit,
    onReplayOnboarding: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showCheatsDialog by remember { mutableStateOf(false) }
    var draftCheats by remember(settings.cheatCodesText) { mutableStateOf(settings.cheatCodesText) }
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember(settings.userName) { mutableStateOf(settings.userName) }
    var showShortcutsDialog by remember { mutableStateOf(false) }

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

        // Section 1: Graphics (الرسم)
        item {
            SettingsGroupHeader(strings.sectionGraphics)
            SettingsGroupCard {
                val filters = listOf("CRT", "Scanlines", "Sharp")
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
                val aspects = listOf("4:3", "16:9", "Original")
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
            }
        }

        // Section 2: Audio & Controls (الصوت والتحكم)
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

        // Section 3: Engine (المحرك)
        item {
            SettingsGroupHeader(strings.sectionEngine)
            SettingsGroupCard {
                val cores = listOf("auto", "nes", "snes", "gba", "psx", "psp", "n64", "mame2003")
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

        // Section 4: App (التطبيق)
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
            }
        }
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
