package com.foxluo.baselib.data.respository

import com.foxluo.baselib.data.api.UploadApi
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.util.StringUtil.getMediaType
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.nio.file.Paths

class UploadRepository: BaseRepository() {
    private val uploadApi by lazy {
        createApi<UploadApi>()
    }

    suspend fun uploadFile(filePath: String): RequestResult {
        val file = File(filePath)
        val fileName = Paths.get(filePath).fileName.toString()
        val fileType = if (fileName.contains(".")) {
            fileName.substring(fileName.lastIndexOf(".") + 1)
        } else {
            throw Exception("文件类型获取错误 ${filePath}")
        }
        val mediaTypeStr = getMediaType(fileType)
        val requestFile = RequestBody.create(MediaType.parse(mediaTypeStr), file)
        val part = MultipartBody.Part.createFormData("file", file.getName(), requestFile)
        val result = if (mediaTypeStr.contains("image")) {
            uploadApi?.uploadImage(part)
        } else if (mediaTypeStr.contains("audio")) {
            null
        } else if (mediaTypeStr.contains("video")) {
            null
        } else {
            null
        }
        return result.toRequestResult()
    }
}