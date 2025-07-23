package com.foxluo.chat.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.blankj.utilcode.util.TimeUtils
import com.foxluo.baselib.util.ImageExt.processUrl
import java.io.File

@Entity(
    tableName = "messages",
    indices = [
        Index(name = "idx_messages_sender", value = ["sender_id"]),
        Index(name = "idx_messages_receiver", value = ["receiver_id"]),
        Index(
            name = "idx_messages_timestamp",
            value = ["send_time"],
            orders = [Index.Order.DESC]
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "sender_id", index = true)
    val sender_id: Int,

    @ColumnInfo(name = "receiver_id", index = true)
    val receiver_id: Int,

    @ColumnInfo(name = "type")
    val type: String,  // 'text','voice','file','audio_call','video_call'

    @ColumnInfo(name = "content")
    val content: String? = null,

    @ColumnInfo(name = "voice_url")
    val voice_url: String? = null,

    @ColumnInfo(name = "voice_duration")
    val voice_duration: Int? = null,

    @ColumnInfo(name = "call_status")
    val call_status: String? = null,  // 'missed','answered','rejected'

    @ColumnInfo(name = "call_duration")
    val call_duration: Int? = null,

    @ColumnInfo(name = "file_url")
    var file_url: String? = null,

    @ColumnInfo(name = "file_type")
    val file_type: String? = null,  // 'image','video','other'

    @ColumnInfo(name = "file_name")
    var file_name: String? = null,

    @ColumnInfo(name = "file_size")
    var file_size: Long? = null,

    /**
     * 本地文件路径
     */
    @ColumnInfo(name = "file_path")
    var file_path: String? = null,

    @ColumnInfo(name = "is_read", defaultValue = "0")
    val is_read: Boolean = false,

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val is_deleted: Boolean = false,

    @ColumnInfo("server_send_time")
    var sent_at: String = "",

    @ColumnInfo(name = "send_time")
    var send_time: Long,

    @ColumnInfo(name = "sender_nickname")
    var sender_nickname: String? = null,

    @ColumnInfo(name = "sender_avatar")
    var sender_avatar: String? = null,

    @ColumnInfo(name = "sender_remark")
    var sender_remark: String? = null,

    /**
     * -1:发送中，0:发送失败，1:发送成功
     */
    @ColumnInfo(name = "send_status")
    var sendStatus: Int = 0,

    @ColumnInfo(name = "task_uuid")
    var taskUuid: String = ""
) {
    fun getFileExistsPath(): String? {
        return if (file_path?.let { File(it).exists() } == true)
            file_path
        else
            processUrl(file_url)
    }

    fun generatePreview(): String {
        val message = this
        return when (message.type) {
            "text" -> message.content?.take(20) ?: ""
            "voice" -> "[${message.voice_duration}s语音]"
            "file" -> "[${
                when (message.file_type) {
                    "image" -> "图片"
                    "video" -> "视频"
                    else -> "文件"
                }
            }] ${message.file_name ?: message.file_path ?: "未知文件"}"

            "audio_call" -> "[语音通话] ${getCallStatusText()}"
            "video_call" -> "[视频通话] ${getCallStatusText()}"
            else -> "[新消息]"
        }
    }

    fun getCallStatusText() = when (call_status) {
        "missed" -> "未接听"
        "answered" -> "通话时长 ${
            TimeUtils.getFitTimeSpan(
                call_duration?.toLong() ?: 0L,
                0L,
                if ((call_duration?.toLong() ?: 0L) > 86400000) 5
                else if ((call_duration?.toLong() ?: 0L) > 3600000) 4
                else if ((call_duration?.toLong() ?: 0L) > 60000) 3
                else 2
            )
        }"

        "rejected" -> "已拒接"
        else -> ""
    }
}