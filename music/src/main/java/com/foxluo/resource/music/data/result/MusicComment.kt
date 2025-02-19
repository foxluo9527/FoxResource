package com.foxluo.resource.music.data.result

data class MusicComment(
    val id: Long,
    val music_id: Long,
    val user_id: Long,
    val content: String,
    val parent_id: Long,
    val is_like: Boolean,
    val create_at: String,
    val user_nickname: String,
    val user_avatar: String,
    val replay_count: Int,
    val replies: List<MusicCommentReplay>
)

data class MusicCommentReplay(
    val id: Long,
    val music_id: Long,
    val user_id: Long,
    val content: String,
    val parent_id: Long,
    val is_like: Boolean,
    val create_at: String,
    val user_nickname: String,
    val user_avatar: String
)
