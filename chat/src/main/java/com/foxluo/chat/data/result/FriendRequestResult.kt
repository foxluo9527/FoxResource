package com.foxluo.chat.data.result

data class FriendRequestResult(
    val id: Int,
    val user_id: Int,
    val created_at: String,
    val message: String,
    val nickname: String?,
    val avatar: String?,
    val signature: String?
)