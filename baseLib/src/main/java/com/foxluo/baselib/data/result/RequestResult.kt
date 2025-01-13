package com.foxluo.baselib.data.result

sealed class RequestResult {
    data class Success<T>(val data: T) : RequestResult()
    data class Error(val message: String) : RequestResult()
}