package ru.iandreyshev.musicplayer.navigation

import ru.iandreyshev.player.PlayerTrack

interface IRouter {
    fun openTrack(track: PlayerTrack)
    fun onBackFromTrack()
}
