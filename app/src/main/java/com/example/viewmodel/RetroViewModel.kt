package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.input.InputManager
import android.net.Uri
import android.view.InputDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CheatEntity
import com.example.data.local.GameEntity
import com.example.data.local.RetroDatabase
import com.example.data.local.SaveStateEntity
import com.example.data.preferences.ControlButtonPlacement
import com.example.data.preferences.RetroSettings
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.GameRepository
import com.example.model.RetroSystem
import com.example.service.AiPerfRecommendation
import com.example.service.DeviceSpecSummary
import com.example.service.GeminiAssistantService
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
import java.io.File
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

data class BulkCoverProgress(
    val current: Int,
    val total: Int,
    val currentTitle: String
)

data class AiChatMessage(
    val isUser: Boolean,
    val text: String
)

enum class LibrarySortOrder {
    LAST_PLAYED,
    NAME_ASC,
    PLAY_TIME_DESC,
    SYSTEM
}

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

    val deviceSpec: DeviceSpecSummary = DeviceSpecSummary.inspect(application)

    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    private val _forceShowOnboarding = MutableStateFlow(false)
    val forceShowOnboarding: StateFlow<Boolean> = _forceShowOnboarding.asStateFlow()

    private val _activeTab = MutableStateFlow(MainTab.HOME)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _selectedSystemFilter = MutableStateFlow<String?>(null)
    val selectedSystemFilter: StateFlow<String?> = _selectedSystemFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(LibrarySortOrder.LAST_PLAYED)
    val sortOrder: StateFlow<LibrarySortOrder> = _sortOrder.asStateFlow()

    private val _playingGame = MutableStateFlow<GameEntity?>(null)
    val playingGame: StateFlow<GameEntity?> = _playingGame.asStateFlow()

    private val _isEngineTestMode = MutableStateFlow(false)
    val isEngineTestMode: StateFlow<Boolean> = _isEngineTestMode.asStateFlow()

    private val _cheatManagerGame = MutableStateFlow<GameEntity?>(null)
    val cheatManagerGame: StateFlow<GameEntity?> = _cheatManagerGame.asStateFlow()

    private val _activeStoreBrowserUrl = MutableStateFlow<Pair<String, String>?>(null)
    val activeStoreBrowserUrl: StateFlow<Pair<String, String>?> = _activeStoreBrowserUrl.asStateFlow()

    private val _downloadTask = MutableStateFlow<DownloadTaskState?>(null)
    val downloadTask: StateFlow<DownloadTaskState?> = _downloadTask.asStateFlow()

    private val _bulkCoverProgress = MutableStateFlow<BulkCoverProgress?>(null)
    val bulkCoverProgress: StateFlow<BulkCoverProgress?> = _bulkCoverProgress.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _connectedGamepadName = MutableStateFlow<String?>(null)
    val connectedGamepadName: StateFlow<String?> = _connectedGamepadName.asStateFlow()

    private val _storageUsedBytes = MutableStateFlow(0L)
    val storageUsedBytes: StateFlow<Long> = _storageUsedBytes.asStateFlow()

    private val _lastDiagnosticLogs = MutableStateFlow("")
    val lastDiagnosticLogs: StateFlow<String> = _lastDiagnosticLogs.asStateFlow()

    // AI Assistant state
    private val _aiBusy = MutableStateFlow(false)
    val aiBusy: StateFlow<Boolean> = _aiBusy.asStateFlow()

    private val _aiChatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiChatMessages: StateFlow<List<AiChatMessage>> = _aiChatMessages.asStateFlow()

    private val _aiDiagnosticResult = MutableStateFlow<String?>(null)
    val aiDiagnosticResult: StateFlow<String?> = _aiDiagnosticResult.asStateFlow()

    private val _aiPerfRecommendation = MutableStateFlow<AiPerfRecommendation?>(null)
    val aiPerfRecommendation: StateFlow<AiPerfRecommendation?> = _aiPerfRecommendation.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = 1L,
                title = "RetroVerse Engine v2",
                detail = "Isolated Iframe Cores • 23 Systems • CWCheat & Gemini AI",
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeGameSaveStates: StateFlow<List<SaveStateEntity>> = _playingGame
        .flatMapLatest { game ->
            if (game == null || _isEngineTestMode.value) flowOf(emptyList()) else gameRepo.observeSaveStates(game.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeGameCheats: StateFlow<List<CheatEntity>> = combine(_playingGame, _cheatManagerGame) { p, m -> m ?: p }
        .flatMapLatest { game ->
            if (game == null || game.id <= 0L) flowOf(emptyList()) else gameRepo.observeCheats(game.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredGames: StateFlow<List<GameEntity>> = combine(
        allGames,
        _selectedSystemFilter,
        _searchQuery,
        _activeTab,
        _sortOrder
    ) { games, sysFilter, query, tab, sort ->
        val filtered = games.filter { game ->
            val matchesFav = if (tab == MainTab.FAVORITES) game.isFavorite else true
            val matchesSys = sysFilter == null || game.systemId.equals(sysFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                game.title.contains(query, ignoreCase = true) ||
                game.systemId.contains(query, ignoreCase = true) ||
                game.fileName.contains(query, ignoreCase = true)
            matchesFav && matchesSys && matchesQuery
        }
        when (sort) {
            LibrarySortOrder.LAST_PLAYED -> filtered.sortedByDescending { it.lastPlayedAt }
            LibrarySortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            LibrarySortOrder.PLAY_TIME_DESC -> filtered.sortedByDescending { it.playTimeMinutes }
            LibrarySortOrder.SYSTEM -> filtered.sortedBy { it.systemId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var playSessionStartMs: Long = 0L
    private var downloadJob: Job? = null

    init {
        viewModelScope.launch {
            gameRepo.ensureSeededHomebrew()
            refreshConnectedGamepads()
            refreshStorageUsage()
            delay(1300L)
            _splashFinished.value = true
        }
    }

    fun refreshStorageUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            _storageUsedBytes.value = gameRepo.calculateStorageUsageBytes()
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
        val prev = _connectedGamepadName.value
        val next = names.firstOrNull()
        _connectedGamepadName.value = next
        if (next != null && prev != next) {
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم توصيل يد التحكم: $next" else "Controller connected: $next")
        }
    }

    fun selectTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun setSortOrder(order: LibrarySortOrder) {
        _sortOrder.value = order
    }

    fun openSystemInLibrary(system: RetroSystem) {
        if (system.isComingSoon) {
            val isAr = settings.value.language == "ar"
            showToast(
                if (isAr) "محاكاة PlayStation 2 غير مدعومة بعد في محرك المتصفح (قريباً). يمكنك تجهيز البيوس والأكواد مسبقاً."
                else "PlayStation 2 emulation is coming soon. You can pre-configure PS2 BIOS & cheats in Settings."
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
        val sys = RetroSystem.fromId(game.systemId)
        if (sys.isComingSoon) {
            openSystemInLibrary(sys)
            return
        }
        _isEngineTestMode.value = false
        _aiDiagnosticResult.value = null
        playSessionStartMs = System.currentTimeMillis()
        _playingGame.value = game
    }

    fun launchEngineTest() {
        _isEngineTestMode.value = true
        _aiDiagnosticResult.value = null
        playSessionStartMs = System.currentTimeMillis()
        _playingGame.value = GameEntity(
            id = -99L,
            title = "RetroVerse Engine Test (NES)",
            systemId = RetroSystem.NES.id,
            coreOverride = "nes",
            fileName = "engine_test.nes",
            localFilePath = null,
            cartridgeMode = "test",
            coverStyleIndex = 6,
            genreAr = "اختبار",
            genreEn = "Test",
            fileSizeBytes = 24592L,
            playTimeMinutes = 0,
            progressPercent = 1f,
            isFavorite = false,
            lastPlayedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
    }

    fun exitGame() {
        val game = _playingGame.value ?: return
        val wasTest = _isEngineTestMode.value
        _isEngineTestMode.value = false
        _playingGame.value = null
        if (!wasTest && game.id > 0) {
            val elapsedMin = ((System.currentTimeMillis() - playSessionStartMs) / 60_000L).toInt().coerceAtLeast(1)
            val nextProgress = (game.progressPercent + 0.04f).coerceAtMost(1.0f)
            viewModelScope.launch {
                gameRepo.recordPlaySession(game.id, elapsedMin, nextProgress)
            }
        }
    }

    fun updateDiagnosticLogs(logs: String) {
        _lastDiagnosticLogs.value = logs
    }

    fun toggleFavorite(game: GameEntity) {
        viewModelScope.launch { gameRepo.toggleFavorite(game) }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            gameRepo.deleteGame(game)
            refreshStorageUsage()
        }
    }

    fun renameGame(gameId: Long, newTitle: String) {
        viewModelScope.launch {
            gameRepo.updateTitle(gameId, newTitle)
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم تعديل اسم اللعبة" else "Game renamed")
        }
    }

    fun updateGameSystemAndCore(gameId: Long, system: RetroSystem, core: String) {
        viewModelScope.launch {
            gameRepo.updateSystemAndCore(gameId, system.id, core)
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم تحديث النظام والنواة" else "System & core updated")
        }
    }

    fun importRomUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _downloadTask.value = DownloadTaskState("ROM...", 0.35f, true)
            runCatching {
                val imported = gameRepo.importRomUris(uris)
                _downloadTask.value = DownloadTaskState(imported.lastOrNull()?.fileName ?: "ROM", 1.0f, false)
                refreshStorageUsage()
                val isAr = settings.value.language == "ar"
                val msg = if (imported.size == 1) {
                    val g = imported.first()
                    val sysName = RetroSystem.fromId(g.systemId).shortName
                    if (isAr) "تم استيراد ${g.title} ($sysName) بنجاح" else "Imported ${g.title} ($sysName)"
                } else {
                    if (isAr) "تم استيراد ${imported.size} ألعاب بنجاح" else "Imported ${imported.size} games"
                }
                showToast(msg)
                pushNotification("ROM Import", msg)
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
                refreshStorageUsage()
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

    // Covers (4 methods + bulk fetch)
    fun updateGameCoverUrl(gameId: Long, coverUrl: String?) {
        viewModelScope.launch {
            gameRepo.updateCoverFromUrlOrCache(gameId, coverUrl)
            refreshStorageUsage()
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم تحديث غلاف اللعبة" else "Game cover updated")
        }
    }

    fun updateGameCoverFromDeviceUri(gameId: Long, uri: Uri) {
        viewModelScope.launch {
            val ok = gameRepo.updateCoverFromDeviceUri(gameId, uri)
            refreshStorageUsage()
            val isAr = settings.value.language == "ar"
            showToast(
                if (ok) (if (isAr) "تم حفظ غلاف اللعبة من الهاتف" else "Saved cover image")
                else (if (isAr) "تعذر قراءة الصورة" else "Failed to read image")
            )
        }
    }

    fun autoFetchCoverForGame(game: GameEntity) {
        viewModelScope.launch {
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "جاري البحث عن غلاف ${game.title}..." else "Searching cover for ${game.title}...")
            var ok = gameRepo.autoFetchCoverForGame(game)
            if (!ok && settings.value.geminiApiKey.isNotBlank()) {
                val sysName = RetroSystem.fromId(game.systemId).fullNameEn
                val aiName = GeminiAssistantService.suggestOfficialCoverTitle(
                    settings.value.geminiApiKey,
                    settings.value.geminiModel,
                    game.fileName,
                    sysName,
                    isAr
                ).getOrNull()
                if (!aiName.isNullOrBlank()) {
                    ok = gameRepo.autoFetchCoverForGame(game, aiName)
                }
            }
            refreshStorageUsage()
            showToast(
                if (ok) (if (isAr) "تم تحميل وحفظ الغلاف تلقائياً ✓" else "Cover downloaded & cached ✓")
                else (if (isAr) "لم يُعثر على غلاف تلقائي، يمكنك لصق رابط أو اختيار صورة" else "Cover not found online; paste URL or pick image")
            )
        }
    }

    fun fetchAllMissingCovers() {
        if (_bulkCoverProgress.value != null) return
        viewModelScope.launch {
            val targets = allGames.value.filter { it.coverUrl.isNullOrBlank() }
            val isAr = settings.value.language == "ar"
            if (targets.isEmpty()) {
                showToast(if (isAr) "جميع الألعاب لديها أغلفة بالفعل!" else "All games already have covers!")
                return@launch
            }
            var found = 0
            targets.forEachIndexed { index, game ->
                _bulkCoverProgress.value = BulkCoverProgress(index + 1, targets.size, game.title)
                if (gameRepo.autoFetchCoverForGame(game)) found++
            }
            _bulkCoverProgress.value = null
            refreshStorageUsage()
            showToast(
                if (isAr) "تم جلب $found من ${targets.size} غلافاً وحفظها للعمل بدون إنترنت"
                else "Fetched & cached $found of ${targets.size} covers"
            )
        }
    }

    // Cheats management
    fun openCheatManager(game: GameEntity) {
        _cheatManagerGame.value = game
    }

    fun closeCheatManager() {
        _cheatManagerGame.value = null
    }

    fun addCheat(gameId: Long, name: String, code: String, cheatType: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            gameRepo.addCheat(gameId, name, code, cheatType)
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تمت إضافة كود الغش" else "Cheat added")
        }
    }

    fun toggleCheat(cheatId: Long, enabled: Boolean) {
        viewModelScope.launch { gameRepo.toggleCheat(cheatId, enabled) }
    }

    fun deleteCheat(cheatId: Long) {
        viewModelScope.launch { gameRepo.deleteCheat(cheatId) }
    }

    fun importCheatFile(game: GameEntity, uri: Uri) {
        viewModelScope.launch {
            val isAr = settings.value.language == "ar"
            runCatching {
                val count = gameRepo.importCheatFileForGame(game, uri)
                showToast(
                    if (count > 0) {
                        if (isAr) "تم استيراد $count كود غش لـ ${game.title}" else "Imported $count cheats for ${game.title}"
                    } else {
                        if (isAr) "لم يتم العثور على أكواد مطابقة في الملف" else "No matching cheats found in file"
                    }
                )
            }.onFailure {
                showToast(it.localizedMessage ?: "Cheat import failed")
            }
        }
    }

    // Store & Saves
    fun openStoreBrowser(title: String, url: String) {
        _activeStoreBrowserUrl.value = title to url
    }

    fun closeStoreBrowser() {
        _activeStoreBrowserUrl.value = null
    }

    fun saveStateToSlot(slotIndex: Int, stateJson: String, screenshotBase64: String?) {
        val game = _playingGame.value ?: return
        if (_isEngineTestMode.value || game.id <= 0L) return
        viewModelScope.launch {
            gameRepo.saveStateToSlot(game, slotIndex, stateJson, screenshotBase64)
            val msg = if (settings.value.language == "ar") {
                if (slotIndex == 0) "تم الحفظ التلقائي" else "تم الحفظ في الخانة $slotIndex"
            } else {
                if (slotIndex == 0) "Auto-saved" else "Saved to Slot $slotIndex"
            }
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

    // Settings updates
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
    fun updateAccentPreset(preset: String) = viewModelScope.launch { settingsRepo.updateAccentPreset(preset) }
    fun updateUiScale(scale: Float) = viewModelScope.launch { settingsRepo.updateUiScale(scale) }
    fun updateFilterMode(filter: String) = viewModelScope.launch { settingsRepo.updateFilterMode(filter) }
    fun updateScanlines(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateScanlines(enabled) }
    fun updateAspectRatio(ratio: String) = viewModelScope.launch { settingsRepo.updateAspectRatio(ratio) }
    fun updateResolutionScale(scale: Int) = viewModelScope.launch { settingsRepo.updateResolutionScale(scale) }
    fun updateRenderScalePercent(pct: Int) = viewModelScope.launch { settingsRepo.updateRenderScalePercent(pct) }
    fun updateShowFps(show: Boolean) = viewModelScope.launch { settingsRepo.updateShowFps(show) }
    fun updateOrientationLock(lock: String) = viewModelScope.launch { settingsRepo.updateOrientationLock(lock) }
    fun updateWakeLock(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateWakeLock(enabled) }
    fun applyPerformancePreset(preset: String) = viewModelScope.launch { settingsRepo.applyPerformancePreset(preset) }
    fun updateFrameSkip(skip: Int) = viewModelScope.launch { settingsRepo.updateFrameSkip(skip) }
    fun updateVsync(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateVsync(enabled) }
    fun updateFpsCap(cap: Int) = viewModelScope.launch { settingsRepo.updateFpsCap(cap) }
    fun updateSmartPerf(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateSmartPerf(enabled) }
    fun updatePreloadRoms(enabled: Boolean) = viewModelScope.launch { settingsRepo.updatePreloadRoms(enabled) }
    fun updateSelectedCore(core: String) = viewModelScope.launch { settingsRepo.updateSelectedCore(core) }
    fun updateWasmThreadsMode(mode: String) = viewModelScope.launch { settingsRepo.updateWasmThreadsMode(mode) }
    fun updateRewindEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateRewindEnabled(enabled) }
    fun updateFastForwardSpeed(speed: Int) = viewModelScope.launch { settingsRepo.updateFastForwardSpeed(speed) }
    fun updateCdnMode(mode: String) = viewModelScope.launch { settingsRepo.updateCdnMode(mode) }
    fun updateCustomDataPath(path: String) = viewModelScope.launch { settingsRepo.updateCustomDataPath(path) }
    fun updateSoundVolume(vol: Float) = viewModelScope.launch { settingsRepo.updateSoundVolume(vol) }
    fun updateAudioMuted(muted: Boolean) = viewModelScope.launch { settingsRepo.updateAudioMuted(muted) }
    fun updateAudioLatency(lat: String) = viewModelScope.launch { settingsRepo.updateAudioLatency(lat) }
    fun updateControlsOpacity(op: Float) = viewModelScope.launch { settingsRepo.updateControlsOpacity(op) }
    fun updateControlsScale(sc: Float) = viewModelScope.launch { settingsRepo.updateControlsScale(sc) }
    fun updateMoveControls(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateMoveControls(enabled) }
    fun updateHaptics(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateHaptics(enabled) }
    fun updateAutoHideTouchOnGamepad(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateAutoHideTouchOnGamepad(enabled) }
    fun updateGamepadDeadzone(dz: Float) = viewModelScope.launch { settingsRepo.updateGamepadDeadzone(dz) }
    fun updateAutoSaveOnExit(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateAutoSaveOnExit(enabled) }
    fun updateCheatsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.updateCheatsEnabled(enabled) }
    fun updateCheatCodes(codes: String) = viewModelScope.launch { settingsRepo.updateCheatCodes(codes) }
    fun updatePspWarning(enabled: Boolean) = viewModelScope.launch { settingsRepo.updatePspWarning(enabled) }
    fun updateGeminiConfig(apiKey: String, model: String) = viewModelScope.launch {
        settingsRepo.updateGeminiConfig(apiKey, model)
        val isAr = settings.value.language == "ar"
        showToast(if (isAr) "تم حفظ إعدادات Gemini AI محلياً" else "Gemini AI config saved locally")
    }

    fun saveControlPlacement(systemProfile: String, isLandscape: Boolean, controlId: String, placement: ControlButtonPlacement) {
        viewModelScope.launch {
            settingsRepo.saveControlPlacement(systemProfile, isLandscape, controlId, placement)
        }
    }

    fun resetControlLayout(systemProfile: String, isLandscape: Boolean) {
        viewModelScope.launch {
            settingsRepo.resetControlLayout(systemProfile, isLandscape)
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تمت إعادة ضبط التخطيط" else "Layout reset")
        }
    }

    fun saveKeyBinding(playerIndex: Int, actionId: String, keyCode: Int) {
        viewModelScope.launch { settingsRepo.saveKeyBinding(playerIndex, actionId, keyCode) }
    }

    fun resetKeyBindings(playerIndex: Int) {
        viewModelScope.launch { settingsRepo.resetKeyBindings(playerIndex) }
    }

    // BIOS
    fun importBiosUri(slotKey: String, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val entry = gameRepo.importBiosFromUri(slotKey, uri)
                settingsRepo.updateBiosEntry(entry)
                refreshStorageUsage()
                val isAr = settings.value.language == "ar"
                showToast(if (isAr) "تم حفظ BIOS (${entry.name})" else "Saved BIOS (${entry.name})")
                pushNotification("${slotKey.uppercase()} BIOS", "${entry.name} • MD5: ${entry.md5.take(8)}...")
            }.onFailure {
                showToast(it.localizedMessage ?: "BIOS import failed")
            }
        }
    }

    fun downloadBiosFromUrl(slotKey: String, url: String) {
        viewModelScope.launch {
            runCatching {
                val entry = gameRepo.downloadBiosFromUrl(slotKey, url.trim())
                settingsRepo.updateBiosEntry(entry)
                refreshStorageUsage()
                val isAr = settings.value.language == "ar"
                showToast(if (isAr) "تم تحميل وحفظ BIOS: ${entry.name}" else "Downloaded BIOS: ${entry.name}")
            }.onFailure {
                val isAr = settings.value.language == "ar"
                showToast(
                    if (isAr) "تعذر تحميل ملف BIOS من الرابط، اختره من الهاتف"
                    else "Failed to download BIOS URL; pick file from device"
                )
            }
        }
    }

    fun removeBios(slotKey: String) {
        viewModelScope.launch {
            val existing = settings.value.getBiosEntry(slotKey)
            if (existing != null) {
                runCatching { File(existing.path).delete() }
            }
            settingsRepo.removeBiosEntry(slotKey)
            refreshStorageUsage()
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم حذف ملف BIOS" else "BIOS removed")
        }
    }

    // Gemini AI Actions
    fun askAiErrorDiagnosis(errorDetail: String, logs: String, gameTitle: String, systemName: String) {
        viewModelScope.launch {
            _aiBusy.value = true
            _aiDiagnosticResult.value = null
            val s = settings.value
            val isAr = s.language == "ar"
            val res = GeminiAssistantService.diagnoseErrorLog(
                apiKey = s.geminiApiKey,
                model = s.geminiModel,
                errorDetail = errorDetail,
                logs = logs,
                gameTitle = gameTitle,
                systemName = systemName,
                device = deviceSpec,
                isArabic = isAr
            )
            _aiBusy.value = false
            res.onSuccess { _aiDiagnosticResult.value = it }
                .onFailure { _aiDiagnosticResult.value = it.localizedMessage ?: "AI Error" }
        }
    }

    fun requestAiPerfRecommendation(gameTitle: String, systemName: String) {
        viewModelScope.launch {
            _aiBusy.value = true
            val s = settings.value
            val isAr = s.language == "ar"
            val res = GeminiAssistantService.recommendPerformance(
                apiKey = s.geminiApiKey,
                model = s.geminiModel,
                systemName = systemName,
                gameTitle = gameTitle,
                device = deviceSpec,
                isArabic = isAr
            )
            _aiBusy.value = false
            res.onSuccess { _aiPerfRecommendation.value = it }
                .onFailure { showToast(it.localizedMessage ?: "AI error") }
        }
    }

    fun applyAiPerfRecommendation(rec: AiPerfRecommendation) {
        viewModelScope.launch {
            settingsRepo.updateFrameSkip(rec.frameSkip)
            settingsRepo.updateResolutionScale(rec.resolutionScale)
            settingsRepo.updateRenderScalePercent(rec.renderScalePercent)
            settingsRepo.updateFpsCap(rec.fpsCap)
            settingsRepo.updateWasmThreadsMode(rec.threadsMode)
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم تطبيق توصيات الذكاء الاصطناعي ✓" else "AI performance settings applied ✓")
        }
    }

    fun fetchAiGameInfo(game: GameEntity) {
        viewModelScope.launch {
            _aiBusy.value = true
            val s = settings.value
            val isAr = s.language == "ar"
            val sysName = RetroSystem.fromId(game.systemId).fullNameEn
            val res = GeminiAssistantService.fetchGameInfo(
                apiKey = s.geminiApiKey,
                model = s.geminiModel,
                gameTitle = game.title,
                systemName = sysName,
                isArabic = isAr
            )
            _aiBusy.value = false
            res.onSuccess { info ->
                gameRepo.updateAiNotes(game.id, info)
                showToast(if (isAr) "تم جلب معلومات اللعبة من Gemini ✓" else "Game info fetched from Gemini ✓")
            }.onFailure {
                showToast(it.localizedMessage ?: "AI error")
            }
        }
    }

    fun sendAiChatPrompt(userText: String) {
        val clean = userText.trim()
        if (clean.isEmpty()) return
        _aiChatMessages.value = _aiChatMessages.value + AiChatMessage(true, clean)
        viewModelScope.launch {
            _aiBusy.value = true
            val s = settings.value
            val isAr = s.language == "ar"
            val sysPrompt = if (isAr) {
                "أنت مساعد تطبيق RetroVerse الخبير في الألعاب الكلاسيكية وملفات BIOS وأكواد الغش (CWCheat/GameShark/Game Genie) وإعدادات EmulatorJS. أجب باختصار ووضوح بالعربية:\n$clean"
            } else {
                "You are RetroVerse's expert assistant for retro consoles, BIOS files, cheat codes (CWCheat/GameShark/Game Genie), and EmulatorJS settings. Answer concisely:\n$clean"
            }
            val res = GeminiAssistantService.callGemini(s.geminiApiKey, s.geminiModel, sysPrompt, isAr)
            _aiBusy.value = false
            val reply = res.getOrElse { it.localizedMessage ?: "AI Error" }
            _aiChatMessages.value = _aiChatMessages.value + AiChatMessage(false, reply)
        }
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = settingsRepo.exportSettingsJson()
            onResult(json)
        }
    }

    fun importBackupJson(json: String) {
        viewModelScope.launch {
            val isAr = settings.value.language == "ar"
            runCatching {
                settingsRepo.importSettingsJson(json)
                showToast(if (isAr) "تمت استعادة الإعدادات والتخطيطات بنجاح" else "Backup restored successfully")
            }.onFailure {
                showToast(if (isAr) "صيغة ملف النسخة الاحتياطية غير صالحة" else "Invalid backup JSON format")
            }
        }
    }

    fun clearTempCache() {
        viewModelScope.launch {
            gameRepo.clearTempCache()
            refreshStorageUsage()
            val isAr = settings.value.language == "ar"
            showToast(if (isAr) "تم مسح التخزين المؤقت بنجاح" else "Temporary cache cleared")
        }
    }
}
