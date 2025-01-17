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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.ConvertUtils.px2dp
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.util.ImageExt.loadUrlWithCircle
import com.foxluo.chat.ui.ChatFragment
import com.foxluo.home.ui.HomeFragment
import com.foxluo.mine.ui.MineFragment
import com.foxluo.resource.R
import com.foxluo.resource.community.ui.CommunityFragment
import com.foxluo.resource.databinding.ActivityMainBinding
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.domain.PlayerController
import com.foxluo.resource.music.ui.activity.PlayActivity
import com.foxluo.resource.service.MusicService
import com.foxluo.resource.service.PlaybackNotification
import com.google.common.util.concurrent.MoreExecutors
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xuexiang.xui.utils.XToastUtils.toast


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
//        startForegroundService(Intent(this, MusicService::class.java))
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

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
            },
            MoreExecutors.directExecutor()
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
                if (!(currentFragment.showPlaView())) {
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
        binding.playState.setOnClickListener{
            PlayerManager.getInstance().togglePlay()
        }
    }

    override fun initObserver() {
        musicViewModel.playingAlbum.observeForever {
            PlayerManager.getInstance().loadAlbum(it.first, it.second)
        }
        PlayerManager.getInstance().uiStates.observe(this) {
            val isPlaying = it != null && it.isPaused == false
            setPlaying(isPlaying)
            if (it != null && it.musicId != musicViewModel.currentMusic.value?.musicId) {
                musicViewModel.currentMusic.value = PlayerManager.getInstance().currentPlayingMusic
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