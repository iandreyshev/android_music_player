package ru.iandreyshev.musicplayer.domain

import ru.iandreyshev.player.PlayerTrack

interface IMusicPlayer {
    fun play(track: PlayerTrack)
    fun playWhenReady()
    fun stop()
}
