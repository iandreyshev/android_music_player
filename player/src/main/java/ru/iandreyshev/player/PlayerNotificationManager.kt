package ru.iandreyshev.player

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*

class PlayerNotificationManager(
    private val context: Context,
    private val exoPlayer: ExoPlayer,
    // FIXME: 14-Jul-20 Что такое MediaControllerCompat?
    private val mediaController: MediaControllerCompat,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    var trackAppearance: PlayerTrack? = null

    private val mServiceJob = SupervisorJob()
    private val mServiceScope = CoroutineScope(Dispatchers.Main + mServiceJob)

    // TODO: 12-Jul-20 Почему не используется платформенный менеджер?
    private val mExoPlayerNotificationManager: PlayerNotificationManager

    init {
        mExoPlayerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOW_PLAYING_CHANNEL_ID,
            R.string.notification_channel,
            R.string.notification_channel_description,
            DescriptionAdapter(mediaController),
            notificationListener
        ).apply {
            setMediaSessionToken(mediaController.sessionToken)
            setSmallIcon(R.drawable.exo_icon_circular_play)
            setUseNavigationActions(false)

            setRewindIncrementMs(0)
            setFastForwardIncrementMs(0)
        }
    }

    fun hideNotification() {
        mExoPlayerNotificationManager.setPlayer(null)
    }

    fun showNotification() {
        mExoPlayerNotificationManager.setPlayer(exoPlayer)
    }

    private inner class DescriptionAdapter(
        private val controller: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconRes: Int? = null
        var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentText(player: Player) =
            "Content text"

        override fun getCurrentContentTitle(player: Player) =
            trackAppearance?.title.orEmpty()

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconRes = trackAppearance?.iconRes
            return if (currentIconRes != iconRes || currentBitmap == null) {

                // Cache the bitmap for the current song so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconRes = iconRes
                mServiceScope.launch {
                    currentBitmap = iconRes?.let { res ->
                        withContext(Dispatchers.IO) {
                            BitmapFactory.decodeResource(context.resources, res)
                        }
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }
    }

}