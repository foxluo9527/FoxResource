package com.foxluo.baselib.domain.usecase

import com.foxluo.baselib.domain.usecase.UseCase.RequestValues
import com.foxluo.baselib.domain.usecase.UseCase.ResponseValue

/**
 * Use cases are the entry points to the domain layer.
 *
 * @param <Q> the request type
 * @param <P> the response type
</P></Q> */
abstract class UseCase<Q : RequestValues?, P : ResponseValue?> {
    private var mRequestValues: Q? = null

    var useCaseCallback: UseCaseCallback<P>? = null

    var requestValues: Q?
        get() = mRequestValues
        set(requestValues) {
            mRequestValues = requestValues
        }

    fun run() {
        executeUseCase(mRequestValues)
    }

    protected abstract fun executeUseCase(requestValues: Q?)

    /**
     * Data passed to a request.
     */
    interface RequestValues

    /**
     * Data received from a request.
     */
    interface ResponseValue

    interface UseCaseCallback<R> {
        fun onSuccess(response: R)

        fun onError() {
        }
    }
}