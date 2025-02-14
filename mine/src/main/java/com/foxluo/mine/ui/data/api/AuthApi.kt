package com.foxluo.mine.ui.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.manager.AuthInfo
import com.foxluo.baselib.data.result.BaseResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi : BaseApi {
    @FormUrlEncoded
    @POST("/api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): BaseResponse<AuthInfo>

    @FormUrlEncoded
    @POST("/api/auth/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String
    ): BaseResponse<AuthInfo>

    @POST("/api/auth/logout")
    suspend fun logout(): BaseResponse<Unit>

    /**
     * 登录状态修改密码
     */
    @POST("/api/auth/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): BaseResponse<Unit>

    /**
     * 忘记密码邮箱验证码发送
     */
    @POST("/api/auth/forgot-password")
    suspend fun sendForgetEmail(@Body body: Map<String, String>): BaseResponse<Unit>

    @POST("/api/auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): BaseResponse<Unit>
}