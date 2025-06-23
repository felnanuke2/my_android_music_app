package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Size
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.activities.MusicPlayerActivity
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.infrastructure.android.models.ListableTrackModel
import java.util.*
import kotlin.concurrent.schedule


const val CHANNEL_ID = "mymusicapp_channel_id"
const val NOTIFICATION_ID = 1
const val MEDIA_SESSION_TAG = "mymusicapp_media_session"

class PlayerService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat

    private val mediaSessionCallBack: MediaNotificationCallBack = MediaNotificationCallBack(this)


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

    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
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

        val stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder.setActions(
            PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE
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
        private val pauseButton = NotificationCompat.Action(
            android.R.drawable.ic_media_pause,
            "Pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                application, PlaybackStateCompat.ACTION_PAUSE
            )
        )


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
                application,
                0,
                Intent(application, MusicPlayerActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            var mediaMetadataBuilder = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.value?.name).putText(
                    MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.value?.artistName
                ).putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION, durationMillis.value?.toLong() ?: 0
                )



            if (currentTrack.value?.imageUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val bitmap = application.contentResolver.loadThumbnail(
                        currentTrack.value!!.imageUri!!, Size(90, 90), null
                    )

                    mediaMetadataBuilder = mediaMetadataBuilder.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap
                    )
                } catch (_: Exception) {

                }
            }
            sessionCompat.setMetadata(
                mediaMetadataBuilder.build()


            )
            val mediaButton = Intent(Intent.ACTION_MEDIA_BUTTON)
            mediaButton.setClass(this.application, MediaButtonReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                application,
                0,
                mediaButton,
                PendingIntent.FLAG_IMMUTABLE,
            )
            sessionCompat.setMediaButtonReceiver(pendingIntent)


            val notification =
                NotificationCompat.Builder(application, CHANNEL_ID).addAction(pauseButton)
                    .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("My Music App")
                    .setContentIntent(contentPendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(sessionCompat.sessionToken)
                            .setShowActionsInCompactView(1)
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

            sessionCompat

            // Check for notification permission on Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        application,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            } else {
                // For Android 12 and below, permission is automatically granted
                notificationManager.notify(NOTIFICATION_ID, notification)
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
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            val keyEvent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent?
                }
            when(keyEvent?.keyCode){
                KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
                KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
            }
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

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