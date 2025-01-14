package com.foxluo.resource.music.ui.fragment

import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.ImageExt.loadUrl
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.FragmentDetailSongBinding

class DetailSongFragment : BaseBindingFragment<FragmentDetailSongBinding>() {
    private var currentMusic: MusicData? = null
    var targetPage :(()->Unit)?=null
    fun initMusicData(data: MusicData) {
        this.currentMusic = data
        initView()
    }

    override fun initView() {
        super.initView()
        currentMusic?.let { data ->
            binding.cover.loadUrl(data.coverImg)
            binding.songName.text = data.title
            binding.singer.text = data.artist.name
            binding.like.isSelected = data.isCollection
        }
    }

    override fun initListener() {
        binding.root.setOnClickListener {
            targetPage?.invoke()
        }
    }

    override fun initBinding() = FragmentDetailSongBinding.inflate(layoutInflater)
}