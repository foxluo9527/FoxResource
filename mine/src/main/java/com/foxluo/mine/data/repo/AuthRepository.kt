package com.foxluo.mine.data.repo

import com.foxluo.baselib.data.manager.AuthInfo
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.mine.data.api.AuthApi

class AuthRepository : BaseRepository() {
    private val api by lazy {
        createApi<AuthApi>()
    }

    suspend fun login(username: String, password: String): RequestResult<AuthInfo?> {
        return runCatching { api?.login(username, password) }.toRequestResult()
    }

    suspend fun register(username: String, email: String, password: String): RequestResult<AuthInfo?> {
        return runCatching { api?.register(username, password, email) }.toRequestResult()
    }

    suspend fun sendForgetEmailCode(email: String): RequestResult<Unit?> {
        val body = mapOf("email" to email)
        val result = runCatching{ api?.sendForgetEmail(body) }
        return result.toRequestResult()
    }

    suspend fun resetForgetPassword(
        email: String,
        code: String,
        newPassword: String
    ): RequestResult<Unit?> {
        val body = mapOf("email" to email, "code" to code, "newPassword" to newPassword)
        return runCatching{ api?.resetPassword(body) }.toRequestResult()
    }

    suspend fun logout(): RequestResult<Unit?> {
        return runCatching{ api?.logout() }.toRequestResult()
    }
}