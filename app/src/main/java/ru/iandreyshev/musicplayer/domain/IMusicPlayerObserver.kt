package ru.iandreyshev.musicplayer.domain

interface IMusicPlayerObserver {
    fun tracksUpdated(tracks: List<Track>)
}
