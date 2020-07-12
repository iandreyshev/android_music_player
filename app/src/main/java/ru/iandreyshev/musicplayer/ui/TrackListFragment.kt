package ru.iandreyshev.musicplayer.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_track_list.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.iandreyshev.musicplayer.R
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
    }

}
