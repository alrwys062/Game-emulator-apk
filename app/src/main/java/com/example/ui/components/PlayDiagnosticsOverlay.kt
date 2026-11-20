package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
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

data class EngineStageInfo(
    val status: String = "pending", // pending | pass | fail
    val detail: String = ""
)

@Composable
fun PlayDiagnosticsOverlay(
    errorDetail: String,
    diagnosticLogs: String,
    stageStates: Map<Int, EngineStageInfo>,
    aiBusy: Boolean,
    aiResult: String?,
    onRetry: () -> Unit,
    onSwitchCdn: () -> Unit,
    onRunWithoutThreads: () -> Unit,
    onCopyLog: () -> Unit,
    onDiagnoseWithAi: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current

    val failedStageTag = errorDetail.substringBefore("|", "")
    val failedStageMsg = errorDetail.substringAfter("|", "")
    val stageTitle = when (failedStageTag) {
        "STAGE_1" -> if (isArabic) "فشل المرحلة [1]: إنشاء حاوية iframe بحجم ظاهر" else "Stage [1] Failed: iframe mount size"
        "STAGE_2" -> if (isArabic) "فشل المرحلة [2]: تحميل ملف loader.js من الـ CDN" else "Stage [2] Failed: loader.js load"
        "STAGE_3" -> if (isArabic) "فشل المرحلة [3]: لم يتم إنشاء window.EJS_emulator خلال 15 ثانية" else "Stage [3] Failed: window.EJS_emulator missing at 15s"
        "STAGE_4" -> if (isArabic) "فشل المرحلة [4]: تعذر تحميل ملفات النواة عبر الشبكة" else "Stage [4] Failed: CDN asset fetch"
        "STAGE_5" -> if (isArabic) "فشل المرحلة [5]: لم يبدأ تشغيل النواة (EJS_onGameStart)" else "Stage [5] Failed: EJS_onGameStart timeout"
        "STAGE_6" -> if (isArabic) "فشل المرحلة [6]: خطأ برمجي / WebGL داخل المحرك" else "Stage [6] Failed: WebGL or JS Runtime error"
        "PSP_NO_SAB" -> strings.engineErrorNoSab
        "OFFLINE_NOT_PREPARED" -> if (isArabic) "هذا النظام يحتاج تجهيزاً مرة واحدة بالإنترنت" else "This system requires a one-time online setup"
        else -> strings.engineErrorCdn
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF507080D))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = strings.engineErrorTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRose.copy(alpha = 0.14f))
                    .border(1.dp, ErrorRose.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stageTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ErrorRose,
                    textAlign = TextAlign.Center
                )
                if (failedStageMsg.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = failedStageMsg,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Stage [1]-[6] Pass/Fail Summary Card
            EngineStagesChecklistCard(stageStates = stageStates, isArabic = isArabic)

            // 2-Column Action Buttons Grid (never clipped!)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DiagActionButton(
                        label = strings.retryBtn,
                        icon = Icons.Rounded.Refresh,
                        primary = true,
                        modifier = Modifier.weight(1f).testTag("diag_retry_btn"),
                        onClick = onRetry
                    )
                    DiagActionButton(
                        label = strings.switchCdnBtn,
                        icon = Icons.Rounded.SwapHoriz,
                        primary = false,
                        modifier = Modifier.weight(1f).testTag("diag_switch_cdn_btn"),
                        onClick = onSwitchCdn
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DiagActionButton(
                        label = strings.runWithoutThreadsBtn,
                        icon = Icons.Rounded.PlayCircleOutline,
                        primary = false,
                        modifier = Modifier.weight(1f).testTag("diag_no_threads_btn"),
                        onClick = onRunWithoutThreads
                    )
                    DiagActionButton(
                        label = strings.copyLogBtn,
                        icon = Icons.Rounded.ContentCopy,
                        primary = false,
                        modifier = Modifier.weight(1f).testTag("diag_copy_log_btn"),
                        onClick = onCopyLog
                    )
                }
                // Full-width Gemini AI diagnosis button
                DiagActionButton(
                    label = strings.diagnoseWithAiBtn,
                    icon = Icons.Rounded.AutoAwesome,
                    primary = false,
                    accentBorder = PrimaryPurple,
                    modifier = Modifier.fillMaxWidth().testTag("diag_ai_btn"),
                    onClick = onDiagnoseWithAi
                )
            }

            if (aiBusy) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SecondaryCyan)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "جاري تحليل السجل عبر Gemini..." else "Analyzing log with Gemini...",
                        color = SecondaryCyan,
                        fontSize = 12.sp
                    )
                }
            }

            if (!aiResult.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple.copy(alpha = 0.16f))
                        .border(1.dp, PrimaryPurple.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = aiResult,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            // Dedicated Scrollable Monospace Diagnostic Log Box (~30% viewport height)
            if (diagnosticLogs.isNotBlank()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 84.dp, max = 180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp)
                    ) {
                        Text(
                            text = diagnosticLogs,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            ),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngineStagesChecklistCard(
    stageStates: Map<Int, EngineStageInfo>,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val stageLabels = listOf(
        1 to (if (isArabic) "[1] إنشاء iframe وحجمه" else "[1] iframe created & sized"),
        2 to (if (isArabic) "[2] تحميل loader.js" else "[2] loader.js script loaded"),
        3 to (if (isArabic) "[3] وجود window.EJS_emulator (15ث)" else "[3] window.EJS_emulator (15s)"),
        4 to (if (isArabic) "[4] طلبات CDN (fetch/XHR)" else "[4] CDN fetch/XHR requests"),
        5 to (if (isArabic) "[5] EJS_ready و EJS_onGameStart" else "[5] EJS_ready & onGameStart"),
        6 to (if (isArabic) "[6] استقرار JS/WebGL" else "[6] JS/WebGL runtime clean")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        stageLabels.forEach { (idx, label) ->
            val info = stageStates[idx] ?: EngineStageInfo()
            val (icon, tint) = when (info.status) {
                "pass" -> Icons.Rounded.CheckCircle to SuccessGreen
                "fail" -> Icons.Rounded.Close to ErrorRose
                else -> Icons.Rounded.HourglassTop to WarningAmber
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (info.detail.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = info.detail,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tint.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagActionButton(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    accentBorder: Color? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (primary) Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan))
                else Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated))
            )
            .border(
                1.dp,
                accentBorder ?: if (primary) Color.Transparent else BorderSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
