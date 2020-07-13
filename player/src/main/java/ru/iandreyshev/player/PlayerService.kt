package ru.iandreyshev.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.ogg.OggExtractor
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import timber.log.Timber


// TODO: 12-Jul-20 Почему не используется обычный Service?
class PlayerService : Service() {

    private lateinit var mNotificationManager: PlayerNotificationManager
    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mMediaSessionConnector: MediaSessionConnector
    private lateinit var mMediaSource: IMediaSource

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
        mMediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        mNotificationManager = PlayerNotificationManager(
            this,
            mExoPlayer,
            mMediaSession.sessionToken,
            PlayerNotificationListener()
        )

//        mMediaSource = object : IMediaSource {
//            override fun get(): MediaMetadataCompat? {
//                return null
//            }
//        }

//        mMediaSessionConnector = MediaSessionConnector(mMediaSession).also { connector ->
//            val dataSourceFactory = DefaultDataSourceFactory(
//                this, Util.getUserAgent(this, MUSIC_PLAYER_USER_AGENT), null
//            )
//
//            // Create the PlaybackPreparer of the media session connector.
//            val playbackPreparer = PlayerPlaybackPreparer(
//                mMediaSource,
//                mExoPlayer,
//                dataSourceFactory
//            )
//
//            connector.setPlayer(mExoPlayer)
//            connector.setPlaybackPreparer(playbackPreparer)
//            connector.setQueueNavigator(QueueNavigator(mMediaSession))
//        }
    }

    fun play(rawId: Int) {
        val dataSpec = DataSpec(RawResourceDataSource.buildRawResourceUri(rawId))
        val dataSource = RawResourceDataSource(this)
        dataSource.open(dataSpec)

        val factory = DataSource.Factory { dataSource }

        val audioSource = ProgressiveMediaSource
            .Factory(factory, OggExtractor.FACTORY)
            .createMediaSource(dataSource.uri)
        val loopingMediaSource = LoopingMediaSource(audioSource)

        mExoPlayer.prepare(loopingMediaSource)
        mExoPlayer.playWhenReady = true
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

    class Binder(
        val service: PlayerService
    ) : android.os.Binder()

    companion object {
        private const val MUSIC_PLAYER_USER_AGENT = "iandreyshev.music.player"
    }

}

private class QueueNavigator(
    mediaSession: MediaSessionCompat
) : TimelineQueueNavigator(mediaSession) {
    private val window = Timeline.Window()
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
        player.currentTimeline
            .getWindow(windowIndex, window).tag as MediaDescriptionCompat
}
