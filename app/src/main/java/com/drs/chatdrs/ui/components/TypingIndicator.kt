package com.drs.chatdrs.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.ui.theme.BubbleOtherDark
import com.drs.chatdrs.ui.theme.BubbleOtherLight
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary

/**
 * Three bouncing dots animation for the typing indicator.
 */
@Composable
fun BouncingDotsIndicator(
    dotSize: Dp = 7.dp,
    dotColor: Color = PurplePrimary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing_dots")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 240),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .offset(y = dot1Offset.dp)
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .offset(y = dot2Offset.dp)
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.85f))
        )
        Box(
            modifier = Modifier
                .offset(y = dot3Offset.dp)
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.7f))
        )
    }
}

/**
 * Full message bubble displaying the typing indicator inside the conversation message stream.
 */
@Composable
fun TypingBubbleMessage(
    userInitials: String,
    gradientIndex: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isDarkTheme) BubbleOtherDark else BubbleOtherLight

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Small avatar of the typing user
        AvatarView(
            initials = userInitials,
            gradientIndex = gradientIndex,
            isOnline = true,
            size = 28.dp,
            showOnlineBadge = false
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Bubble matching other person's bubble specs (22px radius, sharp bottomStart)
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomEnd = 22.dp,
                        bottomStart = 6.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BouncingDotsIndicator(
                dotSize = 7.dp,
                dotColor = PurplePrimary
            )
        }
    }
}

/**
 * Text indicator shown in the conversation top bar under the user name.
 */
@Composable
fun TopBarTypingText(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "يكتب الآن",
            fontFamily = CairoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = PurplePrimary
        )
        Spacer(modifier = Modifier.width(4.dp))
        BouncingDotsIndicator(
            dotSize = 3.5.dp,
            dotColor = PurplePrimary
        )
    }
}
