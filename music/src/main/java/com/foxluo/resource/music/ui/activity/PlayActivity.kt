package com.foxluo.resource.music.ui.activity

import android.content.Intent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foxluo.baselib.R
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.ImageExt.loadUrlWithBlur
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.ActivityPlayBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.domain.PlayingInfoManager.RepeatMode
import com.foxluo.resource.music.ui.dialog.PlayListDialog
import com.foxluo.resource.music.ui.fragment.DetailLyricsFragment
import com.foxluo.resource.music.ui.fragment.DetailSongFragment
import com.google.android.material.tabs.TabLayoutMediator

class PlayActivity : BaseBindingActivity<ActivityPlayBinding>() {
    companion object {
        fun startPlayDetail(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, PlayActivity::class.java))
            activity.overridePendingTransition(R.anim.activity_open, 0)

        }
    }

    override fun initStatusBarView(): View {
        return binding.main
    }

    private var mCurrentMusic: MusicData? = null

    private val playManager by lazy {
        PlayerManager.getInstance()
    }

    private val tabs by lazy {
        arrayOf(getString(R.string.song), getString(R.string.lyrics))
    }

    private val fragments by lazy {
        arrayOf(
            DetailSongFragment().apply {
                targetPage = this@PlayActivity.targetPage
            }, DetailLyricsFragment().apply {
                targetPage = this@PlayActivity.targetPage
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
        TabLayoutMediator(binding.detailTab, binding.detailViewpager, true, false) { tab, position ->
            tab.text = tabs[position]
            tab.view.setOnLongClickListener { true }
            tab.view.tooltipText = null
        }.apply {
            this.attach()
        }
    }

    override fun initListener() {
        binding.back.setOnClickListener {
            finish()
        }
        binding.togglePlay.setOnClickListener {
            if (playManager.currentPlayingMusic == null) return@setOnClickListener
            playManager.togglePlay()
        }
        binding.playNext.setOnClickListener {
            if (playManager.currentPlayingMusic == null) return@setOnClickListener
            playManager.playNext()
        }
        binding.playPrevious.setOnClickListener {
            if (playManager.currentPlayingMusic == null) return@setOnClickListener
            playManager.playPrevious()
        }
        binding.playModel.setOnClickListener {
            if (playManager.currentPlayingMusic == null) return@setOnClickListener
            playManager.changeMode()
        }
        binding.playList.setOnClickListener {
            PlayListDialog().show(supportFragmentManager, "PlayListDialog")
        }
        binding.playProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { playManager.setSeek(it) }
            }
        })
    }

    override fun initObserver() {
        binding.detailViewpager.postDelayed({
            playManager.uiStates.observe(this) {
                it ?: return@observe
                when (it.repeatMode) {
                    RepeatMode.LIST_CYCLE -> binding.playModel.setImageResource(R.drawable.ic_cycle)
                    RepeatMode.SINGLE_CYCLE -> binding.playModel.setImageResource(R.drawable.ic_single)
                    RepeatMode.RANDOM -> binding.playModel.setImageResource(R.drawable.ic_random)
                }
                if (binding.togglePlay.isSelected != playManager.isPlaying) {
                    (fragments[0] as DetailSongFragment).initPlayState(playManager.isPlaying)
                }
                binding.togglePlay.isSelected = playManager.isPlaying
                binding.playProgress.progress = it.progress
                binding.playProgress.secondaryProgress = it.duration / 100 * it.cacheBufferProgress//这里的进度是百分比进度，转换对应秒数
                binding.playProgress.max = it.duration
                (fragments[1] as DetailLyricsFragment).setLyricsDuration(
                    it.progress.toLong()
                )
                binding.nowTime.text = it.nowTime
                binding.totalTime.text = it.allTime
                if (it.musicId != mCurrentMusic?.musicId) {
                    initCurrentMusicDetail()
                }
            }
        }, 100)
    }

    private fun initCurrentMusicDetail() {
        playManager.currentPlayingMusic.let { music ->
            mCurrentMusic = music
            binding.blur.loadUrlWithBlur(music.coverImg)
            (fragments[0] as DetailSongFragment).initMusicData(music)
            (fragments[1] as DetailLyricsFragment).setLyrics(
                music?.lyrics,
                music?.lyricsTrans
            )
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(0, R.anim.activity_close)
    }

    override fun initBinding() = ActivityPlayBinding.inflate(layoutInflater)
}