package ru.iandreyshev.player

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*

class PlayerNotificationManager(
    private val context: Context,
    private val player: ExoPlayer,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val mServiceJob = SupervisorJob()
    private val mServiceScope = CoroutineScope(Dispatchers.Main + mServiceJob)

    // TODO: 12-Jul-20 Почему не используется платформенный менеджер?
    private val mExoPlayerNotificationManager: PlayerNotificationManager

    init {
        // TODO: 12-Jul-20 Узнать, что такое MediaController
        val mediaController = MediaControllerCompat(context, sessionToken)

        mExoPlayerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOW_PLAYING_CHANNEL_ID,
            R.string.notification_channel,
            R.string.notification_channel_description,
            DescriptionAdapter(mediaController),
            notificationListener
        ).apply {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.exo_icon_circular_play)

            setRewindIncrementMs(0)
            setFastForwardIncrementMs(0)
        }
    }

    fun hideNotification() {
        mExoPlayerNotificationManager.setPlayer(null)
    }

    fun showNotification() {
        mExoPlayerNotificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val controller: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentText(player: Player) =
            controller.metadata.description.subtitle.toString()

        override fun getCurrentContentTitle(player: Player) =
            controller.metadata.description.title.toString()

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = controller.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {

                // Cache the bitmap for the current song so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconUri = iconUri
                mServiceScope.launch {
                    currentBitmap = iconUri?.let {
                        resolveUriAsBitmap(it)
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
            return withContext(Dispatchers.IO) {
                val parcelFileDescriptor =
                    context.contentResolver.openFileDescriptor(uri, MODE_READ_ONLY)
                        ?: return@withContext null
                val fileDescriptor = parcelFileDescriptor.fileDescriptor
                BitmapFactory.decodeFileDescriptor(fileDescriptor).apply {
                    parcelFileDescriptor.close()
                }
            }
        }
    }

    companion object {
        private const val MODE_READ_ONLY = "r"
    }

}