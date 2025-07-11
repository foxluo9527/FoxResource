package com.foxluo.resource.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.PendingIntent
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.ConvertUtils.px2dp
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.util.Constant
import com.foxluo.baselib.util.ImageExt.loadUrlWithCircle
import com.foxluo.chat.ui.ChatFragment
import com.foxluo.home.ui.HomeFragment
import com.foxluo.mine.ui.fragment.MineFragment
import com.foxluo.resource.App
import com.foxluo.resource.R
import com.foxluo.resource.community.ui.CommunityFragment
import com.foxluo.resource.databinding.ActivityMainBinding
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.activity.PlayActivity
import com.foxluo.resource.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


const val PERMISSIONS_REQUEST_FOREGROUND_SERVICE = 2333

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {
    private val fragments by lazy {
        listOf(HomeFragment(), ChatFragment(), CommunityFragment(), MineFragment())
    }

    private var controller: MediaController? = null

    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }

    private val animator by lazy {
        ObjectAnimator.ofFloat(binding.playCover, "rotation", 0.0F, 360.0F).apply {
            setDuration(10 * 1000)
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
    }


    override fun initView() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }
        binding.fragmentContainer.adapter = adapter
        binding.fragmentContainer.isUserInputEnabled = false
        binding.playCover.loadUrlWithCircle(null)
        XXPermissions.with(this) // 申请安装包权限
            //.permission(Permission.REQUEST_INSTALL_PACKAGES)
            // 申请悬浮窗权限
            .permission(Permission.SYSTEM_ALERT_WINDOW)
            // 申请通知栏权限
            //.permission(Permission.NOTIFICATION_SERVICE)
            // 申请系统设置权限
            //.permission(Permission.WRITE_SETTINGS)
            // 申请单个权限
            //.permission(Permission.RECORD_AUDIO) // 申请多个权限
            //.permission(Permission.Group.CALENDAR)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    toast("弹窗权限被拒绝，可能导致状态栏跳转异常")
                }
            })
    }

    val sessionActivityPendingIntent: PendingIntent?
        get() = packageManager?.getLaunchIntentForPackage(packageName)
            ?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    887,
                    sessionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

    override fun onDestroy() {
        super.onDestroy()
        controller?.release()
    }

    override fun initListener() {
        binding.navBottom.setOnItemSelectedListener { item ->
            binding.fragmentContainer.currentItem = when (item.itemId) {
                R.id.item_home -> 0
                R.id.item_chat -> 1
                R.id.item_group -> 2
                else -> 3
            }
            val currentFragment = fragments[binding.fragmentContainer.currentItem]
            (currentFragment as? MainPageFragment<*>)?.let {
                if (!(currentFragment.showPlayView())) {
                    binding.playView.visibility = View.INVISIBLE
                    return@let
                }
                binding.playView.visibility = View.VISIBLE
                binding.navBottom.post {
                    binding.playView.setDragPadding(
                        currentFragment.leftPlayPadding,
                        statusBarHeight + currentFragment.topPlayPadding,
                        currentFragment.rightPlayPadding,
                        px2dp(binding.navBottom.height.toFloat()) + currentFragment.bottomPlayPadding + 10
                    )
                }
            }
            true
        }
        val menuView = binding.navBottom.getChildAt(0) as? ViewGroup
        menuView?.post {
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i)
                item.setOnLongClickListener { // 返回true表示消费了事件，不会显示Toast
                    true
                }
            }
        }
        binding.playView.setOnClickListener {
            PlayActivity.startPlayDetail(this)
        }
        binding.playState.setOnClickListener {
            PlayerManager.getInstance().togglePlay()
        }
    }

    override fun initObserver() {
        PlayerManager.getInstance().uiStates.observeForever {
            val isPlaying = it != null && it.isPaused == false && it.isBuffering == false
            val isMusicChanged =
                it != null && it.musicId != musicViewModel.currentMusic.value?.musicId
            if (isMusicChanged && AuthManager.isLogin()) {
                musicViewModel.recordPlayMusicChanged(it.musicId)
            }
            lifecycleScope.launchWhenResumed {
                setPlaying(isPlaying)
            }
            if (isMusicChanged) {
                val currentMusic =
                    PlayerManager.getInstance().currentPlayingMusic ?: return@observeForever
                CoroutineScope(Dispatchers.IO).launch {
                    //更新数据库中正在播放的位置
                    val album = PlayerManager.getInstance().album ?: return@launch
                    album.curMusicId = currentMusic.musicId.toInt()
                    val dao = App.db.albumDao()
                    dao.updateAlbum(album)
                }
                musicViewModel.currentMusic.value = currentMusic
            }
        }
        musicViewModel.currentMusic.observe(this) { currentMusic ->
            currentMusic ?: return@observe
            binding.playCover.loadUrlWithCircle(currentMusic.coverImg)
            if (musicViewModel.isCurrentMusicByUser) {
                PlayActivity.startPlayDetail(this)
                musicViewModel.isCurrentMusicByUser = false
            }
        }
    }

    override fun initData() {
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
            },
            MoreExecutors.directExecutor()
        )
        PlayerManager.getInstance().setInitCallback {
            runBlocking{
                val dao = App.db.albumDao()
                val album = dao.getAlbumWithMusics(Constant.TABLE_ALBUM_PLAYING_ID.toString())
                album.autoPlay = false
                PlayerManager.getInstance().loadAlbum(album, false)
            }
        }
    }

    override fun initBinding() = ActivityMainBinding.inflate(layoutInflater)

    // 更新播放状态
    private fun setPlaying(isPlaying: Boolean) {
        binding.playState.isSelected = isPlaying
        if (isPlaying) {
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
}