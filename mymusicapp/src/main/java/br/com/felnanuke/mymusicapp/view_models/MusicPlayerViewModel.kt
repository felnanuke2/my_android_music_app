package br.com.felnanuke.mymusicapp.view_models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(private val playerManager: TrackPlayerManager) :
    ViewModel() {

    var amplitudes by mutableStateOf<List<Int>>(listOf())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    var playing by mutableStateOf(playerManager.isPlaying.value!!)
    var canPlayNext by mutableStateOf(playerManager.canPlayNext.value!!)
    var canPlayPrevious by mutableStateOf(playerManager.canPlayPrevious.value!!)
    var trackProgress by mutableStateOf(playerManager.trackProgress.value!!)


    init {
        playerManager.currentTrack.observeForever { currentTrack ->
            this.currentTrack = currentTrack
        }
        playerManager.isPlaying.observeForever { isPlaying ->
            this.playing = isPlaying
        }
        playerManager.trackProgress.observeForever { progress ->
            this.trackProgress = progress
        }
        playerManager.canPlayNext.observeForever { canPlayNext ->
            this.canPlayNext = canPlayNext
        }
        playerManager.canPlayPrevious.observeForever { canPlayPrevious ->
            this.canPlayPrevious = canPlayPrevious
        }
        playerManager.amplitudes.observeForever { amplitudes ->
            this.amplitudes = amplitudes
        }
    }

    fun togglePlay() {
        playerManager.togglePlayPause()
    }


    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

}