package com.foxluo.mine.ui.activity

import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.mine.databinding.ActivityForgetBinding

class ForgetActivity : BaseBindingActivity<ActivityForgetBinding>() {
    override fun initBinding() = ActivityForgetBinding.inflate(layoutInflater)
    override fun initStatusBarView()=binding.main
}