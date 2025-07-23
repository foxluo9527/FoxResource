package com.foxluo.chat.data.result

import java.io.Serializable

data class UserSearchResult(
    val id: Int,
    val nickname: String?,
    val avatar: String?,
    val signature: String?,
    var is_requested: Boolean,
    val is_friend: Boolean,
    val mark: String?
): Serializable