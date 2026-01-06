package com.foxluo.resource

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.BuildConfig
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.ui.BaseApplication
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.lyric.manager.LyricStyleManager
import com.foxluo.resource.music.lyric.manager.LyricSyncManager
import kotlinx.coroutines.flow.update
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
        LyricSyncManager.getInstance().init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                EventViewModel.appInForeground.value = true
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                EventViewModel.appInForeground.value= false
            }
        })
    }
}