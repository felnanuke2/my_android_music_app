package br.com.felnanuke.mymusicapp.view_models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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

    val trackPositionMillis: Long
        get() = playerManager.positionMillis.value?.toLong() ?: 0L
    var loading by mutableStateOf(false)
    var tracks by mutableStateOf(listOf<TrackEntity>())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    var hasRequiredPermissions by mutableStateOf(false)
    val activityEvents = MutableLiveData<Int>()
    var isPlaying by mutableStateOf(playerManager.isPlaying.value ?: false)
    var trackProgress by mutableFloatStateOf(playerManager.trackProgress.value ?: 0f)
    var queue by mutableStateOf(playerManager.queue.value ?: listOf())
    
    // Create observer instances for proper cleanup
    private val currentTrackObserver = Observer<TrackEntity?> { track ->
        currentTrack = track
    }
    
    private val isPlayingObserver = Observer<Boolean> { playing ->
        isPlaying = playing ?: false
    }
    
    private val trackProgressObserver = Observer<Float> { progress ->
        trackProgress = progress ?: 0f
    }
    
    private val queueObserver = Observer<List<TrackEntity>> { queueList ->
        queue = queueList ?: listOf()
    }
    
    init {
        // Set up observers to keep Compose state in sync with LiveData
        playerManager.currentTrack.observeForever(currentTrackObserver)
        playerManager.isPlaying.observeForever(isPlayingObserver)
        playerManager.trackProgress.observeForever(trackProgressObserver)
        playerManager.queue.observeForever(queueObserver)
    }


    companion object ActivitiesActions {
        const val OPEN_PLAYER_ACTIVITY_ACTION = 0
        const val REQUEST_PERMISSIONS_ACTION = 1
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


    fun getTracks() {
        loading = true
        println("HomeViewModel: Starting to load tracks...")
        trackRepository.getTracks({ trackEntities ->
            println("HomeViewModel: Loaded ${trackEntities.size} tracks")
            tracks = trackEntities.toMutableList()
            loading = false
        }, {
            println("HomeViewModel: Error loading tracks")
            loading = false
        }

        )

    }

    fun openExpandedPlayer() {
        activityEvents.value = OPEN_PLAYER_ACTIVITY_ACTION
    }

    fun requestPermissions() {
        activityEvents.value = REQUEST_PERMISSIONS_ACTION
    }

    fun updatePermissionStatus(hasPermissions: Boolean) {
        hasRequiredPermissions = hasPermissions
        if (hasPermissions) {
            getTracks()
        }
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
    
    override fun onCleared() {
        super.onCleared()
        // Clean up observers to prevent memory leaks
        playerManager.currentTrack.removeObserver(currentTrackObserver)
        playerManager.isPlaying.removeObserver(isPlayingObserver)
        playerManager.trackProgress.removeObserver(trackProgressObserver)
        playerManager.queue.removeObserver(queueObserver)
    }


}