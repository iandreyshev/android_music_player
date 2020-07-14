package ru.iandreyshev.musicplayer.utils

import android.content.res.Resources
import android.support.v4.media.MediaMetadataCompat
import ru.iandreyshev.musicplayer.R
import ru.iandreyshev.musicplayer.domain.PlaybackTrack
import ru.iandreyshev.player.extensions.*
import kotlin.math.floor

fun MediaMetadataCompat.asPlaybackTrack(resources: Resources) =
    if (duration != 0L && id != null) {
        PlaybackTrack.Track(
            title = title?.trim().orEmpty(),
            artist = displaySubtitle?.trim().orEmpty(),
            progress = timestampToMSS(resources, duration)
        )
    } else PlaybackTrack.NothingPlaying

fun timestampToMSS(resources: Resources, position: Long): String {
    val totalSeconds = floor(position / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)
    return if (position < 0) resources.getString(R.string.duration_unknown)
    else resources.getString(R.string.duration_format).format(minutes, remainingSeconds)
}
