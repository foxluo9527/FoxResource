package com.foxluo.resource.music.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.baselib.domain.bean.SearchHotKeyword
import com.foxluo.resource.music.data.result.ArtistResult
import com.foxluo.resource.music.data.result.DashboardStats
import com.foxluo.resource.music.data.result.MusicComment
import com.foxluo.resource.music.data.result.MusicCommentReplay
import com.foxluo.resource.music.data.result.MusicResult
import com.foxluo.resource.music.data.result.PlaylistDetailResult
import com.foxluo.resource.music.data.result.PlaylistResult
import com.foxluo.resource.music.data.result.Tag
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("/api/playlists/recommended")
    suspend fun getRecommendedPlaylistList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseResponse<List<PlaylistResult>>

    @GET("/api/playlists/{id}")
    suspend fun getPlaylistDetail(
        @Path("id") id: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseResponse<PlaylistDetailResult>

    @GET("/api/playlists")
    suspend fun getPlaylistList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): BaseResponse<List<PlaylistResult>>

    @GET("/api/artists")
    suspend fun getArtistList(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("keyword") keyword: String = "",
        @Query("tag_id") tagId: String? = null
    ): BaseListResponse<ArtistResult>

    /**
     * {
     *     "title": "篝火篝火是否扶手椅差点儿谁账户仿佛当飞机"
     * }
     */
    @POST("/api/playlists")
    suspend fun createPlaylist(@Body map: Map<String, @JvmSuppressWildcards Any>): BaseResponse<Unit>
    /**
     * 导入播放列表
     */
    @POST("/api/import/music")
    suspend fun importPlaylist(@Body map: Map<String, @JvmSuppressWildcards Any>): BaseResponse<Unit>



    /**
     * {
     *     "title": "纸飞机",
     *     "description": "示史完率红再着。法说科用无样查除导。向劳空器易。根民是养火法。命造造列通。九比准。置集群老月半团半们。",
     *     "cover_image": "https://loremflickr.com/400/400?lock=257718078880667",
     *     "tags":[1,2,3]
     * }
     */
    @PUT("/api/playlists/{id}")
    suspend fun updatePlaylist(
        @Path("id") playlistId: String,
        @Body map: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<Unit>

    @DELETE("/api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") id: String): BaseResponse<Unit>

    /**
     * {
     *     "musicIds": [26,27,28]
     * }
     */
    @POST("/api/playlists/{id}/tracks")
    suspend fun addMusicToPlaylist(
        @Path("id") id: String,
        @Body map: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<Unit>

    @HTTP(method = "DELETE", path = "/api/playlists/{id}/batch/tracks",hasBody = true)
    suspend fun deleteMusicInPlaylist(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<Unit>
    
    @GET("/api/tags/music")
    suspend fun getMusicTags(): BaseResponse<List<Tag>>

    @GET("/api/admin/dashboard/stats")
    suspend fun getDashBoardStats(): BaseResponse<DashboardStats>
}