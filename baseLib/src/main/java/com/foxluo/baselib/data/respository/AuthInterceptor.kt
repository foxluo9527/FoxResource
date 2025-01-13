package com.foxluo.baselib.data.respository

import com.foxluo.baselib.data.manager.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        if (AuthManager.isLogin()) {
            builder.addHeader("Authorization", AuthManager.token ?: "")
        }
        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}