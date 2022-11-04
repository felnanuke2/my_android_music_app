package br.com.felnanuke.mymusicapp.view_models

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.felnanuke.mymusicapp.MusicPlayerActivity
import br.com.felnanuke.mymusicapp.PlayerService
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.domain.repositories.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackRepository: TrackRepository, private val application: Application
) : ViewModel() {

    companion object ActivitiesActions {

        const val OPEN_PLAYER_ACTIVITY_ACTION = 0
    }

    var loading by mutableStateOf(false)
    var tracks by mutableStateOf(mutableListOf<TrackEntity>())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    var progress = 0f
    var activityEvents = MutableLiveData<Int>()

    var playerService: PlayerService? = null


    var serviceConnection = object : ServiceConnection {


        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as PlayerService.PlayerServiceBinder
            playerService = binder.getService()

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            playerService = null
        }

    }



    fun getIsPlaying(): Boolean {
        return playerService?.queueManager?.isPlaying() ?: false
    }

    fun togglePlayPause() {
        playerService?.queueManager?.togglePlayPause()
    }

    fun getPlayerProgress(): Float {
        progress = playerService?.queueManager?.getTrackProgress() ?: progress
        return progress

    }

    fun playTrack(trackEntity: TrackEntity) {
        playerService?.queueManager?.addTrack(trackEntity, true)
    }

    fun insertTrackToPlayList(trackEntity: TrackEntity) {
        playerService?.queueManager?.addTrack(trackEntity)
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