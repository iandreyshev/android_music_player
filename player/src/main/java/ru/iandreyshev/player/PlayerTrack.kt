package ru.iandreyshev.player

import java.io.Serializable

data class PlayerTrack(
    val trackRes: Int,
    val title: String,
    val artist: String,
    val duration: Long,
    val iconRes: Int
) : Serializable
