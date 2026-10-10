package com.drs.chatdrs

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.drs.chatdrs.data.local.CachedMessageEntity
import com.drs.chatdrs.data.local.ChatDrsDatabase
import com.drs.chatdrs.data.local.LocalMessageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: ChatDrsDatabase
  private lateinit var localRepository: LocalMessageRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, ChatDrsDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    localRepository = LocalMessageRepository(database.chatMessageDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ChatDrs", appName)
  }

  @Test
  fun `cacheMessages and observeCachedMessages returns ordered offline history`() = runBlocking {
    val chatId = "offline_chat_1"
    val msg1 = CachedMessageEntity(
      messageId = "m1",
      chatId = chatId,
      senderId = "user_a",
      text = "الرسالة الأولى بدون اتصال",
      timestampMillis = 1000L
    )
    val msg2 = CachedMessageEntity(
      messageId = "m2",
      chatId = chatId,
      senderId = "user_b",
      text = "الرسالة الثانية المحفوظة محلياً",
      timestampMillis = 2000L
    )

    localRepository.cacheMessages(listOf(msg2, msg1))

    val cached = localRepository.observeCachedMessages(chatId).first()
    assertEquals(2, cached.size)
    assertEquals("m1", cached[0].messageId)
    assertEquals("الرسالة الأولى بدون اتصال", cached[0].text)
    assertEquals("m2", cached[1].messageId)
    assertEquals("الرسالة الثانية المحفوظة محلياً", cached[1].text)
  }

  @Test
  fun `updateReaction and deleteMessageById update local Room cache`() = runBlocking {
    val chatId = "offline_chat_2"
    val msg = CachedMessageEntity(
      messageId = "msg_react",
      chatId = chatId,
      senderId = "user_a",
      text = "تجربة التفاعل والحذف",
      timestampMillis = 3000L
    )

    localRepository.cacheMessage(msg)
    localRepository.updateReaction("msg_react", "🔥")

    val updated = localRepository.getMessageById("msg_react")
    assertEquals("🔥", updated?.reaction)

    localRepository.deleteMessageById("msg_react")
    assertNull(localRepository.getMessageById("msg_react"))
    assertTrue(localRepository.observeCachedMessages(chatId).first().isEmpty())
  }
}
