package com.drs.chatdrs.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary

@Composable
fun ChatSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int? = null,
    onFilterClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val borderBrush = if (isFocused || query.isNotEmpty()) {
        Brush.linearGradient(
            listOf(PurpleGradientStart.copy(alpha = 0.8f), PurpleGradientEnd.copy(alpha = 0.6f))
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = if (isFocused) 6.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isFocused) PurplePrimary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f),
                spotColor = if (isFocused) PurplePrimary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(surfaceVariantColor)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .height(50.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search Icon with subtle purple tint
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFocused || query.isNotEmpty()) PurplePrimary.copy(alpha = 0.12f)
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "بحث",
                    tint = if (isFocused || query.isNotEmpty()) PurplePrimary else mutedColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Input / Placeholder Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "بحث في المحادثات والرسائل...",
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = mutedColor.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = onSurfaceColor
                    ),
                    cursorBrush = SolidColor(PurplePrimary),
                    singleLine = true,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_search_bar_input")
                )
            }

            // Results count badge when typing and matches found
            if (query.isNotEmpty() && matchCount != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PurplePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$matchCount نتيجة",
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = PurplePrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Clear Button when query has text
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .testTag("clear_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "مسح البحث",
                        tint = onSurfaceColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Filter button when search is idle
            if (query.isEmpty() && onFilterClick != null) {
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .testTag("filter_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "تصفية",
                        tint = mutedColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
