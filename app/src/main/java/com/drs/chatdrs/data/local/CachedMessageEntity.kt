package com.drs.chatdrs.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a locally cached chat message for offline history viewing.
 */
@Entity(
    tableName = "cached_messages",
    indices = [Index(value = ["chatId", "timestampMillis"])]
)
data class CachedMessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val status: String = "SENT",
    val timestampMillis: Long = System.currentTimeMillis(),
    val reaction: String? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val voiceFilePath: String? = null
)
