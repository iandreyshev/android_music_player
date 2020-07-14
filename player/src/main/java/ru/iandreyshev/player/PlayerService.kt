package ru.iandreyshev.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import ru.iandreyshev.player.extensions.artist
import ru.iandreyshev.player.extensions.duration
import ru.iandreyshev.player.extensions.fullDescription
import ru.iandreyshev.player.extensions.title
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PlayerService : Service() {

    lateinit var mediaSession: MediaSessionCompat
        private set

    private lateinit var mNotificationManager: PlayerNotificationManager
    private lateinit var mMediaSessionConnector: MediaSessionConnector

    private val mPlayerListener = PlayerEventListener()
    private val mAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    // TODO: 12-Jul-20 Почему именно такой плеер и что значит EXO ?
    private val mExoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(mAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(mPlayerListener)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = Binder(this)

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // TODO: 12-Jul-20 Для чего используется тег?
        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        val mediaController = MediaControllerCompat(this, mediaSession)

        mNotificationManager = PlayerNotificationManager(
            this,
            mExoPlayer,
            mediaController,
            PlayerNotificationListener()
        )

        // FIXME: 15-Jul-20 Че это ваще такое?
        mMediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            connector.setPlayer(mExoPlayer)
        }
    }

    override fun onDestroy() {
        mediaSession.isActive = false
        mediaSession.release()

        mExoPlayer.removeListener(mPlayerListener)
        mExoPlayer.release()
    }

    fun play(track: PlayerTrack) {
        val mediaMetadata = MediaMetadataCompat.Builder()
            .apply {
                title = track.title
                artist = track.artist
                duration = TimeUnit.SECONDS.toMillis(track.duration)
            }
            .build()
        val rawDataSource = RawResourceDataSource(this)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(track.trackRes)))

        val mediaSource = ProgressiveMediaSource.Factory(DataSource.Factory { rawDataSource })
            .setTag(mediaMetadata.fullDescription)
            .createMediaSource(rawDataSource.uri)

        mNotificationManager.trackAppearance = track

        mExoPlayer.prepare(mediaSource)
        mExoPlayer.playWhenReady = true
    }

    fun playWhenReady() {
        mExoPlayer.playWhenReady = !mExoPlayer.playWhenReady
    }

    fun stop() {
        mExoPlayer.stop()
    }

    private inner class PlayerNotificationListener :
        com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener {

        private var mIsForegroundService = false

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !mIsForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@PlayerService.javaClass)
                )

                startForeground(notificationId, notification)
                mIsForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            mIsForegroundService = false
            stopSelf()
        }
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    mNotificationManager.showNotification()

                    // If playback is paused we remove the foreground state which allows the
                    // notification to be dismissed. An alternative would be to provide a "close"
                    // button in the notification which stops playback and clears the notification.
                    if (playbackState == Player.STATE_READY) {
                        if (!playWhenReady) stopForeground(false)
                    }
                }
                else -> {
                    mNotificationManager.hideNotification()
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            var message = R.string.generic_error
            when (error.type) {
                // If the data from MediaSource object could not be loaded the Exoplayer raises
                // a type_source error.
                // An error message is printed to UI via Toast message to inform the user.
                ExoPlaybackException.TYPE_SOURCE -> {
                    message = R.string.error_media_not_found;
                    Timber.e("TYPE_SOURCE: %s", error.sourceException.message)
                }
                // If the error occurs in a render component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_RENDERER -> {
                    Timber.e("TYPE_RENDERER: %s", error.rendererException.message)
                }
                // If occurs an unexpected RuntimeException Exoplayer raises a type_unexpected error.
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    Timber.e("TYPE_UNEXPECTED: %s", error.unexpectedException.message)
                }
                // Occurs when there is a OutOfMemory error.
                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    Timber.e("TYPE_OUT_OF_MEMORY: %s", error.outOfMemoryError.message)
                }
                // If the error occurs in a remote component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_REMOTE -> {
                    Timber.e("TYPE_REMOTE: %s", error.message)
                }
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mExoPlayer.stop()
    }

    class Binder(
        val service: PlayerService
    ) : android.os.Binder()

}
