package br.com.felnanuke.mymusicapp.core.domain.data_sources

import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

interface ITracksDataSource {
    fun getTracks(onSuccess: (Array<TrackEntity>) -> Unit, onError: () -> Unit)

}