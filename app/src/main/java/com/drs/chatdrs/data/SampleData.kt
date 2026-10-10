package com.drs.chatdrs.data

import com.drs.chatdrs.model.ActiveContact
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.model.ChatType
import com.drs.chatdrs.model.MessageDeliveryStatus

object SampleData {

    val activeContacts = listOf(
        ActiveContact(
            id = "story_me",
            name = "قصتي",
            isMyStory = true,
            hasStory = false,
            isOnline = true,
            avatarInitials = "أنا",
            avatarGradientIndex = 0
        ),
        ActiveContact(
            id = "contact_1",
            name = "د. سارة",
            isMyStory = false,
            hasStory = true,
            isOnline = true,
            avatarInitials = "سم",
            avatarGradientIndex = 1
        ),
        ActiveContact(
            id = "contact_2",
            name = "م. فيصل",
            isMyStory = false,
            hasStory = false,
            isOnline = true,
            avatarInitials = "فت",
            avatarGradientIndex = 2
        ),
        ActiveContact(
            id = "contact_3",
            name = "نورة",
            isMyStory = false,
            hasStory = true,
            isOnline = true,
            avatarInitials = "نق",
            avatarGradientIndex = 3
        ),
        ActiveContact(
            id = "contact_4",
            name = "أحمد الشمري",
            isMyStory = false,
            hasStory = false,
            isOnline = true,
            avatarInitials = "أش",
            avatarGradientIndex = 4
        ),
        ActiveContact(
            id = "contact_5",
            name = "طارق",
            isMyStory = false,
            hasStory = true,
            isOnline = false,
            avatarInitials = "طح",
            avatarGradientIndex = 5
        ),
        ActiveContact(
            id = "contact_6",
            name = "سلطان",
            isMyStory = false,
            hasStory = true,
            isOnline = true,
            avatarInitials = "سع",
            avatarGradientIndex = 6
        )
    )

    val sampleChats = listOf(
        ChatItem(
            id = "chat_pinned_1",
            name = "فريق ChatDrs التقني",
            isOnline = true,
            isVerified = true,
            isPinned = true,
            isMuted = false,
            isFavorite = true,
            lastMessage = "مرحباً بك في ChatDrs! تم تفعيل التشفير التام وتحديث الواجهة الفاخرة.",
            lastMessageTime = "الآن",
            unreadCount = 1,
            deliveryStatus = MessageDeliveryStatus.READ,
            chatType = ChatType.CHANNEL,
            avatarInitials = "CD",
            avatarGradientIndex = 0
        ),
        ChatItem(
            id = "chat_pinned_2",
            name = "د. سارة المنصور",
            isOnline = true,
            isVerified = true,
            isPinned = true,
            isMuted = false,
            isFavorite = true,
            lastMessage = "أرفقت لك التقرير الاستشاري الشامل، بانتظار ملاحظاتك الكريمة.",
            lastMessageTime = "١٢:٣٨ م",
            unreadCount = 2,
            deliveryStatus = MessageDeliveryStatus.NONE,
            chatType = ChatType.DIRECT,
            avatarInitials = "سم",
            avatarGradientIndex = 1
        ),
        ChatItem(
            id = "chat_3",
            name = "م. فيصل التميمي",
            isOnline = true,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = true,
            lastMessage = "يكتب الآن...",
            lastMessageTime = "١٢:١٥ م",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.NONE,
            chatType = ChatType.DIRECT,
            isTyping = true,
            avatarInitials = "فت",
            avatarGradientIndex = 2
        ),
        ChatItem(
            id = "chat_4",
            name = "مجموعة تطوير الواجهات 🎨",
            isOnline = true,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = false,
            lastMessage = "تم اعتماد درجات البنفسجي الفخمة وزوايا 22px للفقاعات!",
            lastMessageTime = "١١:٥٠ ص",
            unreadCount = 4,
            deliveryStatus = MessageDeliveryStatus.NONE,
            chatType = ChatType.GROUP,
            senderPrefix = "عبدالعزيز: ",
            avatarInitials = "تو",
            avatarGradientIndex = 3
        ),
        ChatItem(
            id = "chat_5",
            name = "أحمد الشمري",
            isOnline = true,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = false,
            lastMessage = "تسجيل صوتي (0:38)",
            lastMessageTime = "١٠:٤٢ ص",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.READ,
            chatType = ChatType.DIRECT,
            hasVoiceNote = true,
            voiceDuration = "0:38",
            avatarInitials = "أش",
            avatarGradientIndex = 4
        ),
        ChatItem(
            id = "chat_6",
            name = "قناة التقنية والذكاء العربي",
            isOnline = false,
            isVerified = true,
            isPinned = false,
            isMuted = true,
            isFavorite = false,
            lastMessage = "صورة: المقارنة المعمارية بين محركات التطبيقات السحابية",
            lastMessageTime = "٠٩:٢٠ ص",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.NONE,
            chatType = ChatType.CHANNEL,
            hasPhoto = true,
            avatarInitials = "تق",
            avatarGradientIndex = 5
        ),
        ChatItem(
            id = "chat_7",
            name = "م. نورة القحطاني",
            isOnline = false,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = true,
            lastMessage = "شكراً جزيلاً لك، وصل الملف بالشكل والمواصفات المطلوبة تماماً.",
            lastMessageTime = "أمس",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.READ,
            chatType = ChatType.DIRECT,
            avatarInitials = "نق",
            avatarGradientIndex = 6
        ),
        ChatItem(
            id = "chat_8",
            name = "عائلة آل سعد 🤍",
            isOnline = false,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = false,
            lastMessage = "لا تنسوا اجتماع العائلة يوم الجمعة القادم بعد صلاة المغرب.",
            lastMessageTime = "أمس",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.READ,
            chatType = ChatType.GROUP,
            senderPrefix = "الوالدة: ",
            avatarInitials = "عس",
            avatarGradientIndex = 7
        ),
        ChatItem(
            id = "chat_9",
            name = "عمر الفاروق",
            isOnline = false,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = false,
            lastMessage = "موعدنا غداً بعد العصر بإذن الله لمناقشة التفاصيل.",
            lastMessageTime = "الأحد",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.DELIVERED,
            chatType = ChatType.DIRECT,
            avatarInitials = "عف",
            avatarGradientIndex = 8
        ),
        ChatItem(
            id = "chat_10",
            name = "سلطان العتيبي",
            isOnline = true,
            isVerified = false,
            isPinned = false,
            isMuted = false,
            isFavorite = false,
            lastMessage = "تم التحويل لحسابك بنجاح، تسلم يا غالي وبارك الله فيك.",
            lastMessageTime = "السبت",
            unreadCount = 0,
            deliveryStatus = MessageDeliveryStatus.READ,
            chatType = ChatType.DIRECT,
            avatarInitials = "سع",
            avatarGradientIndex = 9
        )
    )
}
