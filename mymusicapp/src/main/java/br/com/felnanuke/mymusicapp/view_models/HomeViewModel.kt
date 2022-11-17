package br.com.felnanuke.mymusicapp.view_models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackPlayerManager
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackRepository: TrackRepository, val playerManager: TrackPlayerManager,
) : ViewModel() {

    var loading by mutableStateOf(false)
    var tracks by mutableStateOf(listOf<TrackEntity>())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    val activityEvents = MutableLiveData<Int>()
    var isPlaying by mutableStateOf(playerManager.isPlaying.value!!)
    var trackProgress by mutableStateOf(playerManager.trackProgress.value!!)
    var queue by mutableStateOf(playerManager.queue.value!!)


    companion object ActivitiesActions {
        const val OPEN_PLAYER_ACTIVITY_ACTION = 0
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }


    fun playTrack(trackEntity: TrackEntity) {
        playerManager.startQueue(trackEntity)
    }

    fun insertTrackToPlayList(trackEntity: TrackEntity) {
        playerManager.addToQueue(trackEntity)
    }


    fun onStart() {
        loading = true
        trackRepository.getTracks({ trackEntities ->
            tracks = trackEntities.toMutableList()
            loading = false
        }, {
            loading = false
        }

        )

    }

    fun openExpandedPlayer() {
        activityEvents.value = OPEN_PLAYER_ACTIVITY_ACTION
    }

    fun getTrackIndex(trackEntity: TrackEntity): Int {
        val index = queue.indexOf(trackEntity)
        return if (index < 0) 0 else index
    }

    fun getTrack(index: Int): TrackEntity {
        return queue[index]
    }

    fun setTrack(index: Int) {
        playerManager.play(queue[index])
    }


}