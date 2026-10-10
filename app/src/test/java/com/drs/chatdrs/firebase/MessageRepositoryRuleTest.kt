package com.drs.chatdrs.firebase

import com.drs.chatdrs.base.FirestoreEmulatorTestBase
import com.drs.chatdrs.model.ChatItem
import com.drs.chatdrs.viewmodel.ChatMessagesUiState
import com.drs.chatdrs.viewmodel.ChatViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MessageRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun sendMessage_andObserveMessages_authenticatedParticipant_succeeds() = runBlocking {
    val aliceUid = signInTestUser("alice@chatdrs.test")
    val chatId = "chat_${UUID.randomUUID()}"

    // Create participant chat document first
    withTimeout(5000L) {
      firestore.collection("chats").document(chatId).set(
        mapOf(
          "chatId" to chatId,
          "participantIds" to listOf(aliceUid),
          "lastMessage" to "بدء المحادثة",
          "lastSenderId" to aliceUid,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    }

    val repository = MessageRepository(firestore)
    val deferred = CompletableDeferred<Result<String>>()
    repository.sendMessage(chatId, "مرحباً من الاختبار") { result ->
      deferred.complete(result)
    }

    val sendResult = withTimeout(5000L) { deferred.await() }
    assertTrue(sendResult.isSuccess)
    val msgId = sendResult.getOrThrow()

    val messages = withTimeout(3000L) {
      repository.observeMessages(chatId).first { list -> list.any { it.messageId == msgId } }
    }
    assertTrue(messages.any { it.text == "مرحباً من الاختبار" })
  }

  @Test
  fun observeMessages_crossUserNonParticipant_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("alice2@chatdrs.test")
    val chatId = "private_${UUID.randomUUID()}"

    withTimeout(5000L) {
      firestore.collection("chats").document(chatId).set(
        mapOf(
          "chatId" to chatId,
          "participantIds" to listOf(aliceUid),
          "lastMessage" to "خاص",
          "lastSenderId" to aliceUid,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    }

    // Switch to Bob who is not in participantIds
    signInTestUser("bob@chatdrs.test")
    val bobRepository = MessageRepository(firestore)

    try {
      withTimeout(3000L) {
        bobRepository.observeMessages(chatId).first()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun observeMessages_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = MessageRepository(firestore)
    try {
      withTimeout(3000L) {
        repository.observeMessages("any_chat").first()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun chatViewModel_openConversationAndSendMessage_persistsToFirestore() = runBlocking {
    val aliceUid = signInTestUser("alice_vm@chatdrs.test")
    val repository = MessageRepository(firestore)
    val viewModel = ChatViewModel(repository = repository, currentUserId = aliceUid)

    val chatItem = ChatItem(
      id = "vm_chat_${UUID.randomUUID()}",
      name = "محادثة اختبار",
      lastMessage = "مرحباً",
      lastMessageTime = "الآن"
    )

    viewModel.openConversation(chatItem)

    val sendDeferred = CompletableDeferred<String>()
    viewModel.sendMessage(
      chatId = chatItem.id,
      text = "رسالة محفوظة عبر ChatViewModel",
      onSuccess = { msgId -> sendDeferred.complete(msgId) }
    )

    val sentMessageId = withTimeout(5000L) { sendDeferred.await() }
    assertTrue(sentMessageId.isNotEmpty())

    val stateWithMessage = withTimeout(5000L) {
      viewModel.messagesUiState.first { state ->
        state is ChatMessagesUiState.Success &&
          state.messages.any { it.text == "رسالة محفوظة عبر ChatViewModel" }
      }
    } as ChatMessagesUiState.Success

    assertTrue(stateWithMessage.messages.any { it.id == sentMessageId && it.isFromMe })
  }
}
