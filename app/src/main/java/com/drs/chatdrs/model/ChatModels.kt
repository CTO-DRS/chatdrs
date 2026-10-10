package com.drs.chatdrs.model

enum class ChatCategory(val title: String) {
    ALL("الكل"),
    UNREAD("غير مقروءة"),
    GROUPS("المجموعات"),
    CHANNELS("القنوات"),
    FAVORITES("المفضلة")
}

enum class MessageDeliveryStatus {
    NONE,
    SENT,
    DELIVERED,
    READ
}

enum class ChatType {
    DIRECT,
    GROUP,
    CHANNEL,
    VERIFIED_BOT
}

data class ChatItem(
    val id: String,
    val name: String,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isFavorite: Boolean = false,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.NONE,
    val chatType: ChatType = ChatType.DIRECT,
    val isTyping: Boolean = false,
    val hasVoiceNote: Boolean = false,
    val hasPhoto: Boolean = false,
    val voiceDuration: String? = null,
    val senderPrefix: String? = null,
    val avatarInitials: String = "",
    val avatarGradientIndex: Int = 0
)

data class ActiveContact(
    val id: String,
    val name: String,
    val isMyStory: Boolean = false,
    val hasStory: Boolean = false,
    val isOnline: Boolean = true,
    val avatarInitials: String = "",
    val avatarGradientIndex: Int = 0
)
