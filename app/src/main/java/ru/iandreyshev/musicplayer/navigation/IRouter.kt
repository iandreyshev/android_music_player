package ru.iandreyshev.musicplayer.navigation

import ru.iandreyshev.musicplayer.domain.Track

interface IRouter {
    fun openTrack(track: Track)
    fun onBackFromTrack()
}
