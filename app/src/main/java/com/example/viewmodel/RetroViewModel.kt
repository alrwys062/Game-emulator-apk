package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.input.InputManager
import android.net.Uri
import android.view.InputDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameEntity
import com.example.data.local.RetroDatabase
import com.example.data.local.SaveStateEntity
import com.example.data.preferences.RetroSettings
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.GameRepository
import com.example.model.RetroSystem
import com.example.ui.components.MainTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class DownloadTaskState(
    val fileName: String,
    val progress: Float,
    val isActive: Boolean
)

data class AppNotification(
    val id: Long,
    val title: String,
    val detail: String,
    val timestamp: Long
)

class RetroViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RetroDatabase.getInstance(application)
    private val gameRepo = GameRepository(application, db.retroDao())
    private val settingsRepo = SettingsRepository(application)

    val settings: StateFlow<RetroSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, RetroSettings())

    val allGames: StateFlow<List<GameEntity>> = gameRepo.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSaveCount: StateFlow<Int> = gameRepo.totalSaveCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    private val _forceShowOnboarding = MutableStateFlow(false)
    val forceShowOnboarding: StateFlow<Boolean> = _forceShowOnboarding.asStateFlow()

    private val _activeTab = MutableStateFlow(MainTab.HOME)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _selectedSystemFilter = MutableStateFlow<String?>(null) // null = All
    val selectedSystemFilter: StateFlow<String?> = _selectedSystemFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _playingGame = MutableStateFlow<GameEntity?>(null)
    val playingGame: StateFlow<GameEntity?> = _playingGame.asStateFlow()

    private val _activeStoreBrowserUrl = MutableStateFlow<Pair<String, String>?>(null) // title to url
    val activeStoreBrowserUrl: StateFlow<Pair<String, String>?> = _activeStoreBrowserUrl.asStateFlow()

    private val _downloadTask = MutableStateFlow<DownloadTaskState?>(null)
    val downloadTask: StateFlow<DownloadTaskState?> = _downloadTask.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _connectedGamepadName = MutableStateFlow<String?>(null)
    val connectedGamepadName: StateFlow<String?> = _connectedGamepadName.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = 1L,
                title = "RetroVerse Engine",
                detail = "EmulatorJS + 13+ Retro Systems Ready",
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeGameSaveStates: StateFlow<List<SaveStateEntity>> = _playingGame
        .flatMapLatest { game ->
            if (game == null) flowOf(emptyList()) else gameRepo.observeSaveStates(game.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredGames: StateFlow<List<GameEntity>> = combine(
        allGames,
        _selectedSystemFilter,
        _searchQuery,
        _activeTab
    ) { games, sysFilter, query, tab ->
        games.filter { game ->
            val matchesFav = if (tab == MainTab.FAVORITES) game.isFavorite else true
            val matchesSys = sysFilter == null || game.systemId.equals(sysFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                game.title.contains(query, ignoreCase = true) ||
                game.systemId.contains(query, ignoreCase = true)
            matchesFav && matchesSys && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var playSessionStartMs: Long = 0L
    private var downloadJob: Job? = null

    init {
        viewModelScope.launch {
            gameRepo.ensureSeededHomebrew()
            refreshConnectedGamepads()
            delay(1500L)
            _splashFinished.value = true
        }
    }

    fun refreshConnectedGamepads() {
        val im = getApplication<Application>().getSystemService(Context.INPUT_SERVICE) as? InputManager
        val names = mutableListOf<String>()
        im?.inputDeviceIds?.forEach { id ->
            val dev = im.getInputDevice(id)
            if (dev != null && !dev.isVirtual) {
                val sources = dev.sources
                val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                if (isGamepad) names.add(dev.name)
            }
        }
        _connectedGamepadName.value = names.firstOrNull()
    }

    fun selectTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun openSystemInLibrary(system: RetroSystem) {
        if (system.isComingSoon) {
            val isAr = settings.value.language == "ar"
            showToast(
                if (isAr) "محاكاة PlayStation 2 غير مدعومة بعد في محرك المتصفح وستتوفر قريباً."
                else "PlayStation 2 emulation is coming soon."
            )
            return
        }
        _selectedSystemFilter.value = system.id
        _activeTab.value = MainTab.LIBRARY
    }

    fun setSystemFilter(systemId: String?) {
        _selectedSystemFilter.value = systemId
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun launchGame(game: GameEntity) {
        playSessionStartMs = System.currentTimeMillis()
        _playingGame.value = game
    }

    fun exitGame() {
        val game = _playingGame.value ?: return
        val elapsedMin = ((System.currentTimeMillis() - playSessionStartMs) / 60_000L).toInt().coerceAtLeast(1)
        val nextProgress = (game.progressPercent + 0.04f).coerceAtMost(1.0f)
        _playingGame.value = null
        viewModelScope.launch {
            gameRepo.recordPlaySession(game.id, elapsedMin, nextProgress)
        }
    }

    fun toggleFavorite(game: GameEntity) {
        viewModelScope.launch {
            gameRepo.toggleFavorite(game)
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            gameRepo.deleteGame(game)
        }
    }

    fun importRomUri(uri: Uri) {
        viewModelScope.launch {
            _downloadTask.value = DownloadTaskState("ROM...", 0.35f, true)
            runCatching {
                val imported = gameRepo.importRomFromUri(uri)
                _downloadTask.value = DownloadTaskState(imported.fileName, 1.0f, false)
                val sysName = RetroSystem.fromId(imported.systemId).shortName
                val msg = if (settings.value.language == "ar") {
                    "تم استيراد ${imported.title} ($sysName) بنجاح"
                } else {
                    "Imported ${imported.title} ($sysName)"
                }
                showToast(msg)
                pushNotification(imported.title, msg)
            }.onFailure {
                _downloadTask.value = null
                showToast(it.localizedMessage ?: "Import error")
            }
        }
    }

    fun downloadRomFromStoreUrl(url: String, fileName: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _downloadTask.value = DownloadTaskState(fileName, 0.1f, true)
            runCatching {
                val bytes = withContext(Dispatchers.IO) {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 12_000
                    conn.readTimeout = 20_000
                    conn.instanceFollowRedirects = true
                    val total = conn.contentLength.coerceAtLeast(1)
                    conn.inputStream.use { input ->
                        val buffer = ByteArray(8192)
                        val out = java.io.ByteArrayOutputStream()
                        var readTotal = 0
                        while (true) {
                            val r = input.read(buffer)
                            if (r <= 0) break
                            out.write(buffer, 0, r)
                            readTotal += r
                            _downloadTask.value = DownloadTaskState(
                                fileName,
                                (readTotal.toFloat() / total).coerceIn(0.1f, 0.95f),
                                true
                            )
                        }
                        out.toByteArray()
                    }
                }
                val imported = gameRepo.importDownloadedBytes(fileName, bytes)
                _downloadTask.value = DownloadTaskState(imported.fileName, 1.0f, false)
                val sysName = RetroSystem.fromId(imported.systemId).shortName
                val msg = if (settings.value.language == "ar") {
                    "تم تنزيل واستيراد ${imported.title} ($sysName)"
                } else {
                    "Downloaded & imported ${imported.title} ($sysName)"
                }
                showToast(msg)
                pushNotification(imported.title, msg)
            }.onFailure {
                _downloadTask.value = null
                showToast(
                    if (settings.value.language == "ar") "تعذر تنزيل الملف، جرّب الفتح في المتصفح ثم الاستيراد"
                    else "Download failed, try opening in browser then importing"
                )
            }
        }
    }

    fun openStoreBrowser(title: String, url: String) {
        _activeStoreBrowserUrl.value = title to url
    }

    fun closeStoreBrowser() {
        _activeStoreBrowserUrl.value = null
    }

    fun saveStateToSlot(slotIndex: Int, stateJson: String, screenshotBase64: String?) {
        val game = _playingGame.value ?: return
        viewModelScope.launch {
            gameRepo.saveStateToSlot(game, slotIndex, stateJson, screenshotBase64)
            val msg = if (settings.value.language == "ar") "تم الحفظ في الخانة $slotIndex" else "Saved to Slot $slotIndex"
            showToast(msg)
        }
    }

    fun saveScreenshot(screenshotBase64: String) {
        val game = _playingGame.value ?: return
        viewModelScope.launch {
            val savedPath = gameRepo.saveGalleryScreenshot(game.id, screenshotBase64)
            if (savedPath != null) {
                val msg = if (settings.value.language == "ar") "تم حفظ لقطة الشاشة بنجاح" else "Screenshot saved"
                showToast(msg)
                pushNotification(game.title, msg)
            }
        }
    }

    fun showToast(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearToast() {
        _snackbarMessage.value = null
    }

    private fun pushNotification(title: String, detail: String) {
        _notifications.value = listOf(
            AppNotification(System.currentTimeMillis(), title, detail, System.currentTimeMillis())
        ) + _notifications.value.take(9)
    }

    fun completeOnboarding() {
        _forceShowOnboarding.value = false
        viewModelScope.launch { settingsRepo.setOnboardingCompleted(true) }
    }

    fun replayOnboarding() {
        _forceShowOnboarding.value = true
    }

    fun updateUserName(name: String) = viewModelScope.launch { settingsRepo.updateUserName(name) }
    fun updateLanguage(lang: String) = viewModelScope.launch { settingsRepo.updateLanguage(lang) }
    fun updateThemeMode(theme: String) = viewModelScope.launch { settingsRepo.updateThemeMode(theme) }
    fun updateFilterMode(filter: String) = viewModelScope.launch { settingsRepo.updateFilterMode(filter) }
    fun updateScanlines(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateScanlines(enabled) }
    fun updateAspectRatio(ratio: String) = viewModelScope.launch { settingsRepo.updateAspectRatio(ratio) }
    fun updateResolutionScale(scale: Int) = viewModelScope.launch { settingsRepo.updateResolutionScale(scale) }
    fun updateShowFps(show: Boolean) = viewModelScope.launch { settingsRepo.updateShowFps(show) }
    fun updateFrameSkip(skip: Int) = viewModelScope.launch { settingsRepo.updateFrameSkip(skip) }
    fun updateVsync(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateVsync(enabled) }
    fun updateSoundVolume(vol: Float) = viewModelScope.launch { settingsRepo.updateSoundVolume(vol) }
    fun updateAudioLatency(lat: String) = viewModelScope.launch { settingsRepo.updateAudioLatency(lat) }
    fun updateControlsOpacity(op: Float) = viewModelScope.launch { settingsRepo.updateControlsOpacity(op) }
    fun updateControlsScale(sc: Float) = viewModelScope.launch { settingsRepo.updateControlsScale(sc) }
    fun updateMoveControls(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateMoveControls(enabled) }
    fun updateHaptics(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateHaptics(enabled) }
    fun updateSelectedCore(core: String) = viewModelScope.launch { settingsRepo.updateSelectedCore(core) }
    fun updateWasmThreads(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateWasmThreads(enabled) }
    fun updateRewindEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateRewindEnabled(enabled) }
    fun updateFastForwardSpeed(speed: Int) = viewModelScope.launch { settingsRepo.updateFastForwardSpeed(speed) }
    fun updateAutoSaveOnExit(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateAutoSaveOnExit(enabled) }
    fun updateCheatsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateCheatsEnabled(enabled) }
    fun updateCheatCodes(codes: String) = viewModelScope.launch { settingsRepo.updateCheatCodes(codes) }
    fun updatePspWarning(enabled: Boolean) = viewModelScope.launch { settingsRepo.updatePspWarning(enabled) }

    fun updateGameCoverUrl(gameId: Long, coverUrl: String?) {
        viewModelScope.launch {
            gameRepo.updateCoverUrl(gameId, coverUrl)
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم تحديث غلاف اللعبة" else "Game cover updated")
        }
    }

    fun importBiosUri(systemTag: String, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val (path, name) = gameRepo.importBiosFromUri(systemTag, uri)
                settingsRepo.updateBios(systemTag, path, name)
                val isAr = settings.value.language == "ar"
                showToast(if (isAr) "تم حفظ ملف BIOS: $name" else "Saved BIOS: $name")
                pushNotification("${systemTag.uppercase()} BIOS", name)
            }.onFailure {
                showToast(it.localizedMessage ?: "BIOS import failed")
            }
        }
    }

    fun downloadBiosFromUrl(systemTag: String, url: String) {
        viewModelScope.launch {
            runCatching {
                val (path, name) = gameRepo.downloadBiosFromUrl(systemTag, url.trim())
                settingsRepo.updateBios(systemTag, path, name)
                val isAr = settings.value.language == "ar"
                showToast(if (isAr) "تم تحميل وحفظ BIOS: $name" else "Downloaded BIOS: $name")
                pushNotification("${systemTag.uppercase()} BIOS", name)
            }.onFailure {
                val isAr = settings.value.language == "ar"
                showToast(
                    if (isAr) "تعذر تحميل ملف BIOS من الرابط، تحقق من الرابط أو اختره من الهاتف"
                    else "Failed to download BIOS URL; check URL or pick file from device"
                )
            }
        }
    }

    fun clearTempCache() {
        viewModelScope.launch {
            gameRepo.clearTempCache()
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم مسح التخزين المؤقت بنجاح" else "Temporary cache cleared")
        }
    }
}
