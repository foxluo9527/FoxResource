package com.foxluo.resource.music.data.result

import com.foxluo.baselib.ui.adapter.CommentAdapter

data class MusicComment(
    val id: Long,
    val music_id: Long,
    val user_id: Long,
    val user_name: String?,
    val content: String,
    val parent_id: Long?,
    val is_like: Boolean,
    val like_count: Int,
    val created_at: String,
    val user_nickname: String?,
    val user_avatar: String,
    val replay_count: Int,
    val replies: List<MusicCommentReplay>
) {
    fun hadMore() = replay_count - replies.size > 0
}

fun MusicComment.toCommentBean() = CommentAdapter.CommentBean(
    id.toString(),
    user_id.toString(),
    user_nickname ?: user_name ?: "未知用户",
    user_avatar,
    created_at,
    like_count,
    is_like,
    content,
    hadMore(),
    false,
    null,
    null
)

fun MusicComment.toCommentList(): List<CommentAdapter.CommentBean> {
    val list = mutableListOf(toCommentBean())
    replies.mapIndexed { index, replay ->
        replay.toCommentReplay(index == replies.size - 1 && hadMore())
    }.let {
        list.addAll(it)
    }
    return list
}

data class MusicCommentReplay(
    val id: Long,
    val music_id: Long,
    val user_id: Long,
    val content: String,
    val parent_id: Long?,
    val like_count: Int,
    val is_like: Boolean,
    val created_at: String,
    val user_nickname: String?,
    val user_name: String?,
    val user_avatar: String?,
    val reply_to: MusicCommentReplayUser?
)

data class MusicCommentReplayUser(
    val username: String,
    val nickname: String,
    val avatar: String,
    val user_id: Int
)

fun MusicCommentReplay.toCommentReplay(hadMore: Boolean): CommentAdapter.CommentBean {
    return CommentAdapter.CommentBean(
        id.toString(),
        user_id.toString(),
        user_nickname ?: user_name ?: "未知用户",
        user_avatar,
        created_at,
        like_count,
        is_like,
        content,
        hadMore,
        true,
        reply_to?.user_id?.toString(),
        reply_to?.nickname
    )
}

