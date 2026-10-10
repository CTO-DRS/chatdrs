package com.drs.chatdrs.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database holder for offline caching of ChatDrs messages.
 */
@Database(
    entities = [CachedMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDrsDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDrsDatabase? = null

        fun getInstance(context: Context): ChatDrsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChatDrsDatabase::class.java,
                    "chatdrs_offline_cache.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
