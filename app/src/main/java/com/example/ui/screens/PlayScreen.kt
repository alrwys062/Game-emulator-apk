package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.GameEntity
import com.example.data.local.SaveStateEntity
import com.example.data.preferences.RetroSettings
import com.example.model.RetroSystem
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayScreen(
    game: GameEntity,
    settings: RetroSettings,
    saveStates: List<SaveStateEntity>,
    onBack: () -> Unit,
    onSaveState: (slotIndex: Int, stateJson: String, screenshotBase64: String?) -> Unit,
    onCaptureScreenshot: (screenshotBase64: String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val sys = RetroSystem.fromId(game.systemId)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentFps by remember { mutableIntStateOf(60) }
    var fastForward by remember { mutableStateOf(false) }
    var drawerExpanded by remember { mutableStateOf(true) }
    var showPspDialog by remember(game.id) {
        mutableStateOf(sys == RetroSystem.PSP && settings.pspWarningEnabled)
    }
    var selectedSlotForAction by remember { mutableStateOf<SaveStateEntity?>(null) }

    // Draggable control group offsets (used when moveControlsEnabled is true)
    var dpadOffsetX by remember { mutableFloatStateOf(0f) }
    var dpadOffsetY by remember { mutableFloatStateOf(0f) }
    var abxyOffsetX by remember { mutableFloatStateOf(0f) }
    var abxyOffsetY by remember { mutableFloatStateOf(0f) }

    BackHandler(onBack = onBack)

    // Sync runtime settings to WebView whenever changed
    LaunchedEffect(
        webViewRef,
        settings.filterMode,
        settings.scanlinesEnabled,
        settings.aspectRatio,
        settings.soundVolume
    ) {
        webViewRef?.evaluateJavascript(
            "window.RetroConfig && window.RetroConfig.applySettings('${settings.filterMode}', ${settings.scanlinesEnabled}, '${settings.aspectRatio}', ${settings.soundVolume});",
            null
        )
    }

    fun sendButton(name: String, pressed: Boolean) {
        webViewRef?.evaluateJavascript(
            "window.RetroInput && window.RetroInput.setKey('$name', $pressed);",
            null
        )
    }

    fun triggerSaveToSlot(slotIndex: Int) {
        val wv = webViewRef ?: return
        wv.evaluateJavascript("window.RetroConfig && window.RetroConfig.exportState()") { stateRaw ->
            val cleanState = stateRaw?.trim('"')
                ?.replace("\\\"", "\"")
                ?.replace("\\\\", "\\")
                ?: "{}"
            wv.evaluateJavascript("window.RetroConfig && window.RetroConfig.captureScreenshot()") { shotRaw ->
                val cleanShot = shotRaw?.trim('"')
                onSaveState(slotIndex, cleanState, cleanShot)
            }
        }
    }

    fun triggerLoadFromSlot(slot: SaveStateEntity) {
        val escaped = slot.stateDataJson
            .replace("\\", "\\\\")
            .replace("'", "\\'")
        webViewRef?.evaluateJavascript(
            "window.RetroConfig && window.RetroConfig.importState('$escaped');"
        ) {
            onShowMessage(strings.stateLoadedMsg(slot.slotIndex))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val mapped = when (native.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> "up"
                    KeyEvent.KEYCODE_DPAD_DOWN -> "down"
                    KeyEvent.KEYCODE_DPAD_LEFT -> "left"
                    KeyEvent.KEYCODE_DPAD_RIGHT -> "right"
                    KeyEvent.KEYCODE_BUTTON_A -> "a"
                    KeyEvent.KEYCODE_BUTTON_B -> "b"
                    KeyEvent.KEYCODE_BUTTON_X -> "x"
                    KeyEvent.KEYCODE_BUTTON_Y -> "y"
                    KeyEvent.KEYCODE_BUTTON_L1 -> "l1"
                    KeyEvent.KEYCODE_BUTTON_R1 -> "r1"
                    KeyEvent.KEYCODE_BUTTON_START -> "start"
                    KeyEvent.KEYCODE_BUTTON_SELECT -> "select"
                    else -> null
                }
                if (mapped != null) {
                    sendButton(mapped, native.action == KeyEvent.ACTION_DOWN)
                    true
                } else {
                    false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Emulator Screen Viewport
        Box(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF04060A))
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        this.settings.javaScriptEnabled = true
                        this.settings.domStorageEnabled = true
                        this.settings.allowFileAccess = true
                        this.settings.mediaPlaybackRequiresUserGesture = false

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onFpsUpdate(fps: Int) {
                                post { currentFps = fps }
                            }
                        }, "RetroHost")

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val u = request?.url?.toString() ?: return null
                                if (u.startsWith("https://retroverse.local/rom/")) {
                                    val filePath = game.localFilePath ?: return null
                                    val f = File(filePath)
                                    if (!f.exists()) return null
                                    return WebResourceResponse(
                                        "application/octet-stream",
                                        null,
                                        200,
                                        "OK",
                                        mapOf(
                                            "Access-Control-Allow-Origin" to "*",
                                            "Access-Control-Allow-Methods" to "GET, OPTIONS"
                                        ),
                                        FileInputStream(f)
                                    )
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                val safeTitle = game.title.replace("'", "")
                                val core = if (settings.selectedCore == "auto") game.coreOverride else settings.selectedCore
                                if (!game.localFilePath.isNullOrBlank() && File(game.localFilePath).exists()) {
                                    val romUrl = "https://retroverse.local/rom/${game.id}/${game.fileName}"
                                    view?.evaluateJavascript(
                                        "window.RetroEngine && window.RetroEngine.bootRom('$romUrl', '$core', '$safeTitle');",
                                        null
                                    )
                                } else {
                                    view?.evaluateJavascript(
                                        "window.RetroEngine && window.RetroEngine.bootHomebrew('$safeTitle', '${sys.shortName}', '${game.cartridgeMode}');",
                                        null
                                    )
                                }
                                view?.evaluateJavascript(
                                    "window.RetroConfig && window.RetroConfig.applySettings('${settings.filterMode}', ${settings.scanlinesEnabled}, '${settings.aspectRatio}', ${settings.soundVolume});",
                                    null
                                )
                            }
                        }
                        loadUrl("file:///android_asset/emulator_host.html")
                    }
                }
            )

            // Top HUD Overlay: Back button & FPS badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("play_back_button")
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, BorderGlass, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = strings.close,
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.58f))
                            .border(1.dp, BorderGlass, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${sys.shortName} $currentFps",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SecondaryCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.58f))
                            .border(1.dp, BorderGlass, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AspectRatio,
                            contentDescription = settings.aspectRatio,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Virtual Touch Controller Deck (Always LTR physically so D-Pad is left and ABXY is right!)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(settings.controlsOpacity)
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Shoulder buttons L1 / R1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShoulderButton("L1", onPressChange = { sendButton("l1", it) })
                    ShoulderButton("R1", onPressChange = { sendButton("r1", it) })
                }

                // D-Pad + ABXY Cluster
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .scale(settings.controlsScale),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // D-Pad
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dpadOffsetX.roundToInt(), dpadOffsetY.roundToInt()) }
                            .then(
                                if (settings.moveControlsEnabled) {
                                    Modifier.pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            dpadOffsetX += dragAmount.x
                                            dpadOffsetY += dragAmount.y
                                        }
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        DpadCluster(onDirection = { dir, down -> sendButton(dir, down) })
                    }

                    // Y X B A Diamond
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(abxyOffsetX.roundToInt(), abxyOffsetY.roundToInt()) }
                            .then(
                                if (settings.moveControlsEnabled) {
                                    Modifier.pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            abxyOffsetX += dragAmount.x
                                            abxyOffsetY += dragAmount.y
                                        }
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AbxyCluster(onButton = { btn, down -> sendButton(btn, down) })
                    }
                }

                // Select & Start Center Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallPillButton("SELECT") { sendButton("select", it) }
                    Spacer(Modifier.width(18.dp))
                    SmallPillButton("START") { sendButton("start", it) }
                }
            }
        }

        // Bottom Expandable Save State & Quick Actions Drawer
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(SurfaceDark)
                .border(
                    1.dp,
                    BorderSubtle,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Drag Handle + Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { drawerExpanded = !drawerExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.playSaveSlots,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Icon(
                    imageVector = if (drawerExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                    contentDescription = strings.playSaveSlots,
                    tint = TextSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            // 4 Quick Actions Row (Save, Rewind, Fast Forward, Screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Rounded.Save,
                    label = strings.actionSave,
                    active = false,
                    modifier = Modifier.weight(1f),
                    onClick = { triggerSaveToSlot(1) }
                )
                QuickActionTile(
                    icon = Icons.Rounded.FastRewind,
                    label = strings.actionRewind,
                    active = false,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        webViewRef?.evaluateJavascript(
                            "window.RetroConfig && window.RetroConfig.rewindStep();"
                        ) {
                            onShowMessage(strings.rewindSuccessMsg)
                        }
                    }
                )
                QuickActionTile(
                    icon = Icons.Rounded.FastForward,
                    label = strings.actionFastForward,
                    active = fastForward,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        fastForward = !fastForward
                        val mult = if (fastForward) 2 else 1
                        webViewRef?.evaluateJavascript(
                            "window.RetroConfig && window.RetroConfig.setSpeed($mult);",
                            null
                        )
                    }
                )
                QuickActionTile(
                    icon = Icons.Rounded.CameraAlt,
                    label = strings.actionScreenshot,
                    active = false,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.captureScreenshot()") { raw ->
                            val clean = raw?.trim('"')
                            if (!clean.isNullOrBlank()) {
                                onCaptureScreenshot(clean)
                            }
                        }
                    }
                )
            }

            // 5 Save Slots List
            AnimatedVisibility(visible = drawerExpanded) {
                val slotMap = remember(saveStates) { saveStates.associateBy { it.slotIndex } }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..5).toList()) { slotIdx ->
                        val existing = slotMap[slotIdx]
                        SaveSlotRow(
                            slotIndex = slotIdx,
                            saveState = existing,
                            slotLabel = strings.slotLabel(slotIdx),
                            emptyLabel = strings.slotEmpty,
                            hourShort = strings.hourShort,
                            minuteShort = strings.minuteShort,
                            onClick = {
                                if (existing == null) {
                                    triggerSaveToSlot(slotIdx)
                                } else {
                                    selectedSlotForAction = existing
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    selectedSlotForAction?.let { slot ->
        AlertDialog(
            onDismissRequest = { selectedSlotForAction = null },
            containerColor = SurfaceDark,
            title = { Text(strings.slotLabel(slot.slotIndex), color = Color.White) },
            text = { Text(game.title, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    triggerLoadFromSlot(slot)
                    selectedSlotForAction = null
                }) {
                    Text(strings.loadSlot, color = SecondaryCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    triggerSaveToSlot(slot.slotIndex)
                    selectedSlotForAction = null
                }) {
                    Text(strings.overwriteSlot, color = PrimaryPurple)
                }
            }
        )
    }

    if (showPspDialog) {
        AlertDialog(
            onDismissRequest = { showPspDialog = false },
            containerColor = SurfaceDark,
            title = { Text(strings.pspPerfWarningTitle, color = WarningAmber) },
            text = { Text(strings.pspPerfWarningMsg, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showPspDialog = false }) {
                    Text(strings.close, color = SecondaryCyan)
                }
            }
        )
    }
}

@Composable
private fun ShoulderButton(
    label: String,
    onPressChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .width(74.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) PrimaryPurple.copy(alpha = 0.45f) else SurfaceElevated)
            .border(1.dp, if (pressed) SecondaryCyan else BorderSubtle, RoundedCornerShape(12.dp))
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun DpadCluster(
    onDirection: (String, Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DpadKey("▲", "up", onDirection)
        Row(horizontalArrangement = Arrangement.spacedBy(38.dp)) {
            DpadKey("◀", "left", onDirection)
            DpadKey("▶", "right", onDirection)
        }
        DpadKey("▼", "down", onDirection)
    }
}

@Composable
private fun DpadKey(
    symbol: String,
    dir: String,
    onDirection: (String, Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) PrimaryPurple.copy(alpha = 0.55f) else SurfaceElevated)
            .border(1.dp, if (pressed) SecondaryCyan else BorderSubtle, RoundedCornerShape(12.dp))
            .pointerInput(dir) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onDirection(dir, true)
                        tryAwaitRelease()
                        pressed = false
                        onDirection(dir, false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun AbxyCluster(
    onButton: (String, Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ActionCircleButton("Y", "y", SecondaryCyan, onButton)
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            ActionCircleButton("X", "x", PrimaryPurple, onButton)
            ActionCircleButton("B", "b", ErrorRose, onButton)
        }
        ActionCircleButton("A", "a", SuccessGreen, onButton)
    }
}

@Composable
private fun ActionCircleButton(
    label: String,
    btnKey: String,
    accent: Color,
    onButton: (String, Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (pressed) accent.copy(alpha = 0.45f) else accent.copy(alpha = 0.16f))
            .border(1.5.dp, if (pressed) Color.White else accent.copy(alpha = 0.55f), CircleShape)
            .pointerInput(btnKey) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onButton(btnKey, true)
                        tryAwaitRelease()
                        pressed = false
                        onButton(btnKey, false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
    }
}

@Composable
private fun SmallPillButton(
    label: String,
    onPressChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (pressed) SecondaryCyan.copy(alpha = 0.35f) else SurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(50))
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onPressChange(false)
                    }
                )
            }
            .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (active) Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan))
                else Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated))
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) Color.White else TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun SaveSlotRow(
    slotIndex: Int,
    saveState: SaveStateEntity?,
    slotLabel: String,
    emptyLabel: String,
    hourShort: String,
    minuteShort: String,
    onClick: () -> Unit
) {
    val timeText = remember(saveState?.savedAt) {
        saveState?.savedAt?.let {
            SimpleDateFormat("HH:mm · dd/MM", Locale.getDefault()).format(Date(it))
        } ?: emptyLabel
    }
    val bitmap = remember(saveState?.screenshotPath) {
        saveState?.screenshotPath?.let { path ->
            runCatching {
                val f = File(path)
                if (f.exists()) BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() else null
            }.getOrNull()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("save_slot_$slotIndex")
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = slotLabel,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "#$slotIndex",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                val meta = if (saveState != null) {
                    "$timeText · ${formatDuration(saveState.playTimeMinutesAtSave, hourShort, minuteShort)}"
                } else {
                    emptyLabel
                }
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
