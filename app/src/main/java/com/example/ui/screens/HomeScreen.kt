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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameEntity
import com.example.model.RetroSystem
import com.example.ui.components.GameCoverArtwork
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AppNotification
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    games: List<GameEntity>,
    totalSaveCount: Int,
    notifications: List<AppNotification>,
    onPlayGame: (GameEntity) -> Unit,
    onSelectSystem: (RetroSystem) -> Unit,
    onSeeAllLibrary: () -> Unit,
    onUpdateUserName: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    var showNotifications by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember(userName) { mutableStateOf(userName) }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingPrefix = if (hour < 12) strings.greetingMorning else strings.greetingEvening

    val featuredGame = games.firstOrNull()
    val continueGames = if (games.size > 1) games.drop(1) else games
    val totalMinutes = games.sumOf { it.playTimeMinutes }
    val totalHours = totalMinutes / 60

    val systemCounts = remember(games) {
        games.groupingBy { it.systemId.lowercase() }.eachCount()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 104.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header row: Greeting + Notification Bell
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$greetingPrefix، $userName".let {
                        if (strings.navHome == "Home") "$greetingPrefix, $userName" else it
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showNameDialog = true }
                        .padding(vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { showNotifications = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_notifications_button")
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = strings.notificationsTitle,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        // Featured Hero Last Played Card
        if (featuredGame != null) {
            item {
                val sys = RetroSystem.fromId(featuredGame.systemId)
                val formattedTime = formatDuration(featuredGame.playTimeMinutes, strings.hourShort, strings.minuteShort)
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(220.dp)
                        .testTag("home_featured_card")
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
                        .clickable { onPlayGame(featuredGame) }
                ) {
                    GameCoverArtwork(
                        coverUrl = featuredGame.coverUrl,
                        styleIndex = featuredGame.coverStyleIndex,
                        contentDescription = featuredGame.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Bottom readability gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.45f to Color.Black.copy(alpha = 0.22f),
                                    1.0f to Color(0xEA080A12)
                                )
                            )
                    )

                    // White Circular Play Button at top-end
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(18.dp)
                            .size(50.dp)
                            .testTag("home_hero_play_button")
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onPlayGame(featuredGame) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = featuredGame.title,
                            tint = Color(0xFF0A0C14),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Title + Subtitle + Progress Bar at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                            Text(
                                text = featuredGame.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "${sys.shortName} · ${strings.resumeLastSave} · $formattedTime",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Bottom gradient progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(featuredGame.progressPercent.coerceIn(0.08f, 1f))
                                    .height(4.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan))
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Section: Your Systems ("أنظمتك")
        item {
            SectionHeader(
                title = strings.yourSystems,
                actionLabel = strings.all,
                onActionClick = onSeeAllLibrary
            )

            LazyRow(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(RetroSystem.entries) { sys ->
                    val count = systemCounts[sys.id.lowercase()] ?: 0
                    Column(
                        modifier = Modifier
                            .width(78.dp)
                            .height(74.dp)
                            .testTag("home_system_${sys.id}")
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                            .clickable { onSelectSystem(sys) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = sys.shortName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            ),
                            color = if (sys.isComingSoon) WarningAmber else Color.White,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                        if (sys.isComingSoon) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WarningAmber.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = strings.comingSoon,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarningAmber
                                )
                            }
                        } else {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section: Continue Playing ("استمر باللعب")
        item {
            Spacer(Modifier.height(10.dp))
            SectionHeader(
                title = strings.continuePlaying,
                actionLabel = strings.all,
                onActionClick = onSeeAllLibrary
            )

            LazyRow(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continueGames, key = { it.id }) { game ->
                    val sys = RetroSystem.fromId(game.systemId)
                    val timeStr = formatDuration(game.playTimeMinutes, strings.hourShort, strings.minuteShort)
                    Column(
                        modifier = Modifier
                            .width(126.dp)
                            .testTag("home_continue_card_${game.id}")
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onPlayGame(game) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(126.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            GameCoverArtwork(
                                coverUrl = game.coverUrl,
                                styleIndex = game.coverStyleIndex,
                                contentDescription = game.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${sys.shortName} · $timeStr",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Section: Your Stats ("إحصائياتك")
        item {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = strings.yourStats,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    value = games.size.toString(),
                    label = strings.gamesCountLabel,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${totalHours}${strings.hourShort}",
                    label = strings.playTimeLabel,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = totalSaveCount.toString(),
                    label = strings.savesCountLabel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showNotifications) {
        ModalBottomSheet(
            onDismissRequest = { showNotifications = false },
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.notificationsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                notifications.forEach { note ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = SecondaryCyan
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = note.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
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
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onActionClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(vertical = 16.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

fun formatDuration(minutes: Int, hourShort: String, minuteShort: String): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "$h$hourShort $m$minuteShort"
        h > 0 -> "$h$hourShort"
        else -> "${m.coerceAtLeast(1)}$minuteShort"
    }
}
