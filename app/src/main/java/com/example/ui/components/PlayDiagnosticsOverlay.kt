package com.example.ui.components

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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalIsArabic
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayDiagnosticsOverlay(
    errorDetail: String,
    diagnosticLogs: String,
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

    val probableCause = when {
        errorDetail.startsWith("PSP_NO_SAB") -> strings.engineErrorNoSab
        errorDetail.startsWith("NO_INTERNET") -> if (isArabic) "لا يوجد اتصال بالإنترنت لتحميل النواة لأول مرة." else "No internet connection to fetch the core on first run."
        errorDetail.startsWith("NO_WEBGL") -> if (isArabic) "متصفح WebView لا يدعم WebGL2 على هذا الجهاز." else "WebGL2 is unavailable on this WebView."
        errorDetail.startsWith("CDN_LOAD_FAILED") -> strings.engineErrorCdn
        errorDetail.startsWith("TIMEOUT") -> if (isArabic) "انتهت مهلة 25 ثانية دون ظهور أول إطار (قد يكون السبب: بطء الشبكة، نقص BIOS، أو تحتاج تعطيل Threads)." else "25s watchdog expired before first frame (slow network, missing BIOS, or Threads issue)."
        else -> strings.engineErrorCdn
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF007080D))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = strings.engineErrorTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = probableCause,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRose,
                textAlign = TextAlign.Center
            )

            // Action Buttons
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DiagActionChip(strings.retryBtn, Icons.Rounded.Refresh, true, onRetry)
                DiagActionChip(strings.switchCdnBtn, Icons.Rounded.SwapHoriz, false, onSwitchCdn)
                DiagActionChip(strings.runWithoutThreadsBtn, null, false, onRunWithoutThreads)
                DiagActionChip(strings.copyLogBtn, Icons.Rounded.ContentCopy, false, onCopyLog)
                DiagActionChip(strings.diagnoseWithAiBtn, Icons.Rounded.AutoAwesome, false, onDiagnoseWithAi)
            }

            if (aiBusy) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SecondaryCyan)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isArabic) "جاري التحليل عبر Gemini..." else "Analyzing via Gemini...", color = SecondaryCyan, fontSize = 12.sp)
                }
            }

            if (!aiResult.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple.copy(alpha = 0.14f))
                        .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = aiResult,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            // Full Diagnostic Log Box
            if (diagnosticLogs.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(
                        text = diagnosticLogs,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    primary: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (primary) Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan))
                else Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated))
            )
            .border(1.dp, if (primary) Color.Transparent else BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
