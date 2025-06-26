package com.foxluo.resource.music.ui.fragment

import android.os.Build
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blankj.utilcode.util.SPUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.BitmapUtil.ColorFilterCallback
import com.foxluo.baselib.util.BitmapUtil.getImageColors
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.bean.ArtistData
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.FragmentPlayBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.domain.MusicDTO
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayFragment : BaseBindingFragment<FragmentPlayBinding>() {
    private val playManager by lazy {
        PlayerManager.getInstance()
    }

    var currentMusicData: MusicData? = null

    private var colorFilters: Triple<Int, Int, Int>? = null

    private var colorFilterCallback: ColorFilterCallback? = null

    override fun initBinding() = FragmentPlayBinding.inflate(layoutInflater)

    private val tabs by lazy {
        arrayOf(getString(R.string.song), getString(R.string.lyrics))
    }

    private val fragments by lazy {
        arrayOf(
            DetailSongFragment().apply {
                targetPage = this@PlayFragment.targetPage
            }, DetailLyricsFragment().apply {
                targetPage = this@PlayFragment.targetPage
                setDragClickCallback {
                    playManager.setSeek(it.toInt())
                }
            }
        )
    }

    private val targetPage: () -> Unit by lazy {
        {
            binding.detailViewpager.setCurrentItem(
                if (binding.detailViewpager.currentItem == 0) {
                    1
                } else {
                    0
                }, false
            )
        }
    }

    override fun initView() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }
        binding.detailViewpager.adapter = adapter
        binding.detailViewpager.isSaveEnabled = false
        binding.detailViewpager.offscreenPageLimit = 2
        binding.detailViewpager.isUserInputEnabled = false
        TabLayoutMediator(
            binding.detailTab,
            binding.detailViewpager,
            true,
            false
        ) { tab, position ->
            tab.text = tabs[position]
            tab.view.setOnLongClickListener { true }
            tab.view.tooltipText = null
        }.apply {
            this.attach()
        }
    }

    fun setPlayCallback(callback: ColorFilterCallback) {
        this.colorFilterCallback = callback
        colorFilters?.let {
            lifecycleScope.launch {
                callback.onColorFilterChanged(it.first, it.second, it.third)
            }
        }
    }

    override fun initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("musicData", MusicData::class.java)?.let {
                currentMusicData = it
                setMusicData(it)
            }
        }
    }

    private fun setMusicData(music: MusicData) {
        lifecycleScope.launch {
            context?.getImageColors(music.coverImg, object : ColorFilterCallback {
                override suspend fun onColorFilterChanged(
                    primaryColor: Int,
                    primaryTextColor: Int,
                    secondaryTextColor: Int
                ) {
                    colorFilters = Triple(primaryColor, primaryTextColor, secondaryTextColor)
                    colorFilterCallback?.onColorFilterChanged(
                        primaryColor,
                        primaryTextColor,
                        secondaryTextColor
                    )
                    withContext(Dispatchers.Main) {
                        lifecycleScope.launchWhenStarted {
                            (fragments[1] as DetailLyricsFragment).setLyricTextColor(
                                primaryTextColor,
                                secondaryTextColor
                            )
                            (fragments[0] as DetailSongFragment).setPrimaryColor(primaryTextColor)
                            binding.detailTab.setTabTextColors(secondaryTextColor, primaryTextColor)
                        }
                    }
                }
            })
        }
        (fragments[0] as DetailSongFragment).initMusicData(music)
        (fragments[1] as DetailLyricsFragment).setLyrics(
            music.lyrics,
            music.lyricsTrans
        )
    }

    fun updatePlayState(uiState: MusicDTO<AlbumData, MusicData, ArtistData>) {
        if (uiState.musicId != currentMusicData?.musicId) return
        (fragments[0] as DetailSongFragment).initPlayState(!uiState.isPaused)
        (fragments[1] as DetailLyricsFragment).setLyricsDuration(
            uiState.progress.toLong()
        )
    }

    fun onDetached() {
        binding.detailViewpager.currentItem = 0
        (fragments[0] as DetailSongFragment).initPlayState(false)
        (fragments[1] as DetailLyricsFragment).setLyricsDuration(0)
    }
}