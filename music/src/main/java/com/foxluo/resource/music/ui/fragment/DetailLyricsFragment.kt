package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import com.dirror.lyricviewx.OnPlayClickListener
import com.dirror.lyricviewx.OnSingleClickListener
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.resource.music.databinding.FragmentDetailLrcBinding
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ViewExt.getLyricViewTouchEventListener
import com.foxluo.baselib.util.ViewExt.visible

class DetailLyricsFragment : BaseBindingFragment<FragmentDetailLrcBinding>() {
    var targetPage: (() -> Unit)? = null

    private var showTansLrc = false

    private var lyrics: String? = null

    private var lyricTrans: String? = null

    private var currentPlay: ((duration: Long) -> Unit)? = null

    override fun initBinding(): FragmentDetailLrcBinding {
        return FragmentDetailLrcBinding.inflate(layoutInflater)
    }

    fun setLyricTextColor(primaryColor: Int, secondaryColor: Int) {
        binding.lyricView.setNormalColor(secondaryColor)
        binding.lyricView.setCurrentColor(primaryColor)
        binding.lyricView.setTimelineColor(primaryColor)
        binding.lyricView.setTimeTextColor(primaryColor)
        binding.lyricView.setTimelineTextColor(primaryColor)
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
                targetPage?.invoke()
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
    }
}