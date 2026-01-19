package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ConvertUtils
import com.dirror.lyricviewx.OnPlayClickListener
import com.dirror.lyricviewx.OnSingleClickListener
import com.foxluo.baselib.R
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.ViewExt.getLyricViewTouchEventListener
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.databinding.FragmentDetailLrcBinding
import com.foxluo.resource.music.databinding.LayoutLyricSettingsBinding
import com.foxluo.resource.music.lyric.manager.LyricStyleManager
import com.foxluo.resource.music.lyric.manager.LyricSyncManager
import com.foxluo.resource.music.lyric.ui.LyricSettingsExt.setListener
import kotlinx.coroutines.launch

class DetailLyricsFragment : BaseBindingFragment<FragmentDetailLrcBinding>() {
    var targetPage: (() -> Unit)? = null

    private var showTansLrc = false

    private var lyrics: String? = null

    private var lyricTrans: String? = null

    private var currentPlay: ((duration: Long) -> Unit)? = null

    private val lyricSyncManager by lazy {
        LyricSyncManager.getInstance()
    }

    override fun initBinding(): FragmentDetailLrcBinding {
        return FragmentDetailLrcBinding.inflate(layoutInflater)
    }

    fun setLyricTextColor(primaryColor: Int, secondaryColor: Int, fontSize: Float) {
        lifecycleScope.launchWhenResumed {
            binding.lyricView.setNormalColor(secondaryColor)
            binding.lyricView.setCurrentColor(primaryColor)
            binding.lyricView.setTimelineColor(primaryColor)
            binding.lyricView.setTimeTextColor(primaryColor)
            binding.lyricView.setTimelineTextColor(primaryColor)
            binding.lyricView.setNormalTextSize(ConvertUtils.sp2px(fontSize).toFloat())
            binding.lyricView.setCurrentTextSize(ConvertUtils.sp2px(fontSize).toFloat())
        }
    }

    fun setLyrics(lyrics: String?, lyricTrans: String?) {
        this.lyrics = lyrics
        this.lyricTrans = lyricTrans
        showTansLrc = lyricTrans.isNullOrEmpty().not()
        lifecycleScope.launchWhenStarted {
            if (showTansLrc && lyricTrans.isNullOrEmpty().not()) {
                binding.lyricView.loadLyric(lyrics, lyricTrans)
            } else {
                binding.lyricView.loadLyric(lyrics ?: "")
            }
            binding.trans.visible(lyricTrans.isNullOrEmpty().not())
        }
    }

    fun setLyricsDuration(duration: Long) {
        lifecycleScope.launchWhenStarted {
            binding.lyricView.updateTime(duration, true)
        }
    }

    fun setDragClickCallback(currentPlay: (duration: Long) -> Unit) {
        this.currentPlay = currentPlay
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            lyricSyncManager.isDesktopLyricLocked.collect {
                if (!it) {
                    binding.desktopLyric.setImageResource(R.drawable.ic_unlock)
                } else {
                    binding.desktopLyric.setImageResource(R.drawable.ic_lock)
                }
            }
        }
        lifecycleScope.launch {
            lyricSyncManager.isDesktopLyricEnabled.collect {
                if (!it) {
                    binding.desktopLyric.setImageResource(R.drawable.ic_lyric)
                } else {
                    if (!lyricSyncManager.isDesktopLyricLocked.value) {
                        binding.desktopLyric.setImageResource(R.drawable.ic_unlock)
                    } else {
                        binding.desktopLyric.setImageResource(R.drawable.ic_lock)
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {
        binding.root.setOnClickListener {
            targetPage?.invoke()
        }
        binding.lyricView.setDraggable(true, object : OnPlayClickListener {
            override fun onPlayClick(time: Long): Boolean {
                currentPlay?.invoke(time)
                return true
            }
        })
        binding.lyricView.setOnSingerClickListener(object : OnSingleClickListener {
            override fun onClick() {
                if (binding.settings.isVisible.not()) {
                    targetPage?.invoke()
                } else {
                    binding.settings.visible(false)
                }
            }
        })
        binding.lyricView.setOnTouchListener(getLyricViewTouchEventListener())
        binding.trans.setOnClickListener {
            showTansLrc = !showTansLrc
            binding.transText.setText(
                if (!showTansLrc) {
                    R.string.trans_chinese
                } else {
                    R.string.trans_original
                }
            )
            if (showTansLrc) {
                binding.lyricView.loadLyric(lyrics, lyricTrans)
            } else {
                binding.lyricView.loadLyric(lyrics)
            }
        }
        binding.desktopLyric.setOnClickListener {
            if (lyricSyncManager.isDesktopLyricEnabled.value.not()) {
                lyricSyncManager.openDesktopLyric(requireContext())
            } else {
                lyricSyncManager.toggleLock()
            }
        }
        binding.lyricSetting.setOnClickListener {
            binding.settings.visible(!(binding.settings.isVisible))
        }
        val layoutSettings = LayoutLyricSettingsBinding.bind(binding.root)
        layoutSettings.setListener(LyricStyleManager.getInstance())
    }
}