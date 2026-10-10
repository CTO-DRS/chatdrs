package com.drs.chatdrs.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.drs.chatdrs.R
import com.drs.chatdrs.data.SampleData
import com.drs.chatdrs.data.local.CachedMessageEntity
import com.drs.chatdrs.data.local.ChatDrsDatabase
import com.drs.chatdrs.data.local.ChatMessageDao
import com.drs.chatdrs.data.local.LocalMessageRepository
import com.drs.chatdrs.firebase.FirestoreChat
import com.drs.chatdrs.firebase.FirestoreMessage
import com.drs.chatdrs.firebase.MessageRepository
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.model.MessageDeliveryStatus
import com.drs.chatdrs.ui.screens.MessageBubbleData
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sealed UI State hierarchy for observing chat messages and conversations from Firestore and local Room cache.
 */
sealed interface ChatMessagesUiState {
    data object Loading : ChatMessagesUiState
    data class Success(val messages: List<MessageBubbleData>) : ChatMessagesUiState
    data class Error(val errorMessage: String) : ChatMessagesUiState
}

/**
 * ChatViewModel manages the state of chat messages and conversations, coordinating
 * real-time persistent storage with Firebase Firestore via [MessageRepository] and
 * local offline message history caching with Room via [LocalMessageRepository].
 */
class ChatViewModel(
    private val repository: MessageRepository,
    private val currentUserId: String = Firebase.auth.currentUser?.uid.orEmpty(),
    private val localMessageRepository: LocalMessageRepository? = null
) : ViewModel() {

    constructor(
        repository: MessageRepository,
        chatMessageDao: ChatMessageDao,
        currentUserId: String = Firebase.auth.currentUser?.uid.orEmpty()
    ) : this(
        repository = repository,
        currentUserId = currentUserId,
        localMessageRepository = LocalMessageRepository(chatMessageDao)
    )

    // Local conversation list merged with real-time Firestore chat updates
    private val _localChats = MutableStateFlow<List<ChatItem>>(SampleData.sampleChats)

    // Real-time Firestore chats stream with two-tier error handling
    private val _firestoreChats = MutableStateFlow<List<FirestoreChat>>(emptyList())

    val chats: StateFlow<List<ChatItem>> = combine(_localChats, _firestoreChats) { localList, remoteList ->
        val remoteById = remoteList.associateBy { it.chatId }
        val updatedLocal = localList.map { localChat ->
            val remote = remoteById[localChat.id]
            if (remote != null) {
                localChat.copy(
                    lastMessage = remote.lastMessage.ifBlank { localChat.lastMessage },
                    lastMessageTime = formatTimestamp(remote.lastMessageTime ?: remote.updatedAt),
                    deliveryStatus = if (remote.lastSenderId == currentUserId) {
                        MessageDeliveryStatus.READ
                    } else {
                        localChat.deliveryStatus
                    }
                )
            } else {
                localChat
            }
        }

        // Include any newly created Firestore chats that aren't in the initial sample list
        val localIds = localList.map { it.id }.toSet()
        val newRemoteChats = remoteList
            .filter { it.chatId.isNotBlank() && it.chatId !in localIds }
            .map { remote ->
                val displayTitle = remote.chatId
                    .removePrefix("chat_")
                    .replace("_", " ")
                    .ifBlank { "محادثة جديدة" }
                ChatItem(
                    id = remote.chatId,
                    name = displayTitle,
                    isOnline = true,
                    lastMessage = remote.lastMessage.ifBlank { "بدأت محادثة جديدة" },
                    lastMessageTime = formatTimestamp(remote.lastMessageTime ?: remote.updatedAt),
                    deliveryStatus = MessageDeliveryStatus.READ,
                    avatarInitials = displayTitle.take(2)
                )
            }

        newRemoteChats + updatedLocal
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SampleData.sampleChats
    )

    // Active conversation ID and message states
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _messagesUiState = MutableStateFlow<ChatMessagesUiState>(ChatMessagesUiState.Loading)
    val messagesUiState: StateFlow<ChatMessagesUiState> = _messagesUiState.asStateFlow()

    // Local transient metadata (such as emoji reactions or local audio file paths) keyed by messageId
    private val _localReactions = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _localVoiceMetadata = MutableStateFlow<Map<String, Pair<Int, String?>>>(emptyMap())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    private var messagesJob: Job? = null
    private var firestoreSyncJob: Job? = null
    private var chatsJob: Job? = null

    init {
        if (currentUserId.isNotBlank()) {
            observeUserChats()
        }
    }

    /**
     * Starts observing the authenticated user's chats from Firestore.
     */
    fun observeUserChats() {
        chatsJob?.cancel()
        chatsJob = viewModelScope.launch {
            repository.observeUserChats()
                .catch { error ->
                    Log.w(TAG, "Failed to observe user chats from Firestore", error)
                    _errorBanner.value = error.localizedMessage ?: "تعذر مزامنة قائمة المحادثات"
                }
                .collect { remoteChats ->
                    _firestoreChats.value = remoteChats
                }
        }
    }

    /**
     * Selects a conversation, immediately loads any locally cached history from Room,
     * ensures the parent document exists in Firestore, and attaches a real-time sync listener.
     */
    fun openConversation(chat: ChatItem) {
        if (_activeChatId.value == chat.id && messagesJob?.isActive == true) return
        _activeChatId.value = chat.id
        _messagesUiState.value = ChatMessagesUiState.Loading

        // Clear unread count locally when opening a conversation
        _localChats.update { list ->
            list.map { if (it.id == chat.id) it.copy(unreadCount = 0) else it }
        }

        messagesJob?.cancel()
        firestoreSyncJob?.cancel()

        // Start observing the local Room cache and Firestore stream so offline history is available immediately
        startListeningToMessages(chat)

        // Ensure parent chat document exists for the current user so rules allow reading/writing messages
        if (currentUserId.isNotBlank()) {
            repository.ensureChatExists(
                chatId = chat.id,
                initialLastMessage = chat.lastMessage.ifBlank { "مرحباً بك في ChatDrs" }
            ) { result ->
                if (result.isFailure) {
                    val err = result.exceptionOrNull()
                    Log.w(TAG, "Could not ensure chat document for ${chat.id}", err)
                }
                startSyncingFirestoreToRoom(chat.id)
            }
        } else {
            startSyncingFirestoreToRoom(chat.id)
        }
    }

    private fun startSyncingFirestoreToRoom(chatId: String) {
        val localRepo = localMessageRepository ?: return
        firestoreSyncJob?.cancel()
        firestoreSyncJob = viewModelScope.launch {
            repository.observeMessages(chatId)
                .catch { error ->
                    Log.w(TAG, "Firestore sync paused/offline for chat $chatId; serving from Room cache", error)
                }
                .collect { remoteMessages ->
                    val entities = remoteMessages.mapNotNull { msg ->
                        if (msg.messageId.isBlank()) return@mapNotNull null
                        val existing = localRepo.getMessageById(msg.messageId)
                        val voiceMeta = _localVoiceMetadata.value[msg.messageId]
                        val isVoice = msg.text.startsWith(VOICE_NOTE_PREFIX) ||
                            voiceMeta != null ||
                            existing?.isVoiceNote == true
                        val duration = voiceMeta?.first
                            ?: existing?.voiceDurationSeconds?.takeIf { it > 0 }
                            ?: parseVoiceDurationSeconds(msg.text)
                        val filePath = voiceMeta?.second ?: existing?.voiceFilePath
                        val reaction = _localReactions.value[msg.messageId] ?: existing?.reaction
                        val tsMillis = msg.createdAt?.toDate()?.time
                            ?: existing?.timestampMillis
                            ?: System.currentTimeMillis()

                        CachedMessageEntity(
                            messageId = msg.messageId,
                            chatId = chatId,
                            senderId = msg.senderId,
                            text = msg.text,
                            status = msg.status.ifBlank { "READ" },
                            timestampMillis = tsMillis,
                            reaction = reaction,
                            isVoiceNote = isVoice,
                            voiceDurationSeconds = if (isVoice) duration else 0,
                            voiceFilePath = filePath
                        )
                    }
                    localRepo.cacheMessages(entities)
                }
        }
    }

    private fun startListeningToMessages(chat: ChatItem) {
        messagesJob?.cancel()
        val localRepo = localMessageRepository

        if (localRepo != null) {
            messagesJob = viewModelScope.launch {
                combine(
                    localRepo.observeCachedMessages(chat.id),
                    _localReactions,
                    _localVoiceMetadata
                ) { cachedEntities, reactions, voiceMeta ->
                    val mappedCached = cachedEntities.map { entity ->
                        entity.toBubbleData(
                            currentUserId = currentUserId,
                            reactionOverride = reactions[entity.messageId],
                            voiceMetaOverride = voiceMeta[entity.messageId]
                        )
                    }

                    val defaultSeeds = buildDefaultSeedMessages(chat, reactions)
                    if (mappedCached.isEmpty()) {
                        defaultSeeds
                    } else {
                        defaultSeeds + mappedCached
                    }
                }
                    .map<List<MessageBubbleData>, ChatMessagesUiState> { list ->
                        ChatMessagesUiState.Success(list)
                    }
                    .catch { error ->
                        Log.w(TAG, "Error observing cached Room messages for chat ${chat.id}", error)
                        val fallbackSeeds = buildDefaultSeedMessages(chat, _localReactions.value)
                        emit(ChatMessagesUiState.Success(fallbackSeeds))
                    }
                    .collect { uiState ->
                        _messagesUiState.value = uiState
                    }
            }
        } else {
            // Direct Firestore stream when no Room database is provided
            messagesJob = viewModelScope.launch {
                combine(
                    repository.observeMessages(chat.id).catch { error ->
                        Log.w(TAG, "Error observing messages for chat ${chat.id}", error)
                        _errorBanner.value = error.localizedMessage ?: "تعذر تحميل الرسائل السحابية"
                        emit(emptyList())
                    },
                    _localReactions,
                    _localVoiceMetadata
                ) { firestoreMessages, reactions, voiceMeta ->
                    val mappedFirestore = firestoreMessages.map { msg ->
                        msg.toBubbleData(
                            currentUserId = currentUserId,
                            reaction = reactions[msg.messageId],
                            voiceMeta = voiceMeta[msg.messageId]
                        )
                    }

                    val defaultSeeds = buildDefaultSeedMessages(chat, reactions)
                    if (mappedFirestore.isEmpty()) {
                        defaultSeeds
                    } else {
                        defaultSeeds + mappedFirestore
                    }
                }
                    .map<List<MessageBubbleData>, ChatMessagesUiState> { list ->
                        ChatMessagesUiState.Success(list)
                    }
                    .collect { uiState ->
                        _messagesUiState.value = uiState
                    }
            }
        }
    }

    /**
     * Sends a persistent text message to Firestore and caches it locally in Room for offline viewing.
     */
    fun sendMessage(
        chatId: String,
        text: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        _isSending.value = true
        val nowMillis = System.currentTimeMillis()

        repository.sendMessage(
            chatId = chatId,
            text = cleanText
        ) { result ->
            _isSending.value = false
            result.onSuccess { messageId ->
                viewModelScope.launch {
                    localMessageRepository?.cacheMessage(
                        CachedMessageEntity(
                            messageId = messageId,
                            chatId = chatId,
                            senderId = currentUserId,
                            text = cleanText,
                            status = "SENT",
                            timestampMillis = nowMillis
                        )
                    )
                }
                // Update local chat preview immediately
                _localChats.update { list ->
                    list.map {
                        if (it.id == chatId) {
                            it.copy(
                                lastMessage = cleanText,
                                lastMessageTime = "الآن",
                                deliveryStatus = MessageDeliveryStatus.SENT
                            )
                        } else it
                    }
                }
                onSuccess(messageId)
            }.onFailure { error ->
                // Even if offline or Firestore fails, cache the outgoing message locally in Room so it remains visible offline
                val offlineMessageId = "local_${nowMillis}"
                viewModelScope.launch {
                    localMessageRepository?.cacheMessage(
                        CachedMessageEntity(
                            messageId = offlineMessageId,
                            chatId = chatId,
                            senderId = currentUserId,
                            text = cleanText,
                            status = "SENT",
                            timestampMillis = nowMillis
                        )
                    )
                }
                val msg = error.localizedMessage ?: "تعذر إرسال الرسالة"
                _errorBanner.value = msg
                onError(msg)
            }
        }
    }

    /**
     * Sends a persistent voice note message to Firestore and caches it with audio metadata in Room.
     */
    fun sendVoiceNoteMessage(
        chatId: String,
        durationSeconds: Int,
        localFilePath: String?,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val safeDuration = durationSeconds.coerceAtLeast(1)
        val formattedDuration = String.format(Locale.US, "%02d:%02d", safeDuration / 60, safeDuration % 60)
        val voiceText = "$VOICE_NOTE_PREFIX($formattedDuration)"
        val nowMillis = System.currentTimeMillis()

        _isSending.value = true
        repository.sendMessage(
            chatId = chatId,
            text = voiceText
        ) { result ->
            _isSending.value = false
            result.onSuccess { messageId ->
                _localVoiceMetadata.update { map ->
                    map + (messageId to (safeDuration to localFilePath))
                }
                viewModelScope.launch {
                    localMessageRepository?.cacheMessage(
                        CachedMessageEntity(
                            messageId = messageId,
                            chatId = chatId,
                            senderId = currentUserId,
                            text = voiceText,
                            status = "SENT",
                            timestampMillis = nowMillis,
                            isVoiceNote = true,
                            voiceDurationSeconds = safeDuration,
                            voiceFilePath = localFilePath
                        )
                    )
                }
                _localChats.update { list ->
                    list.map {
                        if (it.id == chatId) {
                            it.copy(
                                lastMessage = "تسجيل صوتي ($formattedDuration)",
                                lastMessageTime = "الآن",
                                hasVoiceNote = true,
                                voiceDuration = formattedDuration
                            )
                        } else it
                    }
                }
                onSuccess(messageId)
            }.onFailure { error ->
                val offlineVoiceId = "local_voice_${nowMillis}"
                _localVoiceMetadata.update { map ->
                    map + (offlineVoiceId to (safeDuration to localFilePath))
                }
                viewModelScope.launch {
                    localMessageRepository?.cacheMessage(
                        CachedMessageEntity(
                            messageId = offlineVoiceId,
                            chatId = chatId,
                            senderId = currentUserId,
                            text = voiceText,
                            status = "SENT",
                            timestampMillis = nowMillis,
                            isVoiceNote = true,
                            voiceDurationSeconds = safeDuration,
                            voiceFilePath = localFilePath
                        )
                    )
                }
                val msg = error.localizedMessage ?: "تعذر إرسال الملاحظة الصوتية"
                _errorBanner.value = msg
                onError(msg)
            }
        }
    }

    /**
     * Toggles an emoji reaction on a message and updates the local Room cache.
     */
    fun toggleMessageReaction(messageId: String, emoji: String) {
        var updatedReaction: String? = null
        _localReactions.update { current ->
            val existing = current[messageId]
            if (existing == emoji) {
                updatedReaction = null
                current - messageId
            } else {
                updatedReaction = emoji
                current + (messageId to emoji)
            }
        }
        viewModelScope.launch {
            localMessageRepository?.updateReaction(messageId, updatedReaction)
        }
    }

    /**
     * Deletes a message from Firestore and removes it from the local Room cache.
     */
    fun deleteMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            localMessageRepository?.deleteMessageById(messageId)
        }
        repository.deleteMessage(chatId, messageId) { result ->
            result.onFailure { error ->
                _errorBanner.value = error.localizedMessage ?: "تعذر حذف الرسالة"
            }
        }
    }

    /**
     * Creates or opens a conversation with a contact and persists it in Firestore.
     */
    fun createOrOpenChat(contactName: String): ChatItem {
        val existing = _localChats.value.find { it.name.contains(contactName, ignoreCase = true) }
        if (existing != null) {
            return existing
        }

        val sanitizedId = "chat_${System.currentTimeMillis()}"
        val newChat = ChatItem(
            id = sanitizedId,
            name = contactName,
            isOnline = true,
            lastMessage = "مرحباً! بدأت محادثة جديدة.",
            lastMessageTime = "الآن",
            avatarInitials = contactName.take(2)
        )
        _localChats.update { listOf(newChat) + it }
        repository.ensureChatExists(sanitizedId, newChat.lastMessage)
        return newChat
    }

    fun togglePinChat(chatId: String): Boolean {
        var isNowPinned = false
        _localChats.update { list ->
            list.map {
                if (it.id == chatId) {
                    isNowPinned = !it.isPinned
                    it.copy(isPinned = isNowPinned)
                } else it
            }
        }
        return isNowPinned
    }

    fun toggleMuteChat(chatId: String): Boolean {
        var isNowMuted = false
        _localChats.update { list ->
            list.map {
                if (it.id == chatId) {
                    isNowMuted = !it.isMuted
                    it.copy(isMuted = isNowMuted)
                } else it
            }
        }
        return isNowMuted
    }

    fun toggleReadChat(chatId: String) {
        _localChats.update { list ->
            list.map {
                if (it.id == chatId) {
                    it.copy(unreadCount = if (it.unreadCount > 0) 0 else 1)
                } else it
            }
        }
    }

    fun deleteChat(chatId: String) {
        _localChats.update { list -> list.filter { it.id != chatId } }
        viewModelScope.launch {
            localMessageRepository?.deleteMessagesForChat(chatId)
        }
        repository.deleteChat(chatId)
    }

    fun clearErrorBanner() {
        _errorBanner.value = null
    }

    private fun CachedMessageEntity.toBubbleData(
        currentUserId: String,
        reactionOverride: String?,
        voiceMetaOverride: Pair<Int, String?>?
    ): MessageBubbleData {
        val isVoice = isVoiceNote || text.startsWith(VOICE_NOTE_PREFIX) || voiceMetaOverride != null
        val duration = voiceMetaOverride?.first
            ?: voiceDurationSeconds.takeIf { it > 0 }
            ?: parseVoiceDurationSeconds(text)
        val filePath = voiceMetaOverride?.second ?: voiceFilePath
        val delivery = when (status.uppercase(Locale.ROOT)) {
            "READ" -> MessageDeliveryStatus.READ
            "DELIVERED" -> MessageDeliveryStatus.DELIVERED
            "SENT" -> MessageDeliveryStatus.SENT
            else -> MessageDeliveryStatus.READ
        }

        return MessageBubbleData(
            id = messageId,
            text = if (isVoice) "رسالة صوتية" else text,
            time = formatMillis(timestampMillis),
            isFromMe = senderId == currentUserId,
            deliveryStatus = delivery,
            reaction = reactionOverride ?: reaction,
            isVoiceNote = isVoice,
            voiceDurationSeconds = duration,
            voiceFilePath = filePath
        )
    }

    private fun FirestoreMessage.toBubbleData(
        currentUserId: String,
        reaction: String?,
        voiceMeta: Pair<Int, String?>?
    ): MessageBubbleData {
        val isVoice = text.startsWith(VOICE_NOTE_PREFIX) || voiceMeta != null
        val parsedDuration = voiceMeta?.first ?: parseVoiceDurationSeconds(text)
        val delivery = when (status.uppercase(Locale.ROOT)) {
            "READ" -> MessageDeliveryStatus.READ
            "DELIVERED" -> MessageDeliveryStatus.DELIVERED
            "SENT" -> MessageDeliveryStatus.SENT
            else -> MessageDeliveryStatus.READ
        }

        return MessageBubbleData(
            id = messageId.ifBlank { "msg_${createdAt?.seconds ?: System.currentTimeMillis()}" },
            text = if (isVoice) "رسالة صوتية" else text,
            time = formatTimestamp(createdAt),
            isFromMe = senderId == currentUserId,
            deliveryStatus = delivery,
            reaction = reaction,
            isVoiceNote = isVoice,
            voiceDurationSeconds = parsedDuration,
            voiceFilePath = voiceMeta?.second
        )
    }

    private fun parseVoiceDurationSeconds(text: String): Int {
        val match = Regex("""\((\d{2}):(\d{2})\)""").find(text) ?: return 5
        val minutes = match.groupValues[1].toIntOrNull() ?: 0
        val seconds = match.groupValues[2].toIntOrNull() ?: 5
        return (minutes * 60 + seconds).coerceAtLeast(1)
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "الآن"
        return formatMillis(timestamp.toDate().time)
    }

    private fun formatMillis(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "الآن"
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
            sdf.format(Date(timestampMillis))
        } catch (e: Exception) {
            "الآن"
        }
    }

    private fun buildDefaultSeedMessages(
        chat: ChatItem,
        reactions: Map<String, String>
    ): List<MessageBubbleData> {
        return listOf(
            MessageBubbleData(
                id = "seed_${chat.id}_1",
                text = "السلام عليكم ورحمة الله وبركاته، أهلاً بك في منصة ChatDrs المشفّرة.",
                time = "١١:٣٠ ص",
                isFromMe = false,
                reaction = reactions["seed_${chat.id}_1"]
            ),
            MessageBubbleData(
                id = "seed_${chat.id}_2",
                text = "وعليكم السلام ورحمة الله! يسعدني التواصل معكم. هل تم الانتهاء من مراجعة الملفات؟",
                time = "١١:٣٢ ص",
                isFromMe = true,
                deliveryStatus = MessageDeliveryStatus.READ,
                reaction = reactions["seed_${chat.id}_2"] ?: "❤️"
            ),
            MessageBubbleData(
                id = "seed_${chat.id}_voice",
                text = "رسالة صوتية",
                time = "١١:٣٤ ص",
                isFromMe = false,
                isVoiceNote = true,
                voiceDurationSeconds = 7,
                reaction = reactions["seed_${chat.id}_voice"]
            ),
            MessageBubbleData(
                id = "seed_${chat.id}_3",
                text = chat.lastMessage.ifBlank { "نعم، الملفات جاهزة واعتمدنا جميع المعايير والتصميم الجديد بالكامل." },
                time = chat.lastMessageTime.ifBlank { "١١:٣٥ ص" },
                isFromMe = false,
                reaction = reactions["seed_${chat.id}_3"]
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
        firestoreSyncJob?.cancel()
        chatsJob?.cancel()
    }

    companion object {
        private const val TAG = "ChatViewModel"
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val VOICE_NOTE_PREFIX = "🎤 رسالة صوتية "

        /**
         * Creates a [ViewModelProvider.Factory] that resolves the provisioned
         * `R.string.firestore_database_id` and the local [ChatDrsDatabase] Room instance
         * from the application context, injecting [MessageRepository] and [LocalMessageRepository]
         * into [ChatViewModel].
         */
        fun provideFactory(currentUserId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val roomDatabase = ChatDrsDatabase.getInstance(app)
                ChatViewModel(
                    repository = MessageRepository(db),
                    currentUserId = currentUserId,
                    localMessageRepository = LocalMessageRepository(roomDatabase.chatMessageDao())
                )
            }
        }
    }
}
