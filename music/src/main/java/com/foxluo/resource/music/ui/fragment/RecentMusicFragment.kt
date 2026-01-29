package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.blankj.utilcode.util.BarUtils
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.util.Constant
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.RecentMusicViewModel
import com.foxluo.resource.music.databinding.FragmentRecentMusicListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.xuexiang.xui.R
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.MutableStateFlow

class RecentMusicFragment : MainPageMusicFragment<FragmentRecentMusicListBinding>() {
    override val musicPager: MutableStateFlow<PagingData<MusicEntity>>
        get() = vm.musicPager
    private val vm: RecentMusicViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()
        vm.isLoading.observe(this) {
            setLoading(it)
        }
        vm.toast.observe(this) {
            toast(it.second)
        }
        AuthManager.userInfoStateFlow.asLiveData(lifecycleScope.coroutineContext).observe(this){
            adapter.refresh()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        super.initView()
        // 设置返回键颜色和大小
        binding.toolbar.navigationIcon =
            resources.getDrawable(R.drawable.xui_ic_navigation_back_white, null)
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.apply {
            layoutParams = layoutParams?.apply {
                height = BarUtils.getStatusBarHeight()
            }
        }
        // 设置返回键点击事件
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // 设置CollapsingToolbar标题
        binding.collapsingToolbar.title = "最近播放"
        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)
        binding.collapsingToolbar.setStatusBarScrimColor(
            resources.getColor(
                com.foxluo.baselib.R.color.color_ED6C40,
                null
            )
        )
    }
    override fun onSelect() {
        binding.appBar.setExpanded(false,false)
    }
    override fun initListener() {
        super.initListener()
        binding.appBar.addOnOffsetChangedListener(this::updateMusicListHeight)
    }

    override fun getPlayListId(): String {
       return Constant.TABLE_ALBUM_HISTORY_ID.toString()
    }

    override fun getPlayListTitle(): String {
        return Constant.HISTORY_LIST_ALBUM_TITLE
    }

    override fun onMenuAction(
        action: Int,
        music: MusicEntity?
    ) {
        when (action) {
            0 -> {} //查看歌手
            1->{}//查看专辑
            2->{//添加到播放列表
                toast("已添加到播放队列")
                PlayerManager.getInstance().appendPlayList(listOf(music))
            }
            3->{//添加到歌单
                music?.musicId ?.let {
                    addToPlaylist(listOf(it))
                }
            }
            4->{}//分享
            5->{}//反馈
        }
    }

    override fun initData() {
        vm.loadMusic()
    }


    override fun initBinding() = FragmentRecentMusicListBinding.inflate(layoutInflater)

    override fun showPlayView() = !adapter.isSelectModel

}