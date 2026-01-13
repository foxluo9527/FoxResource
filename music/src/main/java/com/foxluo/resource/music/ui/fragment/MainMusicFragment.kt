package com.foxluo.resource.music.ui.fragment

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.resource.music.R
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.fragment.TempFragment
import com.foxluo.resource.music.databinding.FragmentMainMusicBinding

class MainMusicFragment : BaseBindingFragment<FragmentMainMusicBinding>() {
    private val recommendMusicFragment by lazy {
        RecommendMusicFragment()
    }

    private val recentMusicFragment by lazy {
        RecentMusicFragment()
    }

    private val playListFragment by lazy {
        TempFragment().apply {
            arguments = bundleOf("type" to "歌单")
        }
    }

    private val singerFragment by lazy {
        TempFragment().apply {
            arguments = bundleOf("type" to "歌手")
        }
    }

    private var currentFragment: Fragment? = null


    override fun initView() {

    }

    override fun initListener() {
        binding.recommend.setOnClickListener {
            EventViewModel.showMainPageFragment.value = recommendMusicFragment to System.currentTimeMillis()
        }
        binding.recent.setOnClickListener {
            EventViewModel.showMainPageFragment.value = recentMusicFragment to System.currentTimeMillis()
        }
    }

    override fun initBinding() = FragmentMainMusicBinding.inflate(layoutInflater)
}