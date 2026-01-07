package com.foxluo.mine.ui.fragment

import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alibaba.android.arouter.launcher.ARouter
import com.foxluo.baselib.R
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.manager.AuthManager.userInfoStateFlow
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.util.ImageExt.loadUrlWithCircle
import com.foxluo.mine.data.viewmodel.LoginViewModel
import com.foxluo.mine.databinding.FragmentMineBinding
import com.foxluo.mine.ui.activity.LoginActivity
import com.foxluo.mine.ui.activity.PersonalActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import kotlinx.coroutines.launch


class MineFragment : MainPageFragment<FragmentMineBinding>() {

    private val loginViewModel by viewModels<LoginViewModel>()

    private val fragment by lazy {
        ARouter.getInstance().build("/resource/music/mine")
            .navigation(requireContext()) as Fragment
    }

    override fun initBinding() = FragmentMineBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View {
        return binding.mineGroup
    }

    override fun initView() {
        super.initView()
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragment

            override fun getItemCount() = 1
        }
        binding.mineViewpager.adapter = adapter
    }

    override fun initListener() {
        super.initListener()
        binding.userInfo.setOnClickListener {
            if (!AuthManager.isLogin()) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                startActivity(Intent(requireContext(), PersonalActivity::class.java))
            }
        }
    }

    override fun initObserver() {
        loginViewModel.toast.observe(this) {
            if (it.first == true) {
                success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
        loginViewModel.isLoading.observe(this) {
            setLoading(it)
        }
        lifecycleScope.launch {
            userInfoStateFlow.collect {
                if (it != null) {
                    binding.userName.text = it.nickname ?: it.username
                    binding.icHead.loadUrlWithCircle(it.avatar)
                    binding.sign.text =
                        it.signature.orEmpty().ifEmpty { getString(R.string.sign_empty) }
                } else {
                    binding.userName.text = getString(R.string.click_login)
                    binding.sign.text = getString(R.string.login_sync_info)
                    binding.icHead.loadUrlWithCircle(null)
                }
            }
        }
    }

    override fun initPlayDragPadding(): IntArray {
        return intArrayOf(20, 0, 20, 0)//todo 待视图完全确认后填写播放控件可拖动区域
    }

    override fun showPlayView() = true

    override fun showNavBottom() = true
}