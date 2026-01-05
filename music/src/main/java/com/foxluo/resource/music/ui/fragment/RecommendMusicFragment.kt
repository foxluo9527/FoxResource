package com.foxluo.resource.music.ui.fragment

import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.foxluo.baselib.util.Constant
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.SearchMusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.MutableStateFlow

class RecommendMusicFragment() : BaseMusicFragment<FragmentMusicListBinding>() {
    override val loadingView: SmartRefreshLayout
        get() = binding.loading

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

    override fun initView() {
        super.initView()
        binding.recycleView.adapter = adapter
    }

    override fun initData() {
        super.initData()
        vm.loadMusic()
    }

    override fun initBinding() = FragmentMusicListBinding.inflate(layoutInflater)

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
}