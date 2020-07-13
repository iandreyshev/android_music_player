package ru.iandreyshev.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import ru.iandreyshev.musicplayer.domain.IMusicPlayer
import ru.iandreyshev.musicplayer.domain.Track
import ru.iandreyshev.player.PlayerService

class MusicServiceConnection(
    applicationContext: Context
) : IMusicPlayer {
    val isConnected = MutableLiveData<Boolean>()
        .apply { postValue(false) }
    val networkFailure = MutableLiveData<Boolean>()
        .apply { postValue(false) }

    val playbackState = MutableLiveData<PlaybackStateCompat>()
        .apply { postValue(EMPTY_PLAYBACK_STATE) }
    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
        .apply { postValue(NOTHING_PLAYING) }

    private var mService: PlayerService? = null

    init {
        applicationContext.bindService(
            Intent(applicationContext, PlayerService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    mService = (service as? PlayerService.Binder)?.service
                    isConnected.postValue(mService != null)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isConnected.postValue(false)
                }
            },
            Context.BIND_AUTO_CREATE
        )
    }

    override fun play(track: Track) {
        ifConnected {
            mService?.play(R.raw.audio_scrubs_intro)
        }
    }

    override fun pause() {
    }

    private fun ifConnected(action: () -> Unit) {
        if (isConnected.value == true) action()
    }

}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()
