package com.foxluo.resource.service

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.blankj.utilcode.util.LogUtils
import com.danikula.videocache.HttpProxyCacheServer
import com.foxluo.baselib.R
import com.foxluo.resource.activity.MainActivity
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.contract.ICacheProxy
import com.foxluo.resource.music.ui.activity.PlayActivity


class MusicService : MediaSessionService(), ICacheProxy {
    companion object{
        private val NOW_PLAYING_NOTIFICATION_ID = 0xb339
        private val NOW_PLAYING_CHANNEL_ID = "media.NOW_PLAYING"
        const val MUSIC_ACTION_INTENT_FILTER = "MUSIC_ACTION_INTENT_FILTER"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    private val playManager by lazy {
        PlayerManager.getInstance()
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
                    ACTION_PAUSE,ACTION_PLAY -> playManager.togglePlay()
                }
            }
        }
    }

    private val proxy by lazy {
        HttpProxyCacheServer
            .Builder(this)
            .maxCacheSize(1024 * 1024 * 1024)
            .maxCacheFilesCount(200)
            .build()
    }

    private var mediaSession: MediaSession? = null

    val notificationManager by lazy {
        PlaybackNotification(this, mediaSession!!.token, mediaSession!!.player)
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val mPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(audioAttributes, true)
            .build()
        val commandButtonExtras = Bundle()
        commandButtonExtras.putInt(
            DefaultMediaNotificationProvider.COMMAND_KEY_COMPACT_VIEW_INDEX,
            C.INDEX_UNSET
        )
        mediaSession = MediaSession.Builder(this, mPlayer)
            .setSessionActivity(getJumpPendingIntent())
            .build()
        PlayerManager.getInstance().init(mPlayer, this)
        // 在 Android 13（Tiramisu）及以上版本，需要显式声明 RECEIVER_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                musicActionReceiver,
                IntentFilter().apply {
                    addAction(MUSIC_ACTION_INTENT_FILTER)
                },
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(musicActionReceiver, IntentFilter(MUSIC_ACTION_INTENT_FILTER))
        }
        val notificationProvider = DefaultMediaNotificationProvider
            .Builder(this)
            .setNotificationId(NOW_PLAYING_NOTIFICATION_ID)
            .setChannelId(NOW_PLAYING_CHANNEL_ID)
            .setChannelName(R.string.music_service)
            .build().apply {
                setSmallIcon(R.drawable.exo_notification_small_icon)
            }
        setMediaNotificationProvider(notificationProvider)
    }

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {

    }

    private fun getJumpPendingIntent(): PendingIntent {
        val activityOptions =
            ActivityOptions.makeCustomAnimation(this, R.anim.activity_open, 0)
        return PendingIntent.getActivities(
            this,
            System.currentTimeMillis().toInt(),
            arrayOf(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }, Intent(this, PlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }),
            PendingIntent.FLAG_CANCEL_CURRENT or
                    PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE, activityOptions.toBundle()
        )
    }
    override fun getCacheUrl(url: String?): String? {
        return proxy.getProxyUrl(url)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun getHttpProxy() = proxy
}
