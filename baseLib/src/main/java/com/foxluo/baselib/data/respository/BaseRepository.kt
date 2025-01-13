package com.foxluo.baselib.data.respository

import LogInterceptor
import com.foxluo.baselib.data.api.BaseApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

private const val BASE_URL = "http://39.106.30.151/"

open class BaseRepository{
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LogInterceptor())
            .addInterceptor(AuthInterceptor())
            .build()
    }
    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
    }

    inline fun <reified API:BaseApi> getApi(): API {
        return retrofit.build().create(API::class.java)
    }
}