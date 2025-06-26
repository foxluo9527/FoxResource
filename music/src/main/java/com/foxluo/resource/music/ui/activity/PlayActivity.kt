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
import androidx.viewpager2.widget.ViewPager2
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.BitmapUtil
import com.foxluo.baselib.util.BitmapUtil.ColorFilterCallback
import com.foxluo.baselib.util.BitmapUtil.getImageColors
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.databinding.ActivityPlayBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.domain.PlayingInfoManager.RepeatMode
import com.foxluo.resource.music.ui.adapter.PlayMusicAdapter
import com.foxluo.resource.music.ui.dialog.PlayListDialog
import com.foxluo.resource.music.ui.fragment.DetailLyricsFragment
import com.foxluo.resource.music.ui.fragment.DetailSongFragment
import com.foxluo.resource.music.ui.fragment.PlayFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PlayActivity : BaseBindingActivity<ActivityPlayBinding>(), ColorFilterCallback{
    companion object {
        fun startPlayDetail(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, PlayActivity::class.java))
            activity.overridePendingTransition(R.anim.activity_open, 0)

        }
    }

    private val adapter by lazy {
        PlayMusicAdapter(this, playManager.albumMusics)
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

    private val playManager by lazy {
        PlayerManager.getInstance()
    }

    private var mCurrentMusicFragment: PlayFragment? = null

    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }

    private var onTouchSeekBar = false

    override fun initView() {
        binding.viewPager.apply {
            adapter = this@PlayActivity.adapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 3
        }
    }

    override fun initData() {
        playManager.currentPlayingMusic?.let {
            binding.viewPager.setCurrentItem(
                adapter.getFragmentPositionByMusicId(it.musicId),
                false
            )
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
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (mCurrentMusicFragment != null) {
                    playManager.playAudio(position)
                }
                mCurrentMusicFragment?.onDetached()
                mCurrentMusicFragment = adapter.getFragment(position)
                mCurrentMusicFragment?.setPlayCallback(this@PlayActivity)
            }
        })
    }

    override fun initObserver() {
        binding.viewPager.postDelayed({
            playManager.uiStates.observe(this) {
                it ?: return@observe
                when (it.repeatMode) {
                    RepeatMode.LIST_CYCLE -> binding.playModel.setImageResource(R.drawable.ic_cycle)
                    RepeatMode.SINGLE_CYCLE -> binding.playModel.setImageResource(R.drawable.ic_single)
                    RepeatMode.RANDOM -> binding.playModel.setImageResource(R.drawable.ic_random)
                }
                binding.togglePlay.isSelected = playManager.isPlaying
                if (!onTouchSeekBar) {
                    binding.playProgress.progress = it.progress
                }
                binding.playProgress.secondaryProgress =
                    it.duration / 100 * it.cacheBufferProgress//这里的进度是百分比进度，转换对应秒数
                binding.playProgress.max = it.duration
                setBuffering(it.isBuffering)
                binding.nowTime.text = it.nowTime
                binding.totalTime.text = it.allTime
                mCurrentMusicFragment?.let { fragment ->
                    fragment.updatePlayState(it)
                    if (it.musicId != fragment.currentMusicData?.musicId) {
                        val newMusicIndex = adapter.getFragmentPositionByMusicId(it.musicId)
                        val smoothScroll =
                            abs(newMusicIndex - adapter.getFragmentIndex(fragment)) <= 1
                        //如果超过一个位置，则不滚动切换
                        binding.viewPager.setCurrentItem(newMusicIndex, smoothScroll)
                    }
                }
            }
        }, 100)
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

    override suspend fun onColorFilterChanged(
        primaryColor: Int,
        primaryTextColor: Int,
        secondaryTextColor: Int
    ) {
        withContext(Dispatchers.Main) {
            binding.blur.setBackgroundColor(primaryColor)
            binding.playProgress.secondaryProgressTintList =
                ColorStateList.valueOf(primaryTextColor)
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
    }
}

