package ru.iandreyshev.musicplayer.utils

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

inline val PlaybackStateCompat.isDisabled
    get() = (state == PlaybackStateCompat.STATE_STOPPED) ||
            (state == PlaybackStateCompat.STATE_NONE) ||
            (state == PlaybackStateCompat.STATE_ERROR)

inline val PlaybackStateCompat.isPlaying
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING)

/**
 * Calculates the current playback position based on last update time along with playback
 * state and speed.
 */
inline val PlaybackStateCompat.currentPlayBackPosition: Long
    get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta * playbackSpeed)).toLong()
    } else {
        position
    }
