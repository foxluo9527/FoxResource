package com.foxluo.mine.ui.data.bean

data class PersonalProfile(
    val id: Long,
    val username: String,
    val nickname: String?,
    val avatar: String?,
    val status: String,
    val signature: String?
)