package com.foxluo.chat.data.repo

import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.util.StringUtil.formatServerTimeS
import com.foxluo.baselib.util.StringUtil.toServerTime
import com.foxluo.baselib.util.TimeUtil.nowTime
import com.foxluo.chat.data.api.MessageApi
import com.foxluo.chat.data.database.ChatDao
import com.foxluo.chat.data.database.ChatEntity
import com.foxluo.chat.data.database.FriendDao
import com.foxluo.chat.data.database.MessageDao
import com.foxluo.chat.data.database.MessageEntity
import com.foxluo.chat.data.result.FriendResult
import kotlin.math.max
import kotlin.math.min
import kotlin.sequences.ifEmpty

class ChatRepository(
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val friendDao: FriendDao
) : BaseRepository() {
    private val messageApi by lazy {
        createApi<MessageApi>()
    }

    suspend fun createTempTextMessage(friend: FriendResult, content: String): MessageEntity? {
        AuthManager.authInfo?.user?.let { sender ->
            val id = min(messageDao.getMinMessageId() ?: 0, 0) - 1
            return MessageEntity(
                id = id,
                sender_id = sender.id.toInt(),
                receiver_id = friend.id,
                type = "text",
                content = content,
                sent_at = nowTime.toServerTime(),
                send_time = nowTime,
                sender_nickname = sender.nickname,
                sender_avatar = sender.avatar,
                sendStatus = -1
            ).also {
                insertMessages(listOf(it))
            }
        }
        return null
    }

    /**
     *
     * 发送文字消息
     * {
     *     "receiverId": "3",
     *     "content": "未撤回的消息",
     *     "type": "text"
     * }
     */
    suspend fun sendTextMessage(localMessage: MessageEntity): RequestResult {
        val map = mapOf<String, String>(
            "receiverId" to localMessage.receiver_id.toString(),
            "content" to (localMessage.content ?: "/"),
            "type" to "text"
        )
        var result =
            kotlin.runCatching { messageApi?.sendMessage(map) }.getOrNull().toRequestResult()
        if (result is RequestResult.Success<*>) {
            messageDao.deleteMessage(localMessage)
            insertMessages(listOf(localMessage.apply {
                val resultData = result.data as MessageEntity
                id = resultData.id
                send_time = resultData.sent_at.formatServerTimeS()
                sendStatus = 1
                taskUuid = ""
            }))
        } else if (result is RequestResult.Error) {
            insertMessages(listOf(localMessage.apply {
                sendStatus = 0
            }))
        }
        return result
    }

    suspend fun createTempVoiceMessage(
        friend: FriendResult,
        voiceUrl: String,
        voiceDuration: Int
    ): MessageEntity? {
        val sender = AuthManager.authInfo?.user ?: return null
        val id = min(messageDao.getMinMessageId() ?: 0, 0) - 1
        val localMessage = MessageEntity(
            id = id,
            sender_id = sender.id.toInt(),
            receiver_id = friend.id,
            type = "voice",
            voice_url = voiceUrl,
            voice_duration = voiceDuration,
            sent_at = nowTime.toServerTime(),
            send_time = nowTime,
            sender_nickname = sender.nickname,
            sender_avatar = sender.avatar,
            sendStatus = -1
        )
        insertMessages(listOf(localMessage))
        return localMessage
    }

    /**
     * 发送语音消息
     */
    suspend fun sendVoiceMessage(localMessage: MessageEntity): RequestResult {
        val map = mapOf<String, String>(
            "receiverId" to localMessage.receiver_id.toString(),
            "content" to "/",
            "type" to "text",
            "voice_url" to (localMessage.file_url ?: ""),
            "voice_duration" to localMessage.voice_duration.toString()
        )
        var result =
            kotlin.runCatching { messageApi?.sendMessage(map) }.getOrNull().toRequestResult()
        if (result is RequestResult.Success<*>) {
            messageDao.deleteMessage(localMessage)
            insertMessages(listOf(localMessage.apply {
                val resultData = result.data as MessageEntity
                id = resultData.id
                send_time = resultData.sent_at.formatServerTimeS()
                sendStatus = 1
                taskUuid = ""
            }))
        } else if (result is RequestResult.Error) {
            insertMessages(listOf(localMessage.apply {
                sendStatus = 0
            }))
        }
        return result
    }

    suspend fun createTempFileMessage(
        friend: FriendResult,
        filePath: String,
        fileType: String
    ): MessageEntity? {
        val sender = AuthManager.authInfo?.user ?: return null
        val id = min(messageDao.getMinMessageId() ?: 0, 0) - 1
        val localMessage = MessageEntity(
            id = id,
            sender_id = sender.id.toInt(),
            receiver_id = friend.id,
            type = "file",
            file_path = filePath,
            file_type = fileType,
            sent_at = nowTime.toServerTime(),
            send_time = nowTime,
            sender_nickname = sender.nickname,
            sender_avatar = sender.avatar,
            sendStatus = -1
        )
        insertMessages(listOf(localMessage))
        return localMessage
    }

    /**
     * 发送文件类消息
     * @param fileType 'image','video','other'
     */
    suspend fun sendFileMessage(localMessage: MessageEntity): RequestResult {
        val map = mapOf<String, String>(
            "receiverId" to localMessage.receiver_id.toString(),
            "content" to "[file]${localMessage.file_name}",
            "type" to "file",
            "file_url" to (localMessage.file_url ?: ""),
            "file_type" to (localMessage.file_type ?: ""),
            "file_name" to (localMessage.file_name ?: ""),
            "file_size" to (localMessage.file_size?.toString() ?: ""),
        )
        var result =
            kotlin.runCatching { messageApi?.sendMessage(map) }.getOrNull().toRequestResult()
        if (result is RequestResult.Success<*>) {
            messageDao.deleteMessage(localMessage)
            insertMessages(listOf(localMessage.apply {
                val resultData = result.data as MessageEntity
                id = resultData.id
                send_time = resultData.sent_at.formatServerTimeS()
                sendStatus = 1
                taskUuid = ""
            }))
        } else if (result is RequestResult.Error) {
            insertMessages(listOf(localMessage.apply {
                sendStatus = 0
            }))
        }
        return result
    }


    /**
     * 重试发送
     */
    suspend fun retrySendMessage(localMessage: MessageEntity): RequestResult {
        val map = mutableMapOf<String, String>(
            "receiverId" to localMessage.receiver_id.toString(),
            "content" to (localMessage.content ?: "666"),
            "type" to localMessage.type,
        )
        when (localMessage.type) {
            "voice" -> {
                map["voice_url"] = localMessage.voice_url ?: ""
                map["voice_duration"] = (localMessage.voice_duration ?: 0).toString()
            }

            "file" -> {
                map["file_url"] = localMessage.file_url ?: ""
                map["file_type"] = localMessage.file_type ?: ""
                map["file_name"] = localMessage.file_name ?: ""
                map["file_size"] = (localMessage.file_size ?: 0L).toString()
            }

            else -> Unit
        }
        val sender = AuthManager.authInfo?.user ?: return RequestResult.Error("请先登录")
        var result =
            kotlin.runCatching { messageApi?.sendMessage(map) }.getOrNull().toRequestResult()
        if (result is RequestResult.Success<*>) {
            messageDao.deleteMessage(localMessage)
            insertMessages(listOf((result.data as MessageEntity).apply {
                sender_avatar = sender.avatar
                sender_nickname = sender.nickname
                send_time = sent_at.formatServerTimeS()
                sendStatus = 1
            }))
        } else {
            insertMessages(listOf(localMessage.apply {
                sendStatus = 0
            }))
        }
        return result
    }

    /**
     * 获取新消息
     */
    suspend fun getUnreadMessages(userId: Int = 0): RequestResult {
        var result = kotlin.runCatching {
            messageApi?.getMessages(userId.toString())
        }.getOrNull().toRequestResult()
        if (result is RequestResult.Success<*>) {
            val list = ((result.data as List<MessageEntity>).map { it.apply { sendStatus = 1 } })
            insertMessages(list.map {
                it.apply {
                    send_time = sent_at.formatServerTimeS()
                    taskUuid  = ""
                }
            })
        }
        return result
    }

    suspend fun deleteMessage(id:Int): RequestResult{
        val result=runCatching {
            messageApi?.deleteMessage(id.toString())
        }.getOrNull().toRequestResult()
        return result
    }

    // 插入消息
    suspend fun insertMessages(messages: List<MessageEntity>) {
        if (messages.isNullOrEmpty()) return
        messageDao.insertMessages(messages)
        val message =
            messageDao.getLastMessage(messages.first().sender_id, messages.first().receiver_id)
        updateMessageChat(message)
    }

    suspend fun updateMessageChat(message: MessageEntity){
        val myId = AuthManager.authInfo?.user?.id?.toInt() ?: return
        val isISend = myId == message.sender_id
        val friend =//获取对方的好友信息，若是我发的，则通过接收者id获取，若我是接收方，则通过发送方id获取
            friendDao.getFriendById(if (isISend) message.receiver_id else message.sender_id)
                ?: return
        // 更新会话
        chatDao.upsertChat(
            ChatEntity(
                chatId = "${
                    if(isISend) message.sender_id else message.receiver_id
                }_${
                    if(isISend) message.receiver_id else message.sender_id
                }",
                peerId = myId,
                type = "single",
                lastMessageId = message.id,
                lastMessagePreview = message.generatePreview(),
                lastMessageAt = message.send_time,
                lastSendStatus = message.sendStatus,
                unreadCount = 0,
                chatName = friend.mark?.ifEmpty { friend.nickname } ?: friend.nickname ?: "",
                chatAvatar = friend.avatar
            )
        )
    }
}