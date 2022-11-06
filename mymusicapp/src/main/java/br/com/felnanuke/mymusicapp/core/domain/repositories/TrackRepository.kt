package br.com.felnanuke.mymusicapp.core.domain.repositories

import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.data_sources.ITracksDataSource
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

class TrackRepository(private val tracksDataSource: ITracksDataSource) {
    
    fun getTracks(onSuccess: (Array<TrackEntity>) -> Unit, onError: () -> Unit) {
        tracksDataSource.getTracks(onSuccess, onError)
    }


}