package com.foxluo.mine.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.mine.data.bean.PersonalProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface PersonalApi: com.foxluo.baselib.data.api.BaseApi {
    /**
     * 获取用户信息
     */
    @retrofit2.http.GET("/api/auth/profile")
    suspend fun profile(): com.foxluo.baselib.data.result.BaseResponse<com.foxluo.mine.data.bean.PersonalProfile>

    /**
     * 修改用户信息
     */
    @retrofit2.http.PUT("/api/auth/profile")
    suspend fun profile(@retrofit2.http.Body body: kotlin.collections.Map<kotlin.String, kotlin.String>): com.foxluo.baselib.data.result.BaseResponse<kotlin.Unit>

}