package com.foxluo.resource.music.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.loadUrlWithBlur
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.FragmentDetailBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.domain.PlayingInfoManager.RepeatMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator

class DetailFragment : BottomSheetDialogFragment() {
    private var mCurrentMusic: MusicData? = null

    private val tabs by lazy {
        arrayOf(getString(R.string.song), getString(R.string.lyrics))
    }

    private val fragments by lazy {
        arrayOf(
            DetailSongFragment(), DetailLyricsFragment().apply {
                setDragClickCallback {
                    playManager.setSeek(it.toInt())
                }
            }
        )
    }

    private var binding: FragmentDetailBinding? = null

    private val playManager by lazy {
        PlayerManager.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentDetailBinding.inflate(layoutInflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { binding ->
            binding.back.setOnClickListener {
                dismissAllowingStateLoss()
            }
            val adapter = object : FragmentStateAdapter(this) {
                override fun createFragment(position: Int) = fragments[position]

                override fun getItemCount() = fragments.size
            }
            binding.detailViewpager.adapter = adapter
            binding.detailViewpager.isSaveEnabled = false
            binding.detailViewpager.offscreenPageLimit = 2
            TabLayoutMediator(binding.detailTab, binding.detailViewpager) { tab, position ->
                tab.text = tabs[position]
                tab.view.setOnLongClickListener { true }
                tab.view.tooltipText = null
            }.apply {
                this.attach()
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
            binding.detailViewpager.postDelayed({
                playManager.uiStates.observe(this) {
                    it ?: return@observe
                    when (it.repeatMode) {
                        RepeatMode.LIST_CYCLE -> binding.playModel.setImageResource(R.drawable.ic_cycle)
                        RepeatMode.SINGLE_CYCLE -> binding.playModel.setImageResource(R.drawable.ic_single)
                        RepeatMode.RANDOM -> binding.playModel.setImageResource(R.drawable.ic_random)
                    }
                    binding.togglePlay.isSelected = playManager.isPlaying
                    binding.playProgress.progress = it.progress
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
    }

    private fun initCurrentMusicDetail() {
        playManager.currentPlayingMusic.let { music ->
            mCurrentMusic = music
            (fragments[0] as DetailSongFragment).initMusicData(music)
            (fragments[1] as DetailLyricsFragment).setLyrics(
                music?.lyrics,
                music?.lyricsTrans
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setupRatio(requireContext(), dialog as BottomSheetDialog, 100)
    }

    private fun getBottomSheetDialogDefaultHeight(context: Context, percetage: Int): Int {
        return getWindowHeight(context) * percetage / 100
    }

    private fun getWindowHeight(context: Context): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun setupRatio(context: Context, bottomSheetDialog: BottomSheetDialog, percetage: Int) {
        //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        //id = android.support.design.R.id.design_bottom_sheet for support librares
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(context, percetage)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }
}