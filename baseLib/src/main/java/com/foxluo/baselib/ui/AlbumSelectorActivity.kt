package com.foxluo.baselib.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.R
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.foxluo.baselib.databinding.ActivityAlbumSelectorBinding
import com.foxluo.baselib.ui.adapter.AlbumAdapter
import com.foxluo.baselib.ui.adapter.AlbumAdapter.Image
import com.foxluo.baselib.ui.adapter.GridDividerItemDecoration
import com.foxluo.baselib.util.ContentResolverHelper.loadImages
import com.foxluo.baselib.util.CropImageContract
import com.foxluo.baselib.util.CropImageResult
import com.foxluo.baselib.util.ImageExt.loadUri
import com.foxluo.baselib.util.ViewExt.gone
import com.foxluo.baselib.util.ViewExt.visible
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.launch

class AlbumSelectorActivity : BaseBindingActivity<ActivityAlbumSelectorBinding>() {
    private var currentImage: Image? = null
    private val adapter by lazy {
        AlbumAdapter()
    }

    private val bufferingAnimator by lazy {
        ObjectAnimator.ofFloat(binding.buffering, "rotation", 0.0F, 360.0F).apply {
            setDuration(1000)
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            initData()
        }

    private val cropImageContract =
        registerForActivityResult(CropImageContract()) { uri ->
            uri?.let {
                currentImage?.isCropped = true
                currentImage?.croppedUri = it
                binding.preview.loadUri(it)
            }
        }

    override fun initBinding() = ActivityAlbumSelectorBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View? {
        return binding.main
    }

    override fun initView() {
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItem(position) is AlbumAdapter.Title) {
                        3
                    } else {
                        1
                    }
                }
            }
        }
        binding.playView.player = ExoPlayer.Builder(this).build()
        binding.playView.player?.run {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    binding.togglePlay.setImageResource(if (isPlaying) R.drawable.media3_icon_pause else R.drawable.media3_icon_play)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    binding.togglePlay.visible(playbackState != Player.STATE_BUFFERING)
                    if (playbackState == Player.STATE_ENDED) {
                        currentImage?.let { readyPlay(it) }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    toast(error.message?.toString() ?: "播放错误")
                }
            })
            repeatMode = Player.REPEAT_MODE_OFF
        }
        binding.recyclerView.addItemDecoration(
            GridDividerItemDecoration(dp2px(1.5f))
        )
        binding.recyclerView.adapter = adapter
    }

    // 更新缓冲状态
    private fun setBuffering(isBuffering: Boolean) {
        binding.togglePlay.visible(!isBuffering)
        binding.buffering.visible(isBuffering)
        val animator = bufferingAnimator
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

    override fun initData() {
        checkPermission()
        lifecycleScope.launch {
            contentResolver.loadImages {
                adapter.imageList = it
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_VIDEO
            ) == PERMISSION_GRANTED)
        ) {
            // Full access on Android 13 (API level 33) or higher
            binding.cardLayout.visibility = View.GONE
        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_VISUAL_USER_SELECTED
            ) == PERMISSION_GRANTED
        ) {
            // Partial access on Android 14 (API level 34) or higher
            binding.textView.text = "你已授权访问部分相册的照片和视频"
            binding.button.text = "管理"
            binding.cardLayout.visibility = View.VISIBLE
        } else if (ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        ) {
            // Full access up to Android 12 (API level 32)
            binding.cardLayout.visibility = View.GONE
        } else {
            // Access denied
            binding.textView.text = "你还未授权访问相册的照片和视频"
            binding.button.text = "请求"
            binding.cardLayout.visibility = View.VISIBLE
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionLauncher.launch(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO
                )
            )
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
        } else {
            permissionLauncher.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    override fun initListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentImage != null) {
                    binding.preImageView.gone()
                    binding.prePlayView.gone()
                    if (currentImage?.isVideo == true) {
                        binding.playView.player?.stop()
                    }
                    binding.sure.isEnabled = false
                    currentImage = null
                } else {
                    finish()
                }
            }
        })
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.togglePlay.setOnClickListener {
            binding.playView.player?.run {
                if (isPlaying) {
                    pause()
                } else {
                    play()
                }
            }
        }
        binding.crop.setOnClickListener {
            currentImage?.uri?.let { uri ->
                cropImageContract.launch(CropImageResult(uri, 0f, 0f))
            }
        }
        binding.button.setOnClickListener {
            requestPermissions()
        }
        adapter.setOnItemClickListener { _, item, _ ->
            if (item is Image) {
                currentImage = item
                if (item.isVideo) {
                    binding.prePlayView.visible()
                    readyPlay(item)
                } else {
                    binding.preImageView.visible()
                    binding.preview.loadUri(item.uri)
                }
                binding.sure.isEnabled = true
            }
        }
        binding.sure.setOnClickListener {
            setResult(RESULT_OK, Intent().apply {
                if (currentImage?.isCropped==true){
                    currentImage?.croppedUri?.let { currentImage?.uri = it }
                }
                setData(currentImage?.uri)
                putExtra("image", currentImage)
            })
            finish()
        }
    }

    private fun readyPlay(item: Image) {
        binding.playView.player?.run {
            stop()
            setMediaItem(MediaItem.fromUri(item.uri))
            playWhenReady = false
            prepare()
        }
    }

    override fun onResume() {
        super.onResume()
        // 恢复播放
        binding.playView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 暂停播放
        binding.playView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放播放器资源
        binding.playView.player?.release()
        binding.playView.player = null
    }
}
