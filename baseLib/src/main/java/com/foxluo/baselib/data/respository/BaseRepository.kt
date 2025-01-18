package com.foxluo.baselib.data.respository

import LogInterceptor
import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.result.BaseResponse
import com.foxluo.baselib.util.GsonUtil.toJsonString
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


const val BASE_URL = "http://39.106.30.151:9000"

open class BaseRepository{
    /**
     * 请求访问quest    打印日志
     * response拦截器
     */
    private val authErrorInterceptor: Interceptor = object : Interceptor {
        @Throws(IOException::class)
        public override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            val response: Response = chain.proceed(chain.request())
            val code: Int = response.code()
            if (401 == code) {
                val authErrorResponse = BaseResponse<Unit>().apply {
                    success = false
                    message = if (request.url().url().path == "/api/auth/login")
                        "登录失败，请检查用户名或密码"
                    else
                        "获取登录信息失败".also {
                            AuthManager.logout()
                        }
                }
                return response.newBuilder()
                    .body(
                        ResponseBody.create(
                            MediaType.get("application/json"),
                            authErrorResponse.toJsonString()
                        )
                    )
                    .code(200)
                    .build()
            } else {
                return response
            }
        }
    }
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LogInterceptor())
            .addInterceptor(AuthInterceptor())
            .addInterceptor(authErrorInterceptor)
            .build()
    }
    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
    }

    inline fun <reified API:BaseApi> getApi(): API? {
        return kotlin.runCatching { retrofit.build().create(API::class.java) }.getOrNull()
    }
}