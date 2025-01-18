package com.foxluo.mine.ui.activity

import android.view.View
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.mine.databinding.ActivityRegisterBinding

class RegisterActivity: BaseBindingActivity<ActivityRegisterBinding>() {
    override fun initBinding()=ActivityRegisterBinding.inflate(layoutInflater)
    override fun initStatusBarView()=binding.main
}