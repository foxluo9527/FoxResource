package com.foxluo.mine.ui.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.mine.ui.data.bean.PersonalProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface PersonalApi: BaseApi {
    /**
     * 获取用户信息
     */
    @GET("/api/auth/profile")
    suspend fun profile(): BaseResponse<PersonalProfile>

    /**
     * 修改用户信息
     */
    @PUT("/api/auth/profile")
    suspend fun profile(@Body body: Map<String, String>): BaseResponse<Unit>

}