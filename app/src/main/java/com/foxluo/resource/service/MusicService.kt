package com.foxluo.resource.service

import android.annotation.SuppressLint
import android.app.ActivityOptions
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
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSessionService
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.danikula.videocache.HttpProxyCacheServer
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.resource.activity.MainActivity
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.player.contract.ICacheProxy
import com.foxluo.resource.music.player.contract.IServiceNotifier
import com.foxluo.resource.music.ui.activity.PlayActivity


class MusicService : MediaSessionService(), IServiceNotifier, ICacheProxy {
    private val proxy by lazy {
        HttpProxyCacheServer
            .Builder(this)
            .maxCacheSize(1024 * 1024 * 1024)
            .maxCacheFilesCount(200)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        PlayerManager.getInstance().init(this, this, this)
    }

    private val playManager by lazy {
        PlayerManager.getInstance()
    }

    override fun onGetSession(p0: androidx.media3.session.MediaSession.ControllerInfo): androidx.media3.session.MediaSession? {
        return playManager.mediaSession
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
        super.onDestroy()
        playManager.clear()
    }

    override fun getHttpProxy() = proxy

    override fun notifyService(startOrStop: Boolean) {

    }
}
