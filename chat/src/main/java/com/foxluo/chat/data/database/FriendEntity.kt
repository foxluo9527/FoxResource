package com.foxluo.chat.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.foxluo.chat.data.result.FriendResult

@Entity(tableName = "friend")
data class FriendEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "nickname")
    val nickname: String?,
    @ColumnInfo(name = "avatar")
    val avatar: String?,
    @ColumnInfo(name = "mark")
    val mark: String?,
    @ColumnInfo(name = "signature")
    val signature: String?
) {
    fun toResult() = FriendResult(id, username, nickname, avatar, signature, mark)
}