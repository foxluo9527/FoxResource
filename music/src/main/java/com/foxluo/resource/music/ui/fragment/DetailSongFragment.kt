package com.foxluo.resource.music.ui.fragment

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.ImageViewInfo
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.fastClick
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.databinding.FragmentDetailSongBinding
import com.foxluo.resource.music.lyric.manager.LyricStyleManager
import com.foxluo.resource.music.lyric.manager.LyricSyncManager
import com.foxluo.resource.music.ui.activity.MusicCommentActivity
import com.xuexiang.xui.utils.ViewUtils
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import kotlinx.coroutines.launch

class DetailSongFragment : BaseBindingFragment<FragmentDetailSongBinding>() {
    private var currentMusic: MusicEntity? = null
    private var onPlaying = false
    var targetPage: (() -> Unit)? = null

    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }

    private val lyricSyncManager by lazy {
        LyricSyncManager.getInstance()
    }

    fun initMusicData(data: MusicEntity?) {
        this.currentMusic = data
        lifecycleScope.launchWhenStarted {
            initView()
        }
    }

    fun setPrimaryColor(primaryColor: Int){
        binding.comment.setColorFilter(primaryColor)
        binding.like.setColorFilter(primaryColor)
        binding.downloaded.setColorFilter(primaryColor)
        binding.desktopLyric.setColorFilter(primaryColor)
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
            binding.singer.text = data.artist?.name
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
        lifecycleScope.launch {
            lyricSyncManager.isDesktopLyricLocked.collect {
                if (!it) {
                    binding.desktopLyric.setImageResource(com.foxluo.baselib.R.drawable.ic_unlock)
                } else {
                    binding.desktopLyric.setImageResource(com.foxluo.baselib.R.drawable.ic_lock)
                }
            }
        }
        lifecycleScope.launch {
            lyricSyncManager.isDesktopLyricEnabled.collect {
                if (!it) {
                    binding.desktopLyric.setImageResource(com.foxluo.baselib.R.drawable.ic_lyric)
                } else {
                    if (!lyricSyncManager.isDesktopLyricLocked.value) {
                        binding.desktopLyric.setImageResource(com.foxluo.baselib.R.drawable.ic_unlock)
                    } else {
                        binding.desktopLyric.setImageResource(com.foxluo.baselib.R.drawable.ic_lock)
                    }
                }
            }
        }
    }

    override fun initListener() {
        binding.cover.albumView.setOnClickListener {
            currentMusic?.coverImg?.let {
                PreviewBuilder.from(this)
                    .setImg<ImageViewInfo>(ImageViewInfo(processUrl(it), ViewUtils.calculateViewScreenLocation(binding.cover)))
                    .setType(PreviewBuilder.IndicatorType.Dot)
                    .start()
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
        binding.desktopLyric.setOnClickListener {
            if (lyricSyncManager.isDesktopLyricEnabled.value.not()) {
                lyricSyncManager.openDesktopLyric(requireContext())
            } else {
                lyricSyncManager.toggleLock()
            }
        }
    }

    override fun initBinding() = FragmentDetailSongBinding.inflate(layoutInflater)
}