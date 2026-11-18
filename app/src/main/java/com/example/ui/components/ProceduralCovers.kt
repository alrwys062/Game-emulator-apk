package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun ProceduralCoverArt(
    styleIndex: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (styleIndex % 8) {
            0 -> {
                // Sunset Horizon (Crash Racer style)
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color(0xFF4C1D95),
                        0.45f to Color(0xFFEC4899),
                        0.72f to Color(0xFFF59E0B),
                        0.74f to Color(0xFF141526),
                        1.0f to Color(0xFF07080D)
                    )
                )
                // Glowing retro sun
                val sunCenter = Offset(w * 0.5f, h * 0.44f)
                val sunRadius = minOf(w, h) * 0.25f
                drawCircle(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFDE047), Color(0xFFFB7185))
                    ),
                    radius = sunRadius,
                    center = sunCenter
                )
                // Horizon water scanlines
                for (i in 0..8) {
                    val y = h * (0.74f + i * 0.03f)
                    drawLine(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.35f - i * 0.03f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5f
                    )
                }
            }

            1 -> {
                // Misty Teal Mountain & Moon (Sky Pilot style)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F293A), Color(0xFF06101B)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                // Glowing moon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE0F2FE), Color(0x5522D3EE), Color.Transparent),
                        center = Offset(w * 0.72f, h * 0.28f),
                        radius = minOf(w, h) * 0.36f
                    ),
                    radius = minOf(w, h) * 0.36f,
                    center = Offset(w * 0.72f, h * 0.28f)
                )
                // Mountain silhouette 1
                val m1 = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.65f)
                    lineTo(w * 0.48f, h * 0.42f)
                    lineTo(w, h * 0.72f)
                    lineTo(w, h)
                    close()
                }
                drawPath(m1, brush = Brush.verticalGradient(listOf(Color(0xFF0D2D3E), Color(0xFF050A12))))
                // Foreground ridge
                val m2 = Path().apply {
                    moveTo(0f, h)
                    lineTo(w * 0.25f, h * 0.76f)
                    lineTo(w, h * 0.56f)
                    lineTo(w, h)
                    close()
                }
                drawPath(m2, color = Color(0xFF050A12))
            }

            2 -> {
                // Warm Gold Retro Diamond (Brick Jump style)
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF92400E), Color(0xFF451A03))
                    )
                )
                // Diagonal retro stripes
                for (i in -4..10) {
                    drawLine(
                        color = Color(0xFFFBBF24).copy(alpha = 0.14f),
                        start = Offset(w * i * 0.16f, 0f),
                        end = Offset(w * (i * 0.16f + 0.5f), h),
                        strokeWidth = w * 0.06f
                    )
                }
                val side = minOf(w, h) * 0.44f
                rotate(45f, pivot = Offset(w * 0.5f, h * 0.5f)) {
                    drawRect(
                        color = Color(0xFF140B06),
                        topLeft = Offset(w * 0.5f - side / 2, h * 0.5f - side / 2),
                        size = Size(side, side)
                    )
                    drawRect(
                        color = Color(0xFFFDE68A),
                        topLeft = Offset(w * 0.5f - side / 2, h * 0.5f - side / 2),
                        size = Size(side, side),
                        style = Stroke(width = 4f)
                    )
                }
            }

            3 -> {
                // Neon Magenta Rings (Neon Ring style)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEC4899), Color(0xFF831843), Color(0xFF1E0818)),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = maxOf(w, h) * 0.75f
                    )
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.24f),
                    radius = minOf(w, h) * 0.34f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = minOf(w, h) * 0.08f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.90f),
                    radius = minOf(w, h) * 0.16f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }

            4 -> {
                // Starry Cosmic Planet (Star Quest style)
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF07080D))
                    )
                )
                val starCoords = listOf(
                    0.18f to 0.22f, 0.78f to 0.16f, 0.35f to 0.14f,
                    0.85f to 0.38f, 0.14f to 0.46f, 0.62f to 0.26f
                )
                starCoords.forEach { (sx, sy) ->
                    drawCircle(Color.White.copy(alpha = 0.75f), radius = 2.2f, center = Offset(w * sx, h * sy))
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF312E81)),
                        center = Offset(w * 0.5f, h * 0.44f),
                        radius = minOf(w, h) * 0.26f
                    ),
                    radius = minOf(w, h) * 0.24f,
                    center = Offset(w * 0.5f, h * 0.46f)
                )
                drawOval(
                    color = Color(0xFF090B14),
                    topLeft = Offset(-w * 0.1f, h * 0.68f),
                    size = Size(w * 1.2f, h * 0.5f)
                )
            }

            5 -> {
                // Emerald Moon Forest (Kart Dash style)
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF064E3B), Color(0xFF022C22), Color(0xFF051310))
                    )
                )
                drawCircle(
                    color = Color(0xFFECFDF5).copy(alpha = 0.92f),
                    radius = minOf(w, h) * 0.24f,
                    center = Offset(w * 0.56f, h * 0.40f)
                )
                val hills = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.66f)
                    quadraticTo(w * 0.35f, h * 0.52f, w * 0.68f, h * 0.72f)
                    quadraticTo(w * 0.88f, h * 0.80f, w, h * 0.66f)
                    lineTo(w, h)
                    close()
                }
                drawPath(hills, color = Color(0xFF041C16))
            }

            6 -> {
                // Cyber Purple-Cyan Grid
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF170B3B), Color(0xFF090B15))
                    )
                )
                for (i in 0..6) {
                    val y = h * (0.52f + i * 0.08f)
                    drawLine(Color(0xFF22D3EE).copy(alpha = 0.35f), Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
                }
                val tri = Path().apply {
                    moveTo(w * 0.5f, h * 0.22f)
                    lineTo(w * 0.74f, h * 0.64f)
                    lineTo(w * 0.26f, h * 0.64f)
                    close()
                }
                drawPath(
                    tri,
                    brush = Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF22D3EE)))
                )
            }

            else -> {
                // Prism Aurora Waves
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xAA8B5CF6), Color.Transparent),
                        center = Offset(w * 0.32f, h * 0.38f),
                        radius = minOf(w, h) * 0.45f
                    ),
                    radius = minOf(w, h) * 0.45f,
                    center = Offset(w * 0.32f, h * 0.38f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xAA22D3EE), Color.Transparent),
                        center = Offset(w * 0.68f, h * 0.62f),
                        radius = minOf(w, h) * 0.45f
                    ),
                    radius = minOf(w, h) * 0.45f,
                    center = Offset(w * 0.68f, h * 0.62f)
                )
            }
        }
    }
}
