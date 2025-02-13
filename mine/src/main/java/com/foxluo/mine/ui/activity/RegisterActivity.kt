package com.foxluo.mine.ui.activity

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.mine.databinding.ActivityRegisterBinding
import com.foxluo.mine.ui.data.viewmodel.RegisterViewModel
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity: BaseBindingActivity<ActivityRegisterBinding>() {

    private val viewModel by viewModels<RegisterViewModel>()

    override fun initBinding()=ActivityRegisterBinding.inflate(layoutInflater)

    override fun initStatusBarView()=binding.main

    override fun initListener() {
        binding.back.setOnClickListener {
            finish()
        }
        binding.register.setOnClickListener {
            val username = binding.userName.text?.toString()
            val password = binding.password.text?.toString()
            val email = binding.email.text?.toString()
            if (username.isNullOrEmpty()) {
                toast("请输入用户名/邮箱")
            } else if (password.isNullOrEmpty()) {
                toast("请输入密码")
            } else if (email.isNullOrEmpty()) {
                toast("请输入邮箱")
            } else {
                viewModel.register(username, password, email)
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.toast.observe(this) {
            if (it.first == true) {
                success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
        viewModel.isLoading.observe(this) {
            setLoading(it)
        }
        lifecycleScope.launch {
            AuthManager.userInfoStateFlow.collectLatest {
                if (it != null) {
                    finish()
                }
            }
        }
    }
}