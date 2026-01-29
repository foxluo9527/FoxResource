package com.foxluo.baselib.data.result

import com.foxluo.baselib.domain.AuthorizFailError

sealed class RequestResult<T> {
    abstract fun isSuccess(): Boolean

    class Success<T>(val data: T, val message: String) : RequestResult<T>() {
        override fun isSuccess() = true
    }

    class Error<T>(val code: Int? = null, val message: String) : RequestResult<T>() {
        override fun isSuccess() = false

        fun getError() = if (code == 401) {
            AuthorizFailError()
        } else {
            Throwable(message)
        }
    }

    fun result(
        onSuccess: (T) -> Unit = { },
        onSuccessWithMessage: (T, String) -> Unit = { _, _ -> },
        onError: (String) -> Unit = {}
    ) {
        if (isSuccess()) {
            val successResult = (this as Success<T>)
            onSuccess(successResult.data)
            onSuccessWithMessage(successResult.data, successResult.message)
        } else {
            onError((this as Error<T>).message)
        }
    }
}