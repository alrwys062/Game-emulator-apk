package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.RetroVerseTheme
import com.example.viewmodel.RetroViewModel

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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
    val filteredGames by viewModel.filteredGames.collectAsStateWithLifecycle()
    val totalSaveCount by viewModel.totalSaveCount.collectAsStateWithLifecycle()
    val splashFinished by viewModel.splashFinished.collectAsStateWithLifecycle()
    val forceOnboarding by viewModel.forceShowOnboarding.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedSystem by viewModel.selectedSystemFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val playingGame by viewModel.playingGame.collectAsStateWithLifecycle()
    val activeSaveStates by viewModel.activeGameSaveStates.collectAsStateWithLifecycle()
    val activeStoreUrl by viewModel.activeStoreBrowserUrl.collectAsStateWithLifecycle()
    val downloadTask by viewModel.downloadTask.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val connectedGamepad by viewModel.connectedGamepadName.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val romPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri -> viewModel.importRomUri(uri) }
    }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearToast()
    }

    val isArabic = settings.language == "ar"
    val themeMode = AppThemeMode.fromId(settings.themeMode)

    RetroVerseTheme(themeMode = themeMode, isArabic = isArabic) {
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
                        settings = settings,
                        saveStates = activeSaveStates,
                        onBack = viewModel::exitGame,
                        onSaveState = viewModel::saveStateToSlot,
                        onCaptureScreenshot = viewModel::saveScreenshot,
                        onShowMessage = viewModel::showToast
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
                                        onSearchChange = viewModel::setSearchQuery,
                                        onSelectSystemFilter = viewModel::setSystemFilter,
                                        onSelectSystemSoon = viewModel::openSystemInLibrary,
                                        onImportClick = { romPickerLauncher.launch(arrayOf("*/*")) },
                                        onPlayGame = viewModel::launchGame,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onDeleteGame = viewModel::deleteGame
                                    )

                                    MainTab.STORE -> StoreScreen(
                                        downloadTask = downloadTask,
                                        onOpenSource = { src ->
                                            viewModel.openStoreBrowser(src.title, src.url)
                                        },
                                        onImportFromPhone = { romPickerLauncher.launch(arrayOf("*/*")) }
                                    )

                                    MainTab.SETTINGS -> SettingsScreen(
                                        settings = settings,
                                        connectedGamepadName = connectedGamepad,
                                        onRefreshGamepads = viewModel::refreshConnectedGamepads,
                                        onUpdateFilter = viewModel::updateFilterMode,
                                        onUpdateScanlines = viewModel::updateScanlines,
                                        onUpdateAspectRatio = viewModel::updateAspectRatio,
                                        onUpdateSoundVolume = viewModel::updateSoundVolume,
                                        onUpdateControlsOpacity = viewModel::updateControlsOpacity,
                                        onUpdateControlsScale = viewModel::updateControlsScale,
                                        onUpdateMoveControls = viewModel::updateMoveControls,
                                        onUpdateSelectedCore = viewModel::updateSelectedCore,
                                        onUpdateCheatsEnabled = viewModel::updateCheatsEnabled,
                                        onUpdateCheatCodes = viewModel::updateCheatCodes,
                                        onUpdatePspWarning = viewModel::updatePspWarning,
                                        onUpdateLanguage = viewModel::updateLanguage,
                                        onUpdateThemeMode = viewModel::updateThemeMode,
                                        onUpdateUserName = viewModel::updateUserName,
                                        onReplayOnboarding = viewModel::replayOnboarding
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
        }
    }
}
