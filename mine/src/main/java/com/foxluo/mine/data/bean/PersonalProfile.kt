package com.foxluo.mine.data.bean

data class PersonalProfile(
    val id: kotlin.Long,
    val username: kotlin.String,
    val nickname: kotlin.String?,
    val avatar: kotlin.String?,
    val status: kotlin.String,
    val signature: kotlin.String?
)