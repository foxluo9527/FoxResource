package com.foxluo.baselib.data.result

open class BaseResponse<T> {
    //{\"code\":500,\"message\":\"无效的好友申请\",\"data\":null,\"success\":false}
    var code: Int? = null

    var message: String = ""

    var data: T? = null

    var success: Boolean? = null

    companion object {
        inline fun <reified T> Result<BaseResponse<T>?>.toRequestResult() =
            (this.getOrNull() to this.exceptionOrNull()).let { (resultData, error) ->
                if (resultData?.success == true) {
                    RequestResult.Success<T?>(resultData.data, resultData.message)
                } else {
                    RequestResult.Error(
                        resultData?.code ?: 201,
                        error?.message ?: resultData?.message
                        ?: "网络连接错误,请稍后重试"
                    )
                }
            }
    }
}