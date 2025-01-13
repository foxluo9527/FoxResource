package com.foxluo.resource.music.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.resource.music.data.result.MusicResult
import retrofit2.http.GET

interface MusicApi : BaseApi {
    @GET("api/music")
    suspend fun getMusicList(page: Int, limit: Int, keyword: String = ""): BaseListResponse<MusicResult>
}