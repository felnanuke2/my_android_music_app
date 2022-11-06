package br.com.felnanuke.mymusicapp.di

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackPlayerManager
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackRepository
import br.com.felnanuke.mymusicapp.core.infrastructure.android.data_sources.AndroidTracksDataSource
import br.com.felnanuke.mymusicapp.core.infrastructure.android.services.AndroidTrackPlayerServices
import br.com.felnanuke.mymusicapp.core.infrastructure.android.services.PlayerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import linc.com.amplituda.Amplituda
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTrackRepository(tracksDataSource: AndroidTracksDataSource): TrackRepository {
        return TrackRepository(tracksDataSource = tracksDataSource)

    }

    @Provides
    @Singleton
    fun provideTrackPlayerManager(trackPlayerServices: AndroidTrackPlayerServices): TrackPlayerManager {
        return TrackPlayerManager(trackPlayerServices = trackPlayerServices)
    }

    @Provides
    @Singleton
    fun provideTracksDataSource(application: Application): AndroidTracksDataSource {
        return AndroidTracksDataSource(context = application)
    }

    @Provides
    @Singleton
    fun provideTrackPlayerServices(
        application: Application, amplituda: Amplituda
    ): AndroidTrackPlayerServices {
        return AndroidTrackPlayerServices(application, amplituda)

    }

    @Provides
    @Singleton
    fun provideAmplituda(application: Application): Amplituda {
        return Amplituda(application)
    }


}