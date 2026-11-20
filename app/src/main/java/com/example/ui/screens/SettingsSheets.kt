package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.RetroSettings
import com.example.model.RetroSystem
import com.example.service.AiPerfRecommendation
import com.example.service.DeviceSpecSummary
import com.example.service.GeminiAssistantService
import com.example.service.OfflineCoreBundleState
import com.example.service.OfflineDiagnosticsReport
import com.example.service.OfflinePrepProgress
import com.example.service.RomBundler
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.JetBrainsMonoFontFamily
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
fun BiosManagerDialog(
    settings: RetroSettings,
    onDismiss: () -> Unit,
    onPickBiosFile: (slotKey: String) -> Unit,
    onDownloadBiosUrl: (slotKey: String, url: String) -> Unit,
    onRemoveBios: (slotKey: String) -> Unit,
    onOpenBiosDocs: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    var urlTargetSlot by remember { mutableStateOf<String?>(null) }
    var draftUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.sectionBios, color = Color.White, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onOpenBiosDocs) {
                    Text(strings.biosOpenDocs, color = SecondaryCyan, style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isArabic)
                        "يرفع المستخدم نسخته القانونية الخاصة من ملفات BIOS. تُحفظ محلياً مع حساب بصمة MD5."
                    else
                        "Supply your own legally dumped BIOS files. Stored locally with MD5 verification.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(RetroSystem.biosSpecs, key = { it.key }) { spec ->
                        val entry = settings.getBiosEntry(spec.key)
                        val legacyName = when (spec.key) {
                            "ps1" -> settings.ps1BiosName
                            "ps2" -> settings.ps2BiosName
                            else -> ""
                        }
                        val displayName = entry?.name?.takeIf { it.isNotBlank() } ?: legacyName
                        val isInstalled = displayName.isNotBlank()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isInstalled) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Rounded.DeveloperBoard,
                                        contentDescription = null,
                                        tint = if (isInstalled) SuccessGreen else PrimaryPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isArabic) spec.titleAr else spec.titleEn,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White
                                        )
                                        Text(
                                            text = spec.expectedFiles,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = when {
                                        spec.isComingSoon && !isInstalled -> strings.comingSoon
                                        isInstalled -> if (isArabic) "✓ مثبّت" else "✓ Installed"
                                        spec.isRequired -> if (isArabic) "مطلوب" else "Required"
                                        else -> if (isArabic) "اختياري" else "Optional"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when {
                                        isInstalled -> SuccessGreen
                                        spec.isRequired -> WarningAmber
                                        else -> TextSecondary
                                    }
                                )
                            }

                            if (isInstalled) {
                                val sizeStr = entry?.sizeBytes?.takeIf { it > 0 }?.let { RomBundler.formatFileSize(it) } ?: ""
                                val md5Str = entry?.md5?.takeIf { it.isNotBlank() }?.let { "MD5: ${it.take(12)}..." } ?: ""
                                Text(
                                    text = listOf(displayName, sizeStr, md5Str).filter { it.isNotBlank() }.joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = SuccessGreen
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MiniChipBtn(
                                    icon = Icons.Rounded.FolderOpen,
                                    label = strings.pickBiosFile,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onPickBiosFile(spec.key) }
                                )
                                MiniChipBtn(
                                    icon = Icons.Rounded.CloudDownload,
                                    label = strings.downloadBiosBtn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        draftUrl = ""
                                        urlTargetSlot = spec.key
                                    }
                                )
                                if (isInstalled) {
                                    IconButton(
                                        onClick = { onRemoveBios(spec.key) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = ErrorRose, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) }
        }
    )

    urlTargetSlot?.let { slot ->
        AlertDialog(
            onDismissRequest = { urlTargetSlot = null },
            containerColor = SurfaceDark,
            title = { Text("${strings.downloadBiosUrlTitle} (${slot.uppercase()})", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = draftUrl,
                    onValueChange = { draftUrl = it },
                    placeholder = { Text("https://.../bios.bin", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = draftUrl.isNotBlank(),
                    onClick = {
                        onDownloadBiosUrl(slot, draftUrl)
                        urlTargetSlot = null
                    }
                ) { Text(strings.downloadBiosBtn, color = SecondaryCyan) }
            },
            dismissButton = {
                TextButton(onClick = { urlTargetSlot = null }) { Text(strings.cancelAction, color = TextSecondary) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerformanceEngineDialog(
    settings: RetroSettings,
    deviceSpec: DeviceSpecSummary,
    onDismiss: () -> Unit,
    onApplyPreset: (String) -> Unit,
    onUpdateFrameSkip: (Int) -> Unit,
    onUpdateResolutionScale: (Int) -> Unit,
    onUpdateRenderScalePercent: (Int) -> Unit,
    onUpdateFpsCap: (Int) -> Unit,
    onUpdateThreadsMode: (String) -> Unit,
    onUpdateSmartPerf: (Boolean) -> Unit,
    onUpdatePreloadRoms: (Boolean) -> Unit,
    onUpdateCdnMode: (String) -> Unit,
    onUpdateCustomDataPath: (String) -> Unit,
    onRunEngineTest: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    var draftCustomPath by remember(settings.customDataPath) { mutableStateOf(settings.customDataPath) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(strings.sectionEngine, color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Device spec / Weak device warning
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (deviceSpec.isLowEndDevice) WarningAmber.copy(alpha = 0.16f) else SurfaceElevated)
                        .border(
                            1.dp,
                            if (deviceSpec.isLowEndDevice) WarningAmber.copy(alpha = 0.45f) else BorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (deviceSpec.isLowEndDevice) Icons.Rounded.Warning else Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = if (deviceSpec.isLowEndDevice) WarningAmber else SecondaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) {
                            "${deviceSpec.modelName} • ${deviceSpec.cpuCores} أنوية • ${deviceSpec.totalRamMb} MB RAM" +
                                if (deviceSpec.isLowEndDevice) "\nملاحظة: يُنصح بوضع توفير البطارية أو دقة 75% لألعاب PSP/N64" else ""
                        } else {
                            "${deviceSpec.modelName} • ${deviceSpec.cpuCores} Cores • ${deviceSpec.totalRamMb} MB RAM" +
                                if (deviceSpec.isLowEndDevice) "\nTip: Use Battery Saver or 75% render scale for PSP/N64" else ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }

                // Performance Presets
                Text(if (isArabic) "أوضاع الأداء السريعة:" else "Performance Presets:", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "saver" to (if (isArabic) "توفير البطارية" else "Battery Saver"),
                        "balanced" to (if (isArabic) "متوازن" else "Balanced"),
                        "performance" to (if (isArabic) "أقصى سرعة" else "Max Speed")
                    ).forEach { (id, label) ->
                        SelectPill(label, settings.performancePreset == id) { onApplyPreset(id) }
                    }
                }

                // Frame skip
                Text(
                    "${strings.settingFrameSkip}: ${settings.frameSkip}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..5).forEach { skip ->
                        SelectPill(skip.toString(), settings.frameSkip == skip) { onUpdateFrameSkip(skip) }
                    }
                }

                // Resolution Scale
                Text(
                    "${strings.settingResolutionScale}: ${settings.resolutionScale}x",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { sc ->
                        SelectPill("${sc}x", settings.resolutionScale == sc) { onUpdateResolutionScale(sc) }
                    }
                }

                // Render scale %
                Text(
                    if (isArabic) "دقة العرض الكلية (Render Scale): ${settings.renderScalePercent}%"
                    else "Viewport Render Scale: ${settings.renderScalePercent}%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(50, 75, 100).forEach { pct ->
                        SelectPill("$pct%", settings.renderScalePercent == pct) { onUpdateRenderScalePercent(pct) }
                    }
                }

                // FPS Cap & Threads
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isArabic) "سقف FPS" else "FPS Cap", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SelectPill("30", settings.fpsCap == 30) { onUpdateFpsCap(30) }
                            SelectPill("60", settings.fpsCap == 60) { onUpdateFpsCap(60) }
                        }
                    }
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(strings.settingWasmThreads, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("auto", "on", "off").forEach { m ->
                                SelectPill(m.uppercase(), settings.wasmThreadsMode == m) { onUpdateThreadsMode(m) }
                            }
                        }
                    }
                }

                // Smart auto performance & Preload
                CompactSwitchRow(
                    title = if (isArabic) "ضبط ذكي تلقائي للأداء" else "Smart Auto-Performance",
                    checked = settings.smartPerfEnabled,
                    onCheckedChange = onUpdateSmartPerf
                )
                CompactSwitchRow(
                    title = if (isArabic) "تخزين/تحضير الـ ROM مسبقاً" else "Preload ROM Buffer",
                    checked = settings.preloadRomsEnabled,
                    onCheckedChange = onUpdatePreloadRoms
                )

                // CDN Selector + Custom Data Path
                Text(if (isArabic) "مصدر نواة EmulatorJS (CDN):" else "EmulatorJS Core CDN:", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SelectPill(if (isArabic) "الرسمي" else "Official", settings.cdnMode == "official") { onUpdateCdnMode("official") }
                    SelectPill("jsDelivr", settings.cdnMode == "jsdelivr") { onUpdateCdnMode("jsdelivr") }
                    SelectPill(if (isArabic) "مخصص" else "Custom", settings.cdnMode == "custom") { onUpdateCdnMode("custom") }
                }

                if (settings.cdnMode == "custom") {
                    OutlinedTextField(
                        value = draftCustomPath,
                        onValueChange = {
                            draftCustomPath = it
                            onUpdateCustomDataPath(it)
                        },
                        label = { Text(if (isArabic) "مسار بيانات مخصص (ينتهي بـ /)" else "Custom data/ URL (ends with /)") },
                        placeholder = { Text("https://user.github.io/repo/data/", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Engine Test Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                        .clickable {
                            onDismiss()
                            onRunEngineTest()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.engineTestBtn, color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GamepadMappingDialog(
    settings: RetroSettings,
    connectedGamepadName: String?,
    connectedGamepadNames: List<String>,
    onDismiss: () -> Unit,
    onUpdateAutoHideTouch: (Boolean) -> Unit,
    onUpdateDeadzone: (Float) -> Unit,
    onSaveKeyBinding: (playerIndex: Int, actionId: String, keyCode: Int) -> Unit,
    onResetKeyBindings: (playerIndex: Int) -> Unit,
    onSaveGamepadProfileBinding: (gamepadId: String, actionId: String, keyCode: Int) -> Unit,
    onResetGamepadProfileBindings: (gamepadId: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    var selectedPlayer by remember { mutableIntStateOf(0) }
    var useDeviceProfile by remember(connectedGamepadName) { mutableStateOf(connectedGamepadName != null) }
    var capturingAction by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val activeProfileId = connectedGamepadName.orEmpty()
    val bindings = if (useDeviceProfile && activeProfileId.isNotBlank()) {
        settings.getGamepadProfileBindings(activeProfileId)
    } else {
        settings.getKeyBindings(selectedPlayer)
    }

    LaunchedEffect(capturingAction) {
        if (capturingAction != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    val actions = listOf(
        "UP" to "D-Pad ↑",
        "DOWN" to "D-Pad ↓",
        "LEFT" to "D-Pad ←",
        "RIGHT" to "D-Pad →",
        "A" to "A / ○",
        "B" to "B / ✕",
        "X" to "X / △",
        "Y" to "Y / □",
        "L1" to "L1 / L",
        "R1" to "R1 / R",
        "L2" to "L2",
        "R2" to "R2",
        "L3" to "L3",
        "R3" to "R3",
        "START" to "Start",
        "SELECT" to "Select"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(strings.gamepadMappingTitle, color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { ev ->
                        val target = capturingAction ?: return@onPreviewKeyEvent false
                        if (ev.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (useDeviceProfile && activeProfileId.isNotBlank()) {
                                onSaveGamepadProfileBinding(activeProfileId, target, ev.nativeKeyEvent.keyCode)
                            } else {
                                onSaveKeyBinding(selectedPlayer, target, ev.nativeKeyEvent.keyCode)
                            }
                            capturingAction = null
                            true
                        } else false
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (connectedGamepadNames.isNotEmpty()) {
                        if (isArabic) "✓ تم توصيل (${connectedGamepadNames.size}): ${connectedGamepadNames.joinToString(", ")}"
                        else "✓ Connected (${connectedGamepadNames.size}): ${connectedGamepadNames.joinToString(", ")}"
                    } else {
                        if (isArabic) "اضغط أي زر في اليد لتفعيلها (يدعم أيضاً لوحة المفاتيح HID حتى 4 لاعبين)"
                        else "Press any button on gamepad to activate (4-player & HID keyboard supported)"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (connectedGamepadNames.isNotEmpty()) SuccessGreen else WarningAmber
                )

                CompactSwitchRow(
                    title = if (isArabic) "إخفاء أزرار اللمس تلقائياً عند توصيل يد" else "Auto-hide touch controls on gamepad",
                    checked = settings.autoHideTouchOnGamepad,
                    onCheckedChange = onUpdateAutoHideTouch
                )

                Text(
                    text = if (isArabic) "المنطقة الميتة للعصا (Deadzone): ${(settings.gamepadDeadzone * 100).toInt()}%"
                    else "Analog Deadzone: ${(settings.gamepadDeadzone * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Slider(
                    value = settings.gamepadDeadzone,
                    onValueChange = onUpdateDeadzone,
                    valueRange = 0.05f..0.45f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryCyan)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (activeProfileId.isNotBlank()) {
                        SelectPill(
                            label = if (isArabic) "ملف اليد: ${activeProfileId.take(14)}" else "Device: ${activeProfileId.take(14)}",
                            selected = useDeviceProfile
                        ) {
                            useDeviceProfile = true
                            capturingAction = null
                        }
                    }
                    (0..3).forEach { idx ->
                        SelectPill("P${idx + 1}", !useDeviceProfile && selectedPlayer == idx) {
                            useDeviceProfile = false
                            selectedPlayer = idx
                            capturingAction = null
                        }
                    }
                }

                if (capturingAction != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryPurple.copy(alpha = 0.25f))
                            .border(1.dp, SecondaryCyan, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "اضغط الآن أي زر على يد التحكم أو الكيبورد لربطه بـ $capturingAction..."
                            else "Press any gamepad or keyboard button now to bind $capturingAction...",
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryCyan
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(actions) { (id, label) ->
                        val mappedCode = bindings[id]
                        val mappedLabel = mappedCode?.let { KeyEvent.keyCodeToString(it).removePrefix("KEYCODE_") }
                            ?: (if (isArabic) "افتراضي" else "Default")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .clickable { capturingAction = id }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = if (capturingAction == id) "..." else mappedLabel,
                                color = if (capturingAction == id) WarningAmber else SecondaryCyan,
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (useDeviceProfile && activeProfileId.isNotBlank()) {
                    onResetGamepadProfileBindings(activeProfileId)
                } else {
                    onResetKeyBindings(selectedPlayer)
                }
            }) {
                Text(strings.resetLayout, color = ErrorRose)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiAiDialog(
    settings: RetroSettings,
    aiBusy: Boolean,
    aiPerfRec: AiPerfRecommendation?,
    aiChatMessages: List<AiChatMessage>,
    onDismiss: () -> Unit,
    onSaveGeminiConfig: (apiKey: String, model: String) -> Unit,
    onRequestPerfRec: () -> Unit,
    onApplyPerfRec: (AiPerfRecommendation) -> Unit,
    onSendChat: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    var draftKey by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var draftModel by remember(settings.geminiModel) { mutableStateOf(settings.geminiModel) }
    var revealKey by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = SecondaryCyan)
                Spacer(Modifier.width(8.dp))
                Text(strings.aiAssistantTitle, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = draftKey,
                    onValueChange = { draftKey = it },
                    label = { Text(if (isArabic) "مفتاح Gemini API (يُحفظ محلياً فقط)" else "Gemini API Key (saved locally only)") },
                    visualTransformation = if (revealKey) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { revealKey = !revealKey }) {
                            Text(if (revealKey) (if (isArabic) "إخفاء" else "Hide") else (if (isArabic) "إظهار" else "Show"), fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GeminiAssistantService.supportedModels.forEach { m ->
                        SelectPill(m, draftModel == m) { draftModel = m }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniChipBtn(
                        icon = Icons.Rounded.AutoAwesome,
                        label = strings.saveAction,
                        modifier = Modifier.weight(1f),
                        onClick = { onSaveGeminiConfig(draftKey, draftModel) }
                    )
                    MiniChipBtn(
                        icon = Icons.Rounded.Speed,
                        label = if (isArabic) "توصية الأداء" else "Perf Advice",
                        modifier = Modifier.weight(1f),
                        onClick = onRequestPerfRec
                    )
                }

                if (aiPerfRec != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SecondaryCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(aiPerfRec.summaryText, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { onApplyPerfRec(aiPerfRec) }) {
                            Text(
                                text = if (isArabic) "✓ تطبيق الإعدادات المقترحة بضغطة واحدة" else "✓ Apply Recommended Settings",
                                color = SuccessGreen
                            )
                        }
                    }
                }

                // AI Chat feed
                if (aiChatMessages.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0C12))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        aiChatMessages.takeLast(6).forEach { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (msg.isUser) PrimaryPurple.copy(alpha = 0.25f) else SurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = (if (msg.isUser) "👤 " else "🤖 ") + msg.text,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text(if (isArabic) "اسأل عن لعبة، كود غش، أو BIOS..." else "Ask about games, cheats, or BIOS...", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (aiBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = SecondaryCyan)
                    } else {
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    onSendChat(chatInput)
                                    chatInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = SecondaryCyan)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) } }
    )
}

@Composable
fun DiagnosticLogDialog(
    logs: String,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    val textToShow = logs.ifBlank {
        if (isArabic) "لا توجد سجلات تشخيص مسجّلة حتى الآن." else "No diagnostic logs captured yet."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(if (isArabic) "سجل التشخيص والأعطال" else "Diagnostic & Error Logs", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A0C12))
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    Text(
                        text = textToShow,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniChipBtn(
                        icon = Icons.Rounded.ContentCopy,
                        label = strings.copyLogBtn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("RetroVerseLogs", textToShow))
                            onShowMessage(if (isArabic) "تم نسخ السجل" else "Log copied")
                        }
                    )
                    MiniChipBtn(
                        icon = Icons.Rounded.Share,
                        label = if (isArabic) "مشاركة" else "Share",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textToShow)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "RetroVerse Logs"))
                        }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) } }
    )
}

@Composable
private fun MiniChipBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun SelectPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) PrimaryPurple else SurfaceElevated)
            .border(1.dp, if (selected) SecondaryCyan else BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CompactSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SecondaryCyan)
        )
    }
}

@Composable
fun OfflineBundlesDialog(
    isOnline: Boolean,
    bundles: List<OfflineCoreBundleState>,
    prepProgress: OfflinePrepProgress?,
    onDismiss: () -> Unit,
    onPrepareSystem: (RetroSystem) -> Unit,
    onPrepareAll: () -> Unit,
    onCancelPrep: () -> Unit,
    onDeleteBundle: (systemId: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    val readyCount = bundles.count { it.isReady }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) SuccessGreen else WarningAmber
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "الحزمة الأوفلاين ($readyCount/${bundles.size})"
                        else "Offline Bundles ($readyCount/${bundles.size})",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isArabic) {
                        "حمّل نواة كل نظام مرة واحدة بالإنترنت لتشغيل جميع ألعابه لاحقاً أوفلاين بالكامل (تُطابق إعداد Threads الفعلي)."
                    } else {
                        "Pre-download each system core once online to run all its games 100% offline (uses exact gameplay Threads config)."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                if (prepProgress != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SecondaryCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (prepProgress.isBulk) {
                                    "${prepProgress.statusLabel} (${prepProgress.bulkIndex}/${prepProgress.bulkTotal})"
                                } else prepProgress.statusLabel,
                                color = SecondaryCyan,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onCancelPrep) {
                                Text(strings.cancelAction, color = ErrorRose, fontSize = 11.sp)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { prepProgress.percent / 100f },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = SecondaryCyan,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                            .clickable(enabled = isOnline, onClick = onPrepareAll)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (!isOnline) {
                                if (isArabic) "تجهيز الكل (يحتاج إنترنت)" else "Prepare All (Needs Internet)"
                            } else {
                                if (isArabic) "تجهيز الكل للعمل بدون إنترنت" else "Prepare All Systems Offline"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(bundles, key = { it.systemId }) { item ->
                        val sys = RetroSystem.fromId(item.systemId)
                        val sizeText = if (item.isReady && item.cachedBytes > 0L) {
                            RomBundler.formatFileSize(item.cachedBytes)
                        } else item.approxSizeLabel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (item.isReady) SuccessGreen.copy(alpha = 0.45f) else BorderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.shortName} • ${item.coreId}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (item.isReady) {
                                        (if (isArabic) "جاهز للأوفلاين ✓" else "Ready offline ✓") + " • $sizeText"
                                    } else {
                                        (if (isArabic) "غير محمّل" else "Not downloaded") + " • $sizeText"
                                    },
                                    color = if (item.isReady) SuccessGreen else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            if (item.isReady) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    IconButton(
                                        onClick = { onDeleteBundle(item.systemId) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = if (isArabic) "حذف الحزمة" else "Delete Bundle",
                                            tint = ErrorRose,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                MiniChipBtn(
                                    icon = Icons.Rounded.CloudDownload,
                                    label = if (isArabic) "تجهيز للأوفلاين" else "Prepare",
                                    onClick = {
                                        if (prepProgress == null) onPrepareSystem(sys)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) }
        }
    )
}

@Composable
fun OfflineDiagnosticsDialog(
    report: OfflineDiagnosticsReport?,
    onRefresh: () -> Unit,
    onClearOfflineCache: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = if (isArabic) "تشخيص الأوفلاين والأداء" else "Offline & Hardware Diagnostics",
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (report == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = SecondaryCyan)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isArabic) "جاري الفحص..." else "Checking...", color = TextSecondary)
                    }
                } else {
                    DiagKvRow(
                        label = if (isArabic) "حالة الشبكة" else "Network",
                        value = if (report.isOnline) (if (isArabic) "متصل بالإنترنت" else "Online") else (if (isArabic) "وضع الأوفلاين" else "Offline"),
                        good = report.isOnline
                    )
                    DiagKvRow(
                        label = "Service Worker",
                        value = report.swStatus,
                        good = report.swStatus == "controlled" || report.swStatus == "active_uncontrolled"
                    )
                    DiagKvRow(
                        label = "crossOriginIsolated / SAB",
                        value = if (report.isolated && report.sharedArrayBuffer) "✓ Active (PSP Threads OK)" else "✗ Inactive",
                        good = report.isolated && report.sharedArrayBuffer
                    )
                    val totalCacheStr = RomBundler.formatFileSize(report.androidCacheBytes + report.webStorageUsageBytes)
                    val quotaStr = if (report.webStorageQuotaBytes > 0) RomBundler.formatFileSize(report.webStorageQuotaBytes) else "N/A"
                    DiagKvRow(
                        label = if (isArabic) "حجم الكاش / الحصة" else "Cache Size / Quota",
                        value = "$totalCacheStr / $quotaStr (${report.webCacheEntries} web)",
                        good = (report.androidCacheBytes + report.webStorageUsageBytes) > 0L
                    )
                    DiagKvRow(
                        label = if (isArabic) "الأنظمة الجاهزة أوفلاين (${report.readySystems.size})" else "Prepared Systems (${report.readySystems.size})",
                        value = if (report.readySystems.isEmpty()) (if (isArabic) "لا يوجد بعد" else "None yet") else report.readySystems.joinToString(", "),
                        good = report.readySystems.isNotEmpty()
                    )
                    val allPads = (report.androidGamepads + report.webGamepads).distinct()
                    DiagKvRow(
                        label = if (isArabic) "أيدي التحكم المتصلة (${allPads.size})" else "Connected Gamepads (${allPads.size})",
                        value = if (allPads.isEmpty()) (if (isArabic) "0 — اضغط أي زر في اليد لتفعيلها" else "0 — Press any controller button") else allPads.joinToString(", "),
                        good = allPads.isNotEmpty()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniChipBtn(
                        icon = Icons.Rounded.Refresh,
                        label = if (isArabic) "تحديث الفحص" else "Re-check",
                        modifier = Modifier.weight(1f),
                        onClick = onRefresh
                    )
                    MiniChipBtn(
                        icon = Icons.Rounded.DeleteOutline,
                        label = if (isArabic) "مسح كاش الأوفلاين" else "Clear Offline Cache",
                        modifier = Modifier.weight(1f),
                        onClick = onClearOfflineCache
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close, color = SecondaryCyan) }
        }
    )
}

@Composable
private fun DiagKvRow(label: String, value: String, good: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(1.dp, if (good) SuccessGreen.copy(alpha = 0.35f) else BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color = if (good) SuccessGreen else Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily)
        )
    }
}

