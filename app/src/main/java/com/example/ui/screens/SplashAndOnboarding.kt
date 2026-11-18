package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ProceduralCoverArt
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SplashScreen() {
    val strings = LocalAppStrings.current
    val scale = remember { Animatable(0.82f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(650))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial glow behind the controller icon
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            PrimaryPurple.copy(alpha = 0.28f),
                            SecondaryCyan.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .border(1.5.dp, BorderGlass, RoundedCornerShape(34.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_retroverse_logo),
                    contentDescription = strings.appName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "RETROVERSE",
                style = TextStyle(
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = 4.sp,
                    brush = Brush.horizontalGradient(listOf(Color.White, SecondaryCyan))
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = strings.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val strings = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    BackHandler {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            onFinish()
        }
    }

    val pages = listOf(
        Triple(strings.onboarding1Title, strings.onboarding1Desc, 0),
        Triple(strings.onboarding2Title, strings.onboarding2Desc, 3),
        Triple(strings.onboarding3Title, strings.onboarding3Desc, 4)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = strings.onboardingSkip,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
        ) { pageIndex ->
            val (title, desc, coverStyle) = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .border(1.dp, BorderGlass, RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (pageIndex == 0) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_retroverse_logo),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ProceduralCoverArt(
                            styleIndex = coverStyle,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark.copy(alpha = 0.85f))
                                .border(1.5.dp, BorderGlass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (pageIndex == 1) Icons.Rounded.Save else Icons.Rounded.VerifiedUser,
                                contentDescription = title,
                                tint = if (pageIndex == 1) SecondaryCyan else SuccessGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(36.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Indicator dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { idx ->
                val active = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (active) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) {
                                Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan))
                            } else {
                                Brush.horizontalGradient(listOf(BorderSubtle, BorderSubtle))
                            }
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        val isLast = pagerState.currentPage == 2
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_primary_button")
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryCyan)))
                .clickable {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLast) {
                    Icon(
                        imageVector = Icons.Rounded.Gamepad,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = if (isLast) strings.onboardingStart else strings.onboardingNext,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
