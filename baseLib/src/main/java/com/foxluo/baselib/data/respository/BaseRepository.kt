package com.foxluo.baselib.data.respository

import LogInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.data.api.BaseApi
import com.xuexiang.xui.utils.XToastUtils.toast
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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
            val response: Response = chain.proceed(chain.request())
            val code: Int = response.code()
            if (200 != code) {
                if (code == 401) {
                    toast("请登录")
                    ActivityUtils.getTopActivity()?.let {
                        ARouter.getInstance().build("/mine/login")
                            .navigation(it)
                    }
                }
                return response.newBuilder()
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