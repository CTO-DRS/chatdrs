package com.drs.chatdrs.firebase

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MessageRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        ChatDrsFirebase.getFirestore(context)
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول قبل إجراء عمليات على قاعدة البيانات.")
    }

    /**
     * Ensures the parent chat document exists and includes the current authenticated user
     * in participantIds so subcollection rules succeed.
     */
    fun ensureChatExists(
        chatId: String,
        initialLastMessage: String = "بدء المحادثة",
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val currentUserId = requireUserId()
        val chatDocRef = db.collection("chats").document(chatId)

        chatDocRef.get()
            .addOnSuccessListener { snapshot ->
                val participants = snapshot.get("participantIds") as? List<*>
                if (snapshot.exists() && participants?.contains(currentUserId) == true) {
                    onComplete(Result.success(Unit))
                } else {
                    val payload = mapOf(
                        "chatId" to chatId,
                        "participantIds" to FieldValue.arrayUnion(currentUserId),
                        "lastMessage" to initialLastMessage.take(2000),
                        "lastMessageTime" to FieldValue.serverTimestamp(),
                        "lastSenderId" to currentUserId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    chatDocRef.set(payload, SetOptions.merge())
                        .addOnSuccessListener {
                            onComplete(Result.success(Unit))
                        }
                        .addOnFailureListener { exception ->
                            FirestoreErrorHandler.handleFirestoreError(
                                exception,
                                OperationType.CREATE,
                                chatDocRef.path
                            )
                            onComplete(Result.failure(exception))
                        }
                }
            }
            .addOnFailureListener {
                // Document may not exist yet (which triggers PERMISSION_DENIED on get if resource == null check fails or similar), create it directly
                val payload = mapOf(
                    "chatId" to chatId,
                    "participantIds" to listOf(currentUserId),
                    "lastMessage" to initialLastMessage.take(2000),
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "lastSenderId" to currentUserId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                chatDocRef.set(payload)
                    .addOnSuccessListener {
                        onComplete(Result.success(Unit))
                    }
                    .addOnFailureListener { createEx ->
                        FirestoreErrorHandler.handleFirestoreError(
                            createEx,
                            OperationType.CREATE,
                            chatDocRef.path
                        )
                        onComplete(Result.failure(createEx))
                    }
            }
    }

    /**
     * Sends a new message to a specific chat in Firestore, ensuring the parent chat exists first.
     */
    fun sendMessage(
        chatId: String,
        text: String,
        onComplete: (Result<String>) -> Unit = {}
    ) {
        val currentUserId = requireUserId()
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) {
            onComplete(Result.failure(IllegalArgumentException("نص الرسالة فارغ")))
            return
        }

        ensureChatExists(chatId, trimmedText) { chatResult ->
            if (chatResult.isFailure) {
                onComplete(Result.failure(chatResult.exceptionOrNull() ?: Exception("فشل تهيئة المحادثة")))
                return@ensureChatExists
            }

            val messageId = UUID.randomUUID().toString()
            val messagesRef = db.collection("chats").document(chatId).collection("messages")

            val messagePayload = mapOf(
                "messageId" to messageId,
                "chatId" to chatId,
                "senderId" to currentUserId,
                "text" to trimmedText.take(5000),
                "status" to "SENT",
                "createdAt" to FieldValue.serverTimestamp()
            )

            messagesRef.document(messageId).set(messagePayload)
                .addOnSuccessListener {
                    // Update parent chat's last message snippet
                    db.collection("chats").document(chatId).update(
                        mapOf(
                            "lastMessage" to trimmedText.take(2000),
                            "lastMessageTime" to FieldValue.serverTimestamp(),
                            "lastSenderId" to currentUserId,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).addOnFailureListener { updateEx ->
                        FirestoreErrorHandler.handleFirestoreError(
                            updateEx,
                            OperationType.UPDATE,
                            "chats/$chatId"
                        )
                    }
                    onComplete(Result.success(messageId))
                }
                .addOnFailureListener { exception ->
                    FirestoreErrorHandler.handleFirestoreError(
                        exception,
                        OperationType.CREATE,
                        "${messagesRef.path}/$messageId"
                    )
                    onComplete(Result.failure(exception))
                }
        }
    }

    /**
     * Updates an existing message's text or delivery status in Firestore.
     */
    fun updateMessageStatus(
        chatId: String,
        messageId: String,
        text: String,
        status: String,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val currentUserId = requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        val payload = mapOf(
            "messageId" to messageId,
            "chatId" to chatId,
            "senderId" to currentUserId,
            "text" to text.take(5000),
            "status" to status
        )
        msgRef.set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { exception ->
                FirestoreErrorHandler.handleFirestoreError(
                    exception,
                    OperationType.UPDATE,
                    msgRef.path
                )
                onComplete(Result.failure(exception))
            }
    }

    /**
     * Deletes a message from a conversation in Firestore.
     */
    fun deleteMessage(
        chatId: String,
        messageId: String,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        msgRef.delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { exception ->
                FirestoreErrorHandler.handleFirestoreError(
                    exception,
                    OperationType.DELETE,
                    msgRef.path
                )
                onComplete(Result.failure(exception))
            }
    }

    /**
     * Deletes an entire chat document from Firestore.
     */
    fun deleteChat(
        chatId: String,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        requireUserId()
        val chatRef = db.collection("chats").document(chatId)
        chatRef.delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { exception ->
                FirestoreErrorHandler.handleFirestoreError(
                    exception,
                    OperationType.DELETE,
                    chatRef.path
                )
                onComplete(Result.failure(exception))
            }
    }

    /**
     * Listens to real-time message stream for a given conversation.
     */
    fun observeMessages(chatId: String): Flow<List<FirestoreMessage>> = callbackFlow {
        val messagesRef = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val registration = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                FirestoreErrorHandler.handleFirestoreError(
                    error,
                    OperationType.LIST,
                    "chats/$chatId/messages"
                )
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.toObjects(
                    FirestoreMessage::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                trySend(messages)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Listens to all chats where current user is a participant.
     */
    fun observeUserChats(): Flow<List<FirestoreChat>> = callbackFlow {
        val currentUserId = requireUserId()
        val chatsQuery = db.collection("chats")
            .whereArrayContains("participantIds", currentUserId)

        val registration = chatsQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                FirestoreErrorHandler.handleFirestoreError(
                    error,
                    OperationType.LIST,
                    "chats"
                )
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val chats = snapshot.toObjects(
                    FirestoreChat::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.updatedAt ?: it.createdAt }
                trySend(chats)
            }
        }

        awaitClose {
            registration.remove()
        }
    }
}
