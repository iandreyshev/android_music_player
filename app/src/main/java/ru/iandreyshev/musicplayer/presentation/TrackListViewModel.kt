package ru.iandreyshev.musicplayer.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.iandreyshev.musicplayer.domain.IMusicPlayer
import ru.iandreyshev.musicplayer.domain.Track
import ru.iandreyshev.musicplayer.domain.TrackId
import ru.iandreyshev.musicplayer.navigation.IRouter
import ru.iandreyshev.musicplayer.utils.uiLazy

class TrackListViewModel(
    private val router: IRouter,
    private val player: IMusicPlayer
) : ViewModel() {

    val tracks by uiLazy {
        MutableLiveData(
            listOf(
                Track(TrackId("1"), author = "Ivan Dorn", name = "Afrika"),
                Track(TrackId("2"), author = "John Lennon", name = "Imagine"),
                Track(TrackId("3"), author = "Каста", name = "Корабельная песня")
            )
        )
    }

    fun onTrackClickAt(position: Int) {
        tracks.value
            ?.getOrNull(position)
            ?.let {
                player.play(it)
            }
    }

}
