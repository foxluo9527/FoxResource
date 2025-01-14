package com.foxluo.baselib.domain.viewmodel

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.foxluo.baselib.ui.BaseApplication

lateinit var appContext: Application

fun setApplicationContext(context: Application) {
    appContext = context
}

//定义扩展方法
inline fun <reified VM : ViewModel> Fragment.getAppViewModel(): VM {
    (this.requireActivity().application as? BaseApplication).let {
        if (it == null) {
            throw NullPointerException("Application does not inherit from BaseApplication")
        } else {
            return it.getAppViewModelProvider().get(VM::class.java)
        }
    }
}

//定义扩展方法
inline fun <reified VM : ViewModel> AppCompatActivity.getAppViewModel(): VM {
    (this.application as? BaseApplication).let {
        if (it == null) {
            throw NullPointerException("Application does not inherit from BaseApplication")
        } else {
            return it.getAppViewModelProvider().get(VM::class.java)
        }
    }
}
