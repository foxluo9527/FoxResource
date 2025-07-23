package com.foxluo.mine.ui.activity

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.manager.AuthManager.userInfoStateFlow
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.CropImageContract
import com.foxluo.baselib.util.CropImageResult
import com.foxluo.baselib.util.DialogUtil.showConfirmDialog
import com.foxluo.baselib.util.DialogUtil.showInputDialog
import com.foxluo.baselib.util.ImageExt.loadUrl
import com.foxluo.baselib.util.StringUtil.prefix
import com.foxluo.baselib.util.getFilePath
import com.foxluo.mine.data.viewmodel.LoginViewModel
import com.foxluo.mine.data.viewmodel.PersonalViewModel
import com.foxluo.mine.databinding.ActivityPersonalBinding
import com.foxluo.mine.ui.fragment.ChooseShowHeadDialog
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 个人资料
 */
class PersonalActivity : BaseBindingActivity<ActivityPersonalBinding>() {
    override fun initBinding() = ActivityPersonalBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View {
        return binding.title
    }

    private val loginViewModel by viewModels<LoginViewModel>()

    private val viewModel by viewModels<PersonalViewModel>()

    // 注册相册选择页面回调
    @SuppressLint("SuspiciousIndentation")
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                cropImageContract.launch(CropImageResult(uri, 1f, 1f))
            }
        }

    private val cropImageContract =
        registerForActivityResult(CropImageContract()) { uri ->
            val path = runBlocking { uri?.getFilePath(true) }
            path?.let { viewModel.uploadFile(it) }
        }

    override fun initData() {
        viewModel.initProfile()
    }

    override fun initListener() {
        binding.llHead.setOnClickListener {
            ChooseShowHeadDialog().show(
                supportFragmentManager,
                AuthManager.authInfo?.user?.avatar ?: "",
                binding.icHead
            ) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
        binding.back.setOnClickListener {
            finish()
        }
        binding.llNiceName.setOnClickListener {
            showInputDialog("请输入用户昵称", preFill = binding.nickName.text.toString()) {
                viewModel.updateProfile(nickName = it)
            }
        }
        binding.llSign.setOnClickListener {
            showInputDialog("请输入签名", preFill = binding.sign.text.toString()) {
                viewModel.updateProfile(signature = it)
            }
        }
        binding.logOut.setOnClickListener {
            showConfirmDialog("是否确认退出登录") {
                loginViewModel.logout()
            }
        }
    }

    override fun initObserver() {
        val toastObserver = { it: Pair<Boolean?, String> ->
            if (it.first == true) {
                success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
        viewModel.toast.observe(this, toastObserver)
        loginViewModel.toast.observe(this, toastObserver)
        val loadingObserver = { it: Boolean ->
            setLoading(it)
        }
        viewModel.isLoading.observe(this, loadingObserver)
        loginViewModel.isLoading.observe(this, loadingObserver)
        viewModel.profile.observe(this) {}
        lifecycleScope.launch {
            userInfoStateFlow.collectLatest {
                it?.let {
                    binding.email.text = it.email
                    binding.userName.text = it.username
                    binding.icHead.loadUrl(it.avatar)
                    binding.sign.text = it.signature
                    binding.nickName.text = it.nickname
                } ?: finish()
            }
        }
    }
}