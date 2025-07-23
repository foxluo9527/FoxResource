package com.foxluo.chat.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // 获取所有会话
    @Transaction
    @Query("SELECT * FROM chats WHERE peer_id = :userId ORDER BY last_message_at DESC")
    fun getChats(userId: Int): PagingSource<Int, ChatEntity>

    // 更新会话最后消息
    @Query("""
        UPDATE chats SET 
            last_message_id = :messageId,
            last_message_preview = :preview,
            last_message_at = :timestamp,
            unread_count = unread_count + :unreadIncrement
        WHERE chat_id = :chatId
    """)
    suspend fun updateLastMessage(
        chatId: String,
        messageId: Int,
        preview: String?,
        timestamp: String,
        unreadIncrement: Int
    )

    // 重置未读数
    @Query("UPDATE chats SET unread_count = 0 WHERE chat_id = :chatId")
    suspend fun resetUnreadCount(chatId: String)

    // 插入或更新会话
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChat(chat: ChatEntity)
}