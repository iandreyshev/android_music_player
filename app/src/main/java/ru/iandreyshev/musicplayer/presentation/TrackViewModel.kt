package ru.iandreyshev.musicplayer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.iandreyshev.musicplayer.navigation.IRouter
import ru.iandreyshev.musicplayer.utils.uiLazy
import ru.iandreyshev.player.PlayerTrack

class TrackViewModel(
    track: PlayerTrack,
    private val router: IRouter
) : ViewModel() {

    val track: LiveData<PlayerTrack> by uiLazy { MutableLiveData(track) }

    private val mTrack = track

    fun onBack() {
        router.onBackFromTrack()
    }

}
