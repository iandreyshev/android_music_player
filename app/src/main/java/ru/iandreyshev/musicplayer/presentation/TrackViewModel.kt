package ru.iandreyshev.musicplayer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.iandreyshev.musicplayer.domain.Track
import ru.iandreyshev.musicplayer.navigation.IRouter
import ru.iandreyshev.musicplayer.utils.uiLazy

class TrackViewModel(
    track: Track,
    private val router: IRouter
) : ViewModel() {

    val track: LiveData<Track> by uiLazy { MutableLiveData(track) }

    private val mTrack = track

    fun onBack() {
        router.onBackFromTrack()
    }

}
