package com.foxluo.resource.music.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.resource.music.data.result.MusicComment
import com.foxluo.resource.music.data.result.MusicCommentReplay
import com.foxluo.resource.music.data.result.MusicResult
import com.foxluo.baselib.domain.bean.SearchHotKeyword
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApi : BaseApi {
    @GET("/api/music")
    suspend fun getMusicList(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("keyword") keyword: String = "",
        @Query("sort") sort: String
    ): BaseListResponse<MusicResult>

    @POST("/api/music/{id}/play")
    suspend fun recordMusicPlay(
        @Path("id") id: String,
        @Body map: Map<String,Int>
    ): BaseResponse<Unit>

    @POST("/api/music/{id}/favorite")
    suspend fun favoriteMusic(
        @Path("id") id: String
    ): BaseResponse<Unit>

    @GET("/api/music-history")
    suspend fun getHistoryMusicList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseListResponse<MusicResult>

    @DELETE("/api/music-history")
    suspend fun deletePlayHistory(
        @Body map: Map<String, Array<Int>>
    ): BaseResponse<Unit>

    @GET("/api/music-comments")
    suspend fun getMusicComments(
        @Query("music_id") musicId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseListResponse<MusicComment>

    @GET("/api/music-comments/{id}/replies")
    suspend fun getMusicReplies(
        @Path("id") commentId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseListResponse<MusicCommentReplay>

    @POST("/api/music-comments/like/{id}")
    suspend fun likeMusicComment(
        @Path("id") commentId: String
    ): BaseResponse<Unit>

    /**
     * music_id,content,parent_id
     */
    @POST("/api/music-comments")
    suspend fun postMusicComment(
        @Body map: Map<String, @JvmSuppressWildcards Any>
    ): BaseListResponse<MusicComment>

    @GET("/api/search/hot-keywords")
    suspend fun getSearchHotKeywords(
        @Query("type") type: String = "music",
        @Query("limit") limit: Int = 10
    ): BaseResponse<List<SearchHotKeyword>>
}