package com.foxluo.chat.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.chat.data.result.FriendRequestResult
import com.foxluo.chat.data.result.FriendResult
import com.foxluo.chat.data.result.UserSearchResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendsApi : BaseApi {
    @GET("/api/friends")
    suspend fun getFriendsList(): BaseResponse<List<FriendResult>>

    @GET("/api/friends/requests")
    suspend fun getFriendsRequests(): BaseResponse<List<FriendRequestResult>>

    @GET("/api/friends/search")
    suspend fun search(@Query("keyword") keyword: String): BaseResponse<List<UserSearchResult>>

    /**
     * {
     *     "friend_id": "1",
     *     "message": "hello"
     * }
     */
    @POST("/api/friends/request")
    suspend fun request(@Body map: Map<String, String?>): BaseResponse<Unit>

    /**
     * {
     *     "requestId": "1"
     * }
     */
    @POST("/api/friends/accept")
    suspend fun accept(@Body map: Map<String, String>): BaseResponse<Unit>

    @DELETE("/api/friends/{friendId}")
    suspend fun delete(@Path("friendId") id: String): BaseResponse<Unit>

    @POST("/api/friends/remark")
    suspend fun remark(@Body map: Map<String, String>): BaseResponse<Unit>

}