package com.foxluo.resource.music.ui.fragment

import androidx.fragment.app.viewModels
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.data.domain.viewmodel.RecommendMusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.MusicListAdapter
import com.xuexiang.xui.utils.XToastUtils.toast

class RecommendMusicFragment : BaseBindingFragment<FragmentMusicListBinding>() {
    companion object {
        const val PLAY_LIST_ALBUM_TITLE = "播放列表"
    }
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
                null,
                PLAY_LIST_ALBUM_TITLE,
                null,
                null,
                null,
                adapter.getPlayList()
            ) to position

    }

    override fun initObserver() {
        vm.isLoading.observe(this) {
            setLoading(it)
            binding.refresh.finishRefresh()
            binding.refresh.finishLoadMore()
        }
        vm.toast.observe(this) {
            toast(it.second)
        }
        vm.dataList.observe(this) { dataList ->
            binding.emptyView.visible(dataList.isNullOrEmpty() && vm.page == 1)
            binding.refresh.setEnableLoadMore(!dataList.isNullOrEmpty() && dataList.size == vm.size)
            if (vm.page == 1) {
                adapter.setDataList(dataList)
            } else {
                binding.refresh.setNoMoreData(dataList.isNullOrEmpty())
                adapter.insertDataList(dataList)
                if (playerManager.albumMusics.isNotEmpty() && playerManager.album.title == PLAY_LIST_ALBUM_TITLE) {
                    playerManager.appendPlayList(dataList)
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
            adapter.currentIndex = musicList.indexOf(musicList.find { it.musicId == currentMusic?.musicId })
        }
    }

    override fun initView() {
        binding.recycleView.adapter = adapter
        binding.refresh.setPrimaryColorsId(R.color.color_F05019, R.color.white)
    }

    override fun initListener() {
        binding.refresh.setOnRefreshListener {
            vm.getMusicData(true)
        }
        binding.refresh.setOnLoadMoreListener {
            vm.getMusicData(false)
        }
    }

    override fun initData() {
        binding.refresh.autoRefresh()
    }

    override fun initBinding() = FragmentMusicListBinding.inflate(layoutInflater)
}