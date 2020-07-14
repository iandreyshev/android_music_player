package ru.iandreyshev.musicplayer.domain

sealed class PlaybackTrack {
    class Track(
        val title: String,
        val artist: String,
        val progress: String
    ) : PlaybackTrack()

    object NothingPlaying : PlaybackTrack()
}
