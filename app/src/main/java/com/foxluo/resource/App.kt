package com.foxluo.resource

import android.content.Intent
import com.android.tony.defenselib.DefenseCrash
import com.android.tony.defenselib.handler.IExceptionHandler
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.foxluo.baselib.ui.BaseApplication
import com.foxluo.baselib.util.StringUtil.getUrlName
import com.foxluo.resource.activity.CrashActivity
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.contract.ICacheProxy
import com.foxluo.resource.music.player.contract.IServiceNotifier
import java.io.File

class App : BaseApplication(){
    override fun onCreate() {
        super.onCreate()
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
}