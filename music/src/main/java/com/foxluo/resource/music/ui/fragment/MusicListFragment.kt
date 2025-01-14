package com.foxluo.resource.music.ui.fragment

import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.data.domain.viewmodel.MusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.MusicListAdapter

class MusicListFragment : BaseBindingFragment<FragmentMusicListBinding>() {
    private val vm: MusicViewModel by viewModels()

    private val adapter by lazy {
        MusicListAdapter(false, onClickItem)
    }
    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }
    private val onClickItem: (Boolean, Int) -> Unit = { _: Boolean, position: Int ->
        musicViewModel.isCurrentMusicByUser = true
            PlayerManager.getInstance().loadAlbum(
                AlbumData(
                    null,
                    "播放列表",
                    null,
                    null,
                    null,
                    adapter.getPlayList()
                ), position
            )
        }

    override fun initObserver() {
        vm.isLoading.observe(this) {
            setLoading(it)
            binding.refresh.finishRefresh()
            binding.refresh.finishLoadMore()
        }
        vm.toast.observe(this) {
            ToastUtils.showShort(it.second)
        }
        vm.dataList.observe(this) { dataList ->
            if (vm.page == 1) {
                adapter.setDataList(dataList)
            } else {
                adapter.insertDataList(dataList)
            }
        }
        PlayerManager.getInstance().uiStates.observe(this) {
            if (it != null && it.musicId != vm.currentMusic.value?.musicId) {
                vm.currentMusic.value = PlayerManager.getInstance().currentPlayingMusic
            }
        }
        vm.currentMusic.observe(this) { currentMusic ->
            currentMusic ?: return@observe
            val musicList = adapter.getPlayList()
            adapter.currentIndex = musicList.indexOf(musicList.find { it.musicId == currentMusic.musicId })
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