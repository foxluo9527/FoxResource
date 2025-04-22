package com.foxluo.baselib.data.respository

import LogInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.data.api.BaseApi
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.util.StringUtil.getMediaType
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.nio.file.Paths


const val BASE_URL = "http://39.106.30.151:9000"

open class BaseRepository {
    /**
     * 请求访问quest    打印日志
     * response拦截器
     */
    private val authErrorInterceptor: Interceptor = object : Interceptor {
        public override fun intercept(chain: Interceptor.Chain): Response {
            return kotlin.runCatching {
                val response: Response = chain.proceed(chain.request())
                val code: Int = response.code()
                if (200 != code) {
                    val topActivity = ActivityUtils.getTopActivity()
                    val inLoginPage = topActivity.javaClass.simpleName != "LoginActivity"
                    if (code == 401) {
                        if (inLoginPage) {
                            ARouter.getInstance().build("/mine/login")
                                .navigation(topActivity)
                            return response.newBuilder()
                                .code(200)
                                .body(
                                    ResponseBody.create(
                                        null,
                                        "{\"code\":500,\"message\":\"请登录\",\"data\":null,\"success\":false}"
                                    )
                                )
                                .build()
                        }
                    }
                    response.newBuilder()
                        .code(200)
                        .build()
                } else {
                    response
                }
            }.getOrNull() ?: Response.Builder().build()
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

    inline fun <reified API : BaseApi> createApi(): API? {
        return kotlin.runCatching { retrofit.build().create(API::class.java) }.getOrNull()
    }
}