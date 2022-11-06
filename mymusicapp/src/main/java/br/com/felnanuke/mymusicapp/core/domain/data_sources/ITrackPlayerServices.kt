package br.com.felnanuke.mymusicapp.core.domain.data_sources

import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

interface ITrackPlayerServices {
    val currentTrack: MutableLiveData<TrackEntity?>
    val trackProgress: MutableLiveData<Float>
    val isPlaying: MutableLiveData<Boolean>
    val canPlayNext: MutableLiveData<Boolean>
    val canPlayPrevious: MutableLiveData<Boolean>
    val amplitudes: MutableLiveData<List<Int>>


    fun playNext()
    fun playPrevious()
    fun addToQueue(track: TrackEntity, playNow: Boolean)
    fun removeFromQueue(index: Int)
    fun cleanQueue()
    fun togglePlayAndPause()
}