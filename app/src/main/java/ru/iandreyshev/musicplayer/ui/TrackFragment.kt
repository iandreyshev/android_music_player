package ru.iandreyshev.musicplayer.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_track.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.iandreyshev.musicplayer.R
import ru.iandreyshev.musicplayer.navigation.Router
import ru.iandreyshev.musicplayer.presentation.TrackViewModel

class TrackFragment : BaseFragment(R.layout.fragment_track) {

    private val mViewModel: TrackViewModel by viewModel {
        val args: TrackFragmentArgs by navArgs()
        parametersOf(args.track, Router(findNavController()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener { mViewModel.onBack() }

        mViewModel.track.viewObserveWith { track ->
            name.text = track.title
            author.text = track.artist
        }
    }

}
