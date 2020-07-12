package ru.iandreyshev.player

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import ru.iandreyshev.player.extensions.fullDescription
import ru.iandreyshev.player.extensions.id
import ru.iandreyshev.player.extensions.mediaUri
import timber.log.Timber

class PlayerPlaybackPreparer(
    private val trackSource: IMediaSource,
    private val exoPlayer: ExoPlayer,
    private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {

    override fun onPrepare(playWhenReady: Boolean) = Unit

    // TODO: 12-Jul-20 Что за экшены такие?
    override fun getSupportedPrepareActions() =
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        val mediaFromSource = trackSource.get()
        val mediaToPlay: MediaMetadataCompat? = when (mediaFromSource?.id) {
            mediaId -> mediaFromSource
            else -> null
        }

        if (mediaToPlay == null) {
            Timber.w("Content not found: MediaID=$mediaId")

            // TODO: Notify caller of the error.
        } else {
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .setTag(mediaToPlay.fullDescription)
                .createMediaSource(mediaToPlay.mediaUri)

            exoPlayer.prepare(mediaSource)
            exoPlayer.seekTo(0)
            exoPlayer.playWhenReady = playWhenReady
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit
    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean = false

}
