package com.foxluo.resource.activity

import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.resource.databinding.ActivityCrashBinding

class CrashActivity : BaseBindingActivity<ActivityCrashBinding>() {
    override fun initView() {
        intent.getStringExtra("crash")?.let {
            binding.content.text = it
        }
    }
    override fun initBinding()=ActivityCrashBinding.inflate(layoutInflater)
}