package ru.iandreyshev.player

import android.support.v4.media.MediaMetadataCompat

interface IMediaSource {
    fun get(): MediaMetadataCompat?
}
