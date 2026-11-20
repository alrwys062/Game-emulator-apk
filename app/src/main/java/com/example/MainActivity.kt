package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.hardware.input.InputManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.GameEntity
import com.example.service.OfflineCoreManager
import com.example.service.OfflinePrepRequest
import com.example.ui.components.CheatManagerDialog
import com.example.ui.components.GlassBottomNav
import com.example.ui.components.MainTab
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StoreBrowserScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.RetroVerseTheme
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.RetroViewModel
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            val viewModel: RetroViewModel = viewModel()
            RetroVerseApp(viewModel = viewModel)
        }
    }
}

@Composable
fun RetroVerseApp(viewModel: RetroViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
    val filteredGames by viewModel.filteredGames.collectAsStateWithLifecycle()
    val totalSaveCount by viewModel.totalSaveCount.collectAsStateWithLifecycle()
    val splashFinished by viewModel.splashFinished.collectAsStateWithLifecycle()
    val forceOnboarding by viewModel.forceShowOnboarding.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedSystem by viewModel.selectedSystemFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val playingGame by viewModel.playingGame.collectAsStateWithLifecycle()
    val isEngineTestMode by viewModel.isEngineTestMode.collectAsStateWithLifecycle()
    val cheatManagerGame by viewModel.cheatManagerGame.collectAsStateWithLifecycle()
    val activeSaveStates by viewModel.activeGameSaveStates.collectAsStateWithLifecycle()
    val activeCheats by viewModel.activeGameCheats.collectAsStateWithLifecycle()
    val activeStoreUrl by viewModel.activeStoreBrowserUrl.collectAsStateWithLifecycle()
    val downloadTask by viewModel.downloadTask.collectAsStateWithLifecycle()
    val bulkCoverProgress by viewModel.bulkCoverProgress.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val connectedGamepad by viewModel.connectedGamepadName.collectAsStateWithLifecycle()
    val connectedGamepads by viewModel.connectedGamepadNames.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val offlineBundles by viewModel.offlineBundles.collectAsStateWithLifecycle()
    val offlinePrepProgress by viewModel.offlinePrepProgress.collectAsStateWithLifecycle()
    val activePrepRequest by viewModel.activePrepRequest.collectAsStateWithLifecycle()
    val offlineDiagnostics by viewModel.offlineDiagnostics.collectAsStateWithLifecycle()
    val diagnosticsTriggerId by viewModel.diagnosticsTriggerId.collectAsStateWithLifecycle()
    val storageUsedBytes by viewModel.storageUsedBytes.collectAsStateWithLifecycle()
    val lastDiagnosticLogs by viewModel.lastDiagnosticLogs.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val aiBusy by viewModel.aiBusy.collectAsStateWithLifecycle()
    val aiDiagnosticResult by viewModel.aiDiagnosticResult.collectAsStateWithLifecycle()
    val aiPerfRec by viewModel.aiPerfRecommendation.collectAsStateWithLifecycle()
    val aiChatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var pendingBiosSlotKey by remember { mutableStateOf("ps1") }
    var pendingCoverTargetGameId by remember { mutableStateOf<Long?>(null) }
    var pendingCheatImportGame by remember { mutableStateOf<GameEntity?>(null) }

    // Listen dynamically for Bluetooth / USB controller connect & disconnect
    DisposableEffect(context) {
        val im = context.getSystemService(Context.INPUT_SERVICE) as? InputManager
        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) = viewModel.refreshConnectedGamepads()
            override fun onInputDeviceRemoved(deviceId: Int) = viewModel.refreshConnectedGamepads()
            override fun onInputDeviceChanged(deviceId: Int) = viewModel.refreshConnectedGamepads()
        }
        im?.registerInputDeviceListener(listener, null)
        onDispose { im?.unregisterInputDeviceListener(listener) }
    }

    val romPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importRomUris(uris)
        }
    }

    val biosPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBiosUri(pendingBiosSlotKey, uri)
        }
    }

    val coverPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val gameId = pendingCoverTargetGameId
        if (uri != null && gameId != null) {
            viewModel.updateGameCoverFromDeviceUri(gameId, uri)
        }
    }

    val cheatFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val target = pendingCheatImportGame ?: playingGame ?: cheatManagerGame
        if (uri != null && target != null) {
            viewModel.importCheatFile(target, uri)
        }
    }

    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupJson { json ->
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    val isAr = settings.language == "ar"
                    viewModel.showToast(if (isAr) "تم تصدير النسخة الاحتياطية بنجاح" else "Backup exported successfully")
                }
            }
        }
    }

    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (json.isNotBlank()) viewModel.importBackupJson(json)
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearToast()
    }

    val isArabic = settings.language == "ar"
    val themeMode = AppThemeMode.fromId(settings.themeMode)
    val accentPreset = AccentPreset.fromId(settings.accentPreset)

    RetroVerseTheme(themeMode = themeMode, accentPreset = accentPreset, isArabic = isArabic) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                !splashFinished -> {
                    SplashScreen()
                }
                !settings.onboardingCompleted || forceOnboarding -> {
                    OnboardingScreen(onFinish = viewModel::completeOnboarding)
                }
                playingGame != null -> {
                    val game = playingGame!!
                    PlayScreen(
                        game = game,
                        isEngineTestMode = isEngineTestMode,
                        isOnline = isOnline,
                        offlineCoreManager = viewModel.offlineCoreManager,
                        settings = settings,
                        saveStates = activeSaveStates,
                        cheats = activeCheats,
                        connectedGamepadName = connectedGamepad,
                        connectedGamepadNames = connectedGamepads,
                        aiBusy = aiBusy,
                        aiDiagnosticResult = aiDiagnosticResult,
                        onBack = viewModel::exitGame,
                        onSaveState = viewModel::saveStateToSlot,
                        onCaptureScreenshot = viewModel::saveScreenshot,
                        onShowMessage = viewModel::showToast,
                        onUpdateAspectRatio = viewModel::updateAspectRatio,
                        onUpdateFilterMode = viewModel::updateFilterMode,
                        onUpdateOrientationLock = viewModel::updateOrientationLock,
                        onUpdateCdnMode = viewModel::updateCdnMode,
                        onSaveControlPlacement = viewModel::saveControlPlacement,
                        onResetControlLayout = viewModel::resetControlLayout,
                        onAddCheat = { name, code, type -> viewModel.addCheat(game.id, name, code, type) },
                        onToggleCheat = viewModel::toggleCheat,
                        onDeleteCheat = viewModel::deleteCheat,
                        onImportCheatFile = {
                            pendingCheatImportGame = game
                            cheatFilePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onDiagnosticLogsChanged = viewModel::updateDiagnosticLogs,
                        onAskAiDiagnosis = { err, logs ->
                            viewModel.askAiErrorDiagnosis(err, logs, game.title, game.systemId)
                        }
                    )
                }
                activeStoreUrl != null -> {
                    val (title, url) = activeStoreUrl!!
                    StoreBrowserScreen(
                        title = title,
                        url = url,
                        onClose = viewModel::closeStoreBrowser,
                        onDownloadRomUrl = viewModel::downloadRomFromStoreUrl
                    )
                }
                else -> {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.padding(bottom = 86.dp)
                            )
                        }
                    ) { _ ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            OfflinePrepWebViewHost(
                                isOnline = isOnline,
                                offlineCoreManager = viewModel.offlineCoreManager,
                                activePrepRequest = activePrepRequest,
                                diagnosticsTriggerId = diagnosticsTriggerId,
                                onPrepProgress = viewModel::onOfflinePrepareProgress,
                                onDiagnosticsResult = viewModel::onOfflineWebDiagnosticsResult
                            )

                            Crossfade(
                                targetState = activeTab,
                                animationSpec = tween(220),
                                label = "main_tab_transition"
                            ) { tab ->
                                when (tab) {
                                    MainTab.HOME -> HomeScreen(
                                        userName = settings.userName,
                                        games = allGames,
                                        totalSaveCount = totalSaveCount,
                                        notifications = notifications,
                                        onPlayGame = viewModel::launchGame,
                                        onSelectSystem = viewModel::openSystemInLibrary,
                                        onSeeAllLibrary = {
                                            viewModel.setSystemFilter(null)
                                            viewModel.selectTab(MainTab.LIBRARY)
                                        },
                                        onUpdateUserName = viewModel::updateUserName
                                    )

                                    MainTab.LIBRARY, MainTab.FAVORITES -> LibraryScreen(
                                        isFavoritesMode = tab == MainTab.FAVORITES,
                                        totalGamesCount = allGames.size,
                                        games = filteredGames,
                                        searchQuery = searchQuery,
                                        selectedSystemId = selectedSystem,
                                        sortOrder = sortOrder,
                                        bulkCoverProgress = bulkCoverProgress,
                                        aiBusy = aiBusy,
                                        onSearchChange = viewModel::setSearchQuery,
                                        onSelectSystemFilter = viewModel::setSystemFilter,
                                        onSelectSystemSoon = viewModel::openSystemInLibrary,
                                        onChangeSortOrder = viewModel::setSortOrder,
                                        onImportClick = { romPickerLauncher.launch(arrayOf("*/*")) },
                                        onFetchAllCovers = viewModel::fetchAllMissingCovers,
                                        onPlayGame = viewModel::launchGame,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onUpdateCoverUrl = { game, url -> viewModel.updateGameCoverUrl(game.id, url) },
                                        onPickCoverFromPhone = { game ->
                                            pendingCoverTargetGameId = game.id
                                            coverPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        onAutoFetchCover = viewModel::autoFetchCoverForGame,
                                        onRenameGame = viewModel::renameGame,
                                        onChangeSystemAndCore = viewModel::updateGameSystemAndCore,
                                        onManageCheats = viewModel::openCheatManager,
                                        onFetchAiGameInfo = viewModel::fetchAiGameInfo,
                                        onDeleteGame = viewModel::deleteGame
                                    )

                                    MainTab.STORE -> StoreScreen(
                                        isOnline = isOnline,
                                        downloadTask = downloadTask,
                                        onOpenSource = { src ->
                                            viewModel.openStoreBrowser(src.title, src.url)
                                        },
                                        onImportFromPhone = { romPickerLauncher.launch(arrayOf("*/*")) },
                                        onShowMessage = viewModel::showToast
                                    )

                                    MainTab.SETTINGS -> SettingsScreen(
                                        settings = settings,
                                        isOnline = isOnline,
                                        deviceSpec = viewModel.deviceSpec,
                                        connectedGamepadName = connectedGamepad,
                                        connectedGamepadNames = connectedGamepads,
                                        offlineBundles = offlineBundles,
                                        offlinePrepProgress = offlinePrepProgress,
                                        offlineDiagnostics = offlineDiagnostics,
                                        storageUsedBytes = storageUsedBytes,
                                        lastDiagnosticLogs = lastDiagnosticLogs,
                                        aiBusy = aiBusy,
                                        aiPerfRec = aiPerfRec,
                                        aiChatMessages = aiChatMessages,
                                        onRefreshGamepads = viewModel::refreshConnectedGamepads,
                                        onRefreshStorage = viewModel::refreshStorageUsage,
                                        onPrepareSystemOffline = viewModel::prepareSystemOffline,
                                        onPrepareAllSystemsOffline = viewModel::prepareAllSystemsOffline,
                                        onCancelOfflinePrep = viewModel::cancelOfflinePrep,
                                        onDeleteOfflineBundle = viewModel::deleteOfflineBundle,
                                        onClearAllOfflineBundles = viewModel::clearAllOfflineBundles,
                                        onTriggerOfflineDiagnostics = viewModel::triggerOfflineDiagnostics,
                                        onPickBiosFile = { slot ->
                                            pendingBiosSlotKey = slot
                                            biosPickerLauncher.launch(arrayOf("*/*"))
                                        },
                                        onDownloadBiosUrl = viewModel::downloadBiosFromUrl,
                                        onRemoveBios = viewModel::removeBios,
                                        onOpenBiosDocs = {
                                            viewModel.openStoreBrowser(
                                                "Libretro BIOS Docs",
                                                "https://docs.libretro.com/library/bios/"
                                            )
                                        },
                                        onRunEngineTest = viewModel::launchEngineTest,
                                        onUpdateFilter = viewModel::updateFilterMode,
                                        onUpdateScanlines = viewModel::updateScanlines,
                                        onUpdateAspectRatio = viewModel::updateAspectRatio,
                                        onUpdateResolutionScale = viewModel::updateResolutionScale,
                                        onUpdateRenderScalePercent = viewModel::updateRenderScalePercent,
                                        onUpdateShowFps = viewModel::updateShowFps,
                                        onUpdateOrientationLock = viewModel::updateOrientationLock,
                                        onUpdateWakeLock = viewModel::updateWakeLock,
                                        onApplyPerfPreset = viewModel::applyPerformancePreset,
                                        onUpdateFrameSkip = viewModel::updateFrameSkip,
                                        onUpdateVsync = viewModel::updateVsync,
                                        onUpdateFpsCap = viewModel::updateFpsCap,
                                        onUpdateSmartPerf = viewModel::updateSmartPerf,
                                        onUpdatePreloadRoms = viewModel::updatePreloadRoms,
                                        onUpdateSelectedCore = viewModel::updateSelectedCore,
                                        onUpdateThreadsMode = viewModel::updateWasmThreadsMode,
                                        onUpdateRewindEnabled = viewModel::updateRewindEnabled,
                                        onUpdateFastForwardSpeed = viewModel::updateFastForwardSpeed,
                                        onUpdateCdnMode = viewModel::updateCdnMode,
                                        onUpdateCustomDataPath = viewModel::updateCustomDataPath,
                                        onUpdateSoundVolume = viewModel::updateSoundVolume,
                                        onUpdateAudioMuted = viewModel::updateAudioMuted,
                                        onUpdateAudioLatency = viewModel::updateAudioLatency,
                                        onUpdateControlsOpacity = viewModel::updateControlsOpacity,
                                        onUpdateControlsScale = viewModel::updateControlsScale,
                                        onUpdateMoveControls = viewModel::updateMoveControls,
                                        onUpdateHaptics = viewModel::updateHaptics,
                                        onUpdateAutoHideTouch = viewModel::updateAutoHideTouchOnGamepad,
                                        onUpdateGamepadDeadzone = viewModel::updateGamepadDeadzone,
                                        onSaveKeyBinding = viewModel::saveKeyBinding,
                                        onResetKeyBindings = viewModel::resetKeyBindings,
                                        onSaveGamepadProfileBinding = viewModel::saveGamepadProfileBinding,
                                        onResetGamepadProfileBindings = viewModel::resetGamepadProfileBindings,
                                        onUpdateAutoSaveOnExit = viewModel::updateAutoSaveOnExit,
                                        onUpdateCheatsEnabled = viewModel::updateCheatsEnabled,
                                        onUpdateCheatCodes = viewModel::updateCheatCodes,
                                        onUpdatePspWarning = viewModel::updatePspWarning,
                                        onSaveGeminiConfig = viewModel::updateGeminiConfig,
                                        onRequestAiPerfRec = {
                                            viewModel.requestAiPerfRecommendation("PSP / N64 / PS1", "General")
                                        },
                                        onApplyAiPerfRec = viewModel::applyAiPerfRecommendation,
                                        onSendAiChat = viewModel::sendAiChatPrompt,
                                        onExportBackup = {
                                            backupExportLauncher.launch("retroverse_backup.json")
                                        },
                                        onImportBackup = {
                                            backupImportLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        },
                                        onUpdateLanguage = viewModel::updateLanguage,
                                        onUpdateThemeMode = viewModel::updateThemeMode,
                                        onUpdateAccentPreset = viewModel::updateAccentPreset,
                                        onUpdateUserName = viewModel::updateUserName,
                                        onReplayOnboarding = viewModel::replayOnboarding,
                                        onClearCache = viewModel::clearTempCache,
                                        onShowMessage = viewModel::showToast
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = !isOnline,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(WarningAmber.copy(alpha = 0.16f))
                                        .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudOff,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "وضع الأوفلاين — الأنظمة المجهّزة تعمل بلا إنترنت" else "Offline Mode — Prepared cores work offline",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = WarningAmber
                                    )
                                }
                            }

                            GlassBottomNav(
                                activeTab = activeTab,
                                onSelectTab = viewModel::selectTab,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }

            cheatManagerGame?.let { targetGame ->
                CheatManagerDialog(
                    game = targetGame,
                    cheats = activeCheats,
                    onDismiss = viewModel::closeCheatManager,
                    onAddCheat = { name, code, type -> viewModel.addCheat(targetGame.id, name, code, type) },
                    onToggleCheat = viewModel::toggleCheat,
                    onDeleteCheat = viewModel::deleteCheat,
                    onImportCheatFile = {
                        pendingCheatImportGame = targetGame
                        cheatFilePickerLauncher.launch(arrayOf("*/*"))
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OfflinePrepWebViewHost(
    isOnline: Boolean,
    offlineCoreManager: OfflineCoreManager,
    activePrepRequest: OfflinePrepRequest?,
    diagnosticsTriggerId: Int,
    onPrepProgress: (
        systemId: String,
        coreId: String,
        threadsEnabled: Boolean,
        percent: Int,
        totalBytes: Long,
        fileCount: Int,
        urlsJson: String,
        done: Boolean,
        error: String
    ) -> Unit,
    onDiagnosticsResult: (String) -> Unit
) {
    var pageLoaded by remember { mutableStateOf(false) }
    var hostWebView by remember { mutableStateOf<WebView?>(null) }
    val currentOnline by rememberUpdatedState(isOnline)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        AndroidView(
            modifier = Modifier
                .size(1.dp)
                .alpha(0f),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = false
                        allowContentAccess = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onOfflinePrepareProgress(
                                systemId: String,
                                coreId: String,
                                threadsEnabled: Boolean,
                                percent: Int,
                                totalBytes: Long,
                                fileCount: Int,
                                urlsJson: String,
                                done: Boolean,
                                error: String
                            ) {
                                post {
                                    onPrepProgress(
                                        systemId,
                                        coreId,
                                        threadsEnabled,
                                        percent,
                                        totalBytes,
                                        fileCount,
                                        urlsJson,
                                        done,
                                        error
                                    )
                                }
                            }

                            @JavascriptInterface
                            fun onOfflineDiagnosticsResult(jsonStr: String) {
                                post { onDiagnosticsResult(jsonStr) }
                            }

                            @JavascriptInterface
                            fun onStageUpdate(stageNum: Int, status: String, detail: String) {}

                            @JavascriptInterface
                            fun onDiagnosticLog(level: String, line: String) {}

                            @JavascriptInterface
                            fun onEngineStatus(status: String, detail: String) {}

                            @JavascriptInterface
                            fun onLoadingTick(elapsedSec: Int, totalSec: Int) {}

                            @JavascriptInterface
                            fun onFpsUpdate(fps: Int, frameMs: Float) {}

                            @JavascriptInterface
                            fun onWebGamepadsChanged(count: Int, namesJson: String) {}
                        },
                        "RetroHost"
                    )
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: return null
                            val method = request.method ?: "GET"
                            return offlineCoreManager.interceptRetroLocalOrCdn(reqUrl, method, currentOnline)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            pageLoaded = true
                        }
                    }
                    loadUrl("https://retroverse.local/emulator_host.html")
                    hostWebView = this
                }
            },
            onRelease = { wv ->
                runCatching {
                    wv.evaluateJavascript("window.RetroEngine && window.RetroEngine.teardown();", null)
                    wv.destroy()
                }
            }
        )
    }

    LaunchedEffect(pageLoaded, activePrepRequest?.requestSeq) {
        val req = activePrepRequest ?: return@LaunchedEffect
        val wv = hostWebView ?: return@LaunchedEffect
        if (!pageLoaded) return@LaunchedEffect
        val payload = JSONObject().apply {
            put("systemId", req.systemId)
            put("core", req.coreId)
            put("cdnMode", req.cdnMode)
            put("customDataPath", req.customDataPath)
            put("threadsMode", req.threadsMode)
        }.toString()
        wv.evaluateJavascript("window.RetroEngine && window.RetroEngine.prepareOfflineBundle($payload);", null)
    }

    LaunchedEffect(pageLoaded, diagnosticsTriggerId) {
        if (!pageLoaded || diagnosticsTriggerId <= 0) return@LaunchedEffect
        val wv = hostWebView ?: return@LaunchedEffect
        wv.evaluateJavascript("window.RetroOffline && window.RetroOffline.collectDiagnosticsAsync();", null)
    }
}

