package com.foxluo.baselib.domain

import android.app.Application

// common模块或base模块中
interface ModuleInitializer {
    suspend fun onAppCreate(app: Application)
}