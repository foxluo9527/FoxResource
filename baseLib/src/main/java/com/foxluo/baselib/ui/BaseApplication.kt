package com.foxluo.baselib.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.foxluo.baselib.domain.viewmodel.setApplicationContext

//BaseApplication实现ViewModelStoreOwner接口
open class BaseApplication : Application(), ViewModelStoreOwner {
    private lateinit var mAppViewModelStore: ViewModelStore
    private var mFactory: ViewModelProvider.Factory? = null

    override fun onCreate() {
        super.onCreate()
        //设置全局的上下文
        setApplicationContext(this)
        //创建ViewModelStore
        mAppViewModelStore = ViewModelStore()

    }

    override val viewModelStore: ViewModelStore
        get() {
           return mAppViewModelStore
        }

    /**
     * 获取一个全局的ViewModel
     */
    fun getAppViewModelProvider(): ViewModelProvider {
        return ViewModelProvider(this, this.getAppFactory())
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        }
        return mFactory as ViewModelProvider.Factory
    }
}
