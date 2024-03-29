package br.com.felnanuke.mymusicapp.view_models

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackPlayerManager
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    val playerManager: TrackPlayerManager,
    private val trackRepository: TrackRepository,
) : ViewModel() {
    var queue: List<TrackEntity> = playerManager.queue.value ?: emptyList()
    var queueTracks by mutableStateOf<List<TrackEntity>>(listOf())
    var loading by mutableStateOf(false)
    var amplitudes by mutableStateOf<List<Int>>(listOf())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    var playing by mutableStateOf(playerManager.isPlaying.value!!)
    var canPlayNext by mutableStateOf(playerManager.canPlayNext.value!!)
    var canPlayPrevious by mutableStateOf(playerManager.canPlayPrevious.value!!)
    var trackProgress by mutableStateOf(playerManager.trackProgress.value!!)
    var trackDuration by mutableStateOf(playerManager.durationMillis.value!!)
    var trackPositionMillis by mutableStateOf(playerManager.positionMillis.value!!)
    var trackIndex = 0
    private var tempProgress = 0f
    lateinit var navController: NavHostController


    fun togglePlay() {
        playerManager.togglePlayPause()
    }


    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

    fun formatMillis(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        val secondsString = if (seconds < 10) "0$seconds" else seconds.toString()
        return "$minutes:$secondsString"

    }

    fun onProgressChanged(progress: Float) {
        playerManager.pause()
        this.trackProgress = progress
        this.trackPositionMillis = (progress * trackDuration).toInt()
        tempProgress = progress
    }

    fun onProgressFinished() {
        playerManager.seekTo(tempProgress)
        playerManager.play()
    }

    fun openPlaylist() {
        navController.navigate("playlist")
    }


    fun popBackStack() {
        navController.popBackStack()
    }

    fun reorderQueue(from: Int, to: Int) {
        playerManager.reorderQueue(from, to)

    }

    fun playTrack(track: TrackEntity) {
        playerManager.play(track)
    }

    fun setCurrentTrack(index: Int) {
        if (trackIndex == index) return
        playTrack(queueTracks[index])

    }


    fun trackIndex(track: TrackEntity?): Int {
        return queueTracks.indexOf(track)
    }

    fun startPlayer(uri: Uri) {
        trackRepository.getTrack(uri, onError = { error ->

        }, onSuccess = { track ->
            playerManager.startQueue(track)
        })
    }


}