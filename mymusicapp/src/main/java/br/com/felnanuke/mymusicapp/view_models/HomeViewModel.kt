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
    private val trackRepository: TrackRepository, private val playerManager: TrackPlayerManager,
) : ViewModel() {

    var loading by mutableStateOf(false)
    var tracks by mutableStateOf(mutableListOf<TrackEntity>())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    val activityEvents = MutableLiveData<Int>()
    var isPlaying by mutableStateOf(playerManager.isPlaying.value!!)
    var trackProgress by mutableStateOf(playerManager.trackProgress.value!!)


    init {
        playerManager.currentTrack.observeForever { track ->
            currentTrack = track
        }
        playerManager.isPlaying.observeForever { isPlaying ->
            this.isPlaying = isPlaying
        }
        playerManager.trackProgress.observeForever { progress ->
            this.trackProgress = progress
        }
    }


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


}