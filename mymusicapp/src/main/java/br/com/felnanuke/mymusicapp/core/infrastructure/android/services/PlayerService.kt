package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import android.app.Application
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import java.util.Timer
import kotlin.concurrent.schedule

class PlayerService : Service() {


    var queueManager: QueueManager? = null
    private val binder = PlayerServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    fun startQueueManager() {
        if (queueManager == null) {
            queueManager = QueueManager(this.application)
        }

    }

    /// get the wave form from the track


    class QueueManager(private val application: Application) {

        val currentTrack = MutableLiveData<TrackEntity?>(null)
        val isPlaying = MutableLiveData<Boolean>(false)
        val progress = MutableLiveData<Float>(0f)
        val canPlayNext = MutableLiveData<Boolean>(false)
        val canPlayPrevious = MutableLiveData<Boolean>(false)


        private var mediaPlayer: MediaPlayer? = null

        private var queue: MutableList<TrackEntity> = mutableListOf()

        private var oldProgress = 0f

        init {
            startProgressTimer()
        }


        private fun startProgressTimer() {

            Timer().schedule(0, 100) {
                try {
                    isPlaying.postValue(mediaPlayer?.isPlaying ?: false)
                    if (isPlaying.value!!) {
                        progress.postValue(
                            getTrackProgress()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }


        private fun getTrackProgress(): Float {
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

        fun addTrackToQueue(track: TrackEntity, play: Boolean = false) {
            queue.add(track)
            if (play) {
                playTrack(track)
                currentTrack.value = track
            } else if (queue.isEmpty()) {
                playTrack(track)
                currentTrack.value = track
            }
        }


        fun removeTrack(index: Int) {
            queue.removeAt(index)
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
            //add media player listener to update progress


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

        fun seekTo(milliseconds: Int) {
            mediaPlayer?.seekTo(milliseconds)
        }

        fun cleanQueue() {
            queue.clear()
        }


    }

    inner class PlayerServiceBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }


}