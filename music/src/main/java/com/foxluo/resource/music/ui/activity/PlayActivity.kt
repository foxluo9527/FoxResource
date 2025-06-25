package com.foxluo.resource.music.ui.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.BitmapUtil.getImageColors
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.databinding.ActivityPlayBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.domain.PlayingInfoManager.RepeatMode
import com.foxluo.resource.music.ui.dialog.PlayListDialog
import com.foxluo.resource.music.ui.fragment.DetailLyricsFragment
import com.foxluo.resource.music.ui.fragment.DetailSongFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class PlayActivity : BaseBindingActivity<ActivityPlayBinding>() {
    companion object {
        fun startPlayDetail(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, PlayActivity::class.java))
            activity.overridePendingTransition(R.anim.activity_open, 0)

        }
    }

    private val animator by lazy {
        ObjectAnimator.ofFloat(binding.buffering, "rotation", 0.0F, 360.0F).apply {
            setDuration(1000)
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
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

    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
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

    private var onTouchSeekBar = false

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

    override fun initListener() {
        binding.back.setOnClickListener {
            finish()
        }
        binding.reload.setOnClickListener {
            if (playManager.currentPlayingMusic == null ||
                playManager.currentPlayingMusic.url.contains("http").not()
            ) {
                return@setOnClickListener
            }
            playManager.playAgain()
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
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                onTouchSeekBar = true
            }

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { playManager.setSeek(it) }
                onTouchSeekBar = false
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
                if (!onTouchSeekBar) {
                    binding.playProgress.progress = it.progress
                }
                binding.playProgress.secondaryProgress =
                    it.duration / 100 * it.cacheBufferProgress//这里的进度是百分比进度，转换对应秒数
                binding.playProgress.max = it.duration
                setBuffering(it.isBuffering)
                (fragments[1] as DetailLyricsFragment).setLyricsDuration(
                    it.progress.toLong()
                )
                binding.nowTime.text = it.nowTime
                binding.totalTime.text = it.allTime
                if (it.musicId != mCurrentMusic?.musicId) {
                    lifecycleScope.launch {
                        initCurrentMusicDetail()
                    }
                }
            }
        }, 100)
    }

    private suspend fun initCurrentMusicDetail() {
        playManager.currentPlayingMusic.let { music ->
            mCurrentMusic = music
            getImageColors(music?.coverImg) { primaryColor, primaryTextColor, secondaryTextColor ->
                binding.blur.setBackgroundColor(primaryColor)
                (fragments[1] as DetailLyricsFragment).setLyricTextColor(
                    primaryTextColor,
                    secondaryTextColor
                )
                (fragments[0] as DetailSongFragment).setPrimaryColor(primaryTextColor)
                binding.detailTab.setTabTextColors(secondaryTextColor, primaryTextColor)
                binding.playProgress.secondaryProgressTintList = ColorStateList.valueOf(primaryTextColor)
                binding.playProgress.backgroundTintList = ColorStateList.valueOf(secondaryTextColor)
                binding.back.setColorFilter(primaryTextColor)
                binding.reload.setColorFilter(primaryTextColor)
                binding.playModel.setColorFilter(primaryTextColor)
                binding.playPrevious.setColorFilter(primaryTextColor)
                binding.buffering.setColorFilter(primaryTextColor)
                binding.togglePlay.setColorFilter(primaryTextColor)
                binding.playNext.setColorFilter(primaryTextColor)
                binding.playList.setColorFilter(primaryTextColor)
                binding.nowTime.setTextColor(primaryTextColor)
                binding.totalTime.setTextColor(primaryTextColor)
            }
            (fragments[0] as DetailSongFragment).initMusicData(music)
            (fragments[1] as DetailLyricsFragment).setLyrics(
                music?.lyrics,
                music?.lyricsTrans
            )
        }
    }

    // 更新缓冲状态
    private fun setBuffering(isBuffering: Boolean) {
        binding.togglePlay.visible(!isBuffering)
        binding.buffering.visible(isBuffering)
        if (isBuffering) {
            if (!animator.isRunning) {
                animator.start()
            } else {
                animator.resume()
            }
        } else {
            if (!animator.isStarted || !animator.isRunning) {
                animator.cancel()
            }
            animator.pause()
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(0, R.anim.activity_close)
    }

    override fun initBinding() = ActivityPlayBinding.inflate(layoutInflater)
}