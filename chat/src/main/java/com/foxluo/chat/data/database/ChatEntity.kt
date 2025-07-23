package com.foxluo.chat.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chats",
    indices = [
        Index(
            value = ["peer_id", "last_message_at"],
            orders = [Index.Order.DESC, Index.Order.DESC]
        ),
        Index(value = ["is_pinned"], orders = [Index.Order.DESC])
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["last_message_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ChatEntity(
    @PrimaryKey
    @ColumnInfo(name = "chat_id")
    val chatId: String, // 格式：min(userId,peerId)_max(userId,peerId) 用于单聊

    @ColumnInfo(name = "peer_id")
    val peerId: Int,    // 对方用户ID或群组ID

    @ColumnInfo(name = "chat_type")
    val type: String,   // 'single'/'group'

    @ColumnInfo(name = "last_message_id")
    val lastMessageId: Int?,

    @ColumnInfo(name = "last_message_preview")
    val lastMessagePreview: String?,

    @ColumnInfo(name = "last_message_at")
    val lastMessageAt: Long?,

    @ColumnInfo(name = "last_send_status")
    val lastSendStatus: Int,

    @ColumnInfo(name = "unread_count", defaultValue = "0")
    val unreadCount: Int,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_muted", defaultValue = "0")
    val isMuted: Boolean = false,

    @ColumnInfo(name = "chat_name")
    val chatName: String?,

    @ColumnInfo(name = "chat_avatar")
    val chatAvatar: String?
)