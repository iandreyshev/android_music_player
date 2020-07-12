package ru.iandreyshev.musicplayer.domain

import java.io.Serializable

data class Track(
    val id: TrackId,
    val name: String,
    val author: String
) : Serializable
