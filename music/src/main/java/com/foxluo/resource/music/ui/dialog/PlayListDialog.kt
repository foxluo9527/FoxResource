package com.foxluo.resource.music.ui.dialog

import androidx.lifecycle.MutableLiveData
import com.foxluo.baselib.ui.BaseBottomSheetDialogFragment
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.DialogPlayingListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.PlayingListAdapter

class PlayListDialog : BaseBottomSheetDialogFragment<DialogPlayingListBinding>() {
    private val currentMusic by lazy {
        MutableLiveData<MusicData>(playManager.currentPlayingMusic)
    }

    private val adapter by lazy {
        PlayingListAdapter(onClickItem)
    }

    private val playManager by lazy {
        PlayerManager.getInstance()
    }


    private val onClickItem: (Boolean, Int) -> Unit = { isDelete: Boolean, position: Int ->
        if (isDelete) {
            playManager.removeAlbumIndex(position)
            initData()
        } else {
            playManager.playAudio(position)
        }
    }

    override fun initView() {
        binding.rvList.adapter = adapter
    }

    override fun initHeightPercent(): Int {
        return 70
    }

    override fun initListener() {
        binding.clear.setOnClickListener {
            playManager.clearPlayList()
            initData()
        }
    }

    override fun initData() {
        super.initData()
        adapter.setDataList(playManager.albumMusics)
        currentMusic.value = playManager.currentPlayingMusic
        binding.listTitle.text = playManager.album?.title?.plus("(共${playManager.albumMusics?.size}首)") ?: ""
    }

    override fun initObserver() {
        playManager.uiStates.observe(this) {
            if (it != null && it.musicId != currentMusic.value?.musicId) {
                currentMusic.value = PlayerManager.getInstance().currentPlayingMusic
            }
        }
        currentMusic.observe(this) { currentMusic ->
            currentMusic ?: return@observe
            val musicList = adapter.getPlayList()
            adapter.currentIndex =
                musicList.indexOf(musicList.find { it.musicId == currentMusic.musicId })
            binding.rvList.scrollToPosition(adapter.currentIndex ?: 0)
        }
    }

    override fun initBinding() = DialogPlayingListBinding.inflate(layoutInflater)
}