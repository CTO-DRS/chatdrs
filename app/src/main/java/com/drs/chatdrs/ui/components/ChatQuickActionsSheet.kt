package com.drs.chatdrs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MarkChatRead
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.ErrorRed
import com.drs.chatdrs.ui.theme.PurplePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatQuickActionsSheet(
    chat: ChatItem?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onToggleRead: (String) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    if (chat == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header with Avatar and Chat Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                AvatarView(
                    initials = chat.avatarInitials,
                    gradientIndex = chat.avatarGradientIndex,
                    isOnline = chat.isOnline,
                    size = 46.dp,
                    isOfficialLogo = chat.id == "chat_pinned_1"
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = chat.name,
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (chat.isOnline) "متصل الآن" else "آخر ظهور حديثاً",
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Items
            ActionRow(
                icon = Icons.Outlined.PushPin,
                title = if (chat.isPinned) "إلغاء التثبيت من الأعلى" else "تثبيت في أعلى القائمة",
                onClick = {
                    onTogglePin(chat.id)
                    onDismiss()
                }
            )

            ActionRow(
                icon = Icons.Outlined.NotificationsOff,
                title = if (chat.isMuted) "إلغاء كتم التنبيهات" else "كتم التنبيهات",
                onClick = {
                    onToggleMute(chat.id)
                    onDismiss()
                }
            )

            ActionRow(
                icon = Icons.Outlined.MarkChatRead,
                title = if (chat.unreadCount > 0) "تعيين كمقروء" else "تعيين كغير مقروء",
                onClick = {
                    onToggleRead(chat.id)
                    onDismiss()
                }
            )

            ActionRow(
                icon = Icons.Outlined.Star,
                title = "إضافة إلى المفضلة",
                onClick = {
                    onDismiss()
                }
            )

            // Delete Action in Red (#FF6B6B)
            ActionRow(
                icon = Icons.Outlined.Delete,
                title = "حذف المحادثة",
                tint = ErrorRed,
                onClick = {
                    onDeleteChat(chat.id)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontFamily = CairoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = tint
        )
    }
}
