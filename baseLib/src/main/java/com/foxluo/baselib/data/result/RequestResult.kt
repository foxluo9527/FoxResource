package com.foxluo.baselib.data.result

sealed class RequestResult {
    abstract fun isSuccess(): Boolean

    class Success<T>(val data: T, val message: String) : RequestResult() {
        override fun isSuccess() = true
    }

    class Error(val message: String) : RequestResult() {
        override fun isSuccess() = false
    }
}