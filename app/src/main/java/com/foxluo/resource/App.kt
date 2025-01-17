package com.foxluo.resource

import android.content.Intent
import com.android.tony.defenselib.DefenseCrash
import com.android.tony.defenselib.handler.IExceptionHandler
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.ui.BaseApplication
import com.foxluo.resource.activity.CrashActivity

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