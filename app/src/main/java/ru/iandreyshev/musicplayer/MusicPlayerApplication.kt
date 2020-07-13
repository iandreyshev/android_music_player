package ru.iandreyshev.musicplayer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.iandreyshev.musicplayer.domain.IMusicPlayer
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
                        single<IMusicPlayer> {
                            MusicServiceConnection(applicationContext)
                        }

                        viewModel { TrackListViewModel(it.component1(), get()) }
                        viewModel { TrackViewModel(it.component1(), it.component2()) }
                    }
                )
            )
        }
    }

}
