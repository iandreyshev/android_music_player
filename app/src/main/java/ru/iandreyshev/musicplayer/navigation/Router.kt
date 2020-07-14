package ru.iandreyshev.musicplayer.navigation

import androidx.navigation.NavController
import ru.iandreyshev.musicplayer.ui.TrackListFragmentDirections
import ru.iandreyshev.player.PlayerTrack

class Router(private val navController: NavController) : IRouter {

    override fun openTrack(track: PlayerTrack) {
        val action = TrackListFragmentDirections.openTrack(track = track)
        navController.navigate(action)
    }

    override fun onBackFromTrack() {
        navController.popBackStack()
    }

}
