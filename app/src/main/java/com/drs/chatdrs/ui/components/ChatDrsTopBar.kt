package com.drs.chatdrs.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.R
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.OnlineGreen
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary

@Composable
fun ChatDrsTopBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit = {},
    cachedChatsCount: Int = 8,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var isEdgeTelemetryExpanded by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val infiniteTransition = rememberInfiniteTransition(label = "brand_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Surface(
        color = surfaceColor,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PurpleGradientStart.copy(alpha = if (isDarkTheme) 0.14f else 0.06f),
                            surfaceColor
                        )
                    )
                )
        ) {
            // Topmost Animated Iridescent Crown Edge Strip
            LuminousEdgeStrip(
                height = 3.5.dp,
                isActive = true
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Main Bar Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brand Logo & Wordmark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isEdgeTelemetryExpanded = !isEdgeTelemetryExpanded }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        // Interlocking C-D Logo container with animated luminous edge border
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(13.dp),
                                    ambientColor = PurplePrimary.copy(alpha = glowAlpha),
                                    spotColor = PurplePrimary.copy(alpha = glowAlpha)
                                )
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            PurpleGradientStart.copy(alpha = 0.20f),
                                            PurpleGradientEnd.copy(alpha = 0.10f)
                                        )
                                    )
                                )
                                .border(
                                    1.2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            PurpleGradientStart.copy(alpha = glowAlpha),
                                            OnlineGreen.copy(alpha = glowAlpha * 0.8f),
                                            PurpleGradientEnd.copy(alpha = glowAlpha)
                                        )
                                    ),
                                    RoundedCornerShape(13.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chatdrs_logo),
                                contentDescription = "شعار ChatDrs",
                                modifier = Modifier
                                    .size(29.dp)
                                    .clip(RoundedCornerShape(9.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // App Title & Tagline
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Chat",
                                    style = TextStyle(
                                        fontFamily = CairoFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp,
                                        color = onSurfaceColor
                                    )
                                )
                                Text(
                                    text = "Drs",
                                    style = TextStyle(
                                        fontFamily = CairoFont,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 21.sp,
                                        brush = Brush.linearGradient(
                                            listOf(PurpleGradientStart, PurpleGradientEnd)
                                        )
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Edge Pro Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(PurpleGradientStart, OnlineGreen)
                                            )
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "EDGE+",
                                        fontFamily = CairoFont,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.offset(y = (-2).dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "متصل • مشفّر تماماً • حافة ذكية",
                                    fontFamily = CairoFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = mutedColor
                                )
                            }
                        }
                    }

                    // Top Bar Actions (Edge Process Pulse Button, Dark Mode Toggle, Menu)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Edge Development Process & Telemetry Quick Toggle Button
                        IconButton(
                            onClick = { isEdgeTelemetryExpanded = !isEdgeTelemetryExpanded },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isEdgeTelemetryExpanded) PurplePrimary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            PurpleGradientStart.copy(alpha = if (isEdgeTelemetryExpanded) 0.8f else 0.3f),
                                            OnlineGreen.copy(alpha = if (isEdgeTelemetryExpanded) 0.8f else 0.3f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .testTag("edge_telemetry_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = "شريط معالجة الحافة",
                                tint = if (isEdgeTelemetryExpanded) OnlineGreen else PurplePrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Dark/Light Mode Switcher
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "تغيير المظهر",
                                tint = if (isDarkTheme) Color(0xFFFDCB6E) else PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Overflow Menu
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .testTag("menu_more_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "المزيد",
                                    tint = onSurfaceColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "لوحة معالجة الحافة والبيانات",
                                            fontFamily = CairoFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        isEdgeTelemetryExpanded = !isEdgeTelemetryExpanded
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "إنشاء مجموعة جديدة",
                                            fontFamily = CairoFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onOpenSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "الرسائل المحفوظة",
                                            fontFamily = CairoFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "الإعدادات والخصوصية",
                                            fontFamily = CairoFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onOpenSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "تسجيل الخروج",
                                            fontFamily = CairoFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onSignOut()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Modern Top Strip & Live Edge Process Telemetry Bar
                EdgeProcessTelemetryStrip(
                    isExpanded = isEdgeTelemetryExpanded,
                    onToggleExpand = { isEdgeTelemetryExpanded = !isEdgeTelemetryExpanded },
                    cachedChatsCount = cachedChatsCount
                )
            }

            // Subtle Bottom Edge Divider with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                PurpleGradientStart.copy(alpha = 0.35f),
                                OnlineGreen.copy(alpha = 0.35f),
                                PurpleGradientEnd.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
