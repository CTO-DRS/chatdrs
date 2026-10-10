package com.drs.chatdrs.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.model.ChatType
import com.drs.chatdrs.model.MessageDeliveryStatus
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatItemRow(
    chat: ChatItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Infinite transition for realistic typing dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing_alpha"
    )

    val isHighlightedEdge = chat.unreadCount > 0 || chat.isPinned
    val rowBorderModifier = if (isHighlightedEdge) {
        Modifier.border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    PurpleGradientStart.copy(alpha = if (chat.unreadCount > 0) 0.45f else 0.25f),
                    Color(0xFF00CEC9).copy(alpha = if (chat.unreadCount > 0) 0.35f else 0.15f),
                    PurpleGradientEnd.copy(alpha = if (chat.unreadCount > 0) 0.45f else 0.25f)
                )
            ),
            shape = RoundedCornerShape(18.dp)
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (chat.unreadCount > 0) {
                    Brush.horizontalGradient(
                        listOf(
                            PurpleGradientStart.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .then(rowBorderModifier)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 11.dp)
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AvatarView(
            initials = chat.avatarInitials,
            gradientIndex = chat.avatarGradientIndex,
            isOnline = chat.isOnline,
            size = 54.dp,
            isOfficialLogo = chat.id == "chat_pinned_1"
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Center: Name and Last Message Preview
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name Row + Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chat.name,
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Verified Badge for Official Channels/Accounts
                if (chat.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = "موثّق",
                        tint = PurplePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Mute indicator icon
                if (chat.isMuted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.NotificationsOff,
                        contentDescription = "مكتوم",
                        tint = mutedColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message Preview Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sender Prefix for Groups (e.g. "عبدالعزيز: ")
                if (chat.senderPrefix != null && !chat.isTyping) {
                    Text(
                        text = chat.senderPrefix,
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PurplePrimary
                    )
                }

                // If currently typing
                if (chat.isTyping) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = null,
                            tint = PurplePrimary.copy(alpha = alphaAnim),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "يكتب الآن...",
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = PurplePrimary.copy(alpha = alphaAnim)
                        )
                    }
                } else if (chat.hasVoiceNote) {
                    // Voice note icon + preview
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "رسالة صوتية",
                            tint = PurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "رسالة صوتية ${chat.voiceDuration?.let { "($it)" } ?: ""}",
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )
                    }
                } else if (chat.hasPhoto) {
                    // Photo icon + preview
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "صورة",
                            tint = PurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chat.lastMessage,
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = mutedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    // Regular text message preview
                    Text(
                        text = chat.lastMessage,
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (chat.unreadCount > 0) onSurfaceColor else mutedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right side: Time, Delivery Status, Unread Badge, Pin
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            // Time & Delivery Receipt
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delivery status check marks for sent messages
                when (chat.deliveryStatus) {
                    MessageDeliveryStatus.READ -> {
                        Icon(
                            imageVector = Icons.Outlined.DoneAll,
                            contentDescription = "مقروء",
                            tint = PurplePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    MessageDeliveryStatus.DELIVERED -> {
                        Icon(
                            imageVector = Icons.Outlined.DoneAll,
                            contentDescription = "مستلم",
                            tint = mutedColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    MessageDeliveryStatus.SENT -> {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "مرسل",
                            tint = mutedColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    MessageDeliveryStatus.NONE -> {}
                }

                Text(
                    text = chat.lastMessageTime,
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = if (chat.unreadCount > 0) PurplePrimary else mutedColor
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Bottom Right Badges (Unread Count Pill / Pin Icon)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (chat.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "مثبتة",
                        tint = mutedColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Unread Count Badge with Purple Gradient & Pill Shape
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PurpleGradientStart, PurpleGradientEnd)
                                )
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "+99" else chat.unreadCount.toString(),
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
