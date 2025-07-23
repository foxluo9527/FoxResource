package com.foxluo.chat.data.result

import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.chat.data.database.FriendEntity
import java.io.Serializable

data class FriendResult(
    val id: Int,
    val username: String,
    val nickname: String?,
    val avatar: String?,
    val signature: String?,
    var mark: String?
): Serializable {
    fun toEntity() =
        FriendEntity(
            id,
            AuthManager.authInfo?.user?.id?.toInt() ?: 0,
            username,
            nickname,
            avatar,
            mark,
            signature
        )
}