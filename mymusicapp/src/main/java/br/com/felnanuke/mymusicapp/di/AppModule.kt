package br.com.felnanuke.mymusicapp.di

import android.app.Application
import android.content.Context
import br.com.felnanuke.mymusicapp.core.domain.data_sources.TracksDataSource
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackRepository
import br.com.felnanuke.mymusicapp.core.infrastructure.android.data_sources.AndroidTracksDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTrackRepository(tracksDataSource: AndroidTracksDataSource) : TrackRepository{
        return TrackRepository(tracksDataSource = tracksDataSource)

    }

    @Provides
    @Singleton
    fun provideTracksDataSource(application: Application) : AndroidTracksDataSource {
        return AndroidTracksDataSource(context = application)
    }



}