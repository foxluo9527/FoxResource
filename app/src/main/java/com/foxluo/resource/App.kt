package com.foxluo.resource

import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.BuildConfig
import com.foxluo.baselib.ui.BaseApplication
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import kotlinx.coroutines.runBlocking

class App : BaseApplication() {
    private val initializer by lazy {
        listOf(MusicModuleInitializer(), ChatModuleInitializer())
    }

    override fun onCreate() {
        super.onCreate()
        // 确保 ARouter 正确初始化
        if (BuildConfig.DEBUG) {
            ARouter.openLog()     // 打印日志
            ARouter.openDebug()   // 开启调试模式
        }
        try {
            ARouter.init(this)
        } catch (e: Exception) {
            Log.e("ARouterInit", "Initialization failed", e)
        }
        runBlocking {
            initializer.forEach {
                it.onAppCreate(this@App)
            }
        }
    }
}