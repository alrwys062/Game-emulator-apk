package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.GameEntity
import com.example.model.RetroSystem
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalIsArabic
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary

@Composable
fun CoverManagerDialog(
    game: GameEntity,
    onDismiss: () -> Unit,
    onSaveUrl: (String?) -> Unit,
    onPickFromPhone: () -> Unit,
    onAutoFetchOnline: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    var draftUrl by remember(game.id) { mutableStateOf(game.coverUrl.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(strings.setCoverUrlTitle, color = Color.White) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(game.title, style = MaterialTheme.typography.labelLarge, color = SecondaryCyan)

                OutlinedTextField(
                    value = draftUrl,
                    onValueChange = { draftUrl = it },
                    placeholder = { Text(strings.setCoverUrlHint, color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cover_url_input")
                )

                Text(
                    text = if (isArabic)
                        "يدعم الروابط المباشرة وروابط GitHub (يحوّل github.com/.../blob/... تلقائياً إلى raw)"
                    else
                        "Supports direct URLs and GitHub links (auto-converts github.com/.../blob/... to raw)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                DialogActionTile(
                    icon = Icons.Rounded.PhotoLibrary,
                    tint = SecondaryCyan,
                    label = strings.pickCoverFromPhone,
                    onClick = {
                        onDismiss()
                        onPickFromPhone()
                    }
                )

                DialogActionTile(
                    icon = Icons.Rounded.AutoAwesome,
                    tint = PrimaryPurple,
                    label = strings.autoFetchCoverOnline,
                    onClick = {
                        onDismiss()
                        onAutoFetchOnline()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSaveUrl(draftUrl.trim().ifEmpty { null })
                onDismiss()
            }) {
                Text(strings.saveAction, color = SecondaryCyan)
            }
        },
        dismissButton = {
            Row {
                if (!game.coverUrl.isNullOrBlank()) {
                    TextButton(onClick = {
                        onSaveUrl(null)
                        onDismiss()
                    }) {
                        Text(strings.clearCoverUrl, color = ErrorRose)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(strings.cancelAction, color = TextSecondary)
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameManageDialog(
    game: GameEntity,
    aiBusy: Boolean,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onRename: (String) -> Unit,
    onChangeSystemAndCore: (RetroSystem, String) -> Unit,
    onOpenCoverDialog: () -> Unit,
    onManageCheats: () -> Unit,
    onFetchAiInfo: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current
    val currentSys = RetroSystem.fromId(game.systemId)

    var showRename by remember { mutableStateOf(false) }
    var draftTitle by remember(game.id) { mutableStateOf(game.title) }
    var showSystemPicker by remember { mutableStateOf(false) }
    var pickedSystem by remember(game.id) { mutableStateOf(currentSys) }
    var pickedCore by remember(game.id) { mutableStateOf(game.coreOverride) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Column {
                Text(game.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${currentSys.shortName} • ${game.coreOverride} • ${game.fileName}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialogActionTile(
                    icon = Icons.Rounded.PlayArrow,
                    tint = Color.White,
                    label = if (isArabic) "تشغيل اللعبة الآن" else "Launch Game",
                    highlighted = true,
                    onClick = {
                        onDismiss()
                        onPlay()
                    }
                )

                DialogActionTile(
                    icon = Icons.Rounded.Edit,
                    tint = SecondaryCyan,
                    label = strings.renameGame,
                    onClick = { showRename = !showRename }
                )

                if (showRename) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = draftTitle,
                            onValueChange = { draftTitle = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            onRename(draftTitle)
                            showRename = false
                        }) {
                            Text(strings.saveAction, color = SecondaryCyan)
                        }
                    }
                }

                DialogActionTile(
                    icon = Icons.Rounded.Memory,
                    tint = PrimaryPurple,
                    label = strings.changeSystemCore,
                    onClick = { showSystemPicker = !showSystemPicker }
                )

                if (showSystemPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "اختر النظام:" else "Select System:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RetroSystem.playableEntries.forEach { sys ->
                                val selected = sys == pickedSystem
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) PrimaryPurple else SurfaceDark)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable {
                                            pickedSystem = sys
                                            pickedCore = sys.ejsCore
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(sys.shortName, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (pickedSystem.availableCores.isNotEmpty()) {
                            Text(
                                text = if (isArabic) "اختر النواة (Core):" else "Select Core:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                pickedSystem.availableCores.forEach { core ->
                                    val selected = core == pickedCore
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selected) SecondaryCyan.copy(alpha = 0.25f) else SurfaceDark)
                                            .border(
                                                1.dp,
                                                if (selected) SecondaryCyan else BorderSubtle,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { pickedCore = core }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(core, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                onChangeSystemAndCore(pickedSystem, pickedCore)
                                showSystemPicker = false
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(strings.saveAction, color = SecondaryCyan)
                        }
                    }
                }

                DialogActionTile(
                    icon = Icons.Rounded.Code,
                    tint = SecondaryCyan,
                    label = strings.manageCheats,
                    onClick = {
                        onDismiss()
                        onManageCheats()
                    }
                )

                DialogActionTile(
                    icon = Icons.Rounded.Image,
                    tint = SecondaryCyan,
                    label = strings.setCoverUrlButton,
                    onClick = {
                        onDismiss()
                        onOpenCoverDialog()
                    }
                )

                DialogActionTile(
                    icon = Icons.Rounded.Psychology,
                    tint = PrimaryPurple,
                    label = if (isArabic) "معلومات اللعبة بالذكاء الاصطناعي (Gemini)" else "AI Game Summary (Gemini)",
                    trailingBusy = aiBusy,
                    onClick = onFetchAiInfo
                )

                if (!game.aiNotes.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = game.aiNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = ErrorRose)
                Spacer(Modifier.width(4.dp))
                Text(strings.deleteGame, color = ErrorRose)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = TextSecondary)
            }
        }
    )
}

@Composable
private fun DialogActionTile(
    icon: ImageVector,
    tint: Color,
    label: String,
    highlighted: Boolean = false,
    trailingBusy: Boolean = false,
    onClick: () -> Unit
) {
    val bgModifier = if (highlighted) {
        Modifier.background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
    } else {
        Modifier
            .background(SurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(bgModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (trailingBusy) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SecondaryCyan)
        }
    }
}
