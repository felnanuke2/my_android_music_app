package br.com.felnanuke.mymusicapp.core.domain.repositories

import br.com.felnanuke.mymusicapp.core.domain.data_sources.TracksDataSource
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

class TrackRepository(private val tracksDataSource: TracksDataSource) {


    fun getTracks(onSuccess: (Array<TrackEntity>) -> Unit, onError: () -> Unit) {
        tracksDataSource.getTracks(onSuccess, onError)
    }

}