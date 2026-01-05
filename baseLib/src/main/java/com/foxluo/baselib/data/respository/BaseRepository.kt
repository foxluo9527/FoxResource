package com.foxluo.baselib.data.respository

import LogInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.data.api.BaseApi
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


const val BASE_URL = "http://39.106.30.151:9000"

open class BaseRepository {
    /**
     * 请求访问quest    打印日志
     * response拦截器
     */
    private val authErrorInterceptor: Interceptor = object : Interceptor {
        public override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            return try {
                val response = chain.proceed(request)
                when (response.code()) {
                    401 -> handleUnauthorized(response, request)
                    else -> response
                }
            } catch (e: Exception) {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("Network Error")
                    .body("{\"code\":500,\"message\":\"${e.message}\",\"data\":null,\"success\":false}".toResponseBody())
                    .build()
            }
        }
    }

    private fun handleUnauthorized(originalResponse: Response, request: Request): Response {
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity.javaClass.simpleName != "LoginActivity") {
            ARouter.getInstance().build("/mine/login").navigation(topActivity)
        }

        return originalResponse.newBuilder()
            .code(200)
            .body(
                """
                {
                    "code": 401,
                    "message": "请登录",
                    "data": null,
                    "success": false
                }
                """.trimIndent().toResponseBody("application/json")
            )
            .build()
    }

    private fun String.toResponseBody(contentType: String = "text/plain"): ResponseBody {
        return ResponseBody.create(
            MediaType.parse(contentType),
            this
        )
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

    inline fun <reified API : BaseApi> createApi(): API? {
        return kotlin.runCatching { retrofit.build().create(API::class.java) }.getOrNull()
    }

    interface ResultCallback<T> {
        fun onSuccess(data: T, msg: String)
        fun onError(msg: String)
    }
}