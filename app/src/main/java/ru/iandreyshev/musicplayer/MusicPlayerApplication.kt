package ru.iandreyshev.musicplayer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.iandreyshev.musicplayer.presentation.TrackListViewModel
import ru.iandreyshev.musicplayer.presentation.TrackViewModel

class MusicPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MusicPlayerApplication)
            modules(
                listOf(
                    module {
                        viewModel { TrackListViewModel(it.component1()) }
                        viewModel { TrackViewModel(it.component1(), it.component2()) }
                    }
                )
            )
        }
    }

}