package com.drs.chatdrs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for querying and persisting cached chat messages locally.
 */
@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM cached_messages WHERE chatId = :chatId ORDER BY timestampMillis ASC")
    fun getMessagesForChat(chatId: String): Flow<List<CachedMessageEntity>>

    @Query("SELECT * FROM cached_messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): CachedMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CachedMessageEntity>)

    @Query("UPDATE cached_messages SET reaction = :reaction WHERE messageId = :messageId")
    suspend fun updateMessageReaction(messageId: String, reaction: String?)

    @Query("DELETE FROM cached_messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM cached_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)
}
