package com.foxluo.chat.data.repo

import com.foxluo.chat.data.database.MessageEntity

object MessagePreviewGenerator {
    fun generatePreview(message: MessageEntity): String {
        return when (message.type) {
            "text" -> message.content?.take(20) ?: ""
            "voice" -> "[${message.voice_duration}s语音]"
            "file" -> "[文件] ${message.file_name ?: "未知文件"}"
            "audio_call" -> "[语音通话] ${message.call_status}"
            "video_call" -> "[视频通话] ${message.call_status}"
            else -> "[新消息]"
        }
    }
}