package ru.iandreyshev.musicplayer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.iandreyshev.musicplayer.R
import ru.iandreyshev.musicplayer.domain.IMusicPlayer
import ru.iandreyshev.musicplayer.domain.PlaybackState
import ru.iandreyshev.musicplayer.domain.PlaybackTrack
import ru.iandreyshev.musicplayer.navigation.IRouter
import ru.iandreyshev.musicplayer.utils.uiLazy
import ru.iandreyshev.player.PlayerTrack

class TrackListViewModel(
    private val router: IRouter,
    private val player: IMusicPlayer,
    val playbackState: LiveData<PlaybackState>,
    val playbackTrack: LiveData<PlaybackTrack>
) : ViewModel() {

    val tracks by uiLazy {
        MutableLiveData(
            listOf(
                PlayerTrack(
                    trackRes = R.raw.audio_scrubs,
                    artist = "Lloyd, Miserlis, McNiven, Perry",
                    title = "Superman",
                    duration = 100,
                    iconRes = R.drawable.thumbnail_scrubs
                ),
                PlayerTrack(
                    trackRes = R.raw.audio_bieber,
                    artist = "Justin Bieber",
                    title = "Baby Baby",
                    duration = 100,
                    iconRes = R.drawable.thumbnail_bieber
                ),
                PlayerTrack(
                    trackRes = R.raw.audio_crush,
                    artist = "Tessa Violet",
                    title = "Crush",
                    duration = 100,
                    iconRes = R.drawable.thumbnail_crush
                )
            )
        )
    }

    fun onTrackClickAt(position: Int) {
        tracks.value
            ?.getOrNull(position)
            ?.let(player::play)
    }

    fun onPlayWhenReady() {
        player.playWhenReady()
    }

    fun onStop() {
        player.stop()
    }

}
