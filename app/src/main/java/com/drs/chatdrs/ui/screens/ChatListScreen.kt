package com.drs.chatdrs.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drs.chatdrs.data.SampleData
import com.drs.chatdrs.model.ChatCategory
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.model.ChatType
import com.drs.chatdrs.ui.components.ActiveStoriesBar
import com.drs.chatdrs.ui.components.ChatDrsTopBar
import com.drs.chatdrs.ui.components.ChatFilterTabs
import com.drs.chatdrs.ui.components.ChatItemRow
import com.drs.chatdrs.ui.components.ChatPreviewDialog
import com.drs.chatdrs.ui.components.ChatQuickActionsSheet
import com.drs.chatdrs.ui.components.ChatSearchBar
import com.drs.chatdrs.ui.components.EmptyChatsView
import com.drs.chatdrs.ui.components.NewChatDialog
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary
import com.drs.chatdrs.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    chatViewModel: ChatViewModel? = null,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Observe conversation data from ChatViewModel when available, or fallback to local state
    val vmChatsState = chatViewModel?.chats?.collectAsStateWithLifecycle()
    var fallbackChats by remember { mutableStateOf(SampleData.sampleChats) }
    val chats = vmChatsState?.value ?: fallbackChats

    var selectedCategory by remember { mutableStateOf(ChatCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog & Sheet States
    var selectedChatForActions by remember { mutableStateOf<ChatItem?>(null) }
    var selectedChatForPreview by remember { mutableStateOf<ChatItem?>(null) }
    var activeConversation by remember { mutableStateOf<ChatItem?>(null) }
    var showNewChatSheet by remember { mutableStateOf(false) }

    if (activeConversation != null) {
        ConversationScreen(
            chat = activeConversation!!,
            isDarkTheme = isDarkTheme,
            chatViewModel = chatViewModel,
            onBack = { activeConversation = null }
        )
        return
    }

    val quickActionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val newChatSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Filtering logic
    val filteredChats = chats.filter { chat ->
        val matchesCategory = when (selectedCategory) {
            ChatCategory.ALL -> true
            ChatCategory.UNREAD -> chat.unreadCount > 0
            ChatCategory.GROUPS -> chat.chatType == ChatType.GROUP
            ChatCategory.CHANNELS -> chat.chatType == ChatType.CHANNEL
            ChatCategory.FAVORITES -> chat.isFavorite
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            chat.name.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)
        }

        matchesCategory && matchesSearch
    }

    // Separate pinned and unpinned when searching or showing ALL
    val pinnedChats = filteredChats.filter { it.isPinned }
    val regularChats = filteredChats.filter { !it.isPinned }

    val totalUnreadCount = chats.sumOf { it.unreadCount }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatDrsTopBar(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onOpenSettings = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("ChatDrs: إعدادات التشفير التام والمظهر قيد التشغيل")
                    }
                },
                onSignOut = onSignOut,
                cachedChatsCount = chats.size
            )
        },
        floatingActionButton = {
            // Fully rounded FAB with vibrant purple gradient
            FloatingActionButton(
                onClick = { showNewChatSheet = true },
                shape = CircleShape,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PurpleGradientStart, PurpleGradientEnd)
                        )
                    )
                    .testTag("new_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "محادثة جديدة",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Prominent Chat Search Bar at the Top of the Conversation List
            ChatSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                matchCount = if (searchQuery.isNotEmpty()) filteredChats.size else null,
                onFilterClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تصفية المحادثات: حسب الوسائط أو الروابط أو الملفات")
                    }
                }
            )

            // Active Stories / Contacts row (hidden when searching for clarity)
            AnimatedVisibility(visible = searchQuery.isEmpty()) {
                ActiveStoriesBar(
                    contacts = SampleData.activeContacts,
                    onContactClick = { contact ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("عرض حالة: ${contact.name}")
                        }
                    },
                    onAddStoryClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("إضافة قصة جديدة إلى ChatDrs")
                        }
                    }
                )
            }

            // Category Filter Pills
            ChatFilterTabs(
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                unreadCount = totalUnreadCount
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Chat List
            if (filteredChats.isEmpty()) {
                EmptyChatsView(searchQuery = searchQuery)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chats_lazy_column")
                ) {
                    // Pinned Chats Section
                    if (pinnedChats.isNotEmpty() && selectedCategory == ChatCategory.ALL && searchQuery.isEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PushPin,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المحادثات المثبتة",
                                    fontFamily = CairoFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PurplePrimary
                                )
                            }
                        }

                        items(pinnedChats, key = { it.id }) { chat ->
                            ChatItemRow(
                                chat = chat,
                                onClick = { activeConversation = chat },
                                onLongClick = { selectedChatForActions = chat }
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                thickness = 0.8.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        }
                    }

                    // All / Regular Chats Section Header
                    if (pinnedChats.isNotEmpty() && regularChats.isNotEmpty() && selectedCategory == ChatCategory.ALL && searchQuery.isEmpty()) {
                        item {
                            Text(
                                text = "جميع المحادثات",
                                fontFamily = CairoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Render list of chats
                    val chatsToDisplay = if (selectedCategory == ChatCategory.ALL && searchQuery.isEmpty()) {
                        regularChats
                    } else {
                        filteredChats
                    }

                    items(chatsToDisplay, key = { it.id }) { chat ->
                        ChatItemRow(
                            chat = chat,
                            onClick = { activeConversation = chat },
                            onLongClick = { selectedChatForActions = chat }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Quick Actions Sheet (Long Click)
    if (selectedChatForActions != null) {
        ChatQuickActionsSheet(
            chat = selectedChatForActions,
            sheetState = quickActionSheetState,
            onDismiss = { selectedChatForActions = null },
            onTogglePin = { chatId ->
                val isNowPinned = if (chatViewModel != null) {
                    chatViewModel.togglePinChat(chatId)
                } else {
                    fallbackChats = fallbackChats.map { if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it }
                    fallbackChats.find { it.id == chatId }?.isPinned == true
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(if (isNowPinned) "تم تثبيت المحادثة في الأعلى" else "تم إلغاء التثبيت")
                }
            },
            onToggleMute = { chatId ->
                val isNowMuted = if (chatViewModel != null) {
                    chatViewModel.toggleMuteChat(chatId)
                } else {
                    fallbackChats = fallbackChats.map { if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it }
                    fallbackChats.find { it.id == chatId }?.isMuted == true
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(if (isNowMuted) "تم كتم التنبيهات" else "تم إلغاء كتم التنبيهات")
                }
            },
            onToggleRead = { chatId ->
                if (chatViewModel != null) {
                    chatViewModel.toggleReadChat(chatId)
                } else {
                    fallbackChats = fallbackChats.map {
                        if (it.id == chatId) {
                            it.copy(unreadCount = if (it.unreadCount > 0) 0 else 1)
                        } else it
                    }
                }
            },
            onDeleteChat = { chatId ->
                val chatToDelete = chats.find { it.id == chatId }
                if (chatViewModel != null) {
                    chatViewModel.deleteChat(chatId)
                } else {
                    fallbackChats = fallbackChats.filter { it.id != chatId }
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم حذف محادثة ${chatToDelete?.name ?: ""}")
                }
            }
        )
    }

    // Live Message Preview Dialog (Click on Chat)
    if (selectedChatForPreview != null) {
        ChatPreviewDialog(
            chat = selectedChatForPreview,
            sheetState = previewSheetState,
            isDarkTheme = isDarkTheme,
            onDismiss = { selectedChatForPreview = null }
        )
    }

    // New Chat Sheet (FAB Click)
    if (showNewChatSheet) {
        NewChatDialog(
            sheetState = newChatSheetState,
            onDismiss = { showNewChatSheet = false },
            onSelectContact = { contactName ->
                activeConversation = if (chatViewModel != null) {
                    chatViewModel.createOrOpenChat(contactName)
                } else {
                    val existing = SampleData.sampleChats.find { it.name.contains(contactName) }
                    existing ?: ChatItem(
                        id = "chat_${System.currentTimeMillis()}",
                        name = contactName,
                        isOnline = true,
                        lastMessage = "مرحباً! بدأت محادثة جديدة.",
                        lastMessageTime = "الآن",
                        avatarInitials = contactName.take(2)
                    )
                }
            }
        )
    }
}
