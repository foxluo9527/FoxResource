package com.foxluo.chat.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    // 按会话查询（支持分页）
    @Query(
        """
        SELECT * FROM messages 
        WHERE (sender_id = :userId AND receiver_id = :peerId)
           OR (sender_id = :peerId AND receiver_id = :userId)
        ORDER BY send_time ASC
    """
    )
    fun getMessages(
        userId: Int,
        peerId: Int
    ): PagingSource<Int,MessageEntity>

    // 标记消息已读
    @Query(
        """
        UPDATE messages 
        SET is_read = 1 
        WHERE receiver_id = :userId 
          AND sender_id = :peerId 
          AND is_read = 0
    """
    )
    suspend fun markMessagesAsRead(userId: Int, peerId: Int)

    @Query(
        """
        SELECT * 
        FROM messages 
        WHERE (receiver_id = :senderId AND sender_id = :receiverId ) 
        OR (receiver_id = :receiverId AND sender_id = :senderId)
        ORDER BY send_time DESC
        LIMIT 1
    """
    )
    suspend fun getLastMessage(senderId: Int, receiverId: Int): MessageEntity

    // 插入消息（自动处理冲突）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("SELECT MIN(id) FROM messages")
    suspend fun getMinMessageId(): Int?
}