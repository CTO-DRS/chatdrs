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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.ChatTimeStyle
import com.drs.chatdrs.ui.theme.PurplePrimary

private val WaveformHeights = listOf(
    6, 12, 18, 10, 22, 16, 26, 18, 14, 28, 20, 12,
    16, 24, 18, 10, 22, 14, 20, 16, 10, 14, 8, 6
)

@Composable
fun VoiceNoteBubbleContent(
    durationFormatted: String,
    timeFormatted: String,
    isPlaying: Boolean,
    progressFraction: Float,
    isFromMe: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isFromMe) Color.White else PurplePrimary
    val inactiveColor = if (isFromMe) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val buttonBgColor = if (isFromMe) Color.White.copy(alpha = 0.25f) else PurplePrimary.copy(alpha = 0.12f)
    val textColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .widthIn(min = 210.dp, max = 280.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Play / Pause Circle Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(buttonBgColor)
                    .clickable(onClick = onTogglePlay)
                    .testTag("voice_note_play_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                    tint = activeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Waveform Bars
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp),
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalBars = WaveformHeights.size
                val currentActiveBarIndex = (progressFraction * totalBars).toInt()

                WaveformHeights.forEachIndexed { index, barHeight ->
                    val isBarActive = index <= currentActiveBarIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isBarActive) activeColor else inactiveColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom row: Duration and Message time & read receipt
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = durationFormatted,
                fontFamily = CairoFont,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.85f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeFormatted,
                    style = ChatTimeStyle.copy(
                        color = textColor.copy(alpha = 0.75f)
                    )
                )

                if (isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.DoneAll,
                        contentDescription = "تم القراءة",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
