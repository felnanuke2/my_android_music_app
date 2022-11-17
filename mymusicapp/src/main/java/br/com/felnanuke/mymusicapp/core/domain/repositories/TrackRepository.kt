package br.com.felnanuke.mymusicapp.core.domain.repositories

import android.content.ContentUris
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.data_sources.ITracksDataSource
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

class TrackRepository(private val tracksDataSource: ITracksDataSource) {

    fun getTracks(onSuccess: (Array<TrackEntity>) -> Unit, onError: () -> Unit) {
        tracksDataSource.getTracks(onSuccess, onError)
    }

    fun getTrack(uri: Uri, onSuccess: (TrackEntity) -> Unit, onError: (Exception) -> Unit) {
        val id = uri.trackId()
        tracksDataSource.getTrack(id, onSuccess, onError)
    }


    private fun Uri.trackId(): Long {
        return ContentUris.parseId(this)
    }


}