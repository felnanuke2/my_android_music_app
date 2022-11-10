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
    val duration: MutableLiveData<Int>
    val position: MutableLiveData<Int>
    val queue: MutableLiveData<List<TrackEntity>>


    fun playNext()
    fun playPrevious()
    fun addToQueue(track: TrackEntity, playNow: Boolean)
    fun removeFromQueue(index: Int)
    fun cleanQueue()
    fun setQueue(queue: List<TrackEntity>)
    fun togglePlayAndPause()
    fun seekTo(milliseconds: Int)
    fun seekTo(progress: Float)
    fun pause()
    fun play()
    fun play(track: TrackEntity)
    fun reorderQueue(from: Int, to: Int)
}