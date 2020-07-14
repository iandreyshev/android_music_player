package ru.iandreyshev.musicplayer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.iandreyshev.musicplayer.presentation.TrackListViewModel
import ru.iandreyshev.musicplayer.presentation.TrackViewModel
import timber.log.Timber

class MusicPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin {
            androidContext(this@MusicPlayerApplication)
            modules(
                listOf(
                    module {
                        single {
                            MusicServiceConnection(applicationContext)
                        }

                        viewModel {
                            val serviceConnection = get<MusicServiceConnection>()
                            TrackListViewModel(
                                router = it.component1(),
                                player = serviceConnection,
                                playbackState = serviceConnection.playbackState,
                                playbackTrack = serviceConnection.playbackTrack
                            )
                        }
                        viewModel { TrackViewModel(it.component1(), it.component2()) }
                    }
                )
            )
        }
    }

}
