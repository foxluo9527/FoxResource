package com.foxluo.resource.music.ui.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.Constant
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.data.domain.viewmodel.RecommendMusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.MusicListAdapter
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecommendMusicFragment : BaseBindingFragment<FragmentMusicListBinding>() {

    private val vm: RecommendMusicViewModel by viewModels()

    private val adapter by lazy {
        MusicListAdapter(false, onClickItem)
    }
    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }
    private val playerManager by lazy {
        PlayerManager.getInstance()
    }
    private val onClickItem: (Boolean, Int) -> Unit = { _: Boolean, position: Int ->
        musicViewModel.isCurrentMusicByUser = true
        musicViewModel.playingAlbum.value =
            AlbumData(
                albumId = Constant.TABLE_ALBUM_PLAYING_ID.toString(),
                title = Constant.PLAY_LIST_ALBUM_TITLE,
                curMusicId = position
            ).apply {
                musics = adapter.getPlayList().distinctBy { it.musicId }
            }
    }

    override fun initObserver() {
        vm.isLoading.observe(this) {
            setLoading(it)
        }
        vm.toast.observe(this) {
            toast(it.second)
        }
        lifecycleScope.launch {
            vm.musicPager.collectLatest {
                adapter.submitData(it)
            }
        }
        adapter.addLoadStateListener { loadState ->
            when (loadState.refresh) {
                is LoadState.Error -> {
                    binding.refresh.finishRefresh()
                }
                is LoadState.Loading -> binding.refresh.autoLoadMore()
                is LoadState.NotLoading -> {
                    binding.refresh.finishRefresh()
                    binding.refresh.finishLoadMore()
                    val dataList = adapter.getPlayList()
                    binding.emptyView.visible(dataList.isNullOrEmpty() && vm.page == 1)
                    binding.refresh.setEnableLoadMore(!dataList.isNullOrEmpty() && dataList.size == vm.size)
                    binding.refresh.setNoMoreData(dataList.isNullOrEmpty())
                }
            }
        }

        PlayerManager.getInstance().uiStates.observe(this) {
            if (it != null && it.musicId != musicViewModel.currentMusic.value?.musicId) {
                musicViewModel.currentMusic.value = PlayerManager.getInstance().currentPlayingMusic
            }
        }
        musicViewModel.currentMusic.observe(this) { currentMusic ->
            val musicList = adapter.getPlayList()
            adapter.currentIndex =
                musicList.indexOf(musicList.find { it.musicId == currentMusic?.musicId })
        }
    }

    override fun initView() {
        binding.recycleView.adapter = adapter
        binding.refresh.setPrimaryColorsId(R.color.color_F05019, R.color.white)
        binding.refresh.setEnableLoadMore(false)
    }

    override fun initData() {
        vm.loadMusic()
    }

    override fun initBinding() = FragmentMusicListBinding.inflate(layoutInflater)
}