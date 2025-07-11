package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.data_sources.IAudioWaveformProcessor
import br.com.felnanuke.mymusicapp.core.domain.data_sources.ITrackPlayerServices
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class AndroidTrackPlayerServices(
    private val application: Application, private val audioWaveformProcessor: IAudioWaveformProcessor
) : ITrackPlayerServices, ServiceConnection {

    init {
        val playerServiceIntent = Intent(application, PlayerService::class.java)
        application.bindService(playerServiceIntent, this, Context.BIND_AUTO_CREATE)

    }

    override val currentTrack = MutableLiveData<TrackEntity?>(null)

    override val isPlaying = MutableLiveData<Boolean>(false)

    override val trackProgress = MutableLiveData<Float>(0f)

    override val canPlayNext = MutableLiveData<Boolean>(false)

    override val canPlayPrevious = MutableLiveData<Boolean>(false)

    override val amplitudes = MutableLiveData<List<Int>>(listOf())

    override val duration = MutableLiveData<Int>(0)

    override val position = MutableLiveData<Int>(0)

    override val queue = MutableLiveData<List<TrackEntity>>(listOf())

    private var playerService: PlayerService? = null

    private val queueManager: PlayerService.QueueManager?
        get() = playerService?.queueManager


    override fun playNext() {
        if (canPlayNext.value == true) {
            queueManager?.nextTrack()
        }
    }

    override fun playPrevious() {
        val progress = trackProgress.value
        val canPrevious = canPlayPrevious.value
        
        if (progress == null || progress > 0.1f || canPrevious != true) {
            queueManager?.seekTo(0)
        } else {
            queueManager?.previousTrack()
        }
    }

    override fun addToQueue(track: TrackEntity, playNow: Boolean) {
        queueManager?.addTrackToQueue(track, playNow)
    }

    override fun removeFromQueue(index: Int) {
        queueManager?.removeTrack(index)
    }

    override fun cleanQueue() {
        queueManager?.cleanQueue()
    }

    override fun setQueue(queue: List<TrackEntity>) {
        queueManager?.setQueue(queue)
    }

    override fun togglePlayAndPause() {
        queueManager?.togglePlayPause()
    }

    override fun seekTo(milliseconds: Int) {
        queueManager?.seekTo(milliseconds)
    }

    override fun seekTo(progress: Float) {
        queueManager?.seekTo(progress)
    }

    override fun pause() {
        queueManager?.pause()
    }

    override fun play() {
        queueManager?.play()
    }

    override fun play(track: TrackEntity) {
        queueManager?.play(track)
    }

    private fun startObserver() {
        queueManager?.currentTrack?.observeForever {
            currentTrack.value = it
            loadWaveForm()
        }
        queueManager?.isPlaying?.observeForever {
            isPlaying.value = it
        }
        queueManager?.progress?.observeForever {
            trackProgress.value = it
        }
        queueManager?.canPlayNext?.observeForever {
            canPlayNext.value = it
        }
        queueManager?.canPlayPrevious?.observeForever {
            canPlayPrevious.value = it
        }
        queueManager?.durationMillis?.observeForever {
            duration.value = it
        }
        queueManager?.positionMillis?.observeForever {
            position.value = it
        }
        queueManager?.queue?.observeForever {
            queue.value = it

        }

    }

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        val playerServiceBinder = binder as PlayerService.PlayerServiceBinder
        playerService = playerServiceBinder.getService()
        playerService?.startQueueManager()
        startObserver()

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        playerService = null
    }

    override fun reorderQueue(from: Int, to: Int) {
        queueManager?.reorderQueue(from, to)
    }

    /**
     * Initializes the player service connection if not already connected.
     * This method allows manual re-initialization of the service connection if needed.
     */
    fun initialize() {
        if (playerService == null) {
            val playerServiceIntent = Intent(application, PlayerService::class.java)
            application.bindService(playerServiceIntent, this, Context.BIND_AUTO_CREATE)
        }
    }

    private fun loadWaveForm() {
        currentTrack.value?.getAudioByteStream { audioStream ->
            thread {
                audioStream?.let { inputStream ->
                    audioWaveformProcessor.processAudio(
                        inputStream = inputStream,
                        onSuccess = { amplitudesList ->
                            amplitudes.postValue(amplitudesList)
                        },
                        onError = { error ->
                            error.printStackTrace()
                        }
                    )
                }
            }
        }
    }


}