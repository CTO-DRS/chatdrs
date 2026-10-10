package com.drs.chatdrs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.drs.chatdrs.model.ActiveContact
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.OnlineGreen
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary

@Composable
fun ActiveStoriesBar(
    contacts: List<ActiveContact>,
    onContactClick: (ActiveContact) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Section title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "النشطون الآن",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
            }

            Text(
                text = "${contacts.count { it.isOnline }} متصل",
                fontFamily = CairoFont,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = PurplePrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Row of Avatars
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(contacts, key = { it.id }) { contact ->
                ActiveStoryItem(
                    contact = contact,
                    onClick = {
                        if (contact.isMyStory) onAddStoryClick() else onContactClick(contact)
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveStoryItem(
    contact: ActiveContact,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val storyBrush = Brush.sweepGradient(
        listOf(
            PurpleGradientStart,
            Color(0xFFA29BFE),
            Color(0xFF00CEC9),
            PurpleGradientEnd,
            PurpleGradientStart
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("story_item_${contact.id}")
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Story Ring if has story
            if (contact.hasStory) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.5.dp, storyBrush, CircleShape)
                )
            }

            // Avatar Container
            AvatarView(
                initials = contact.avatarInitials,
                gradientIndex = contact.avatarGradientIndex,
                isOnline = contact.isOnline,
                size = if (contact.hasStory) 48.dp else 52.dp,
                showOnlineBadge = !contact.isMyStory
            )

            // Add (+) Badge for My Story
            if (contact.isMyStory) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 1.dp, y = 1.dp)
                        .border(2.dp, surfaceColor, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PurpleGradientStart, PurpleGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة قصة",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = contact.name,
            fontFamily = CairoFont,
            fontWeight = if (contact.isMyStory) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
