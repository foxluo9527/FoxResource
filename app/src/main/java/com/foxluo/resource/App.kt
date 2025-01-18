package com.foxluo.resource

import android.content.Intent
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tony.defenselib.DefenseCrash
import com.android.tony.defenselib.handler.IExceptionHandler
import com.blankj.utilcode.BuildConfig
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.ui.BaseApplication
import com.foxluo.resource.activity.CrashActivity
import com.xuexiang.xui.logs.UILog.isDebug

class App : BaseApplication(){
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
    }
}