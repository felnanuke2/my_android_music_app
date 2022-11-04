package br.com.felnanuke.mymusicapp.view_models

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.felnanuke.mymusicapp.PlayerService
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import linc.com.amplituda.Amplituda

class MusicPlayerViewModel(private val amplituda: Amplituda) : ViewModel() {

    var playerService: PlayerService? = null
    var amplitudes = mutableStateOf<List<Int>>(listOf())
    var currentTrack by mutableStateOf<TrackEntity?>(null)
    var serviceConnection = object : ServiceConnection {


        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as PlayerService.PlayerServiceBinder
            playerService = binder.getService()
            onChangeTrack()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            playerService = null
        }
    }


    fun onChangeTrack() {
        loadWaveForm(currentTrack)
    }

    fun getTrackProgress(): Float {
        return playerService?.queueManager?.getTrackProgress() ?: 0f

    }


    private fun loadWaveForm(currentTrack: TrackEntity?) {
        CoroutineScope(Dispatchers.IO).launch {
            currentTrack?.getAudioByteStream?.let { getInputStream ->
                getInputStream()?.let { inputStream ->
                    amplituda.processAudio(inputStream).get({ success ->
                        amplitudes.value = success.amplitudesAsList()
                    }, { error ->
                        error.printStackTrace()
                    })
                }


            }

        }


    }

    fun togglePlay() {
        playerService?.queueManager?.togglePlayPause()
    }

    fun getIsPlaying(): Boolean {
        return playerService?.queueManager?.isPlaying() ?: false

    }

    fun getCanSkipNext(): Boolean {
        return playerService?.queueManager?.canSkipNext() ?: false
    }

    fun skipNext() {
        playerService?.queueManager?.nextTrack()
    }

    fun skipPrevious() {
        playerService?.queueManager?.previousTrack()
    }

}