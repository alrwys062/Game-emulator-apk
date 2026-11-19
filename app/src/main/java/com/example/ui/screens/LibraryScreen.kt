package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.GameEntity
import com.example.model.RetroSystem
import com.example.ui.components.GameCoverArtwork
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalIsArabic
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.BulkCoverProgress
import com.example.viewmodel.LibrarySortOrder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    isFavoritesMode: Boolean,
    totalGamesCount: Int,
    games: List<GameEntity>,
    searchQuery: String,
    selectedSystemId: String?,
    sortOrder: LibrarySortOrder,
    bulkCoverProgress: BulkCoverProgress?,
    aiBusy: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectSystemFilter: (String?) -> Unit,
    onSelectSystemSoon: (RetroSystem) -> Unit,
    onChangeSortOrder: (LibrarySortOrder) -> Unit,
    onImportClick: () -> Unit,
    onFetchAllCovers: () -> Unit,
    onPlayGame: (GameEntity) -> Unit,
    onToggleFavorite: (GameEntity) -> Unit,
    onUpdateCoverUrl: (GameEntity, String?) -> Unit,
    onPickCoverFromPhone: (GameEntity) -> Unit,
    onAutoFetchCover: (GameEntity) -> Unit,
    onRenameGame: (Long, String) -> Unit,
    onChangeSystemAndCore: (Long, RetroSystem, String) -> Unit,
    onManageCheats: (GameEntity) -> Unit,
    onFetchAiGameInfo: (GameEntity) -> Unit,
    onDeleteGame: (GameEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    var gameToManageId by remember { mutableStateOf<Long?>(null) }
    var gameForCoverDialogId by remember { mutableStateOf<Long?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val gameToManage = games.firstOrNull { it.id == gameToManageId }
    val gameForCoverDialog = games.firstOrNull { it.id == gameForCoverDialogId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Title + Fetch All Covers + Sort + Circular '+' Import
        Row(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFavoritesMode) strings.navFavorites else strings.navLibrary,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFavoritesMode && games.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .clickable(enabled = bulkCoverProgress == null, onClick = onFetchAllCovers)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = strings.fetchAllCovers,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        containerColor = SurfaceDark
                    ) {
                        LibrarySortOrder.entries.forEach { order ->
                            val label = when (order) {
                                LibrarySortOrder.LAST_PLAYED -> if (isArabic) "آخر لعب" else "Last Played"
                                LibrarySortOrder.NAME_ASC -> if (isArabic) "الاسم (أ-ي)" else "Name (A-Z)"
                                LibrarySortOrder.PLAY_TIME_DESC -> if (isArabic) "الأكثر لعباً" else "Most Played"
                                LibrarySortOrder.SYSTEM -> if (isArabic) "حسب النظام" else "By System"
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        label,
                                        color = if (order == sortOrder) SecondaryCyan else Color.White
                                    )
                                },
                                onClick = {
                                    onChangeSortOrder(order)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onImportClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("library_import_button")
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = strings.importRom,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Bulk Cover Fetch Progress Banner
        if (bulkCoverProgress != null) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "جلب الأغلفة: ${bulkCoverProgress.currentTitle}" else "Fetching cover: ${bulkCoverProgress.currentTitle}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${bulkCoverProgress.current} / ${bulkCoverProgress.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryCyan
                    )
                }
                LinearProgressIndicator(
                    progress = { bulkCoverProgress.current.toFloat() / bulkCoverProgress.total.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth(),
                    color = SecondaryCyan,
                    trackColor = SurfaceDark
                )
            }
        }

        // Capsule Search Bar
        Row(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, BorderSubtle, RoundedCornerShape(50))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = strings.searchPlaceholder(totalGamesCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    cursorBrush = SolidColor(SecondaryCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )
            }
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Horizontal System Filter Pills
        LazyRow(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipPill(
                    label = strings.all,
                    active = selectedSystemId == null,
                    isSoon = false,
                    onClick = { onSelectSystemFilter(null) }
                )
            }
            items(RetroSystem.entries) { sys ->
                val active = selectedSystemId.equals(sys.id, ignoreCase = true)
                FilterChipPill(
                    label = if (sys.isComingSoon) "${sys.shortName} · ${strings.comingSoon}" else sys.shortName,
                    active = active,
                    isSoon = sys.isComingSoon,
                    onClick = {
                        if (sys.isComingSoon) {
                            onSelectSystemSoon(sys)
                        } else {
                            onSelectSystemFilter(if (active) null else sys.id)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavoritesMode) Icons.Outlined.FavoriteBorder else Icons.Rounded.SportsEsports,
                            contentDescription = null,
                            tint = SecondaryCyan,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (isFavoritesMode) strings.emptyFavoritesTitle else strings.emptyLibraryTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isFavoritesMode) strings.emptyFavoritesHint else strings.emptyLibraryHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (!isFavoritesMode) {
                        Spacer(Modifier.height(18.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                                .clickable(onClick = onImportClick)
                                .padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = strings.importRom,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 800.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 108.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(games, key = { it.id }) { game ->
                    val sys = RetroSystem.fromId(game.systemId)
                    val genre = if (isArabic) game.genreAr else game.genreEn
                    val duration = formatDuration(game.playTimeMinutes, strings.hourShort, strings.minuteShort)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("library_game_card_${game.id}")
                            .clip(RoundedCornerShape(22.dp))
                            .combinedClickable(
                                onClick = { onPlayGame(game) },
                                onLongClick = { gameToManageId = game.id }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            GameCoverArtwork(
                                coverUrl = game.coverUrl,
                                styleIndex = game.coverStyleIndex,
                                contentDescription = game.title,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.62f))
                                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = sys.shortName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { onToggleFavorite(game) },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(2.dp)
                                    .size(48.dp)
                                    .testTag("favorite_toggle_${game.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.48f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (game.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = strings.favoritesOnly,
                                        tint = if (game.isFavorite) ErrorRose else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { gameToManageId = game.id },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(2.dp)
                                    .size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.52f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = strings.renameGame,
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { gameForCoverDialogId = game.id },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp)
                                    .size(48.dp)
                                    .testTag("cover_url_button_${game.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.52f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Image,
                                        contentDescription = strings.setCoverUrlButton,
                                        tint = SecondaryCyan,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$genre · $duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    gameToManage?.let { selected ->
        GameManageDialog(
            game = selected,
            aiBusy = aiBusy,
            onDismiss = { gameToManageId = null },
            onPlay = { onPlayGame(selected) },
            onRename = { newTitle -> onRenameGame(selected.id, newTitle) },
            onChangeSystemAndCore = { sys, core -> onChangeSystemAndCore(selected.id, sys, core) },
            onOpenCoverDialog = { gameForCoverDialogId = selected.id },
            onManageCheats = { onManageCheats(selected) },
            onFetchAiInfo = { onFetchAiGameInfo(selected) },
            onDelete = { onDeleteGame(selected) }
        )
    }

    gameForCoverDialog?.let { target ->
        CoverManagerDialog(
            game = target,
            onDismiss = { gameForCoverDialogId = null },
            onSaveUrl = { url -> onUpdateCoverUrl(target, url) },
            onPickFromPhone = { onPickCoverFromPhone(target) },
            onAutoFetchOnline = { onAutoFetchCover(target) }
        )
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    active: Boolean,
    isSoon: Boolean,
    onClick: () -> Unit
) {
    val bgModifier = if (active) {
        Modifier.background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
    } else {
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isSoon) WarningAmber.copy(alpha = 0.35f) else BorderSubtle,
                RoundedCornerShape(50)
            )
    }

    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .then(bgModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = when {
                active -> Color.White
                isSoon -> WarningAmber
                else -> TextSecondary
            },
            maxLines = 1
        )
    }
}
