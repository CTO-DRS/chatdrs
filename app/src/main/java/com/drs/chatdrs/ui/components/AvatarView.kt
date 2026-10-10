package com.drs.chatdrs.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.R
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.OnlineGreen
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart

// Modern luxurious gradient palettes for avatars
val AvatarGradients = listOf(
    listOf(PurpleGradientStart, PurpleGradientEnd),
    listOf(Color(0xFF6C5CE7), Color(0xFFA29BFE)),
    listOf(Color(0xFF00CEC9), Color(0xFF81ECEC)),
    listOf(Color(0xFFFF7675), Color(0xFFFAB1A0)),
    listOf(Color(0xFF0984E3), Color(0xFF74B9FF)),
    listOf(Color(0xFF6C5CE7), Color(0xFFFD79A8)),
    listOf(Color(0xFF00B894), Color(0xFF55EFC4)),
    listOf(Color(0xFFE17055), Color(0xFFFDCB6E)),
    listOf(Color(0xFF2D3436), Color(0xFF636E72)),
    listOf(Color(0xFF4834D4), Color(0xFF686DE0))
)

@Composable
fun AvatarView(
    initials: String,
    gradientIndex: Int,
    isOnline: Boolean,
    size: Dp = 54.dp,
    showOnlineBadge: Boolean = true,
    isOfficialLogo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val gradientColors = AvatarGradients[gradientIndex % AvatarGradients.size]
    val avatarBrush = Brush.linearGradient(gradientColors)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isOfficialLogo) {
            // High-res interlocking C-D official brand icon
            Image(
                painter = painterResource(id = R.drawable.chatdrs_logo),
                contentDescription = "ChatDrs Logo",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.dp, PurpleGradientStart.copy(alpha = 0.3f), CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Stylized initials with subtle ambient gradient
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(avatarBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.34f).sp
                )
            }
        }

        // Online dot indicator in turquoise (#00CEC9) with surface border
        if (showOnlineBadge && isOnline) {
            val badgeSize = (size.value * 0.26f).coerceAtLeast(12f).dp
            val badgeBorder = (size.value * 0.045f).coerceAtLeast(2f).dp

            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .border(badgeBorder, surfaceColor, CircleShape)
                    .clip(CircleShape)
                    .background(OnlineGreen)
            )
        }
    }
}
