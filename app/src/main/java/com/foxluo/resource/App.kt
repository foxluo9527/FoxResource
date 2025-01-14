package com.foxluo.resource

import android.app.Application
import android.content.Context
import android.content.Intent
import com.android.tony.defenselib.DefenseCrash
import com.android.tony.defenselib.handler.IExceptionHandler
import com.blankj.utilcode.util.ActivityUtils
import com.danikula.videocache.HttpProxyCacheServer
import com.foxluo.resource.activity.CrashActivity
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.contract.ICacheProxy
import com.foxluo.resource.music.player.contract.IServiceNotifier

class App : Application(), IServiceNotifier, ICacheProxy {
    private val proxy by lazy {
        HttpProxyCacheServer
            .Builder(this)
            .maxCacheSize(1024 * 1024 * 1024)
            .maxCacheFilesCount(200)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        PlayerManager.getInstance().init(this, this, this)
        DefenseCrash.initialize(this)
        DefenseCrash.install(object : IExceptionHandler{
            override fun onCaughtException(
                thread: Thread?,
                throwable: Throwable?,
                isSafeMode: Boolean,
                isCrashInChoreographer: Boolean
            ) {
                val intent=Intent(ActivityUtils.getTopActivity(),CrashActivity::class.java)
                intent.putExtra("crash",throwable?.message)
                startActivity(intent)
            }
        })
    }

    override fun notifyService(startOrStop: Boolean) {
        //todo 通知栏改变播放状态
    }

    override fun getCacheUrl(url: String?): String? {
        return proxy.getProxyUrl(url)
    }
}