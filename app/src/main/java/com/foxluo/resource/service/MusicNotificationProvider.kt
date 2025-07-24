package com.foxluo.resource.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.R
import androidx.media3.session.SessionCommand
import com.blankj.utilcode.util.LogUtils
import com.foxluo.baselib.util.TimeUtil.nowTime
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import java.util.Arrays
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor

@UnstableApi
class MusicNotificationProvider(private val context: Context) : MediaNotification.Provider {
    private val TAG="MusicNotificationProvider"
    private val NOW_PLAYING_NOTIFICATION_ID = 0xb339
    private val NOW_PLAYING_CHANNEL_ID = "media.NOW_PLAYING"
    private var notificationManager: NotificationManager? = null
    private var pendingOnBitmapLoadedFutureCallback:  OnBitmapLoadedFutureCallback? =
        null
    init {
        notificationManager = Assertions.checkStateNotNull(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        )
    }

    override fun createNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        // TODO: b/332877990 - More accurately reflect media button preferences in the notification.
        val mediaButtonPreferencesWithEnabledCommandButtonsOnly =
            ImmutableList.Builder<CommandButton>()
        for (i in mediaButtonPreferences.indices) {
            val button = mediaButtonPreferences[i]
            if (button.sessionCommand != null && button.sessionCommand!!.commandCode == SessionCommand.COMMAND_CODE_CUSTOM && button.isEnabled) {
                mediaButtonPreferencesWithEnabledCommandButtonsOnly.add(mediaButtonPreferences[i])
            }
        }
        val player = mediaSession.player
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL_ID)
        val notificationId: Int = NOW_PLAYING_NOTIFICATION_ID
        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
        val compactViewIndices: IntArray =
            addNotificationActions(
                mediaSession,
                getMediaButtons(
                    mediaSession,
                    player.availableCommands,
                    mediaButtonPreferencesWithEnabledCommandButtonsOnly.build(),
                    !Util.shouldShowPlayButton(
                        player, mediaSession.showPlayButtonIfPlaybackIsSuppressed
                    )
                ),
                builder,
                actionFactory
            )
        mediaStyle.setShowActionsInCompactView(*compactViewIndices)
        if (player.isCommandAvailable(Player.COMMAND_GET_METADATA)) {
            val metadata = player.mediaMetadata
            builder
                .setContentTitle(metadata.title)
                .setContentText(metadata.artist)
            val bitmapFuture =
                mediaSession.bitmapLoader.loadBitmapFromMetadata(metadata)
            if (bitmapFuture != null) {
                pendingOnBitmapLoadedFutureCallback?.discardIfPending()
                if (bitmapFuture.isDone) {
                    try {
                        builder.setLargeIcon(Futures.getDone(bitmapFuture))
                    } catch (e: CancellationException) {
                        LogUtils.w(getBitmapLoadErrorMessage(e))
                    } catch (e: ExecutionException) {
                        LogUtils.w( getBitmapLoadErrorMessage(e))
                    }
                } else {
                    pendingOnBitmapLoadedFutureCallback =
                        OnBitmapLoadedFutureCallback(
                            notificationId, builder, onNotificationChangedCallback
                        )
                    pendingOnBitmapLoadedFutureCallback?.let {
                        Futures.addCallback<Bitmap>(
                            bitmapFuture,
                            it,  // This callback must be executed on the next looper iteration, after this method has
                            // returned a media notification.
                            Executor { r: Runnable? ->
                                Handler().apply {
                                    post(r!!)
                                }
                            })
                    }
                }
            }
        }

        val playbackStartTimeMs = getPlaybackStartTimeEpochMs(player)
        val displayElapsedTimeWithChronometer = playbackStartTimeMs != C.TIME_UNSET
        builder
            .setWhen(if (displayElapsedTimeWithChronometer) playbackStartTimeMs else 0L)
            .setShowWhen(displayElapsedTimeWithChronometer)
            .setUsesChronometer(displayElapsedTimeWithChronometer)

        if (Util.SDK_INT >= 31) {
            Api31.setForegroundServiceBehavior(builder)
        }

        val notification: Notification =
            builder
                .setContentIntent(mediaSession.sessionActivity)
                .setDeleteIntent(
                    actionFactory.createMediaActionPendingIntent(
                        mediaSession,
                        Player.COMMAND_STOP.toLong()
                    )
                )
                .setOnlyAlertOnce(true)
                .setSmallIcon(com.foxluo.baselib.R.drawable.exo_notification_small_icon)
                .setStyle(mediaStyle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setGroup(DefaultMediaNotificationProvider.GROUP_KEY)
                .build()
        return MediaNotification(notificationId, notification).apply {
            builder.build()
        }
    }

    protected fun getMediaButtons(
        session: MediaSession?,
        playerCommands: Player.Commands,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        showPauseButton: Boolean
    ): ImmutableList<CommandButton> {
        // Skip to previous action.
        val commandButtons = ImmutableList.Builder<CommandButton>()
        if (playerCommands.containsAny(
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
            )
        ) {
            val commandButtonExtras = Bundle()
            commandButtonExtras.putInt(
                DefaultMediaNotificationProvider.COMMAND_KEY_COMPACT_VIEW_INDEX,
                C.INDEX_UNSET
            )
            commandButtons.add(
                CommandButton.Builder(CommandButton.ICON_PREVIOUS)
                    .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .setDisplayName(
                        context.getString(R.string.media3_controls_seek_to_previous_description)
                    )
                    .setIconResId(com.foxluo.baselib.R.drawable.exo_notification_previous)
                    .setExtras(commandButtonExtras)
                    .build()
            )
        }
        if (playerCommands.contains(Player.COMMAND_PLAY_PAUSE)) {
            val commandButtonExtras = Bundle()
            commandButtonExtras.putInt(
                DefaultMediaNotificationProvider.COMMAND_KEY_COMPACT_VIEW_INDEX,
                C.INDEX_UNSET
            )
            if (showPauseButton) {
                commandButtons.add(
                    CommandButton.Builder(CommandButton.ICON_PAUSE)
                        .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                        .setIconResId(com.foxluo.baselib.R.drawable.exo_notification_play)
                        .setExtras(commandButtonExtras)
                        .setDisplayName(context.getString(R.string.media3_controls_pause_description))
                        .build()
                )
            } else {
                commandButtons.add(
                    CommandButton.Builder(CommandButton.ICON_PLAY)
                        .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                        .setExtras(commandButtonExtras)
                        .setIconResId(com.foxluo.baselib.R.drawable.exo_notification_pause)
                        .setDisplayName(context.getString(R.string.media3_controls_play_description))
                        .build()
                )
            }
        }
        // Skip to next action.
        if (playerCommands.containsAny(
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
            )
        ) {
            val commandButtonExtras = Bundle()
            commandButtonExtras.putInt(
                DefaultMediaNotificationProvider.COMMAND_KEY_COMPACT_VIEW_INDEX,
                C.INDEX_UNSET
            )
            commandButtons.add(
                CommandButton.Builder(CommandButton.ICON_NEXT)
                    .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .setExtras(commandButtonExtras)
                    .setIconResId(com.foxluo.baselib.R.drawable.exo_notification_next)
                    .setDisplayName(context.getString(R.string.media3_controls_seek_to_next_description))
                    .build()
            )
        }
        for (i in mediaButtonPreferences.indices) {
            val button = mediaButtonPreferences[i]
            if (button.sessionCommand != null
                && button.sessionCommand!!.commandCode == SessionCommand.COMMAND_CODE_CUSTOM
            ) {
                commandButtons.add(button)
            }
        }
        return commandButtons.build()
    }

    protected fun addNotificationActions(
        mediaSession: MediaSession?,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {
        var compactViewIndices = IntArray(3)
        val defaultCompactViewIndices = IntArray(3)
        Arrays.fill(compactViewIndices, C.INDEX_UNSET)
        Arrays.fill(defaultCompactViewIndices, C.INDEX_UNSET)
        var compactViewCommandCount = 0
        for (i in mediaButtons.indices) {
            val commandButton = mediaButtons[i]
            if (commandButton.sessionCommand != null) {
                builder.addAction(
                    actionFactory.createCustomActionFromCustomCommandButton(
                        mediaSession!!,
                        commandButton
                    )
                )
            } else {
                Assertions.checkState(commandButton.playerCommand != Player.COMMAND_INVALID)
                builder.addAction(
                    actionFactory.createMediaAction(
                        mediaSession!!,
                        IconCompat.createWithResource(context, commandButton.iconResId),
                        commandButton.displayName,
                        commandButton.playerCommand
                    )
                )
            }
            if (compactViewCommandCount == 3) {
                continue
            }
            val compactViewIndex =
                commandButton.extras.getInt(
                    DefaultMediaNotificationProvider.COMMAND_KEY_COMPACT_VIEW_INDEX,  /* defaultValue= */
                    C.INDEX_UNSET
                )
            if (compactViewIndex >= 0 && compactViewIndex < compactViewIndices.size) {
                compactViewCommandCount++
                compactViewIndices[compactViewIndex] = i
            } else if (commandButton.playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS
                || commandButton.playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
            ) {
                defaultCompactViewIndices[0] = i
            } else if (commandButton.playerCommand == Player.COMMAND_PLAY_PAUSE) {
                defaultCompactViewIndices[1] = i
            } else if (commandButton.playerCommand == Player.COMMAND_SEEK_TO_NEXT
                || commandButton.playerCommand == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
            ) {
                defaultCompactViewIndices[2] = i
            }
        }
        if (compactViewCommandCount == 0) {
            // If there is no custom configuration we use the seekPrev (if any), play/pause (if any),
            // seekNext (if any) action in compact view.
            var indexInCompactViewIndices = 0
            for (i in defaultCompactViewIndices.indices) {
                if (defaultCompactViewIndices[i] == C.INDEX_UNSET) {
                    continue
                }
                compactViewIndices[indexInCompactViewIndices] = defaultCompactViewIndices[i]
                indexInCompactViewIndices++
            }
        }
        var i = 0
        while (i < compactViewIndices.size) {
            if (compactViewIndices[i] == C.INDEX_UNSET) {
                compactViewIndices = compactViewIndices.copyOf(i)
                break
            }
            i++
        }
        return compactViewIndices
    }

    private fun ensureNotificationChannel() {
        if (Util.SDK_INT < 26 || notificationManager?.getNotificationChannel(NOW_PLAYING_CHANNEL_ID) != null) {
            return
        }
        notificationManager?.let {
            Api26.createNotificationChannel(
                it,
                NOW_PLAYING_CHANNEL_ID,
                context.getString(com.foxluo.baselib.R.string.music_service)
            )
        }
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ) = DefaultMediaNotificationProvider(context).handleCustomCommand(session, action, extras)

    private fun getPlaybackStartTimeEpochMs(player: Player): Long {
        if (player.isPlaying
            && !player.isPlayingAd
            && !player.isCurrentMediaItemDynamic
            && player.playbackParameters.speed == 1f
        ) {
            return nowTime - player.contentPosition
        } else {
            return C.TIME_UNSET
        }
    }

    private class OnBitmapLoadedFutureCallback(
        private val notificationId: Int,
        private val builder: NotificationCompat.Builder,
        private val onNotificationChangedCallback: MediaNotification.Provider.Callback
    ) : FutureCallback<Bitmap?> {
        private var discarded = false

        fun discardIfPending() {
            discarded = true
        }

        override fun onSuccess(result: Bitmap?) {
            if (!discarded) {
                builder.setLargeIcon(result)
                onNotificationChangedCallback.onNotificationChanged(
                    MediaNotification(notificationId, builder.build())
                )
            }
        }

        @SuppressLint("Range")
        override fun onFailure(t: Throwable) {
            if (!discarded) {
                Log.w("MusicServiceNotification", "Failed to load bitmap: " + t.message)
            }
        }
    }

    private object Api26 {
        fun createNotificationChannel(
            notificationManager: NotificationManager, channelId: String?, channelName: String?
        ) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            if (Util.SDK_INT <= 27) {
                // API 28+ will automatically hide the app icon 'badge' for notifications using
                // Notification.MediaStyle, but we have to manually hide it for APIs 26 (when badges were
                // added) and 27.
                channel.setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(31)
    private object Api31 {
        fun setForegroundServiceBehavior(builder: NotificationCompat.Builder) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
    }

    private fun getBitmapLoadErrorMessage(throwable: Throwable): String {
        return "Failed to load bitmap: " + throwable.message
    }
}