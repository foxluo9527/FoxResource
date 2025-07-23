package com.foxluo.chat.ui

import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alibaba.android.arouter.launcher.ARouter
import com.foxluo.baselib.R
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.databinding.FragmentHomeChatBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatHomeFragment : MainPageFragment<FragmentHomeChatBinding>() {
    private val fragments by lazy {
        arrayOf(
            ChatFragment(), FriendsFragment()
        )
    }
    private val tabs by lazy {
        arrayOf(getString(R.string.message), getString(R.string.friends))
    }

    override fun initBinding() = FragmentHomeChatBinding.inflate(layoutInflater)

    override fun showPlayView() = false

    override fun initStatusBarView(): View? {
        return binding.root
    }

    override fun initView() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }
        binding.chatViewpager.adapter = adapter
        binding.chatViewpager.isSaveEnabled = false
        binding.chatViewpager.offscreenPageLimit = 2
        binding.chatViewpager.isUserInputEnabled = false
        TabLayoutMediator(
            binding.chatTab,
            binding.chatViewpager,
            true,
            false
        ) { tab, position ->
            tab.text = tabs[position]
            tab.view.setOnLongClickListener { true }
            tab.view.tooltipText = null
        }.apply {
            this.attach()
        }
    }

    override fun initListener() {
        lifecycleScope.launch {
            AuthManager.userInfoStateFlow.collectLatest {
                binding.unLogin.visible(it == null)
            }
        }
        binding.search.setOnClickListener {
            startActivity(Intent(context, SearchFriendActivity::class.java))
        }
        binding.unLogin.setOnClickListener {
            ARouter.getInstance().build("/mine/login").navigation(requireActivity())
        }
    }
}