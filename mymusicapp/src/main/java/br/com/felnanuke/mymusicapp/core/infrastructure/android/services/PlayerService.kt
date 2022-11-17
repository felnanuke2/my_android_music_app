package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import android.app.Application
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.infrastructure.android.models.ListableTrackModel
import java.util.Timer
import kotlin.concurrent.schedule

class PlayerService : Service() {

    inner class PlayerServiceBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }


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


    class QueueManager(private val application: Application) {

        val currentTrack = MutableLiveData<TrackEntity?>(null)
        val isPlaying = MutableLiveData<Boolean>(false)
        val progress = MutableLiveData<Float>(0f)
        val canPlayNext = MutableLiveData<Boolean>(false)
        val canPlayPrevious = MutableLiveData<Boolean>(false)
        val positionMillis = MutableLiveData<Int>(0)
        val durationMillis = MutableLiveData<Int>(0)
        val queue = MutableLiveData<MutableList<ListableTrackModel>>(mutableListOf())
        private var mediaPlayer: MediaPlayer? = null

        private var oldProgress = 0f

        init {
            startProgressTimer()
        }


        fun TrackEntity.index(): Int {
            return if (queue.value!!.contains(this)) {
                queue.value!!.indexOf(this)
            } else {
                -1
            }
        }

        private fun startProgressTimer() {


            Timer().schedule(0, 100) {
                try {
                    isPlaying.postValue(mediaPlayer?.isPlaying ?: false)
                    canPlayNext.postValue(canPlayNext())
                    canPlayPrevious.postValue(canPlayPrevious())
                    positionMillis.postValue(mediaPlayer?.currentPosition ?: 0)
                    durationMillis.postValue(mediaPlayer?.duration ?: 0)

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

        private fun queueIsEnd(): Boolean {
            return currentTrack.value == queue.value!!.last()
        }

        private fun getCurrentTrackIndex(): Int {
            return queue.value!!.indexOf(currentTrack.value)
        }

        fun addTrackToQueue(track: TrackEntity, play: Boolean = false) {
            val listableTrack = ListableTrackModel(track)
            queue.value  = queue.value!!.apply {
                add(listableTrack)
            }
            if (play) {
                playTrack(listableTrack)
                currentTrack.value = listableTrack
            } else if (queue.value!!.isEmpty()) {
                playTrack(listableTrack)
                currentTrack.value = listableTrack
            }
        }


        fun removeTrack(index: Int) {
            queue.value!!.removeAt(index)
        }

        fun nextTrack(): TrackEntity? {

            if (getCurrentTrackIndex() == queue.value!!.size - 1) {
                return null
            }
            val nextTrack = queue.value!!.elementAt(getCurrentTrackIndex() + 1)
            playTrack(nextTrack)
            currentTrack.value = nextTrack
            playTrack(nextTrack)
            return nextTrack
        }

        fun previousTrack(): TrackEntity? {
            if (getCurrentTrackIndex() == 0) {
                return null
            }
            val previousTrack = queue.value!!.elementAt(getCurrentTrackIndex() - 1)
            playTrack(previousTrack)
            currentTrack.value = previousTrack
            playTrack(previousTrack)
            return previousTrack
        }


        private fun playTrack(track: TrackEntity) {
            mediaPlayer?.stop()
            mediaPlayer = MediaPlayer.create(this.application.applicationContext, track.audioUri)
            mediaPlayer!!.setOnCompletionListener {
                if (queueIsEnd()) {
                    mediaPlayer?.seekTo(0)
                    mediaPlayer?.pause()

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


        private fun canPlayPrevious(): Boolean {
            return getCurrentTrackIndex() > 0
        }

        private fun canPlayNext(): Boolean {
            if (getCurrentTrackIndex() == queue.value!!.size - 1) {
                return false
            }
            return true
        }

        fun seekTo(milliseconds: Int) {
            mediaPlayer?.seekTo(milliseconds)
        }

        fun reorderQueue(from: Int, to: Int) {
            queue.value!!.apply { add(to, removeAt(from)) }
        }

        fun seekTo(progress: Float) {
            mediaPlayer?.seekTo((mediaPlayer?.duration?.times(progress))?.toInt() ?: 0)
        }

        fun cleanQueue() {
            queue.value?.clear()
        }

        fun pause() {
            mediaPlayer?.pause()

        }

        fun play() {
            mediaPlayer?.start()
        }

        fun play(track: TrackEntity) {
            val index = track.index()
            if (index == currentTrack.value?.index()) return
            if (index != -1) {
                playTrack(track)
                currentTrack.value = track
            }
        }


        fun setQueue(queue: List<TrackEntity>) {
            this.queue.value = queue.map { ListableTrackModel(it) }.toMutableList()
        }


    }


}