package com.foxluo.mine.ui.activity

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.mine.R
import com.foxluo.mine.databinding.ActivityForgetBinding
import com.foxluo.mine.ui.data.viewmodel.ForgetViewModel
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import com.xuexiang.xui.utils.XToastUtils.toast

class ForgetActivity : BaseBindingActivity<ActivityForgetBinding>() {
    private val viewModel by viewModels<ForgetViewModel>()

    override fun initListener() {
        binding.send.setOnClickListener {
            val email = binding.email.text?.toString()
            if (email.isNullOrEmpty()) {
                toast("请输入邮箱")
            } else {
                viewModel.sendCode(email)
            }
        }
        binding.changePass.setOnClickListener {
            val email = binding.email.text?.toString()
            val password = binding.password.text?.toString()
            val code = binding.code.text?.toString()
            if (email.isNullOrEmpty()) {
                toast("请输入邮箱")
            } else if (password.isNullOrEmpty()) {
                toast("请输入密码")
            } else if (code.isNullOrEmpty()) {
                toast("请输入邮箱验证码")
            } else {
                viewModel.changePass(email, code, password)
            }
        }
    }

    override fun initBinding() = ActivityForgetBinding.inflate(layoutInflater)

    override fun initStatusBarView()=binding.main

    @SuppressLint("SetTextI18n")
    override fun initObserver() {
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
        viewModel.codeSendState.observe(this) {
            binding.send.isEnabled = it <= 0
            if (it > 0) {
                binding.send.setText("${it}S")
            } else {
                binding.send.setText(R.string.send)
            }
        }
        viewModel.changePassResult.observe(this) {
            if (it) {
                finish()
            }
        }
    }
}