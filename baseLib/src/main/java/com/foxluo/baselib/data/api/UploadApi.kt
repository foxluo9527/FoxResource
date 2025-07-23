package com.foxluo.baselib.data.api

import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.baselib.data.result.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi:BaseApi {
    /**
     * 上传图片文件
     */
    @Multipart
    @POST("/api/upload/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): BaseResponse<FileUploadResponse>

    /**
     * 上传音频文件
     */
    @Multipart
    @POST("/api/upload/audio")
    suspend fun uploadAudio(
        @Part file: MultipartBody.Part
    ): BaseResponse<FileUploadResponse>

    /**
     * 上传视频文件
     */
    @Multipart
    @POST("/api/upload/video")
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part
    ): BaseResponse<FileUploadResponse>

    /**
     * 上传通用文件
     */
    @Multipart
    @POST("/api/upload/file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): BaseResponse<FileUploadResponse>

    /**
     * 通过网络链接上传图片文件到服务器
     * body { url }
     */
    @POST("/api/upload/image")
    suspend fun uploadImage(
        @Body body: Map<String, String>
    ): BaseResponse<FileUploadResponse>
}