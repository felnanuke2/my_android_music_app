package br.com.felnanuke.mymusicapp.core.domain.repositories

import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.data_sources.ITrackPlayerServices
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

class TrackPlayerManager(private val trackPlayerServices: ITrackPlayerServices) {

    val currentTrack: MutableLiveData<TrackEntity?>
        get() = trackPlayerServices.currentTrack
    val isPlaying: MutableLiveData<Boolean>
        get() = trackPlayerServices.isPlaying
    val canPlayNext: MutableLiveData<Boolean>
        get() = trackPlayerServices.canPlayNext
    val canPlayPrevious: MutableLiveData<Boolean>
        get() = trackPlayerServices.canPlayPrevious
    val trackProgress: MutableLiveData<Float>
        get() = trackPlayerServices.trackProgress
    val amplitudes: MutableLiveData<List<Int>>
        get() = trackPlayerServices.amplitudes


    fun togglePlayPause() {
        trackPlayerServices.togglePlayAndPause()
    }

    fun playNext() {
        if (canPlayNext.value!!) {
            trackPlayerServices.playNext()

        }
    }

    fun playPrevious() {
        if (canPlayPrevious.value!!) {
            trackPlayerServices.playPrevious()
        }
    }

    fun addToQueue(track: TrackEntity, playNow: Boolean = false) {
        trackPlayerServices.addToQueue(track, playNow)
    }

    fun removeFromQueue(index: Int) {
        trackPlayerServices.removeFromQueue(index)
    }

    fun startQueue(track: TrackEntity) {
        trackPlayerServices.cleanQueue()
        addToQueue(track, true)
    }


}
