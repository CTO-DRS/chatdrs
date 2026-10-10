package com.drs.chatdrs.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.drs.chatdrs.audio.VoicePlayerManager
import com.drs.chatdrs.audio.VoiceRecorderManager
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.model.MessageDeliveryStatus
import com.drs.chatdrs.ui.components.AvatarView
import com.drs.chatdrs.ui.components.ConversationEdgeSubStrip
import com.drs.chatdrs.ui.components.EmojiReactionPickerBar
import com.drs.chatdrs.ui.components.LuminousEdgeStrip
import com.drs.chatdrs.ui.components.ReactionBadge
import com.drs.chatdrs.ui.components.TopBarTypingText
import com.drs.chatdrs.ui.components.TypingBubbleMessage
import com.drs.chatdrs.ui.components.VoiceNoteBubbleContent
import com.drs.chatdrs.ui.theme.BubbleOtherDark
import com.drs.chatdrs.ui.theme.BubbleOtherLight
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.ChatMessagePreviewStyle
import com.drs.chatdrs.ui.theme.ChatTimeStyle
import com.drs.chatdrs.ui.theme.ErrorRed
import com.drs.chatdrs.ui.theme.OnlineGreen
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary
import com.drs.chatdrs.ui.theme.TextOtherDark
import com.drs.chatdrs.ui.theme.TextOtherLight
import com.drs.chatdrs.viewmodel.ChatMessagesUiState
import com.drs.chatdrs.viewmodel.ChatViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

data class MessageBubbleData(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean,
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.READ,
    val reaction: String? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val voiceFilePath: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    chat: ChatItem,
    isDarkTheme: Boolean,
    chatViewModel: ChatViewModel? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button press
    BackHandler { onBack() }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Activate Firestore message observation when entering this conversation
    LaunchedEffect(chat.id, chatViewModel) {
        chatViewModel?.openConversation(chat)
    }

    val errorBannerState = chatViewModel?.errorBanner?.collectAsStateWithLifecycle()
    LaunchedEffect(errorBannerState?.value) {
        val msg = errorBannerState?.value
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            chatViewModel.clearErrorBanner()
        }
    }

    val recorderManager = remember { VoiceRecorderManager(context) }
    val playerManager = remember { VoicePlayerManager() }

    var inputText by remember { mutableStateOf("") }
    var isOtherUserTyping by remember { mutableStateOf(chat.isTyping || chat.id == "chat_3") }
    var activeReactionMessageId by remember { mutableStateOf<String?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var playingMessageId by remember { mutableStateOf<String?>(null) }
    var currentPlayingFraction by remember { mutableStateOf(0f) }

    fun toggleVoicePlayback(msg: MessageBubbleData) {
        if (playingMessageId == msg.id) {
            playerManager.stop()
            playingMessageId = null
            currentPlayingFraction = 0f
        } else {
            playerManager.stop()
            playingMessageId = msg.id
            currentPlayingFraction = 0f

            val filePath = msg.voiceFilePath
            if (filePath != null && File(filePath).exists()) {
                playerManager.play(
                    filePath = filePath,
                    scope = scope,
                    onProgress = { currentMs, totalMs ->
                        if (totalMs > 0) {
                            currentPlayingFraction = currentMs.toFloat() / totalMs
                        }
                    },
                    onComplete = {
                        playingMessageId = null
                        currentPlayingFraction = 0f
                    }
                )
            } else {
                // Simulated smooth playback for pre-seeded voice note samples
                scope.launch {
                    val totalDurationSec = msg.voiceDurationSeconds.coerceAtLeast(3)
                    val totalSteps = totalDurationSec * 10
                    for (step in 1..totalSteps) {
                        if (playingMessageId != msg.id) break
                        delay(100)
                        currentPlayingFraction = step.toFloat() / totalSteps
                    }
                    if (playingMessageId == msg.id) {
                        playingMessageId = null
                        currentPlayingFraction = 0f
                    }
                }
            }
        }
    }

    fun startRecordingFlow() {
        val file = File(context.cacheDir, "voice_note_${System.currentTimeMillis()}.m4a")
        val success = recorderManager.startRecording(file)
        if (success) {
            isRecording = true
            recordingSeconds = 0
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("تعذر بدء التسجيل الصوتي، يرجى المحاولة مرة أخرى.")
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecordingFlow()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("يلزم إذن الميكروفون لتسجيل وإرسال الملاحظات الصوتية")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorderManager.cancelRecording()
            playerManager.stop()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Fallback local messages if ChatViewModel is not provided (e.g., in previews)
    var fallbackMessages by remember {
        mutableStateOf(
            listOf(
                MessageBubbleData(
                    id = "msg_1",
                    text = "السلام عليكم ورحمة الله وبركاته، أهلاً بك في منصة ChatDrs المشفّرة.",
                    time = "١١:٣٠ ص",
                    isFromMe = false
                ),
                MessageBubbleData(
                    id = "msg_2",
                    text = "وعليكم السلام ورحمة الله! يسعدني التواصل معكم. هل تم الانتهاء من مراجعة الملفات؟",
                    time = "١١:٣٢ ص",
                    isFromMe = true,
                    deliveryStatus = MessageDeliveryStatus.READ,
                    reaction = "❤️"
                ),
                MessageBubbleData(
                    id = "msg_voice_sample",
                    text = "رسالة صوتية",
                    time = "١١:٣٤ ص",
                    isFromMe = false,
                    isVoiceNote = true,
                    voiceDurationSeconds = 7
                ),
                MessageBubbleData(
                    id = "msg_3",
                    text = chat.lastMessage.ifBlank { "نعم، الملفات جاهزة واعتمدنا جميع المعايير والتصميم الجديد بالكامل." },
                    time = chat.lastMessageTime.ifBlank { "١١:٣٥ ص" },
                    isFromMe = false
                )
            )
        )
    }

    val vmMessagesState = chatViewModel?.messagesUiState?.collectAsStateWithLifecycle()
    val vmIsSendingState = chatViewModel?.isSending?.collectAsStateWithLifecycle()
    val isSendingMessage = vmIsSendingState?.value ?: false
    val messages: List<MessageBubbleData> = when (val uiState = vmMessagesState?.value) {
        is ChatMessagesUiState.Success -> uiState.messages
        else -> fallbackMessages
    }

    // Scroll to latest message or typing indicator when state updates
    LaunchedEffect(messages.size, isOtherUserTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    val otherBubbleBg = if (isDarkTheme) BubbleOtherDark else BubbleOtherLight
    val otherTextColor = if (isDarkTheme) TextOtherDark else TextOtherLight
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Conversation Top Bar with Luminous Top Strip & Edge Process Sub-Strip
            Surface(
                color = surfaceColor,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LuminousEdgeStrip(
                        height = 3.dp,
                        isActive = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button & User Info Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("conversation_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = onSurfaceColor
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Avatar
                            AvatarView(
                                initials = chat.avatarInitials,
                                gradientIndex = chat.avatarGradientIndex,
                                isOnline = chat.isOnline,
                                size = 42.dp,
                                isOfficialLogo = chat.id == "chat_pinned_1"
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Name & Real-time Typing Status
                            Column {
                                Text(
                                    text = chat.name,
                                    fontFamily = CairoFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = onSurfaceColor,
                                    maxLines = 1
                                )

                                // Status: Typing or Online
                                if (isOtherUserTyping) {
                                    TopBarTypingText()
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(OnlineGreen)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "متصل الآن",
                                            fontFamily = CairoFont,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = mutedColor
                                        )
                                    }
                                }
                            }
                        }

                        // Top Bar Action Buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Quick Toggle Button to Simulate Typing in Real-Time
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isOtherUserTyping) PurplePrimary.copy(alpha = 0.15f)
                                        else surfaceColor
                                    )
                                    .border(
                                        1.dp,
                                        if (isOtherUserTyping) PurplePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        isOtherUserTyping = !isOtherUserTyping
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (isOtherUserTyping) "تم تشغيل مؤشر الكتابة الفوري"
                                                else "تم إيقاف مؤشر الكتابة"
                                            )
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("toggle_typing_simulation")
                            ) {
                                Text(
                                    text = if (isOtherUserTyping) "يكتب ⚡" else "محاكاة الكتابة",
                                    fontFamily = CairoFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isOtherUserTyping) PurplePrimary else mutedColor
                                )
                            }

                            IconButton(
                                onClick = {
                                    scope.launch { snackbarHostState.showSnackbar("مكالمة هاتفية مشفرة") }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Call,
                                    contentDescription = "اتصال",
                                    tint = onSurfaceColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    scope.launch { snackbarHostState.showSnackbar("مكالمة فيديو فائقة الدقة") }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = "فيديو",
                                    tint = onSurfaceColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Edge Process & Sync Telemetry Sub-Strip
                    ConversationEdgeSubStrip(
                        isSending = isSendingMessage || isRecording,
                        messageCount = messages.size
                    )
                }
            }
        },
        bottomBar = {
            // Modern Message Input Bar / Voice Note Recorder Bar with Luminous Edge Top Accent
            Surface(
                color = surfaceColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LuminousEdgeStrip(
                        height = 2.dp,
                        isActive = inputText.isNotEmpty() || isRecording
                    )
                if (isRecording) {
                    // Active Voice Recording Panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cancel / Delete Recording (Trash Icon in Red)
                        IconButton(
                            onClick = {
                                recorderManager.cancelRecording()
                                isRecording = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم إلغاء التسجيل الصوتي")
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ErrorRed.copy(alpha = 0.12f))
                                .testTag("cancel_voice_recording")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "إلغاء التسجيل",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Pulsing Red Recording Dot & Live Timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val pulseAlpha by rememberInfiniteTransition(label = "rec_pulse").animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                                label = "rec_pulse_alpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed.copy(alpha = pulseAlpha))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                                fontFamily = CairoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "جارٍ تسجيل الصوت...",
                                fontFamily = CairoFont,
                                fontSize = 12.sp,
                                color = PurplePrimary
                            )
                        }

                        // Stop & Send Voice Note Button
                        IconButton(
                            onClick = {
                                val recordedFile = recorderManager.stopRecording()
                                val duration = if (recordingSeconds > 0) recordingSeconds else 1
                                isRecording = false

                                if (chatViewModel != null) {
                                    chatViewModel.sendVoiceNoteMessage(
                                        chatId = chat.id,
                                        durationSeconds = duration,
                                        localFilePath = recordedFile?.absolutePath
                                    )
                                } else {
                                    val newVoiceMsg = MessageBubbleData(
                                        id = "msg_voice_${System.currentTimeMillis()}",
                                        text = "رسالة صوتية",
                                        time = "الآن",
                                        isFromMe = true,
                                        isVoiceNote = true,
                                        voiceDurationSeconds = duration,
                                        voiceFilePath = recordedFile?.absolutePath
                                    )
                                    fallbackMessages = fallbackMessages + newVoiceMsg
                                }

                                scope.launch {
                                    snackbarHostState.showSnackbar("تم إرسال الملاحظة الصوتية ($duration ثوانٍ)")
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PurpleGradientStart, PurpleGradientEnd)
                                    )
                                )
                                .testTag("send_voice_recording")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "إرسال التسجيل",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment Icon
                        IconButton(
                            onClick = {
                                scope.launch { snackbarHostState.showSnackbar("إرفاق ملف أو وسائط") }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AttachFile,
                                contentDescription = "إرفاق",
                                tint = mutedColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // OutlinedTextField for message input
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = "اكتب رسالة...",
                                    fontFamily = CairoFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = mutedColor
                                )
                            },
                            textStyle = TextStyle(
                                fontFamily = CairoFont,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = onSurfaceColor
                            ),
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                cursorColor = PurplePrimary
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Text or Mic Button with Gradient that updates ChatViewModel state
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val sentText = inputText
                                    inputText = ""
                                    if (chatViewModel != null) {
                                        chatViewModel.sendMessage(
                                            chatId = chat.id,
                                            text = sentText
                                        )
                                    } else {
                                        fallbackMessages = fallbackMessages + MessageBubbleData(
                                            id = "msg_${System.currentTimeMillis()}",
                                            text = sentText,
                                            time = "الآن",
                                            isFromMe = true,
                                            deliveryStatus = MessageDeliveryStatus.READ
                                        )

                                        // Simulate other user typing back after 1 second!
                                        scope.launch {
                                            delay(800)
                                            isOtherUserTyping = true
                                            delay(2400)
                                            isOtherUserTyping = false
                                            fallbackMessages = fallbackMessages + MessageBubbleData(
                                                id = "msg_reply_${System.currentTimeMillis()}",
                                                text = "وصلت رسالتك! أقوم بالعمل عليها الآن وسأوافيك بالتفاصيل في أقرب وقت.",
                                                time = "الآن",
                                                isFromMe = false
                                            )
                                        }
                                    }
                                } else {
                                    // Trigger Voice Recording Flow
                                    val permissionCheck = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        startRecordingFlow()
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PurpleGradientStart, PurpleGradientEnd)
                                    )
                                )
                                .testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Outlined.Send else Icons.Outlined.Mic,
                                contentDescription = if (inputText.isNotBlank()) "إرسال" else "تسجيل صوتي",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("chat_history_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date Divider
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "اليوم",
                                fontFamily = CairoFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = mutedColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Render conversation messages
                items(messages, key = { it.id }) { msg ->
                    if (msg.isFromMe) {
                        // My Message Bubble (Purple Gradient, sharp bottomEnd corner)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Floating Emoji Reaction Picker Bar when this message is long-pressed
                            AnimatedVisibility(
                                visible = activeReactionMessageId == msg.id,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                EmojiReactionPickerBar(
                                    currentReaction = msg.reaction,
                                    onSelectEmoji = { emoji ->
                                        if (chatViewModel != null) {
                                            chatViewModel.toggleMessageReaction(msg.id, emoji)
                                        } else {
                                            fallbackMessages = fallbackMessages.map {
                                                if (it.id == msg.id) it.copy(reaction = if (it.reaction == emoji) null else emoji)
                                                else it
                                            }
                                        }
                                        activeReactionMessageId = null
                                    },
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // Message Bubble
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 22.dp,
                                            topEnd = 22.dp,
                                            bottomStart = 22.dp,
                                            bottomEnd = 6.dp
                                        )
                                    )
                                    .background(
                                        Brush.linearGradient(
                                            listOf(PurpleGradientStart, PurpleGradientEnd)
                                        )
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            if (activeReactionMessageId != null) activeReactionMessageId = null
                                        },
                                        onLongClick = {
                                            activeReactionMessageId = if (activeReactionMessageId == msg.id) null else msg.id
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 11.dp)
                                    .testTag("message_bubble_${msg.id}")
                            ) {
                                if (msg.isVoiceNote) {
                                    VoiceNoteBubbleContent(
                                        durationFormatted = String.format("%02d:%02d", msg.voiceDurationSeconds / 60, msg.voiceDurationSeconds % 60),
                                        timeFormatted = msg.time,
                                        isPlaying = (playingMessageId == msg.id),
                                        progressFraction = if (playingMessageId == msg.id) currentPlayingFraction else 0f,
                                        isFromMe = true,
                                        onTogglePlay = { toggleVoicePlayback(msg) }
                                    )
                                } else {
                                    Text(
                                        text = msg.text,
                                        style = ChatMessagePreviewStyle.copy(
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.time,
                                            style = ChatTimeStyle.copy(
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Outlined.DoneAll,
                                            contentDescription = "تم القراءة",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Reaction Badge Displayed Beneath the Message Bubble
                            if (msg.reaction != null) {
                                ReactionBadge(
                                    emoji = msg.reaction,
                                    onClick = {
                                        activeReactionMessageId = if (activeReactionMessageId == msg.id) null else msg.id
                                    },
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .offset(y = (-8).dp)
                                )
                            }
                        }
                    } else {
                        // Other user's message bubble (F1F2F6 / 262B3E, sharp bottomStart corner)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Floating Emoji Reaction Picker Bar when this message is long-pressed
                            AnimatedVisibility(
                                visible = activeReactionMessageId == msg.id,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                EmojiReactionPickerBar(
                                    currentReaction = msg.reaction,
                                    onSelectEmoji = { emoji ->
                                        if (chatViewModel != null) {
                                            chatViewModel.toggleMessageReaction(msg.id, emoji)
                                        } else {
                                            fallbackMessages = fallbackMessages.map {
                                                if (it.id == msg.id) it.copy(reaction = if (it.reaction == emoji) null else emoji)
                                                else it
                                            }
                                        }
                                        activeReactionMessageId = null
                                    },
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // Message Bubble
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 22.dp,
                                            topEnd = 22.dp,
                                            bottomEnd = 22.dp,
                                            bottomStart = 6.dp
                                        )
                                    )
                                    .background(otherBubbleBg)
                                    .combinedClickable(
                                        onClick = {
                                            if (activeReactionMessageId != null) activeReactionMessageId = null
                                        },
                                        onLongClick = {
                                            activeReactionMessageId = if (activeReactionMessageId == msg.id) null else msg.id
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 11.dp)
                                    .testTag("message_bubble_${msg.id}")
                            ) {
                                if (msg.isVoiceNote) {
                                    VoiceNoteBubbleContent(
                                        durationFormatted = String.format("%02d:%02d", msg.voiceDurationSeconds / 60, msg.voiceDurationSeconds % 60),
                                        timeFormatted = msg.time,
                                        isPlaying = (playingMessageId == msg.id),
                                        progressFraction = if (playingMessageId == msg.id) currentPlayingFraction else 0f,
                                        isFromMe = false,
                                        onTogglePlay = { toggleVoicePlayback(msg) }
                                    )
                                } else {
                                    Text(
                                        text = msg.text,
                                        style = ChatMessagePreviewStyle.copy(
                                            color = otherTextColor,
                                            fontSize = 15.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = msg.time,
                                        style = ChatTimeStyle.copy(color = mutedColor),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }

                            // Reaction Badge Displayed Beneath the Message Bubble
                            if (msg.reaction != null) {
                                ReactionBadge(
                                    emoji = msg.reaction,
                                    onClick = {
                                        activeReactionMessageId = if (activeReactionMessageId == msg.id) null else msg.id
                                    },
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .offset(y = (-8).dp)
                                )
                            }
                        }
                    }
                }

                // Real-Time Typing Indicator Bubble
                item {
                    AnimatedVisibility(
                        visible = isOtherUserTyping,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        TypingBubbleMessage(
                            userInitials = chat.avatarInitials,
                            gradientIndex = chat.avatarGradientIndex,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
