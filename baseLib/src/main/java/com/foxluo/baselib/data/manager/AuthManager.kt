package com.foxluo.baselib.data.manager

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object AuthManager {
    val userInfoStateFlow by lazy {
        MutableSharedFlow<UserInfo?>(0).apply {
            CoroutineScope(Dispatchers.IO).launch {
                emit(authInfo?.user)
            }
        }
    }

    private val sp by lazy {
        SPUtils.getInstance("auth")
    }

    val token: String?
        get() = authInfo?.token

    var authInfo: AuthInfo?
        get() {
            val authInfoStr = sp.getString("KEY_SP_AUTH_INFO")
            return GsonUtils.fromJson(authInfoStr, AuthInfo::class.java)
        }
        set(value) {
            value?.let {
                sp.put("KEY_SP_AUTH_INFO", GsonUtils.toJson(value))
            } ?: run {
                sp.remove("KEY_SP_AUTH_INFO")
            }
            CoroutineScope(Dispatchers.IO).launch {
                userInfoStateFlow.emit(authInfo?.user)
            }
        }

    fun isLogin() = !token.isNullOrEmpty()

    fun logout() {
        authInfo = null
    }

    fun login(authInfo: AuthInfo) {
        this.authInfo = authInfo
    }

    fun updatePersonalInfo(
        username: String,
        nickname: String?,
        avatar: String?,
        signature: String?
    ) {
        authInfo = authInfo?.apply {
            user.username = username
            user.avatar = avatar
            user.signature = signature
            user.nickname = nickname
        }
    }
}

/**
 * "token": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywidXNlcm5hbWUiOiJmb3hsdW8xIiwicm9sZSI6InVzZXIiLCJpYXQiOjE3MzY3NTE2MTMsImV4cCI6MTczNzM1NjQxM30.Rvi9fVskCidZobKTHRdzd4NR7H0ij2nXIebcQECqOxY",
 *         "user": {
 *             "id": 3,
 *             "username": "foxluo1",
 *             "email": "luofox373@gmail.com",
 *             "role": "user",
 *             "status": "active",
 *             "created_at": "2025-01-12T11:53:59.000Z",
 *             "last_login": "2025-01-12T12:08:28.000Z"
 *         }
 */
data class AuthInfo(val token: String, val user: UserInfo)

data class UserInfo(
    val id: Long,
    var username: String,
    var signature: String?,
    var nickname: String?,
    val email: String,
    val status: String,
    var avatar: String?,
    val created_at: String,
    val last_login: String
)