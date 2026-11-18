package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.LegalSource
import com.example.model.StoreCatalog
import com.example.ui.components.ProceduralCoverArt
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalIsArabic
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.DownloadTaskState

@Composable
fun StoreScreen(
    downloadTask: DownloadTaskState?,
    onOpenSource: (LegalSource) -> Unit,
    onImportFromPhone: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocalIsArabic.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.storeTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.legalSourcesOnly,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }
        }

        // Fixed Legal Notice Banner
        item {
            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(WarningAmber.copy(alpha = 0.08f))
                    .border(1.dp, WarningAmber.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = strings.legalWarningBanner,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = WarningAmber
                )
            }
        }

        // Legal Store Source Cards
        items(StoreCatalog.legalSources, key = { it.id }) { source ->
            LegalSourceCard(
                source = source,
                subtitle = if (isArabic) source.subtitleAr else source.subtitleEn,
                legalBadge = strings.legalBadge,
                onClick = { onOpenSource(source) }
            )
        }

        // Import from Phone Dashed Card
        item {
            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                PrimaryPurple.copy(alpha = 0.14f),
                                SecondaryCyan.copy(alpha = 0.14f)
                            )
                        )
                    )
                    .testTag("store_import_phone_card")
                    .clickable(onClick = onImportFromPhone)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRoundRect(
                        color = SecondaryCyan.copy(alpha = 0.45f),
                        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.importFromPhone,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = strings.importFromPhoneSub,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PrimaryPurple, SecondaryCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = strings.importFromPhone,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Downloads / Active Imports Progress Section
        item {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.downloadingTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    if (downloadTask != null) {
                        val pct = (downloadTask.progress * 100).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = downloadTask.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (pct >= 100) SuccessGreen else SecondaryCyan
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(downloadTask.progress.coerceIn(0.02f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                            )
                        }
                    } else {
                        Text(
                            text = strings.noDownloadsMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Helpful Docs Section ("مفيد")
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = strings.helpfulResourcesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        items(StoreCatalog.helpfulResources, key = { it.id }) { doc ->
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { onOpenSource(doc) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = SecondaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = if (isArabic) doc.subtitleAr else doc.subtitleEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = strings.openInBrowser,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LegalSourceCard(
    source: LegalSource,
    subtitle: String,
    legalBadge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("store_source_${source.id}")
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            ProceduralCoverArt(
                styleIndex = source.badgeColorIndex,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = legalBadge.removePrefix("✓ ").trim(),
                    style = MaterialTheme.typography.labelMedium,
                    color = SuccessGreen
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(PrimaryPurple, SecondaryCyan)))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = source.title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StoreBrowserScreen(
    title: String,
    url: String,
    onClose: () -> Unit,
    onDownloadRomUrl: (downloadUrl: String, fileName: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var loadProgress by remember { mutableIntStateOf(0) }

    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Browser Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.close,
                        tint = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .clickable {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = strings.openInBrowser,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = strings.openInBrowser,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        // Fixed Legal Reminder Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarningAmber.copy(alpha = 0.14f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = strings.legalWarningBanner,
                style = MaterialTheme.typography.labelMedium,
                color = WarningAmber
            )
        }

        if (loadProgress in 1..99) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = SecondaryCyan,
                trackColor = Color.Transparent
            )
        }

        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false
                    }
                    setDownloadListener { downloadUrl, _, contentDisposition, _, _ ->
                        val guessedName = Uri.parse(downloadUrl).lastPathSegment
                            ?: contentDisposition?.substringAfter("filename=", "homebrew_rom.zip")?.trim('"')
                            ?: "homebrew_rom.zip"
                        onDownloadRomUrl(downloadUrl, guessedName)
                    }
                    loadUrl(url)
                }
            }
        )
    }
}
