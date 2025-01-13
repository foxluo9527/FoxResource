package com.foxluo.baselib.data.manager

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import kotlinx.coroutines.flow.MutableSharedFlow

object AuthManager {
    val userInfoStateFlow by lazy {
        MutableSharedFlow<UserInfo?>(0)
    }

    private val sp by lazy {
        SPUtils.getInstance("auth")
    }

    val token: String?
        get() = authInfo?.token

    private var authInfo: AuthInfo? = null
        get() {
            val authInfoStr = sp.getString("KEY_SP_AUTH_INFO")
            return GsonUtils.fromJson(authInfoStr, AuthInfo::class.java)
        }
        set(value) {
            val userChanged = value?.user?.id != field?.user?.id
            value?.let {
                sp.put("KEY_SP_AUTH_INFO", GsonUtils.toJson(value))
            } ?: run {
                sp.remove("KEY_SP_AUTH_INFO")
            }
            if (userChanged) {
                userInfoStateFlow.tryEmit(authInfo?.user)
            }
        }

    fun isLogin() = !token.isNullOrEmpty()

    fun logout() {
        authInfo = null
    }

    fun login(authInfo: AuthInfo) {
        this.authInfo = authInfo
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

data class UserInfo(val id: Long, val username: String, val email: String, val status: String, val created_at: String, val last_login: String)