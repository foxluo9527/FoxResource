package com.foxluo.mine.ui.fragment

import android.content.Intent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alibaba.android.arouter.launcher.ARouter
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.manager.AuthManager.userInfoStateFlow
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.ui.fragment.TempFragment
import com.foxluo.baselib.R
import com.foxluo.mine.databinding.FragmentMineBinding
import com.foxluo.mine.ui.activity.LoginActivity
import com.foxluo.mine.ui.data.viewmodel.LoginViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MineFragment : MainPageFragment<FragmentMineBinding>() {
    private val tabs by lazy {
        arrayOf(
            getString(com.foxluo.baselib.R.string.music),
            getString(com.foxluo.baselib.R.string.video),
            getString(
                com.foxluo.baselib.R.string.novel
            )
        )
    }

    private val loginViewModel by viewModels<LoginViewModel>()//todo暂时未做设置页退出登录

    private val fragments by lazy {
        arrayOf(
            ARouter.getInstance().build("/resource/music/mine")
                .navigation(requireContext()) as Fragment,
            TempFragment().apply {
                arguments = bundleOf("type" to "功能开发中~")
            },
            TempFragment().apply {
                arguments = bundleOf("type" to "功能开发中~")
            })
    }

    override fun initBinding() = FragmentMineBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View {
        return binding.mineGroup
    }

    override fun initView() {
        super.initView()
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }
        binding.mineViewpager.adapter = adapter
        TabLayoutMediator(binding.mineTab, binding.mineViewpager) { tab, position ->
            tab.text = tabs[position]
            tab.view.setOnLongClickListener { true }
            tab.view.tooltipText = null
        }.apply {
            this.attach()
        }
    }

    override fun initListener() {
        super.initListener()
        binding.userInfo.setOnClickListener {
            if (!AuthManager.isLogin()){
                startActivity(Intent(requireContext(),LoginActivity::class.java))
            }else{
                loginViewModel.logout()
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
                    binding.userName.text = it.username
                    binding.sign.text =
                        it.signature.orEmpty().ifEmpty { getString(R.string.sign_empty) }
                } else {
                    binding.userName.text = getString(R.string.click_login)
                    binding.sign.text = getString(R.string.login_sync_info)
                }
            }
        }
    }

    override fun initPlayDragPadding(): IntArray {
        return intArrayOf(20,0,20,0)//todo 待视图完全确认后填写播放控件可拖动区域
    }

    override fun showPlayView() = true
}