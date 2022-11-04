package br.com.felnanuke.mymusicapp

import android.app.Application
import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import kotlinx.coroutines.delay

class PlayerService : Service() {

    companion object CurrentTrack {
        var currentTrack = MutableLiveData<TrackEntity?>(null)
    }


    var queueManager: QueueManager? = null
    private val binder = PlayerServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        queueManager = QueueManager(this.application)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /// get the wave form from the track


    class QueueManager(private val application: Application) {


        private var mediaPlayer: MediaPlayer? = null

        private var queue: MutableList<TrackEntity> = mutableListOf()

        private var oldProgress = 0f


        fun getTrackProgress(): Float {
            if (mediaPlayer?.currentPosition != null && mediaPlayer?.duration != null) {
                oldProgress =
                    mediaPlayer?.currentPosition?.toFloat()!! / mediaPlayer?.duration?.toFloat()!!
                if (oldProgress > 0.99f || oldProgress < 0.001f) {
                    oldProgress = 0f
                }

            }
            return oldProgress
        }

        fun queueIsEnd(): Boolean {
            return currentTrack.value == queue.last()
        }

        fun getCurrentTrackIndex(): Int {
            return queue.indexOf(currentTrack.value)
        }

        fun addTrack(track: TrackEntity, play: Boolean = false) {
            queue.add(track)
            if (play) {
                playTrack(track)
                currentTrack.value = track
            } else if (queue.isEmpty()) {
                playTrack(track)
                currentTrack.value = track
            }
        }

        fun removeTrack(track: TrackEntity) {
            queue.remove(track)
        }

        fun nextTrack(): TrackEntity? {

            if (getCurrentTrackIndex() == queue.size - 1) {
                return null
            }
            val nextTrack = queue.elementAt(getCurrentTrackIndex() + 1)
            playTrack(nextTrack)
            currentTrack.value = nextTrack
            playTrack(nextTrack)
            return nextTrack
        }

        fun previousTrack(): TrackEntity? {
            if (getCurrentTrackIndex() == 0) {
                return null
            }
            val previousTrack = queue.elementAt(getCurrentTrackIndex() - 1)
            playTrack(previousTrack)
            currentTrack.value = previousTrack
            playTrack(previousTrack)
            return previousTrack
        }


        fun playTrack(track: TrackEntity) {
            mediaPlayer?.audioSessionId
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this.application.applicationContext, track.audioUri)
            mediaPlayer!!.setOnCompletionListener {
                if (queueIsEnd()) {
                    currentTrack.value = null
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                } else {
                    nextTrack()

                }
            }

            mediaPlayer?.start()
        }

        fun togglePlayPause() {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
        }

        fun isPlaying(): Boolean? {
            return mediaPlayer?.isPlaying

        }

        fun canSkipNext(): Boolean {
            if (getCurrentTrackIndex() == queue.size - 1) {
                return false
            }
            return true
        }


    }

    inner class PlayerServiceBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }


}