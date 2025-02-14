package com.foxluo.mine.ui.data.repo

import com.foxluo.baselib.data.manager.AuthInfo
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.util.GsonUtil.toJsonString
import com.foxluo.mine.ui.data.api.AuthApi

class AuthRepository : BaseRepository() {
    private val api by lazy {
        getApi<AuthApi>()
    }

    suspend fun login(username: String, password: String): RequestResult {
        val result = api?.login(username, password)
        val data = result?.data
        return if (data != null) {
            RequestResult.Success<AuthInfo?>(result.data,result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun register(username: String, email: String, password: String): RequestResult {
        val result = api?.register(username, password, email)
        val data = result?.data
        return if (data != null) {
            RequestResult.Success<AuthInfo?>(result.data,result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun sendForgetEmailCode(email: String): RequestResult {
        val body = mapOf("email" to email)
        val result = api?.sendForgetEmail(body)
        val isSuccess = result?.success
        return if (isSuccess == true) {
            RequestResult.Success<Unit>(Unit,result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun resetForgetPassword(
        email: String,
        code: String,
        newPassword: String
    ): RequestResult {
        val body = mapOf("email" to email, "code" to code, "newPassword" to newPassword)
        val result = api?.resetPassword(body)
        val isSuccess = result?.success
        return if (isSuccess == true) {
            RequestResult.Success<Unit>(Unit,result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun logout(): RequestResult {
        val result = api?.logout()
        val isSuccess = result?.success
        return if (isSuccess == true) {
            RequestResult.Success<Unit>(Unit,result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }
}