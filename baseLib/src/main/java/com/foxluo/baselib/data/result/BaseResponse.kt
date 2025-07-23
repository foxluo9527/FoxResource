package com.foxluo.baselib.data.result

open class BaseResponse<T> {
    //{\"code\":500,\"message\":\"无效的好友申请\",\"data\":null,\"success\":false}
    val code: Int? = null

    var message: String = ""

    val data: T? = null

    var success: Boolean? = null

    companion object {
        inline fun <reified T> BaseResponse<T>?.toRequestResult() =
            if (this != null && this.success == true) {
                RequestResult.Success<T?>(this.data, message)
            } else {
                RequestResult.Error(this?.message ?: "网络连接错误,请稍后重试")
            }
    }
}