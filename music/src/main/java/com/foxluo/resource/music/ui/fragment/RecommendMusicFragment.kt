package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.foxluo.baselib.util.Constant
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.SearchMusicViewModel
import com.foxluo.resource.music.databinding.FragmentRecommendMusicListBinding
import com.xuexiang.xui.R
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.MutableStateFlow

class RecommendMusicFragment() : MainPageMusicFragment<FragmentRecommendMusicListBinding>() {

    override val musicPager: MutableStateFlow<PagingData<MusicEntity>>
        get() = vm.musicPager

    private val vm: SearchMusicViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()
        vm.isLoading.observe(this) {
            setLoading(it)
        }
        vm.toast.observe(this) {
            toast(it.second)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        super.initView()
        // 设置返回键颜色和大小
        binding.toolbar.navigationIcon =
            resources.getDrawable(R.drawable.xui_ic_navigation_back_white, null)
        binding.toolbar.setTitleTextColor(Color.WHITE)
        // 设置返回键点击事件
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // 设置CollapsingToolbar标题
        binding.collapsingToolbar.title = "推荐音乐"
        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)
        binding.collapsingToolbar.setStatusBarScrimColor(
            resources.getColor(
                com.foxluo.baselib.R.color.color_ED6C40,
                null
            )
        )
    }

    override fun initListener() {
        super.initListener()
        binding.appBar.addOnOffsetChangedListener(this::updateMusicListHeight)
    }

    override fun initData() {
        super.initData()
        vm.loadMusic()
    }

    override fun initBinding() = FragmentRecommendMusicListBinding.inflate(layoutInflater)

    override fun getPlayListId(): String {
        return Constant.TABLE_ALBUM_PLAYING_ID.toString()
    }

    override fun getPlayListTitle(): String {
        return Constant.PLAY_LIST_ALBUM_TITLE
    }

    override fun onMenuAction(
        action: Int,
        music: MusicEntity?
    ) {
        when (action) {
            0 -> toast("查看歌手: ${music?.artist?.name}")
            1 -> toast("查看专辑: ")
            2 -> toast("下一首播放")
            3 -> toast("添加到歌单: ${music?.title}")
            4 -> toast("分享: ${music?.title}")
            5 -> toast("举报: ${music?.title}")
        }
    }

    override fun showPlayView() = true
}