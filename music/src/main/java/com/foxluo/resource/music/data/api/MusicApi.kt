package com.foxluo.resource.music.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.resource.music.data.result.MusicResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApi : BaseApi {
    @GET("/api/music")
    suspend fun getMusicList(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("keyword") keyword: String = ""
    ): BaseListResponse<MusicResult>

    @POST("/api/music/{id}/play")
    suspend fun recordMusicPlay(
        @Path("id") id: String
    ): BaseResponse<Unit>
}