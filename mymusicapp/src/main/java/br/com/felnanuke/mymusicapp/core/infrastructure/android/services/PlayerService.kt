package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import android.app.*
import android.content.Intent
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Size
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.decodeBitmap
import androidx.lifecycle.MutableLiveData
import androidx.media.session.MediaButtonReceiver
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.activities.HomeActivity
import br.com.felnanuke.mymusicapp.activities.MusicPlayerActivity
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.infrastructure.android.models.ListableTrackModel
import java.util.*
import kotlin.concurrent.schedule


const val CHANNEL_ID = "mymusicapp_channel_id"
const val NOTIFICATION_ID = 1
const val MEDIA_SESSION_TAG = "mymusicapp_media_session"

class PlayerService : Service() {

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var mediaSessionCallBack: MediaNotificationCallBack

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
            createMediaSessionState()
            queueManager = QueueManager(this.application, mediaSession)

        }
    }

    private fun createMediaSessionState() {
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
        mediaSessionCallBack = MediaNotificationCallBack(this)


        val stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder.setActions(
            PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        MediaButtonReceiver.handleIntent(mediaSession, Intent(Intent.ACTION_MEDIA_BUTTON))
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.setCallback(mediaSessionCallBack)
        mediaSession.isActive = true

    }


    class QueueManager(
        private val application: Application, private val sessionCompat: MediaSessionCompat
    ) {

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
            currentTrack.observeForever { track ->
                if (track != null) {
                    displayNotificationMediaController()
                } else {
                    hiddenNotificationMediaController()
                }

            }
            positionMillis.observeForever {
                sessionCompat.setPlaybackState(
                    PlaybackStateCompat.Builder().setState(
                        PlaybackStateCompat.STATE_PLAYING, it.toLong(), 1f
                    ).build()
                )
            }


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

        private fun hiddenNotificationMediaController() {
            val notificationManager = NotificationManagerCompat.from(application)
            notificationManager.cancel(NOTIFICATION_ID)
        }

        private fun displayNotificationMediaController() {


            val contentPendingIntent: PendingIntent = PendingIntent.getActivity(
                application, 0, Intent(application, MusicPlayerActivity::class.java), 0
            )
            var mediaMetadataBuilder = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.value?.name).putText(
                    MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.value?.artistName
                ).putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION, durationMillis.value?.toLong() ?: 0
                )



            if (currentTrack.value?.imageUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bitmap = application.contentResolver.loadThumbnail(
                    currentTrack.value!!.imageUri!!, Size(90, 90), null
                )

                mediaMetadataBuilder = mediaMetadataBuilder.putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap
                )
            }
            sessionCompat.setMetadata(
                mediaMetadataBuilder.build()


            )


            val notification = NotificationCompat.Builder(application, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("My Music App")
                .setContentIntent(contentPendingIntent).addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_baseline_pause_circle_filled_24,
                        "Play",
                        PendingIntent.getActivity(
                            application,
                            0,
                            Intent(
                                application,
                                MediaButtonReceiver::class.java
                            ).putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent.KEYCODE_MEDIA_PLAY),
                            0
                        )
                    )
                ).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionCompat.sessionToken).setShowActionsInCompactView(1)
                ).setContentText("Playing ${currentTrack.value?.name}")

                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

            val notificationManager = NotificationManagerCompat.from(application)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID, "My Music App", NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }



            notificationManager.notify(NOTIFICATION_ID, notification)

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
            queue.value = queue.value!!.apply {
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

    class MediaNotificationCallBack(private val service: PlayerService) :
        MediaSessionCompat.Callback() {


        override fun onPlay() {
            service.queueManager?.play()
            super.onPlay()
        }

        override fun onPause() {
            service.queueManager?.pause()
            super.onPause()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
        }

        override fun onFastForward() {
            super.onFastForward()
        }

        override fun onRewind() {
            super.onRewind()
        }

        override fun onStop() {
            super.onStop()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
        }
    }


}