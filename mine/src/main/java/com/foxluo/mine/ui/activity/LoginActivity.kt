package com.foxluo.mine.ui.activity

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.mine.databinding.ActivityLoginBinding
import com.foxluo.mine.ui.data.viewmodel.LoginViewModel
import com.xuexiang.xui.utils.XToastUtils.error
import com.xuexiang.xui.utils.XToastUtils.success
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Route(path = "/mine/login")
class LoginActivity : BaseBindingActivity<ActivityLoginBinding>() {
    private val viewModel by viewModels<LoginViewModel>()
    override fun initListener() {
        binding.back.setOnClickListener {
            finish()
        }
        binding.login.setOnClickListener {
            val username = binding.userName.text?.toString()
            val password = binding.password.text?.toString()
            if (username.isNullOrEmpty()) {
                toast("请输入用户名/邮箱")
            } else if (password.isNullOrEmpty()) {
                toast("请输入密码")
            } else {
                viewModel.login(username, password)
            }
        }
        binding.register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.forget.setOnClickListener {
            startActivity(Intent(this, ForgetActivity::class.java))
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.toast.observe(this) {
            if (it.first == true) {
                success(it.second)
            } else {
                error(it.second)
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

    override fun initBinding() = ActivityLoginBinding.inflate(layoutInflater)
    override fun initStatusBarView(): View {
        return binding.main
    }
}