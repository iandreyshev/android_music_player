package ru.iandreyshev.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.musicplayer.domain.IMusicPlayer
import ru.iandreyshev.musicplayer.domain.PlaybackState
import ru.iandreyshev.musicplayer.domain.PlaybackTrack
import ru.iandreyshev.musicplayer.utils.asPlaybackTrack
import ru.iandreyshev.musicplayer.utils.currentPlayBackPosition
import ru.iandreyshev.musicplayer.utils.isDisabled
import ru.iandreyshev.musicplayer.utils.isPlaying
import ru.iandreyshev.player.PlayerService
import ru.iandreyshev.player.PlayerTrack

class MusicServiceConnection(
    applicationContext: Context
) : IMusicPlayer {

    val isConnected = MutableLiveData(false)
    val playbackState = MutableLiveData(PlaybackState.DISABLED)
    val playbackTrack = MutableLiveData<PlaybackTrack>(null)
    val playbackPosition = MutableLiveData(0L)

    private var mService: PlayerService? = null
    private var mMediaController: MediaControllerCompat? = null
    private var mPlaybackState: PlaybackStateCompat? = null
    private var mCheckPlaybackPositionJob: Job? = null

    private val mMediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            playbackTrack.postValue(
                metadata?.asPlaybackTrack(applicationContext.resources)
                    ?: PlaybackTrack.NothingPlaying
            )
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(
                when (state) {
                    null -> PlaybackState.DISABLED
                    else -> when {
                        state.isDisabled -> PlaybackState.DISABLED
                        state.isPlaying -> PlaybackState.CAN_PAUSE
                        else -> PlaybackState.CAN_PLAY
                    }
                }
            )
            mPlaybackState = state
        }

        override fun onSessionDestroyed() {
            mService = null
            mMediaController?.unregisterCallback(this)
            mMediaController = null
        }
    }

    init {
        applicationContext.bindService(
            Intent(applicationContext, PlayerService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    mService = (service as? PlayerService.Binder)?.service?.also {
                        mMediaController =
                            MediaControllerCompat(applicationContext, it.mediaSession)
                        mMediaController?.registerCallback(mMediaControllerCallback)
                    }
                    isConnected.postValue(mService != null)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    mService = null
                    mMediaController?.unregisterCallback(mMediaControllerCallback)
                    mMediaController = null
                    isConnected.postValue(false)
                }
            },
            Context.BIND_AUTO_CREATE
        )
        mCheckPlaybackPositionJob = GlobalScope.launch {
            playbackPosition.postValue(mPlaybackState?.currentPlayBackPosition)
            delay(CHECK_PLAYBACK_STATE_PAUSE)
        }
    }

    override fun play(track: PlayerTrack) {
        mService?.play(track)
    }

    override fun playWhenReady() {
        mService?.playWhenReady()
    }

    override fun stop() {
        mService?.stop()
    }

    companion object {
        private const val CHECK_PLAYBACK_STATE_PAUSE = 1000L
    }

}
