package com.foxluo.resource.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder
import android.view.KeyEvent
import android.widget.RemoteViews
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.danikula.videocache.HttpProxyCacheServer
import com.foxluo.baselib.ui.BaseApplication.Companion.proxy
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.resource.activity.MainActivity
import com.foxluo.resource.music.R
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.contract.ICacheProxy
import com.foxluo.resource.music.player.contract.IServiceNotifier
import com.foxluo.resource.music.ui.activity.PlayActivity
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class MusicService : Service(), IServiceNotifier, ICacheProxy {

    companion object {
        const val NOTIFICATION_ID = 11123
        const val MUSIC_ACTION_INTENT_FILTER = "MUSIC_ACTION_INTENT_FILTER"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"

        internal annotation class KeyActions {
            //所有keyCode参考:https://www.apiref.com/android-zh/android/view/KeyEvent.html
            companion object {
                var PLAY_ACTION: Int = 126
                var PAUSE_ACTION: Int = 127
                var PREV_ACTION: Int = 88
                var NEXT_ACTION: Int = 87
            }
        }
    }


    private val mMediaSession by lazy {
        MediaSession(this, javaClass.name)
    }

    private val musicActionReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent?) {
                LogUtils.e("广播接收器被触发")
                LogUtils.e("Intent: $intent")
                intent ?: return
                var action = intent.getStringExtra("action")
                when (action) {
                    ACTION_PREV -> playManager.playPrevious()
                    ACTION_NEXT -> playManager.playNext()
                    ACTION_PAUSE -> playManager.pauseAudio()
                    ACTION_PLAY -> playManager.playAudio()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        proxy = HttpProxyCacheServer
            .Builder(this)
            .maxCacheSize(1024 * 1024 * 1024)
            .maxCacheFilesCount(200)
            .build()
        PlayerManager.getInstance().init(this, this, this)
        // 在 Android 13（Tiramisu）及以上版本，需要显式声明 RECEIVER_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                musicActionReceiver,
                IntentFilter().apply {
                    addAction(MUSIC_ACTION_INTENT_FILTER)
                },
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(musicActionReceiver, IntentFilter(MUSIC_ACTION_INTENT_FILTER))
        }
        mMediaSession.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                val keyEvent: KeyEvent =
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        ?: return false
                when (keyEvent.keyCode) {
                    KeyActions.PLAY_ACTION -> playManager.playAudio()
                    KeyActions.PAUSE_ACTION -> playManager.pauseAudio()
                    KeyActions.PREV_ACTION -> playManager.playPrevious()
                    KeyActions.NEXT_ACTION -> playManager.playNext()
                }
                //返回值的作用跟事件分发的原理是一样的,返回true代表事件被消费,其他应用也就收不到了
                return true
            }
        })
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mMediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notifyService(false)
        return START_STICKY
    }

    private val playManager by lazy {
        PlayerManager.getInstance()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    protected fun RemoteViews.loadImage(
        url: String?, id: Int
    ) {
        val awt = AppWidgetTarget(
            this@MusicService,
            id,
            this,
            ComponentName(this@MusicService, javaClass)
        )
        try {
            val defaultCarPic = com.foxluo.baselib.R.mipmap.ic_app
            Glide.with(this@MusicService).asBitmap().load(processUrl(url))
                .apply(
                    RequestOptions()
                        .placeholder(defaultCarPic).error(defaultCarPic)
                )
                .into(awt)
        } catch (e: Exception) {
            LogUtils.d("load image error ${e.message}")
        }
    }

    @SuppressLint("RemoteViewLayout")
    override fun notifyService(startOrStop: Boolean) {
        val remoteViews = RemoteViews(
            packageName,
            R.layout.layout_music_notification
        ).apply {
            val currentMusic = playManager.currentPlayingMusic
            setImageViewResource(
                R.id.play,
                if (playManager.isPlaying)
                    com.foxluo.baselib.R.drawable.iv_pause
                else
                    com.foxluo.baselib.R.drawable.iv_play
            )
            if (currentMusic == null) {
                setTextViewText(R.id.title, "暂未播放任何音乐")
            } else {
                setTextViewText(R.id.title, "${currentMusic.title}-${currentMusic.artist?.name}")
                loadImage(currentMusic.coverImg, R.id.cover)
                setOnClickPendingIntent(R.id.prev, getActionPendingIntent(ACTION_PREV))
                setOnClickPendingIntent(
                    R.id.play,
                    getActionPendingIntent(
                        if (playManager.isPlaying)
                            ACTION_PAUSE
                        else ACTION_PLAY
                    )
                )
                setOnClickPendingIntent(R.id.next, getActionPendingIntent(ACTION_NEXT))
            }
            setOnClickPendingIntent(R.id.root,getJumpPendingIntent())
        }
        val context = this
        val notificationManger =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "charge_status"
        val notificationChannel = NotificationChannel(
            channelId,
            "播放服务",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = "音乐后台播放服务，停止可能导致后台播放异常"
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManger.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(context, channelId)
            .setCustomContentView(remoteViews)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(com.foxluo.baselib.R.mipmap.ic_app)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setLargeIcon(
                AppCompatResources.getDrawable(
                    context,
                    com.foxluo.baselib.R.mipmap.ic_app
                )?.toBitmap()
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 安卓10要添加一个参数，在manifest中配置
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun getJumpPendingIntent(): PendingIntent {
        return PendingIntent.getActivities(
            this,
            105,
            arrayOf(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }, Intent(this, PlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getActionPendingIntent(action: String? = null): PendingIntent {
        val requestCode = when (action) {
            ACTION_PREV -> 101
            ACTION_NEXT -> 102
            ACTION_PLAY -> 103
            ACTION_PAUSE -> 104
            else -> 100
        }
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(MUSIC_ACTION_INTENT_FILTER).apply {
                setPackage(packageName)
            putExtra("action", action)
        }, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun getCacheUrl(url: String?): String? {
        return proxy.getProxyUrl(url)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicActionReceiver)
    }

    override fun getHttpProxy() = proxy
}
