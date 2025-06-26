package com.foxluo.resource.music.ui.fragment

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.flyjingfish.openimagelib.OpenImage
import com.flyjingfish.openimagelib.enums.MediaType
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.fastClick
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.databinding.FragmentDetailSongBinding
import com.foxluo.resource.music.ui.activity.MusicCommentActivity

class DetailSongFragment : BaseBindingFragment<FragmentDetailSongBinding>() {
    private var currentMusic: MusicData? = null
    private var onPlaying = false
    var targetPage: (() -> Unit)? = null

    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }

    fun initMusicData(data: MusicData?) {
        this.currentMusic = data
        lifecycleScope.launchWhenStarted {
            initView()
        }
    }

    fun setPrimaryColor(primaryColor: Int){
        binding.comment.setColorFilter(primaryColor)
        binding.like.setColorFilter(primaryColor)
        binding.downloaded.setColorFilter(primaryColor)
        binding.songName.setTextColor(primaryColor)
        binding.singer.setTextColor(primaryColor)
    }

    fun initPlayState(playing: Boolean) {
        onPlaying = playing
        lifecycleScope.launchWhenStarted {
            binding.cover.setPlaying(playing)
        }
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

    override fun initObserver() {
        musicViewModel.musicFavoriteState.observe(this) {
            binding.like.isSelected = it
        }
    }

    override fun initListener() {
        binding.cover.albumView.setOnClickListener {
            currentMusic?.coverImg?.let {
                OpenImage
                    .with(this)
                    .setImageUrl(processUrl(it), MediaType.IMAGE)
                    .setNoneClickView()
                    .show()
            }
        }
        binding.root.setOnClickListener {
            targetPage?.invoke()
        }
        binding.like.fastClick {
            currentMusic?.musicId?.let { musicId -> musicViewModel.favoriteMusic(musicId) }
        }
        binding.comment.fastClick {
            currentMusic?.musicId?.let { musicId ->
                startActivity(
                    Intent(
                        requireContext(),
                        MusicCommentActivity::class.java
                    ).apply {
                        putExtra("music_id", musicId)
                    }
                )
            }
        }
    }

    override fun initBinding() = FragmentDetailSongBinding.inflate(layoutInflater)
}