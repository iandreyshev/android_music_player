package ru.iandreyshev.musicplayer.navigation

import androidx.navigation.NavController
import ru.iandreyshev.musicplayer.domain.Track
import ru.iandreyshev.musicplayer.ui.TrackListFragmentDirections

class Router(private val navController: NavController) : IRouter {

    override fun openTrack(track: Track) {
        val action = TrackListFragmentDirections.openTrack(track = track)
        navController.navigate(action)
    }

    override fun onBackFromTrack() {
        navController.popBackStack()
    }

}
