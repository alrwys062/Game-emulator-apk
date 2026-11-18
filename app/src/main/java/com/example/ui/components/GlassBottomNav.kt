package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextSecondary

enum class MainTab(val testTag: String) {
    HOME("nav_home"),
    LIBRARY("nav_library"),
    STORE("nav_store"),
    FAVORITES("nav_favorites"),
    SETTINGS("nav_settings")
}

private data class NavItemSpec(
    val tab: MainTab,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

@Composable
fun GlassBottomNav(
    activeTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val items = remember(strings) {
        listOf(
            NavItemSpec(MainTab.HOME, strings.navHome, Icons.Rounded.Home, Icons.Outlined.Home),
            NavItemSpec(MainTab.LIBRARY, strings.navLibrary, Icons.Rounded.GridView, Icons.Outlined.GridView),
            NavItemSpec(MainTab.STORE, strings.navStore, Icons.Rounded.ShoppingBag, Icons.Outlined.ShoppingBag),
            NavItemSpec(MainTab.FAVORITES, strings.navFavorites, Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder),
            NavItemSpec(MainTab.SETTINGS, strings.navSettings, Icons.Rounded.Settings, Icons.Outlined.Settings)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = PrimaryPurple.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                )
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SurfaceGlass.copy(alpha = 0.94f),
                            Color(0xEC0C0E16)
                        )
                    )
                )
                .border(1.dp, BorderGlass, RoundedCornerShape(34.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.tab == activeTab
                val pillModifier = if (selected) {
                    Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PrimaryPurple, SecondaryCyan)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                }

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .testTag(item.tab.testTag)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectTab(item.tab) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = pillModifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.label,
                            tint = if (selected) Color.White else TextSecondary,
                            modifier = Modifier.size(21.dp)
                        )
                        if (selected) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
