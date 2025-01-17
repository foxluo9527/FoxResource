package com.foxluo.resource.music.ui.fragment

import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.FragmentDetailSongBinding

class DetailSongFragment : BaseBindingFragment<FragmentDetailSongBinding>() {
    private var currentMusic: MusicData? = null
    var targetPage :(()->Unit)?=null

    fun initMusicData(data: MusicData?) {
        this.currentMusic = data
        initView()
    }

    fun initPlayState(playing: Boolean) {
        binding.cover.setPlaying(playing)
    }

    override fun initView() {
        super.initView()
        currentMusic?.let { data ->
            binding.cover.setAlbumPic(data.coverImg)
            binding.songName.text = data.title
            binding.singer.text = data.artist.name
            binding.like.isSelected = data.isCollection
        }?: kotlin.run {
            binding.cover.setAlbumPic(null)
            binding.songName.text = ""
            binding.singer.text = ""
            binding.like.isSelected = false
        }
        binding.coverContainer.post {
            binding.coverContainer.radius = (binding.coverContainer.width / 2).toFloat()
        }
    }

    override fun initListener() {
        binding.root.setOnClickListener {
            targetPage?.invoke()
        }
    }

    override fun initBinding() = FragmentDetailSongBinding.inflate(layoutInflater)
}