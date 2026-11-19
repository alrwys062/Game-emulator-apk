package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.ControlButtonPlacement
import com.example.data.preferences.RetroSettings
import com.example.model.ControllerProfile
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.LocalIsArabic
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Official EmulatorJS GameManager simulateInput indices (0..23)
 */
object EjsButtonIndex {
    const val B_CROSS = 0
    const val Y_SQUARE = 1
    const val SELECT = 2
    const val START = 3
    const val UP = 4
    const val DOWN = 5
    const val LEFT = 6
    const val RIGHT = 7
    const val A_CIRCLE = 8
    const val X_TRIANGLE = 9
    const val L1 = 10
    const val R1 = 11
    const val L2 = 12
    const val R2 = 13
    const val L3 = 14
    const val R3 = 15
    const val L_STICK_RIGHT = 16
    const val L_STICK_LEFT = 17
    const val L_STICK_DOWN = 18
    const val L_STICK_UP = 19
    const val R_STICK_RIGHT = 20
    const val R_STICK_LEFT = 21
    const val R_STICK_DOWN = 22
    const val R_STICK_UP = 23
}

@Composable
fun TouchControllerDeck(
    profile: ControllerProfile,
    isLandscape: Boolean,
    settings: RetroSettings,
    editMode: Boolean,
    onSimulateInput: (buttonIndex: Int, value: Float) -> Unit,
    onSavePlacement: (controlId: String, placement: ControlButtonPlacement) -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val isArabic = LocalIsArabic.current
    val profileKey = profile.name

    val selectLabel = when {
        profile == ControllerProfile.SEGA6 -> "MODE"
        isArabic -> "تحديد"
        else -> "SELECT"
    }
    val startLabel = if (isArabic) "ابدأ" else "START"

    fun triggerInput(idx: Int, value: Float) {
        if (value > 0.5f && settings.hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        onSimulateInput(idx, value)
    }

    fun sendStick(isRight: Boolean, nx: Float, ny: Float) {
        val base = if (isRight) 20 else 16
        onSimulateInput(base + 0, if (nx > 0.08f) nx else 0f)
        onSimulateInput(base + 1, if (nx < -0.08f) -nx else 0f)
        onSimulateInput(base + 2, if (ny > 0.08f) ny else 0f)
        onSimulateInput(base + 3, if (ny < -0.08f) -ny else 0f)
    }

    // Physical controller deck is always Left-to-Right (D-Pad on left, Face buttons on right) even in RTL Arabic UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .alpha(if (editMode) 1f else settings.controlsOpacity)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            val availW = maxWidth.value
            val availH = maxHeight.value

            if (isLandscape) {
                val cellSize = ((availW * 0.24f) / 3f * settings.controlsScale)
                    .coerceIn(32f, 46f)
                    .coerceAtMost((availH * 0.36f) / 3f)
                    .dp
                val stickSize = (72f * settings.controlsScale)
                    .coerceIn(58f, 92f)
                    .coerceAtMost(availH * 0.28f)
                    .dp
                val sideColWidth = (cellSize * 3f + 16.dp).coerceAtLeast(148.dp)

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left Side Column: L2/L1, 3x3 D-Pad, Left Stick, L3/Select
                    Column(
                        modifier = Modifier
                            .width(sideColWidth)
                            .padding(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
                        horizontalAlignment = Alignment.Start
                    ) {
                        LeftShoulderRow(profile, profileKey, true, settings, editMode, ::triggerInput, onSavePlacement)

                        EditableControlSlot(
                            controlId = "dpad",
                            placement = settings.getControlPlacement(profileKey, true, "dpad"),
                            editMode = editMode,
                            onSave = { onSavePlacement("dpad", it) }
                        ) {
                            DpadGrid3x3(cellSize = cellSize, onDirection = ::triggerInput)
                        }

                        if (profile == ControllerProfile.PSX || profile == ControllerProfile.PSP || profile == ControllerProfile.N64) {
                            EditableControlSlot(
                                controlId = "lstick",
                                placement = settings.getControlPlacement(profileKey, true, "lstick"),
                                editMode = editMode,
                                onSave = { onSavePlacement("lstick", it) }
                            ) {
                                VirtualAnalogStick(
                                    label = "L",
                                    sizeDp = stickSize,
                                    onMove = { nx, ny -> sendStick(false, nx, ny) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (profile == ControllerProfile.PSX) {
                                EditableControlSlot(
                                    controlId = "l3",
                                    placement = settings.getControlPlacement(profileKey, true, "l3"),
                                    editMode = editMode,
                                    onSave = { onSavePlacement("l3", it) }
                                ) {
                                    MiniRoundButton("L3") { triggerInput(EjsButtonIndex.L3, if (it) 1f else 0f) }
                                }
                            }
                            SmallPillButton(selectLabel, "ctrl_select") { triggerInput(EjsButtonIndex.SELECT, if (it) 1f else 0f) }
                        }
                    }

                    // Right Side Column: R1/R2, 3x3 Face Buttons, Right Stick / C-Buttons, Start/R3
                    Column(
                        modifier = Modifier
                            .width(sideColWidth)
                            .padding(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
                        horizontalAlignment = Alignment.End
                    ) {
                        RightShoulderRow(profile, profileKey, true, settings, editMode, ::triggerInput, onSavePlacement)

                        EditableControlSlot(
                            controlId = "face",
                            placement = settings.getControlPlacement(profileKey, true, "face"),
                            editMode = editMode,
                            onSave = { onSavePlacement("face", it) }
                        ) {
                            FaceButtonsGrid3x3(profile = profile, cellSize = cellSize, onButton = ::triggerInput)
                        }

                        if (profile == ControllerProfile.PSX) {
                            EditableControlSlot(
                                controlId = "rstick",
                                placement = settings.getControlPlacement(profileKey, true, "rstick"),
                                editMode = editMode,
                                onSave = { onSavePlacement("rstick", it) }
                            ) {
                                VirtualAnalogStick(
                                    label = "R",
                                    sizeDp = stickSize,
                                    onMove = { nx, ny -> sendStick(true, nx, ny) }
                                )
                            }
                        } else if (profile == ControllerProfile.N64) {
                            N64CButtonsGrid(cellSize = (cellSize.value * 0.72f).dp, onButton = ::triggerInput)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            SmallPillButton(startLabel, "ctrl_start") { triggerInput(EjsButtonIndex.START, if (it) 1f else 0f) }
                            if (profile == ControllerProfile.PSX) {
                                EditableControlSlot(
                                    controlId = "r3",
                                    placement = settings.getControlPlacement(profileKey, true, "r3"),
                                    editMode = editMode,
                                    onSave = { onSavePlacement("r3", it) }
                                ) {
                                    MiniRoundButton("R3") { triggerInput(EjsButtonIndex.R3, if (it) 1f else 0f) }
                                }
                            }
                        }
                    }
                }
            } else {
                // PORTRAIT 4-ROW GUARANTEED NON-OVERLAPPING GRID:
                // Row 1: Shoulder Triggers (L2 L1 | R1 R2)
                // Row 2: Primary Action Row (3x3 D-pad left | 3x3 Face buttons right)
                // Row 3: Analog Sticks Row BELOW Row 2 (PS1 dual L/R sticks; PSP/N64 single L stick; never collides with Row 2!)
                // Row 4: Center Utility Bar (L3 | تحديد | ⋯ | ابدأ | R3)
                val hasAnalogRow = profile == ControllerProfile.PSX || profile == ControllerProfile.PSP || profile == ControllerProfile.N64
                val maxCellFromWidth = ((availW - 28f) / 6f).coerceIn(32f, 54f)
                val maxCellFromHeight = if (hasAnalogRow) {
                    ((availH - 185f) / 3f).coerceIn(32f, 52f)
                } else {
                    ((availH - 96f) / 3f).coerceIn(38f, 56f)
                }
                val cellSize = (minOf(maxCellFromWidth, maxCellFromHeight) * settings.controlsScale)
                    .coerceIn(32f, maxCellFromWidth)
                    .dp

                val stickDiameter = ((availH - (cellSize.value * 3f) - 98f))
                    .coerceIn(74f, 108f)
                    .coerceAtMost((availW - 44f) / 2f)
                    .dp

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: Shoulder Triggers (40dp height)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (profile == ControllerProfile.NES) 4.dp else 40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LeftShoulderRow(profile, profileKey, false, settings, editMode, ::triggerInput, onSavePlacement)
                        RightShoulderRow(profile, profileKey, false, settings, editMode, ::triggerInput, onSavePlacement)
                    }

                    // Row 2: 3x3 D-Pad on Left, 3x3 Face Buttons on Right (zero center sticks squeezing them!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditableControlSlot(
                            controlId = "dpad",
                            placement = settings.getControlPlacement(profileKey, false, "dpad"),
                            editMode = editMode,
                            onSave = { onSavePlacement("dpad", it) }
                        ) {
                            DpadGrid3x3(cellSize = cellSize, onDirection = ::triggerInput)
                        }

                        EditableControlSlot(
                            controlId = "face",
                            placement = settings.getControlPlacement(profileKey, false, "face"),
                            editMode = editMode,
                            onSave = { onSavePlacement("face", it) }
                        ) {
                            FaceButtonsGrid3x3(profile = profile, cellSize = cellSize, onButton = ::triggerInput)
                        }
                    }

                    // Row 3: Dual/Single Analog Sticks cleanly BELOW Row 2 (never collides with D-Pad or ○)
                    if (hasAnalogRow) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditableControlSlot(
                                controlId = "lstick",
                                placement = settings.getControlPlacement(profileKey, false, "lstick"),
                                editMode = editMode,
                                onSave = { onSavePlacement("lstick", it) }
                            ) {
                                VirtualAnalogStick(
                                    label = "L",
                                    sizeDp = stickDiameter,
                                    onMove = { nx, ny -> sendStick(false, nx, ny) }
                                )
                            }

                            if (profile == ControllerProfile.PSX) {
                                EditableControlSlot(
                                    controlId = "rstick",
                                    placement = settings.getControlPlacement(profileKey, false, "rstick"),
                                    editMode = editMode,
                                    onSave = { onSavePlacement("rstick", it) }
                                ) {
                                    VirtualAnalogStick(
                                        label = "R",
                                        sizeDp = stickDiameter,
                                        onMove = { nx, ny -> sendStick(true, nx, ny) }
                                    )
                                }
                            } else if (profile == ControllerProfile.N64) {
                                N64CButtonsGrid(cellSize = (stickDiameter.value * 0.34f).coerceIn(28f, 38f).dp, onButton = ::triggerInput)
                            } else {
                                Spacer(Modifier.size(stickDiameter))
                            }
                        }
                    }

                    // Row 4: Centered Bottom Bar — L3 · تحديد · ⋯ · ابدأ · R3
                    EditableControlSlot(
                        controlId = "start_select",
                        placement = settings.getControlPlacement(profileKey, false, "start_select"),
                        editMode = editMode,
                        onSave = { onSavePlacement("start_select", it) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (profile == ControllerProfile.PSX) {
                                EditableControlSlot(
                                    controlId = "l3",
                                    placement = settings.getControlPlacement(profileKey, false, "l3"),
                                    editMode = editMode,
                                    onSave = { onSavePlacement("l3", it) }
                                ) {
                                    MiniRoundButton("L3") { triggerInput(EjsButtonIndex.L3, if (it) 1f else 0f) }
                                }
                            }
                            if (profile != ControllerProfile.N64) {
                                SmallPillButton(selectLabel, "ctrl_select") {
                                    triggerInput(EjsButtonIndex.SELECT, if (it) 1f else 0f)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.6f), CircleShape)
                                    .clickable(onClick = onOpenMenu)
                                    .testTag("ctrl_menu_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("•••", color = SecondaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            SmallPillButton(startLabel, "ctrl_start") {
                                triggerInput(EjsButtonIndex.START, if (it) 1f else 0f)
                            }
                            if (profile == ControllerProfile.PSX) {
                                EditableControlSlot(
                                    controlId = "r3",
                                    placement = settings.getControlPlacement(profileKey, false, "r3"),
                                    editMode = editMode,
                                    onSave = { onSavePlacement("r3", it) }
                                ) {
                                    MiniRoundButton("R3") { triggerInput(EjsButtonIndex.R3, if (it) 1f else 0f) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeftShoulderRow(
    profile: ControllerProfile,
    profileKey: String,
    isLandscape: Boolean,
    settings: RetroSettings,
    editMode: Boolean,
    triggerInput: (Int, Float) -> Unit,
    onSavePlacement: (String, ControlButtonPlacement) -> Unit
) {
    if (profile == ControllerProfile.NES) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (profile == ControllerProfile.PSX || profile == ControllerProfile.N64) {
            EditableControlSlot(
                controlId = "l2",
                placement = settings.getControlPlacement(profileKey, isLandscape, "l2"),
                editMode = editMode,
                onSave = { onSavePlacement("l2", it) }
            ) {
                ShoulderButton(if (profile == ControllerProfile.N64) "Z" else "L2") {
                    triggerInput(EjsButtonIndex.L2, if (it) 1f else 0f)
                }
            }
        }
        EditableControlSlot(
            controlId = "l1",
            placement = settings.getControlPlacement(profileKey, isLandscape, "l1"),
            editMode = editMode,
            onSave = { onSavePlacement("l1", it) }
        ) {
            ShoulderButton(if (profile == ControllerProfile.PSX) "L1" else "L") {
                triggerInput(EjsButtonIndex.L1, if (it) 1f else 0f)
            }
        }
    }
}

@Composable
private fun RightShoulderRow(
    profile: ControllerProfile,
    profileKey: String,
    isLandscape: Boolean,
    settings: RetroSettings,
    editMode: Boolean,
    triggerInput: (Int, Float) -> Unit,
    onSavePlacement: (String, ControlButtonPlacement) -> Unit
) {
    if (profile == ControllerProfile.NES) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        EditableControlSlot(
            controlId = "r1",
            placement = settings.getControlPlacement(profileKey, isLandscape, "r1"),
            editMode = editMode,
            onSave = { onSavePlacement("r1", it) }
        ) {
            ShoulderButton(if (profile == ControllerProfile.PSX) "R1" else "R") {
                triggerInput(EjsButtonIndex.R1, if (it) 1f else 0f)
            }
        }
        if (profile == ControllerProfile.PSX) {
            EditableControlSlot(
                controlId = "r2",
                placement = settings.getControlPlacement(profileKey, isLandscape, "r2"),
                editMode = editMode,
                onSave = { onSavePlacement("r2", it) }
            ) {
                ShoulderButton("R2") { triggerInput(EjsButtonIndex.R2, if (it) 1f else 0f) }
            }
        }
    }
}

@Composable
private fun EditableControlSlot(
    controlId: String,
    placement: ControlButtonPlacement,
    editMode: Boolean,
    onSave: (ControlButtonPlacement) -> Unit,
    content: @Composable () -> Unit
) {
    if (!placement.visible && !editMode) return

    var offsetX by remember(controlId, placement.offsetX) { mutableFloatStateOf(placement.offsetX) }
    var offsetY by remember(controlId, placement.offsetY) { mutableFloatStateOf(placement.offsetY) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .scale(placement.scale)
            .alpha(if (placement.visible) 1f else 0.35f)
            .then(
                if (editMode) {
                    Modifier
                        .border(1.dp, SecondaryCyan.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .pointerInput(controlId, placement) {
                            detectDragGestures(
                                onDragEnd = {
                                    onSave(placement.copy(offsetX = offsetX, offsetY = offsetY))
                                }
                            ) { change, drag ->
                                change.consume()
                                offsetX += drag.x
                                offsetY += drag.y
                            }
                        }
                } else Modifier
            )
    ) {
        content()
        if (editMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.78f))
                    .clickable {
                        onSave(placement.copy(offsetX = offsetX, offsetY = offsetY, visible = !placement.visible))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (placement.visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = controlId,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun VirtualAnalogStick(
    label: String,
    sizeDp: Dp,
    onMove: (nx: Float, ny: Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadiusPx = sizeDp.value * 0.95f

    Box(
        modifier = Modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(SurfaceElevated.copy(alpha = 0.78f))
            .border(1.5.dp, BorderGlass, CircleShape)
            .pointerInput(label) {
                detectDragGestures(
                    onDragStart = { start ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = start - center
                        val dist = hypot(delta.x, delta.y).coerceAtLeast(1f)
                        val clamped = if (dist > maxRadiusPx) delta * (maxRadiusPx / dist) else delta
                        knobOffset = clamped
                        onMove((clamped.x / maxRadiusPx).coerceIn(-1f, 1f), (clamped.y / maxRadiusPx).coerceIn(-1f, 1f))
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                ) { change, drag ->
                    change.consume()
                    val raw = knobOffset + drag
                    val dist = hypot(raw.x, raw.y).coerceAtLeast(1f)
                    val clamped = if (dist > maxRadiusPx) raw * (maxRadiusPx / dist) else raw
                    knobOffset = clamped
                    onMove((clamped.x / maxRadiusPx).coerceIn(-1f, 1f), (clamped.y / maxRadiusPx).coerceIn(-1f, 1f))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension * 0.36f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(SecondaryCyan, PrimaryPurple)),
                radius = size.minDimension * 0.24f,
                center = center + Offset(knobOffset.x * 0.45f, knobOffset.y * 0.45f)
            )
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DpadGrid3x3(
    cellSize: Dp,
    onDirection: (Int, Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Spacer(Modifier.size(cellSize))
            DpadKey("▲", EjsButtonIndex.UP, cellSize, "ctrl_dpad_up", onDirection)
            Spacer(Modifier.size(cellSize))
        }
        Row {
            DpadKey("◀", EjsButtonIndex.LEFT, cellSize, "ctrl_dpad_left", onDirection)
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .background(SurfaceElevated.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(cellSize * 0.28f)
                        .clip(CircleShape)
                        .background(BorderSubtle)
                )
            }
            DpadKey("▶", EjsButtonIndex.RIGHT, cellSize, "ctrl_dpad_right", onDirection)
        }
        Row {
            Spacer(Modifier.size(cellSize))
            DpadKey("▼", EjsButtonIndex.DOWN, cellSize, "ctrl_dpad_down", onDirection)
            Spacer(Modifier.size(cellSize))
        }
    }
}

@Composable
private fun FaceButtonsGrid3x3(
    profile: ControllerProfile,
    cellSize: Dp,
    onButton: (Int, Float) -> Unit
) {
    when (profile) {
        ControllerProfile.PSX, ControllerProfile.PSP -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Spacer(Modifier.size(cellSize))
                    ActionCircleButton("△", EjsButtonIndex.X_TRIANGLE, Color(0xFF34D399), cellSize, "ctrl_face_triangle", onButton)
                    Spacer(Modifier.size(cellSize))
                }
                Row {
                    ActionCircleButton("□", EjsButtonIndex.Y_SQUARE, Color(0xFFF472B6), cellSize, "ctrl_face_square", onButton)
                    Spacer(Modifier.size(cellSize))
                    ActionCircleButton("○", EjsButtonIndex.A_CIRCLE, Color(0xFFF43F5E), cellSize, "ctrl_face_circle", onButton)
                }
                Row {
                    Spacer(Modifier.size(cellSize))
                    ActionCircleButton("✕", EjsButtonIndex.B_CROSS, Color(0xFF38BDF8), cellSize, "ctrl_face_cross", onButton)
                    Spacer(Modifier.size(cellSize))
                }
            }
        }
        ControllerProfile.SEGA6 -> {
            val subCell = (cellSize.value * 0.92f).dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionCircleButton("X", EjsButtonIndex.X_TRIANGLE, PrimaryPurple, subCell, "ctrl_sega_x", onButton)
                    ActionCircleButton("Y", EjsButtonIndex.Y_SQUARE, SecondaryCyan, subCell, "ctrl_sega_y", onButton)
                    ActionCircleButton("Z", EjsButtonIndex.L1, WarningAmber, subCell, "ctrl_sega_z", onButton)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionCircleButton("A", EjsButtonIndex.B_CROSS, SuccessGreen, subCell, "ctrl_sega_a", onButton)
                    ActionCircleButton("B", EjsButtonIndex.A_CIRCLE, ErrorRose, subCell, "ctrl_sega_b", onButton)
                    ActionCircleButton("C", EjsButtonIndex.R1, Color(0xFF38BDF8), subCell, "ctrl_sega_c", onButton)
                }
            }
        }
        ControllerProfile.GBA, ControllerProfile.NES -> {
            val largeBtn = (cellSize.value * 1.15f).coerceAtMost(58f).dp
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionCircleButton("B", EjsButtonIndex.B_CROSS, ErrorRose, largeBtn, "ctrl_face_b", onButton)
                ActionCircleButton("A", EjsButtonIndex.A_CIRCLE, SuccessGreen, largeBtn, "ctrl_face_a", onButton)
            }
        }
        else -> {
            // SNES / N64 / NDS
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Spacer(Modifier.size(cellSize))
                    ActionCircleButton("X", EjsButtonIndex.X_TRIANGLE, SecondaryCyan, cellSize, "ctrl_face_x", onButton)
                    Spacer(Modifier.size(cellSize))
                }
                Row {
                    ActionCircleButton("Y", EjsButtonIndex.Y_SQUARE, PrimaryPurple, cellSize, "ctrl_face_y", onButton)
                    Spacer(Modifier.size(cellSize))
                    ActionCircleButton("A", EjsButtonIndex.A_CIRCLE, ErrorRose, cellSize, "ctrl_face_a", onButton)
                }
                Row {
                    Spacer(Modifier.size(cellSize))
                    ActionCircleButton("B", EjsButtonIndex.B_CROSS, SuccessGreen, cellSize, "ctrl_face_b", onButton)
                    Spacer(Modifier.size(cellSize))
                }
            }
        }
    }
}

@Composable
private fun N64CButtonsGrid(cellSize: Dp, onButton: (Int, Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Spacer(Modifier.size(cellSize))
            ActionCircleButton("C▲", EjsButtonIndex.R_STICK_UP, WarningAmber, cellSize, "ctrl_c_up", onButton)
            Spacer(Modifier.size(cellSize))
        }
        Row {
            ActionCircleButton("C◀", EjsButtonIndex.R_STICK_LEFT, WarningAmber, cellSize, "ctrl_c_left", onButton)
            Spacer(Modifier.size(cellSize))
            ActionCircleButton("C▶", EjsButtonIndex.R_STICK_RIGHT, WarningAmber, cellSize, "ctrl_c_right", onButton)
        }
        Row {
            Spacer(Modifier.size(cellSize))
            ActionCircleButton("C▼", EjsButtonIndex.R_STICK_DOWN, WarningAmber, cellSize, "ctrl_c_down", onButton)
            Spacer(Modifier.size(cellSize))
        }
    }
}

@Composable
private fun DpadKey(symbol: String, idx: Int, sizeDp: Dp, tag: String, onDirection: (Int, Float) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(sizeDp)
            .testTag(tag)
            .clip(RoundedCornerShape(11.dp))
            .background(if (pressed) PrimaryPurple.copy(alpha = 0.6f) else SurfaceElevated.copy(alpha = 0.85f))
            .border(1.dp, if (pressed) SecondaryCyan else BorderSubtle, RoundedCornerShape(11.dp))
            .pointerInput(idx) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onDirection(idx, 1f)
                        tryAwaitRelease()
                        pressed = false
                        onDirection(idx, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = if (pressed) Color.White else TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun ActionCircleButton(
    label: String,
    idx: Int,
    accent: Color,
    sizeDp: Dp,
    tag: String,
    onButton: (Int, Float) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(sizeDp)
            .testTag(tag)
            .clip(CircleShape)
            .background(if (pressed) accent.copy(alpha = 0.58f) else accent.copy(alpha = 0.22f))
            .border(1.5.dp, if (pressed) Color.White else accent.copy(alpha = 0.72f), CircleShape)
            .pointerInput(idx) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onButton(idx, 1f)
                        tryAwaitRelease()
                        pressed = false
                        onButton(idx, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = (sizeDp.value * 0.34f).coerceIn(11f, 18f).sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun ShoulderButton(label: String, onPressChange: (Boolean) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (pressed) PrimaryPurple.copy(alpha = 0.5f) else SurfaceElevated.copy(alpha = 0.85f))
            .border(1.dp, if (pressed) SecondaryCyan else BorderSubtle, RoundedCornerShape(11.dp))
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
        Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
private fun SmallPillButton(label: String, tag: String, onPressChange: (Boolean) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .height(30.dp)
            .testTag(tag)
            .clip(RoundedCornerShape(50))
            .background(if (pressed) SecondaryCyan.copy(alpha = 0.35f) else SurfaceElevated.copy(alpha = 0.88f))
            .border(1.dp, if (pressed) SecondaryCyan else BorderSubtle, RoundedCornerShape(50))
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
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
private fun MiniRoundButton(label: String, onPressChange: (Boolean) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (pressed) PrimaryPurple.copy(alpha = 0.5f) else SurfaceElevated.copy(alpha = 0.88f))
            .border(1.dp, if (pressed) SecondaryCyan else BorderSubtle, CircleShape)
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
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
