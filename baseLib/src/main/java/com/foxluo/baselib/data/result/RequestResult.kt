package com.foxluo.baselib.data.result

import com.foxluo.baselib.data.respository.BaseRepository

sealed class RequestResult {
    abstract fun isSuccess(): Boolean

    class Success<T>(val data: T, val message: String) : RequestResult() {
        override fun isSuccess() = true
    }

    class Error(val message: String) : RequestResult() {
        override fun isSuccess() = false
    }

    fun <T> invokeCallback(callback: BaseRepository.ResultCallback<T>) {
        if (isSuccess()) {
            val successResult = (this as Success<T>)
            callback.onSuccess(successResult.data, successResult.message)
        } else {
            callback.onError((this as Error).message)
        }
    }
}