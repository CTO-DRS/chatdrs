package com.drs.chatdrs.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class FirestoreUser(
    val userId: String = "",
    val displayName: String = "",
    val status: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class FirestoreChat(
    val chatId: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    @ServerTimestamp val lastMessageTime: Timestamp? = null,
    val lastSenderId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class FirestoreMessage(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val status: String = "SENT",
    @ServerTimestamp val createdAt: Timestamp? = null
)
