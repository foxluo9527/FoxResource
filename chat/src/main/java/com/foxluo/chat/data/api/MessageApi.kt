package com.foxluo.chat.data.api

import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.chat.data.database.MessageEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi : BaseApi {
    @GET("/api/messages/unread/{targetId}")
    suspend fun getMessages(@Path("targetId") id: String = "0"): BaseResponse<List<MessageEntity>>

    @POST("/api/messages/")
    suspend fun sendMessage(@Body map: Map<String, String>): BaseResponse<MessageEntity>

    @POST("/api/messages/{id}/recall")
    suspend fun deleteMessage(@Path("id") id: String = "0"): BaseResponse<Unit>
}