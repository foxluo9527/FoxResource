package com.foxluo.baselib.data.result

open class BaseResponse<T> {
    val code: Int? = null

    var message: String = ""

    var data: T? = null

    var success: Boolean? = null
}