package ru.iandreyshev.musicplayer.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_track_list.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.iandreyshev.musicplayer.R
import ru.iandreyshev.musicplayer.domain.PlaybackState
import ru.iandreyshev.musicplayer.domain.PlaybackTrack
import ru.iandreyshev.musicplayer.navigation.Router
import ru.iandreyshev.musicplayer.presentation.TrackListViewModel
import ru.iandreyshev.musicplayer.utils.uiLazy

class TrackListFragment : BaseFragment(R.layout.fragment_track_list) {

    private val mViewModel: TrackListViewModel by viewModel {
        parametersOf(Router(findNavController()))
    }
    private val mTrackListAdapter by uiLazy { TrackListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTrackListAdapter.setOnClickListener(mViewModel::onTrackClickAt)
        trackList.adapter = mTrackListAdapter

        mViewModel.tracks.viewObserveWith(mTrackListAdapter::submitList)
        mViewModel.playbackState.viewObserveWith { state ->
            nowPlayingWidget.isVisible = state != PlaybackState.DISABLED
            when (state) {
                PlaybackState.CAN_PLAY -> playButton.setImageResource(R.drawable.ic_play_button)
                PlaybackState.CAN_PAUSE -> playButton.setImageResource(R.drawable.ic_pause_button)
                else -> Unit
            }
        }
        mViewModel.playbackTrack.viewObserveWith { track ->
            when (track) {
                is PlaybackTrack.Track -> {
                    trackTitle.text = track.title
                    trackArtist.text = track.artist

                }
                PlaybackTrack.NothingPlaying -> Unit
            }
        }

        playButton.setOnClickListener { mViewModel.onPlayWhenReady() }
        stopPlaybackButton.setOnClickListener { mViewModel.onStop() }
    }

}
