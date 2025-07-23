package com.foxluo.mine.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.manager.AuthInfo
import com.foxluo.baselib.data.result.BaseResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi : com.foxluo.baselib.data.api.BaseApi {
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("/api/auth/login")
    suspend fun login(
        @retrofit2.http.Field("username") username: kotlin.String,
        @retrofit2.http.Field("password") password: kotlin.String
    ): com.foxluo.baselib.data.result.BaseResponse<com.foxluo.baselib.data.manager.AuthInfo>

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("/api/auth/register")
    suspend fun register(
        @retrofit2.http.Field("username") username: kotlin.String,
        @retrofit2.http.Field("password") password: kotlin.String,
        @retrofit2.http.Field("email") email: kotlin.String
    ): com.foxluo.baselib.data.result.BaseResponse<com.foxluo.baselib.data.manager.AuthInfo>

    @retrofit2.http.POST("/api/auth/logout")
    suspend fun logout(): com.foxluo.baselib.data.result.BaseResponse<kotlin.Unit>

    /**
     * 登录状态修改密码
     */
    @retrofit2.http.POST("/api/auth/change-password")
    suspend fun changePassword(@retrofit2.http.Body body: kotlin.collections.Map<kotlin.String, kotlin.String>): com.foxluo.baselib.data.result.BaseResponse<kotlin.Unit>

    /**
     * 忘记密码邮箱验证码发送
     */
    @retrofit2.http.POST("/api/auth/forgot-password")
    suspend fun sendForgetEmail(@retrofit2.http.Body body: kotlin.collections.Map<kotlin.String, kotlin.String>): com.foxluo.baselib.data.result.BaseResponse<kotlin.Unit>

    @retrofit2.http.POST("/api/auth/reset-password")
    suspend fun resetPassword(@retrofit2.http.Body body: kotlin.collections.Map<kotlin.String, kotlin.String>): com.foxluo.baselib.data.result.BaseResponse<kotlin.Unit>
}