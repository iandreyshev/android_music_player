package ru.iandreyshev.musicplayer.domain

interface IMusicPlayer {
    fun play(track: Track)
    fun pause()
}
