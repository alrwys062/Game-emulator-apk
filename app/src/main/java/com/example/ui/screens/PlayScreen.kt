package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.local.CheatEntity
import com.example.data.local.GameEntity
import com.example.data.local.SaveStateEntity
import com.example.data.preferences.ControlButtonPlacement
import com.example.data.preferences.RetroSettings
import com.example.model.RetroSystem
import com.example.ui.components.CheatManagerDialog
import com.example.ui.components.EjsButtonIndex
import com.example.ui.components.PlayDiagnosticsOverlay
import com.example.ui.components.TouchControllerDeck
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun buildLocalFileResponse(
    file: File,
    method: String,
    rangeHeader: String?
): WebResourceResponse {
    val totalLength = file.length()
    val corsHeaders = mutableMapOf(
        "Access-Control-Allow-Origin" to "*",
        "Access-Control-Allow-Methods" to "GET, HEAD, OPTIONS",
        "Access-Control-Allow-Headers" to "*",
        "Cross-Origin-Resource-Policy" to "cross-origin",
        "Cross-Origin-Opener-Policy" to "same-origin",
        "Cross-Origin-Embedder-Policy" to "credentialless",
        "Accept-Ranges" to "bytes",
        "Cache-Control" to "no-store"
    )

    if (method.equals("HEAD", ignoreCase = true)) {
        corsHeaders["Content-Length"] = totalLength.toString()
        return WebResourceResponse("application/octet-stream", null, 200, "OK", corsHeaders, ByteArrayInputStream(ByteArray(0)))
    }

    if (!rangeHeader.isNullOrBlank() && rangeHeader.startsWith("bytes=")) {
        val spec = rangeHeader.removePrefix("bytes=").substringBefore(",")
        val parts = spec.split("-")
        val start = parts.getOrNull(0)?.toLongOrNull() ?: 0L
        val end = parts.getOrNull(1)?.toLongOrNull()?.coerceAtMost(totalLength - 1) ?: (totalLength - 1)
        val len = (end - start + 1).coerceAtLeast(0L)
        val raf = RandomAccessFile(file, "r")
        raf.seek(start)
        val boundedStream = object : java.io.InputStream() {
            var remaining = len
            override fun read(): Int {
                if (remaining <= 0) return -1
                val b = raf.read()
                if (b != -1) remaining--
                return b
            }

            override fun read(b: ByteArray, off: Int, l: Int): Int {
                if (remaining <= 0) return -1
                val toRead = minOf(l.toLong(), remaining).toInt()
                val n = raf.read(b, off, toRead)
                if (n > 0) remaining -= n
                return n
            }

            override fun close() {
                raf.close()
            }
        }
        corsHeaders["Content-Range"] = "bytes $start-$end/$totalLength"
        corsHeaders["Content-Length"] = len.toString()
        return WebResourceResponse("application/octet-stream", null, 206, "Partial Content", corsHeaders, boundedStream)
    }

    corsHeaders["Content-Length"] = totalLength.toString()
    return WebResourceResponse("application/octet-stream", null, 200, "OK", corsHeaders, file.inputStream())
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayScreen(
    game: GameEntity,
    isEngineTestMode: Boolean,
    settings: RetroSettings,
    saveStates: List<SaveStateEntity>,
    cheats: List<CheatEntity>,
    connectedGamepadName: String?,
    aiBusy: Boolean,
    aiDiagnosticResult: String?,
    onBack: () -> Unit,
    onSaveState: (slotIndex: Int, stateJson: String, screenshotBase64: String?) -> Unit,
    onCaptureScreenshot: (screenshotBase64: String) -> Unit,
    onShowMessage: (String) -> Unit,
    onUpdateAspectRatio: (String) -> Unit,
    onUpdateFilterMode: (String) -> Unit,
    onUpdateOrientationLock: (String) -> Unit,
    onUpdateCdnMode: (String) -> Unit,
    onSaveControlPlacement: (systemProfile: String, isLandscape: Boolean, controlId: String, placement: ControlButtonPlacement) -> Unit,
    onResetControlLayout: (systemProfile: String, isLandscape: Boolean) -> Unit,
    onAddCheat: (name: String, code: String, cheatType: String) -> Unit,
    onToggleCheat: (cheatId: Long, enabled: Boolean) -> Unit,
    onDeleteCheat: (cheatId: Long) -> Unit,
    onImportCheatFile: () -> Unit,
    onDiagnosticLogsChanged: (String) -> Unit,
    onAskAiDiagnosis: (errorDetail: String, logs: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val sys = RetroSystem.fromId(game.systemId)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentFps by remember { mutableIntStateOf(60) }
    var frameTimeMs by remember { mutableFloatStateOf(16.6f) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }
    var isPaused by remember { mutableStateOf(false) }
    var engineStatus by remember { mutableStateOf("loading") } // loading | running | error
    var engineErrorDetail by remember { mutableStateOf("") }
    var diagnosticLogs by remember { mutableStateOf("") }
    var hudVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var drawerExpanded by remember { mutableStateOf(false) }
    var editLayoutMode by remember { mutableStateOf(false) }
    var showCheatDialog by remember { mutableStateOf(false) }
    var showDiscDialog by remember { mutableStateOf(false) }
    var showQuickSettingsDialog by remember { mutableStateOf(false) }
    var selectedSlotForAction by remember { mutableStateOf<SaveStateEntity?>(null) }

    var activeCdnOverride by remember { mutableStateOf<String?>(null) }
    var forceNoThreads by remember { mutableStateOf(false) }
    var pendingSaveSlot by remember { mutableIntStateOf(-1) }
    var pendingStatePayload by remember { mutableStateOf<String?>(null) }

    val resolvedCore = remember(game, settings.selectedCore) {
        if (settings.selectedCore == "auto") game.coreOverride else settings.selectedCore
    }

    val resolvedBiosFile = remember(sys, settings) {
        val slotKey = sys.biosSlotKey
        val fromMap = slotKey?.let { settings.getBiosEntry(it)?.path }?.takeIf { File(it).exists() }
        fromMap ?: when (sys) {
            RetroSystem.PS1 -> settings.ps1BiosPath.takeIf { it.isNotBlank() && File(it).exists() }
            RetroSystem.PS2 -> settings.ps2BiosPath.takeIf { it.isNotBlank() && File(it).exists() }
            else -> settings.extraBiosPath.takeIf { it.isNotBlank() && File(it).exists() }
        }
    }

    var showBiosWarning by remember(game.id, resolvedBiosFile) {
        mutableStateOf(
            !isEngineTestMode && game.localFilePath != null && sys.requiresBios && resolvedBiosFile == null
        )
    }

    var showPspDialog by remember(game.id) {
        mutableStateOf(!isEngineTestMode && sys == RetroSystem.PSP && settings.pspWarningEnabled)
    }

    // Wake lock & orientation lock management
    val activity = context as? Activity
    DisposableEffect(settings.wakeLockEnabled, settings.orientationLock, isFullscreen) {
        activity?.window?.let { win ->
            if (settings.wakeLockEnabled) win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            val controller = WindowCompat.getInsetsController(win, win.decorView)
            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        activity?.requestedOrientation = when (settings.orientationLock) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose { }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.window?.let { win ->
                WindowCompat.getInsetsController(win, win.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
            runCatching {
                webViewRef?.evaluateJavascript("window.RetroEngine && window.RetroEngine.teardown();", null)
                webViewRef?.destroy()
            }
        }
    }

    fun exitWithOptionalAutoSave() {
        if (settings.autoSaveOnExit && !isEngineTestMode && engineStatus == "running") {
            pendingSaveSlot = 0
            webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.exportStateAsync('auto');", null)
        }
        onBack()
    }

    BackHandler(onBack = ::exitWithOptionalAutoSave)

    fun bootEngineInWebView(view: WebView?, cdnOverride: String? = activeCdnOverride, noThreads: Boolean = forceNoThreads) {
        val wv = view ?: return
        engineStatus = "loading"
        engineErrorDetail = ""
        diagnosticLogs = ""

        if (isEngineTestMode) {
            val effectiveCdn = cdnOverride ?: settings.cdnMode
            val safeCustom = settings.customDataPath.replace("'", "")
            wv.evaluateJavascript(
                "window.RetroEngine && window.RetroEngine.bootEngineTest('$effectiveCdn', '$safeCustom');",
                null
            )
            return
        }

        val localRom = game.localFilePath?.let { File(it) }?.takeIf { it.exists() }
        if (localRom != null) {
            val encodedFile = Uri.encode(game.fileName)
            val romUrl = "https://retroverse.local/rom/${game.id}/$encodedFile"
            val biosUrl = if (resolvedBiosFile != null) {
                "https://retroverse.local/bios/${Uri.encode(File(resolvedBiosFile).name)}"
            } else ""

            val cheatsArr = JSONArray().apply {
                if (settings.cheatsEnabled) {
                    cheats.filter { it.enabled }.forEach { c ->
                        put(JSONArray().apply {
                            put(c.name)
                            put(c.code)
                            put(true)
                        })
                    }
                    if (settings.cheatCodesText.isNotBlank()) {
                        put(JSONArray().apply {
                            put("Global Cheat")
                            put(settings.cheatCodesText)
                            put(true)
                        })
                    }
                }
            }

            val payload = JSONObject().apply {
                put("romUrl", romUrl)
                put("core", resolvedCore)
                put("title", game.title)
                put("fileSizeBytes", localRom.length())
                put("biosUrl", biosUrl)
                put("threadsMode", if (noThreads) "off" else settings.wasmThreadsMode)
                put("rewind", settings.rewindEnabled)
                put("vsync", settings.vsyncEnabled)
                put("frameSkip", settings.frameSkip)
                put("resolutionScale", settings.resolutionScale)
                put("renderScalePercent", settings.renderScalePercent)
                put("ffSpeed", settings.fastForwardSpeed)
                put("cdnMode", settings.cdnMode)
                put("customDataPath", settings.customDataPath)
                put("cdnOverride", cdnOverride ?: "")
                put("cheatsList", cheatsArr)
            }.toString()
            wv.evaluateJavascript("window.RetroEngine && window.RetroEngine.bootRom($payload);", null)
        } else {
            val safeTitle = game.title.replace("'", "")
            wv.evaluateJavascript(
                "window.RetroEngine && window.RetroEngine.bootHomebrew('$safeTitle', '${sys.shortName}', '${game.cartridgeMode}');",
                null
            )
        }
        wv.evaluateJavascript(
            "window.RetroConfig && window.RetroConfig.applySettings('${settings.filterMode}', ${settings.scanlinesEnabled}, '${settings.aspectRatio}', ${settings.soundVolume}, ${settings.audioMuted}, ${settings.renderScalePercent});",
            null
        )
    }

    LaunchedEffect(
        webViewRef,
        settings.filterMode,
        settings.scanlinesEnabled,
        settings.aspectRatio,
        settings.soundVolume,
        settings.audioMuted,
        settings.renderScalePercent
    ) {
        webViewRef?.evaluateJavascript(
            "window.RetroConfig && window.RetroConfig.applySettings('${settings.filterMode}', ${settings.scanlinesEnabled}, '${settings.aspectRatio}', ${settings.soundVolume}, ${settings.audioMuted}, ${settings.renderScalePercent});",
            null
        )
    }

    // Live sync cheats when toggled inside PlayScreen
    LaunchedEffect(cheats, engineStatus) {
        if (engineStatus == "running") {
            val arr = JSONArray().apply {
                cheats.forEach { c ->
                    put(JSONArray().apply {
                        put(c.name)
                        put(c.code)
                        put(c.enabled)
                    })
                }
            }
            webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.applyCheats($arr);", null)
        }
    }

    fun sendButtonIndex(idx: Int, value: Float) {
        webViewRef?.evaluateJavascript(
            "window.RetroInput && window.RetroInput.simulate($idx, $value);",
            null
        )
    }

    fun triggerSaveToSlot(slotIndex: Int) {
        val wv = webViewRef ?: return
        pendingSaveSlot = slotIndex
        wv.evaluateJavascript("window.RetroConfig && window.RetroConfig.exportStateAsync('slot_$slotIndex');", null)
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

    // Keyboard & Physical Gamepad mapping
    val keyBindings = remember(settings.gamepadBindingsJson) { settings.getKeyBindings(0) }
    val actionToIndex = remember {
        mapOf(
            "up" to EjsButtonIndex.UP,
            "down" to EjsButtonIndex.DOWN,
            "left" to EjsButtonIndex.LEFT,
            "right" to EjsButtonIndex.RIGHT,
            "b" to EjsButtonIndex.B_CROSS,
            "a" to EjsButtonIndex.A_CIRCLE,
            "y" to EjsButtonIndex.Y_SQUARE,
            "x" to EjsButtonIndex.X_TRIANGLE,
            "l1" to EjsButtonIndex.L1,
            "r1" to EjsButtonIndex.R1,
            "l2" to EjsButtonIndex.L2,
            "r2" to EjsButtonIndex.R2,
            "l3" to EjsButtonIndex.L3,
            "r3" to EjsButtonIndex.R3,
            "start" to EjsButtonIndex.START,
            "select" to EjsButtonIndex.SELECT
        )
    }

    val hideTouchDeck = settings.autoHideTouchOnGamepad && connectedGamepadName != null && !editLayoutMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .then(if (!isFullscreen) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier)
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val customMapped = keyBindings.entries.firstOrNull { it.value == native.keyCode }?.key
                val ejsIdx = if (customMapped != null) {
                    actionToIndex[customMapped.lowercase()]
                } else {
                    when (native.keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> EjsButtonIndex.UP
                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> EjsButtonIndex.DOWN
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> EjsButtonIndex.LEFT
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> EjsButtonIndex.RIGHT
                        KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_Z -> EjsButtonIndex.B_CROSS
                        KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_X -> EjsButtonIndex.A_CIRCLE
                        KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_C -> EjsButtonIndex.Y_SQUARE
                        KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_V -> EjsButtonIndex.X_TRIANGLE
                        KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_Q -> EjsButtonIndex.L1
                        KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_E -> EjsButtonIndex.R1
                        KeyEvent.KEYCODE_BUTTON_L2 -> EjsButtonIndex.L2
                        KeyEvent.KEYCODE_BUTTON_R2 -> EjsButtonIndex.R2
                        KeyEvent.KEYCODE_BUTTON_THUMBL -> EjsButtonIndex.L3
                        KeyEvent.KEYCODE_BUTTON_THUMBR -> EjsButtonIndex.R3
                        KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_ENTER -> EjsButtonIndex.START
                        KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_SHIFT_RIGHT -> EjsButtonIndex.SELECT
                        else -> null
                    }
                }
                if (ejsIdx != null) {
                    sendButtonIndex(ejsIdx, if (native.action == KeyEvent.ACTION_DOWN) 1f else 0f)
                    true
                } else false
            }
    ) {
        // Shared WebView Viewport composable
        val webViewContent: @Composable (Modifier) -> Unit = { boxMod ->
            Box(
                modifier = boxMod
                    .clip(RoundedCornerShape(if (isLandscape) 14.dp else 22.dp))
                    .background(Color(0xFF04060A))
                    .border(1.dp, BorderGlass, RoundedCornerShape(if (isLandscape) 14.dp else 22.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewRef = this
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            this.settings.javaScriptEnabled = true
                            this.settings.domStorageEnabled = true
                            this.settings.databaseEnabled = true
                            this.settings.allowFileAccess = true
                            this.settings.allowContentAccess = true
                            this.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            this.settings.mediaPlaybackRequiresUserGesture = false

                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onFpsUpdate(fps: Int, ms: Float) {
                                    post {
                                        currentFps = fps
                                        frameTimeMs = ms
                                    }
                                }

                                @JavascriptInterface
                                fun onEngineStatus(status: String, detail: String) {
                                    post {
                                        engineStatus = status
                                        engineErrorDetail = detail
                                    }
                                }

                                @JavascriptInterface
                                fun onDiagnosticLog(level: String, line: String) {
                                    post {
                                        diagnosticLogs = (diagnosticLogs + "\n" + line).trim().takeLast(6000)
                                        onDiagnosticLogsChanged(diagnosticLogs)
                                    }
                                }

                                @JavascriptInterface
                                fun onStateExported(cbName: String, dataJson: String) {
                                    post {
                                        val slot = pendingSaveSlot.takeIf { it >= 0 } ?: 1
                                        pendingStatePayload = dataJson
                                        evaluateJavascript("window.RetroConfig && window.RetroConfig.captureScreenshotAsync('$cbName');", null)
                                        if (dataJson.isNotBlank() && slot == 0) {
                                            onSaveState(0, dataJson, null)
                                        }
                                    }
                                }

                                @JavascriptInterface
                                fun onScreenshotCaptured(cbName: String, dataUrl: String) {
                                    post {
                                        if (cbName == "snap_only") {
                                            if (dataUrl.isNotBlank()) onCaptureScreenshot(dataUrl)
                                        } else {
                                            val slot = pendingSaveSlot.takeIf { it >= 0 } ?: 1
                                            val st = pendingStatePayload ?: "{}"
                                            onSaveState(slot, st, dataUrl.takeIf { it.isNotBlank() })
                                            pendingSaveSlot = -1
                                            pendingStatePayload = null
                                        }
                                    }
                                }
                            }, "RetroHost")

                            webChromeClient = WebChromeClient()

                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val u = request?.url?.toString() ?: return null
                                    val method = request.method ?: "GET"

                                    if (u.startsWith("https://retroverse.local/") && method.equals("OPTIONS", ignoreCase = true)) {
                                        return WebResourceResponse(
                                            "text/plain", "UTF-8", 200, "OK",
                                            mapOf(
                                                "Access-Control-Allow-Origin" to "*",
                                                "Access-Control-Allow-Methods" to "GET, HEAD, OPTIONS",
                                                "Access-Control-Allow-Headers" to "*",
                                                "Cross-Origin-Resource-Policy" to "cross-origin",
                                                "Cross-Origin-Opener-Policy" to "same-origin",
                                                "Cross-Origin-Embedder-Policy" to "credentialless"
                                            ),
                                            ByteArrayInputStream(ByteArray(0))
                                        )
                                    }

                                    if (u.startsWith("https://retroverse.local/coi-serviceworker.js")) {
                                        val coiStream = runCatching { context.assets.open("coi-serviceworker.js") }.getOrElse { ByteArrayInputStream(ByteArray(0)) }
                                        return WebResourceResponse(
                                            "application/javascript", "UTF-8", 200, "OK",
                                            mapOf(
                                                "Cross-Origin-Opener-Policy" to "same-origin",
                                                "Cross-Origin-Embedder-Policy" to "credentialless",
                                                "Access-Control-Allow-Origin" to "*"
                                            ),
                                            coiStream
                                        )
                                    }

                                    if (u.startsWith("https://retroverse.local/emulator_host.html")) {
                                        val htmlStream = context.assets.open("emulator_host.html")
                                        return WebResourceResponse(
                                            "text/html", "UTF-8", 200, "OK",
                                            mapOf(
                                                "Cross-Origin-Opener-Policy" to "same-origin",
                                                "Cross-Origin-Embedder-Policy" to "credentialless",
                                                "Access-Control-Allow-Origin" to "*"
                                            ),
                                            htmlStream
                                        )
                                    }

                                    if (u.startsWith("https://retroverse.local/rom/")) {
                                        val filePath = game.localFilePath ?: return null
                                        val f = File(filePath)
                                        if (!f.exists()) return null
                                        return buildLocalFileResponse(f, method, request.requestHeaders["Range"])
                                    }

                                    if (u.startsWith("https://retroverse.local/bios/")) {
                                        val biosPath = resolvedBiosFile ?: return null
                                        val f = File(biosPath)
                                        if (!f.exists()) return null
                                        return buildLocalFileResponse(f, method, request.requestHeaders["Range"])
                                    }

                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    bootEngineInWebView(view)
                                }
                            }
                            loadUrl("https://retroverse.local/emulator_host.html")
                        }
                    }
                )

                // Loading Overlay
                if (engineStatus == "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xD907080D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SecondaryCyan, strokeWidth = 3.dp)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = strings.engineLoadingText(resolvedCore.uppercase()),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (engineStatus == "error") {
                    PlayDiagnosticsOverlay(
                        errorDetail = engineErrorDetail,
                        diagnosticLogs = diagnosticLogs,
                        aiBusy = aiBusy,
                        aiResult = aiDiagnosticResult,
                        onRetry = { bootEngineInWebView(webViewRef) },
                        onSwitchCdn = {
                            val next = if (activeCdnOverride == null) {
                                "https://cdn.jsdelivr.net/gh/EmulatorJS/EmulatorJS@latest/data/"
                            } else {
                                null
                            }
                            activeCdnOverride = next
                            onUpdateCdnMode(if (next != null) "jsdelivr" else "official")
                            bootEngineInWebView(webViewRef, cdnOverride = next)
                        },
                        onRunWithoutThreads = {
                            forceNoThreads = true
                            bootEngineInWebView(webViewRef, noThreads = true)
                        },
                        onCopyLog = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("RetroVerse Log", diagnosticLogs))
                            onShowMessage(if (isArabic) "تم نسخ سجل الأخطاء" else "Error log copied")
                        },
                        onDiagnoseWithAi = {
                            onAskAiDiagnosis(engineErrorDetail, diagnosticLogs)
                        }
                    )
                }

                // Collapsible Top HUD Bar
                if (hudVisible) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            HudIconBtn(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, strings.close, "play_back_button", ::exitWithOptionalAutoSave)
                            HudIconBtn(if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause, strings.actionPause, "play_pause_btn") {
                                isPaused = !isPaused
                                webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.togglePause();", null)
                            }
                            HudIconBtn(Icons.Rounded.Refresh, strings.actionReset, "play_reset_btn") {
                                webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.resetGame();", null)
                            }
                            HudIconBtn(Icons.Rounded.ScreenRotation, "Rotate", "play_rotate_btn") {
                                val nextLock = when (settings.orientationLock) {
                                    "auto" -> "landscape"
                                    "landscape" -> "portrait"
                                    else -> "auto"
                                }
                                onUpdateOrientationLock(nextLock)
                                onShowMessage(nextLock.uppercase())
                            }
                            HudIconBtn(
                                if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                "Fullscreen",
                                "play_fullscreen_btn"
                            ) { isFullscreen = !isFullscreen }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            if (settings.showFps) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .border(1.dp, BorderGlass, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${sys.shortName} $currentFps FPS • ${String.format("%.1f", frameTimeMs)}ms",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = SecondaryCyan
                                    )
                                }
                            }
                            HudIconBtn(Icons.Rounded.AspectRatio, settings.aspectRatio, "play_aspect_btn") {
                                val list = listOf("Original", "4:3", "16:9", "Stretch", "Integer")
                                val next = list[(list.indexOf(settings.aspectRatio) + 1) % list.size]
                                onUpdateAspectRatio(next)
                                onShowMessage(next)
                            }
                            HudIconBtn(Icons.Rounded.VisibilityOff, "Hide HUD", "play_hide_hud_btn") { hudVisible = false }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable { hudVisible = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Visibility, contentDescription = "Show HUD", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (isLandscape) {
            // Landscape: Full-height centered viewport with translucent controls overlaid on left/right edges
            Box(modifier = Modifier.fillMaxSize()) {
                webViewContent(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(horizontal = if (hideTouchDeck) 8.dp else 72.dp, vertical = 4.dp)
                        .align(Alignment.Center)
                )
                if (!hideTouchDeck) {
                    TouchControllerDeck(
                        profile = sys.controllerProfile,
                        isLandscape = true,
                        settings = settings,
                        editMode = editLayoutMode,
                        onSimulateInput = ::sendButtonIndex,
                        onSavePlacement = { cid, pl -> onSaveControlPlacement(sys.controllerProfile.name, true, cid, pl) },
                        onOpenMenu = { drawerExpanded = !drawerExpanded },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (drawerExpanded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = 560.dp)
                    ) {
                        QuickToolsAndSavesDrawer(
                            settings = settings,
                            saveStates = saveStates,
                            drawerExpanded = true,
                            editLayoutMode = editLayoutMode,
                            speedMultiplier = speedMultiplier,
                            onToggleExpand = { drawerExpanded = false },
                            onQuickSave = { triggerSaveToSlot(1) },
                            onRewind = {
                                webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.rewindStep();", null)
                                onShowMessage(strings.rewindSuccessMsg)
                            },
                            onCycleSpeed = {
                                val speeds = listOf(1f, 1.5f, 2f, 3f, 4f, 0.5f)
                                speedMultiplier = speeds[(speeds.indexOf(speedMultiplier) + 1) % speeds.size]
                                webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.setSpeed($speedMultiplier);", null)
                            },
                            onScreenshot = {
                                webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.captureScreenshotAsync('snap_only');", null)
                            },
                            onOpenCheats = { showCheatDialog = true },
                            onOpenDisc = { showDiscDialog = true },
                            onToggleEditLayout = { editLayoutMode = !editLayoutMode },
                            onResetLayout = { onResetControlLayout(sys.controllerProfile.name, true) },
                            onOpenQuickSettings = { showQuickSettingsDialog = true },
                            onSlotClick = { slotIdx, existing ->
                                if (existing == null) triggerSaveToSlot(slotIdx) else selectedSlotForAction = existing
                            }
                        )
                    }
                }
            }
        } else {
            // Portrait: Viewport on top, Touch Deck in middle, Quick Tools & Saves drawer at bottom
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                webViewContent(
                    Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .height(255.dp)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                if (!hideTouchDeck) {
                    TouchControllerDeck(
                        profile = sys.controllerProfile,
                        isLandscape = false,
                        settings = settings,
                        editMode = editLayoutMode,
                        onSimulateInput = ::sendButtonIndex,
                        onSavePlacement = { cid, pl -> onSaveControlPlacement(sys.controllerProfile.name, false, cid, pl) },
                        onOpenMenu = { drawerExpanded = !drawerExpanded },
                        modifier = Modifier
                            .widthIn(max = 620.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "يد التحكم متصلة: $connectedGamepadName" else "Controller active: $connectedGamepadName",
                            color = SecondaryCyan,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                QuickToolsAndSavesDrawer(
                    settings = settings,
                    saveStates = saveStates,
                    drawerExpanded = drawerExpanded,
                    editLayoutMode = editLayoutMode,
                    speedMultiplier = speedMultiplier,
                    onToggleExpand = { drawerExpanded = !drawerExpanded },
                    onQuickSave = { triggerSaveToSlot(1) },
                    onRewind = {
                        webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.rewindStep();", null)
                        onShowMessage(strings.rewindSuccessMsg)
                    },
                    onCycleSpeed = {
                        val speeds = listOf(1f, 1.5f, 2f, 3f, 4f, 0.5f)
                        speedMultiplier = speeds[(speeds.indexOf(speedMultiplier) + 1) % speeds.size]
                        webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.setSpeed($speedMultiplier);", null)
                    },
                    onScreenshot = {
                        webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.captureScreenshotAsync('snap_only');", null)
                    },
                    onOpenCheats = { showCheatDialog = true },
                    onOpenDisc = { showDiscDialog = true },
                    onToggleEditLayout = { editLayoutMode = !editLayoutMode },
                    onResetLayout = { onResetControlLayout(sys.controllerProfile.name, false) },
                    onOpenQuickSettings = { showQuickSettingsDialog = true },
                    onSlotClick = { slotIdx, existing ->
                        if (existing == null) triggerSaveToSlot(slotIdx) else selectedSlotForAction = existing
                    }
                )
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
                }) { Text(strings.loadSlot, color = SecondaryCyan) }
            },
            dismissButton = {
                TextButton(onClick = {
                    triggerSaveToSlot(slot.slotIndex)
                    selectedSlotForAction = null
                }) { Text(strings.overwriteSlot, color = PrimaryPurple) }
            }
        )
    }

    if (showCheatDialog) {
        CheatManagerDialog(
            game = game,
            cheats = cheats,
            onDismiss = { showCheatDialog = false },
            onAddCheat = onAddCheat,
            onToggleCheat = onToggleCheat,
            onDeleteCheat = onDeleteCheat,
            onImportCheatFile = onImportCheatFile
        )
    }

    if (showDiscDialog) {
        AlertDialog(
            onDismissRequest = { showDiscDialog = false },
            containerColor = SurfaceDark,
            title = { Text(if (isArabic) "تبديل القرص (Multi-Disc)" else "Swap Disc", color = Color.White) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..3).forEach { idx ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .clickable {
                                    webViewRef?.evaluateJavascript("window.RetroConfig && window.RetroConfig.setDisc($idx);", null)
                                    onShowMessage((if (isArabic) "تم التبديل للقرص " else "Switched to Disc ") + (idx + 1))
                                    showDiscDialog = false
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Disc ${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiscDialog = false }) { Text(strings.close, color = SecondaryCyan) }
            }
        )
    }

    if (showQuickSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showQuickSettingsDialog = false },
            containerColor = SurfaceDark,
            title = { Text(if (isArabic) "إعدادات سريعة" else "Quick Settings", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val filters = listOf("Sharp", "CRT", "Scanlines", "LCD", "Smooth")
                    QuickOptionRow(strings.settingFilter, settings.filterMode) {
                        val next = filters[(filters.indexOf(settings.filterMode) + 1) % filters.size]
                        onUpdateFilterMode(next)
                    }
                    val aspects = listOf("Original", "4:3", "16:9", "Stretch", "Integer")
                    QuickOptionRow(strings.settingAspectRatio, settings.aspectRatio) {
                        val next = aspects[(aspects.indexOf(settings.aspectRatio) + 1) % aspects.size]
                        onUpdateAspectRatio(next)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickSettingsDialog = false }) { Text(strings.close, color = SecondaryCyan) }
            }
        )
    }

    if (showBiosWarning) {
        AlertDialog(
            onDismissRequest = { showBiosWarning = false },
            containerColor = SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = WarningAmber)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isArabic) "ملف BIOS غير مثبّت" else "BIOS Not Installed", color = WarningAmber)
                }
            },
            text = {
                Text(
                    text = if (isArabic) {
                        "نظام ${sys.shortName} يوصى له بملف BIOS لضمان أفضل توافق. يمكنك الاستمرار بوضع HLE الآن أو رفع ملف BIOS من الإعدادات."
                    } else {
                        "${sys.shortName} recommends a BIOS file for full compatibility. You can continue with HLE or upload BIOS in Settings."
                    },
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showBiosWarning = false }) {
                    Text(if (isArabic) "متابعة" else "Continue", color = SecondaryCyan)
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
                TextButton(onClick = { showPspDialog = false }) { Text(strings.close, color = SecondaryCyan) }
            }
        )
    }
}

@Composable
private fun QuickToolsAndSavesDrawer(
    settings: RetroSettings,
    saveStates: List<SaveStateEntity>,
    drawerExpanded: Boolean,
    editLayoutMode: Boolean,
    speedMultiplier: Float,
    onToggleExpand: () -> Unit,
    onQuickSave: () -> Unit,
    onRewind: () -> Unit,
    onCycleSpeed: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenCheats: () -> Unit,
    onOpenDisc: () -> Unit,
    onToggleEditLayout: () -> Unit,
    onResetLayout: () -> Unit,
    onOpenQuickSettings: () -> Unit,
    onSlotClick: (slotIdx: Int, existing: SaveStateEntity?) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current

    Column(
        modifier = Modifier
            .widthIn(max = 620.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.playSaveSlots,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Icon(
                imageVector = if (drawerExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                contentDescription = strings.playSaveSlots,
                tint = TextSecondary
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            QuickActionTile(Icons.Rounded.Save, strings.actionSave, false, Modifier.weight(1f), onQuickSave)
            QuickActionTile(Icons.Rounded.FastRewind, strings.actionRewind, false, Modifier.weight(1f), onRewind)
            QuickActionTile(Icons.Rounded.FastForward, "${speedMultiplier}x", speedMultiplier != 1f, Modifier.weight(1f), onCycleSpeed)
            QuickActionTile(Icons.Rounded.CameraAlt, strings.actionScreenshot, false, Modifier.weight(1f), onScreenshot)
            QuickActionTile(Icons.Rounded.Code, if (isArabic) "غش" else "Cheats", false, Modifier.weight(1f), onOpenCheats)
        }

        AnimatedVisibility(visible = drawerExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickActionTile(Icons.Rounded.OpenWith, if (isArabic) "تحريك الأزرار" else "Edit Pad", editLayoutMode, Modifier.weight(1f), onToggleEditLayout)
                    QuickActionTile(Icons.Rounded.Refresh, if (isArabic) "ضبط التخطيط" else "Reset Pad", false, Modifier.weight(1f), onResetLayout)
                    QuickActionTile(Icons.Rounded.Album, if (isArabic) "القرص" else "Disc", false, Modifier.weight(1f), onOpenDisc)
                    QuickActionTile(Icons.Rounded.Tune, if (isArabic) "إعدادات" else "Quick", false, Modifier.weight(1f), onOpenQuickSettings)
                }

                val slotMap = remember(saveStates) { saveStates.associateBy { it.slotIndex } }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items((0..5).toList()) { slotIdx ->
                        val existing = slotMap[slotIdx]
                        SaveSlotRow(
                            slotIndex = slotIdx,
                            saveState = existing,
                            slotLabel = strings.slotLabel(slotIdx),
                            emptyLabel = strings.slotEmpty,
                            hourShort = strings.hourShort,
                            minuteShort = strings.minuteShort,
                            onClick = { onSlotClick(slotIdx, existing) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudIconBtn(
    icon: ImageVector,
    desc: String,
    tag: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .testTag(tag)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, BorderGlass, CircleShape)
    ) {
        Icon(imageVector = icon, contentDescription = desc, tint = Color.White, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun QuickOptionRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(value, color = SecondaryCyan, fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (active) Brush.linearGradient(listOf(PrimaryPurple, SecondaryCyan))
                else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated))
            )
            .border(1.dp, if (active) Color.Transparent else BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = if (active) Color.White else SecondaryCyan, modifier = Modifier.size(17.dp))
        Spacer(Modifier.height(3.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 50.dp, height = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0B0D14))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val bmp = remember(saveState?.screenshotPath) {
                saveState?.screenshotPath?.let { path ->
                    val f = File(path)
                    if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                }
            }
            if (bmp != null) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = slotLabel, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text("#$slotIndex", style = MaterialTheme.typography.labelMedium, color = if (saveState != null) SuccessGreen else TextSecondary)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = slotLabel, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
            if (saveState != null) {
                val dateStr = remember(saveState.savedAt) { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(saveState.savedAt)) }
                val durStr = formatDuration(saveState.playTimeMinutesAtSave, hourShort, minuteShort)
                Text(text = "$dateStr · $durStr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            } else {
                Text(text = emptyLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}
