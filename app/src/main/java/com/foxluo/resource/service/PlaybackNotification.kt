package com.foxluo.resource.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.RemoteViews
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.foxluo.baselib.R
import com.foxluo.resource.service.MusicService.Companion.ACTION_NEXT
import com.foxluo.resource.service.MusicService.Companion.ACTION_PAUSE
import com.foxluo.resource.service.MusicService.Companion.ACTION_PLAY
import com.foxluo.resource.service.MusicService.Companion.ACTION_PREV
import com.foxluo.resource.service.MusicService.Companion.MUSIC_ACTION_INTENT_FILTER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A wrapper class for ExoPlayer's PlayerNotificationManager.
 * It sets up the notification shown to the user during audio playback and provides track metadata,
 * such as track title and icon image.
 * @param context The context used to create the notification.
 * @param sessionToken The session token used to build MediaController.
 * @param player The ExoPlayer instance.
 * @param notificationListener The listener for notification events.
 */

class PlaybackNotification(
    private val context: Context,
    sessionToken: SessionToken,
    private val player: Player
) {
    /**
     * The channel ID for the notification.
     */
    private val NOW_PLAYING_CHANNEL_ID = "media.NOW_PLAYING"

    /**
     * The notification ID.
     */
    private val NOW_PLAYING_NOTIFICATION_ID = 0xb339
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val notificationManager: NotificationManager? = null
    private val mediaController = MediaController.Builder(context, sessionToken).buildAsync()

    /**
     * Hides the notification.
     */
    fun hideNotification() {
        notificationManager?.cancel(NOW_PLAYING_NOTIFICATION_ID)
    }

    /**
     * Shows the notification for the given player.
     * @param player The player instance for which the notification is shown.
     */
    fun showNotificationForPlayer() {
        serviceScope.launch {
            delay(100)
            hideNotification()
            val remoteViews = RemoteViews(
                context.packageName,
                com.foxluo.resource.music.R.layout.layout_music_notification
            ).apply {
                val isPlaying = mediaController.get().isPlaying
                val currentMusic = mediaController.get().mediaMetadata
                setImageViewResource(
                    com.foxluo.resource.music.R.id.play,
                    if (!isPlaying)
                        R.drawable.exo_notification_play
                    else
                        R.drawable.exo_notification_pause
                )
                setOnClickPendingIntent(
                    com.foxluo.resource.music.R.id.root,
                    mediaController.get().sessionActivity
                )
                setTextViewText(
                    com.foxluo.resource.music.R.id.title,
                    "${currentMusic.title ?: "未知曲名"}-${currentMusic.artist ?: "未知艺术家"}"
                )
                setImageViewBitmap(
                    com.foxluo.resource.music.R.id.cover,
                    resolveUriAsBitmap(currentMusic.artworkUri)
                )
                setOnClickPendingIntent(
                    com.foxluo.resource.music.R.id.prev,
                    getActionPendingIntent(ACTION_PREV)
                )
                setOnClickPendingIntent(
                    com.foxluo.resource.music.R.id.play,
                    getActionPendingIntent(
                        if (isPlaying)
                            ACTION_PAUSE
                        else
                            ACTION_PLAY
                    )
                )
                setOnClickPendingIntent(
                    com.foxluo.resource.music.R.id.next,
                    getActionPendingIntent(ACTION_NEXT)
                )
            }
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
                .setSmallIcon(R.drawable.exo_notification_small_icon)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            notificationManger.notify(NOW_PLAYING_NOTIFICATION_ID, notification)
        }
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
            context,
            requestCode,
            Intent(MUSIC_ACTION_INTENT_FILTER).apply {
                putExtra("action", action)
            }, PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun resolveUriAsBitmap(uri: Uri?): Bitmap? {
        return withContext(Dispatchers.IO) {
            runCatching {
                Glide.with(context).asBitmap().load(uri ?: R.mipmap.ic_app).submit().get()
            }.getOrNull()
        }
    }
}