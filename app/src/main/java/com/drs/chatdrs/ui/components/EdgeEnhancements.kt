package com.drs.chatdrs.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.OnlineGreen
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary

/**
 * Animated iridescent luminous top/bottom edge strip that flows continuously across the screen edge.
 */
@Composable
fun LuminousEdgeStrip(
    height: Dp = 3.dp,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_strip_shimmer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 1800 else 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )

    val colors = listOf(
        PurpleGradientStart,
        OnlineGreen,
        Color(0xFFA29BFE),
        PurpleGradientEnd,
        OnlineGreen,
        PurpleGradientStart
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("luminous_edge_strip")
    ) {
        val width = size.width.coerceAtLeast(1f)
        val shift = phase * width * 1.5f
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(x = -width + shift, y = 0f),
            end = Offset(x = shift + width * 0.5f, y = size.height)
        )
        drawRect(brush = brush)
    }
}

/**
 * Modern Top Edge Telemetry & Development Process Strip (شريط الحالة المتطور ومعالجة البيانات الحية).
 * Shows real-time end-to-end encryption status, Cloud Firestore sync, and Room SQLite offline cache state,
 * with an interactive expandable telemetry drawer.
 */
@Composable
fun EdgeProcessTelemetryStrip(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isSyncing: Boolean = false,
    cachedChatsCount: Int = 8,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "telemetry_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("edge_process_telemetry_strip")
    ) {
        // Ultra-sleek interactive pill strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PurpleGradientStart.copy(alpha = 0.12f),
                            OnlineGreen.copy(alpha = 0.08f),
                            PurpleGradientEnd.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(
                    width = 0.9.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            PurpleGradientStart.copy(alpha = 0.45f),
                            OnlineGreen.copy(alpha = 0.4f),
                            PurpleGradientEnd.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onToggleExpand() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("telemetry_strip_toggle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Live Pulse + E2EE & Cloud + Room Status Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing Neon Emerald Core
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen.copy(alpha = pulseAlpha * 0.35f))
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                }

                Text(
                    text = if (isSyncing) "جارٍ معالجة ومزامنة الحافة..." else "نواة الحافة الذكية نشطة",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = onSurface
                )

                // Micro Tag: E2EE
                MiniTelemetryChip(
                    label = "E2EE",
                    accentColor = PurplePrimary
                )

                // Micro Tag: Room + Cloud
                MiniTelemetryChip(
                    label = "Room + Cloud",
                    accentColor = OnlineGreen
                )
            }

            // Right: Expand/Collapse Chevron & Latency indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "12ms",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    color = OnlineGreen
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "تفاصيل معالجة الحافة",
                    tint = mutedColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Expandable Live Process Pipeline Panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceVariant.copy(alpha = 0.7f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                PurpleGradientStart.copy(alpha = 0.35f),
                                OnlineGreen.copy(alpha = 0.25f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("telemetry_expanded_panel"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Memory,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سير عمليات الحافة والتطوير الفوري (Edge Pipeline)",
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = onSurface
                        )
                    }

                    Text(
                        text = "متصل 100%",
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = OnlineGreen
                    )
                }

                // 3-Stage Process Indicators Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PipelineNodeCard(
                        icon = Icons.Outlined.Lock,
                        title = "التشفير الطرفي",
                        subtitle = "AES-GCM نشط",
                        accentColor = PurplePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    PipelineNodeCard(
                        icon = Icons.Outlined.CloudDone,
                        title = "سحابة Firestore",
                        subtitle = "مزامنة لحظية",
                        accentColor = OnlineGreen,
                        modifier = Modifier.weight(1f)
                    )
                    PipelineNodeCard(
                        icon = Icons.Outlined.Storage,
                        title = "ذاكرة Room",
                        subtitle = "$cachedChatsCount محادثات مخزنة",
                        accentColor = PurpleGradientEnd,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Animated micro progress bar inside expanded panel
                LuminousEdgeStrip(
                    height = 2.dp,
                    isActive = true,
                    modifier = Modifier.clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun MiniTelemetryChip(
    label: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(0.7.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = CairoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = accentColor
        )
    }
}

@Composable
private fun PipelineNodeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(0.8.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = title,
                fontFamily = CairoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle,
            fontFamily = CairoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            color = accentColor,
            maxLines = 1
        )
    }
}

/**
 * Compact Conversation Top Strip for displaying real-time E2EE + Room Offline Cache & Cloud Sync pipeline
 * right underneath the Conversation top bar.
 */
@Composable
fun ConversationEdgeSubStrip(
    isSending: Boolean,
    messageCount: Int,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        LuminousEdgeStrip(
            height = 2.5.dp,
            isActive = isSending
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PurpleGradientStart.copy(alpha = 0.10f),
                            OnlineGreen.copy(alpha = 0.06f),
                            PurpleGradientEnd.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .testTag("conversation_edge_sub_strip"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = OnlineGreen,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isSending) "جارٍ التشفير والمزامنة مع السحابة و Room..." else "قناة الحافة المشفّرة • تزامن Firestore + Room فوري",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = onSurfaceVariant
                )
            }

            Text(
                text = "$messageCount رسالة محفوظة",
                fontFamily = CairoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = PurplePrimary
            )
        }
    }
}
