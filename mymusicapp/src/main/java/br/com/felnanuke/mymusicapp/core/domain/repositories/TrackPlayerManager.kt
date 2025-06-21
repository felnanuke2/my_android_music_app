package br.com.felnanuke.mymusicapp.core.domain.repositories

import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.data_sources.ITrackPlayerServices
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity

class TrackPlayerManager(private val trackPlayerServices: ITrackPlayerServices) {

    val currentTrack: MutableLiveData<TrackEntity?>
        get() = trackPlayerServices.currentTrack
    val isPlaying: MutableLiveData<Boolean>
        get() {
          return   trackPlayerServices.isPlaying
        }
    val canPlayNext: MutableLiveData<Boolean>
        get() {
            return trackPlayerServices.canPlayNext
        }
    val canPlayPrevious: MutableLiveData<Boolean>
        get() = trackPlayerServices.canPlayPrevious
    val trackProgress: MutableLiveData<Float>
        get() = trackPlayerServices.trackProgress
    val amplitudes: MutableLiveData<List<Int>>
        get() = trackPlayerServices.amplitudes
    val durationMillis: MutableLiveData<Int>
        get() = trackPlayerServices.duration
    val positionMillis: MutableLiveData<Int>
        get() = trackPlayerServices.position
    val queue: MutableLiveData<List<TrackEntity>>
        get() = trackPlayerServices.queue


    fun togglePlayPause() {
        trackPlayerServices.togglePlayAndPause()
    }

    fun playNext() {
        if (canPlayNext.value!!) {
            trackPlayerServices.playNext()

        }
    }

    fun playPrevious() {
        if (trackProgress.value!! > 0.1f || !canPlayPrevious.value!!) {
            trackPlayerServices.seekTo(0)
        } else {
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

    fun seekTo(progress: Float) {
        trackPlayerServices.seekTo(progress)
    }

    fun pause() {
        trackPlayerServices.pause()
    }

    fun play() {
        trackPlayerServices.play()
    }

    fun play(track: TrackEntity) {
        trackPlayerServices.play(track)
    }

    fun setQueue(queue: List<TrackEntity>) {
        trackPlayerServices.setQueue(queue)
    }

    fun reorderQueue(from: Int, to: Int) {
        trackPlayerServices.reorderQueue(from, to)
    }

}
