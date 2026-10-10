package com.drs.chatdrs.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting local Room database access for offline message caching.
 */
class LocalMessageRepository(private val chatMessageDao: ChatMessageDao) {

    fun observeCachedMessages(chatId: String): Flow<List<CachedMessageEntity>> {
        return chatMessageDao.getMessagesForChat(chatId)
    }

    suspend fun getMessageById(messageId: String): CachedMessageEntity? {
        return chatMessageDao.getMessageById(messageId)
    }

    suspend fun cacheMessage(message: CachedMessageEntity) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun cacheMessages(messages: List<CachedMessageEntity>) {
        if (messages.isNotEmpty()) {
            chatMessageDao.insertMessages(messages)
        }
    }

    suspend fun updateReaction(messageId: String, reaction: String?) {
        chatMessageDao.updateMessageReaction(messageId, reaction)
    }

    suspend fun deleteMessageById(messageId: String) {
        chatMessageDao.deleteMessageById(messageId)
    }

    suspend fun deleteMessagesForChat(chatId: String) {
        chatMessageDao.deleteMessagesForChat(chatId)
    }
}
