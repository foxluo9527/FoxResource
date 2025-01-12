package com.foxluo.resource.activity

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ConvertUtils.px2dp
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.chat.ui.ChatFragment
import com.foxluo.home.ui.HomeFragment
import com.foxluo.mine.ui.MineFragment
import com.foxluo.resource.community.ui.CommunityFragment
import com.foxluo.resource.databinding.ActivityMainBinding
import com.foxluo.resource.R

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {
    private val home by lazy {
        HomeFragment()
    }
    private val chat by lazy {
        ChatFragment()
    }
    private val community by lazy {
        CommunityFragment()
    }
    private val mine by lazy {
        MineFragment()
    }

    override fun initView() {
        replaceFragment(home, "HomeFragment")
    }

    override fun initListener() {
        binding.navBottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_home -> replaceFragment(home, "HomeFragment")
                R.id.item_chat -> replaceFragment(chat, "ChatFragment")
                R.id.item_group -> replaceFragment(community, "CommunityFragment")
                else -> replaceFragment(mine, "MineFragment")
            }

            true
        }
        val menuView = binding.navBottom.getChildAt(0) as? ViewGroup
        menuView?.post {
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i)
                item.setOnLongClickListener { // 返回true表示消费了事件，不会显示Toast
                    true
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        var currentFragment = supportFragmentManager.findFragmentByTag(tag)?:fragment
        if (!currentFragment.isAdded) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment, tag)
                .commitAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag).commit()
            currentFragment = fragment
        }
        (currentFragment as? MainPageFragment<*>)?.let {
            if (!(currentFragment.showPlaView())) {
                binding.playView.visibility = View.INVISIBLE
                return
            }
            binding.playView.visibility = View.VISIBLE
            binding.navBottom.post {
                binding.playView.setDragPadding(
                    currentFragment.leftPlayPadding,
                    statusBarHeight + currentFragment.topPlayPadding,
                    currentFragment.rightPlayPadding,
                    px2dp(binding.navBottom.height.toFloat()) + currentFragment.bottomPlayPadding + 10
                )
            }
        }
    }

    override fun initBinding() = ActivityMainBinding.inflate(layoutInflater)
}