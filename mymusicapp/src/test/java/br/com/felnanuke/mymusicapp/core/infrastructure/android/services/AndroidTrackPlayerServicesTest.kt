package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.com.felnanuke.mymusicapp.core.domain.data_sources.IAudioWaveformProcessor
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.infrastructure.android.models.ListableTrackModel
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.InputStream

@ExtendWith(InstantTaskExecutorRuleExtension::class)
class AndroidTrackPlayerServicesTest {

    companion object {
        @JvmField
        @RegisterExtension
        val instantTaskExecutorRule = InstantTaskExecutorRuleExtension()
    }

    lateinit var application: Application
    lateinit var audioWaveformProcessor: IAudioWaveformProcessor
    lateinit var playerService: PlayerService
    lateinit var queueManager: PlayerService.QueueManager
    lateinit var binder: PlayerService.PlayerServiceBinder
    lateinit var trackPlayerServices: AndroidTrackPlayerServices

    private val testUri1 = mockk<Uri>()
    private val testUri2 = mockk<Uri>()
    
    private val testTrack = mockk<TrackEntity>()
    private val testTrack1 = mockk<TrackEntity>()

    @BeforeEach
    fun setUp() {
        // Mock Android framework classes first to prevent initialization issues
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        
        // Mock Intent and ComponentName
        mockkStatic(Intent::class)
        mockkStatic(ComponentName::class)
        
        // Create mocks manually instead of using annotations
        application = mockk(relaxed = true)
        audioWaveformProcessor = mockk()
        playerService = mockk(relaxed = true)
        queueManager = mockk(relaxed = true)
        binder = mockk(relaxed = true)

        // Mock Uri.parse static method
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns testUri1

        // Mock the service binding - prevent actual service creation
        every { application.bindService(any(), any(), any<Int>()) } returns true
        every { binder.getService() } returns playerService
        every { playerService.startQueueManager() } just Runs
        every { playerService.queueManager } returns queueManager

        // Setup LiveData for testing
        every { queueManager.currentTrack } returns MutableLiveData()
        every { queueManager.isPlaying } returns MutableLiveData()
        every { queueManager.progress } returns MutableLiveData()
        every { queueManager.canPlayNext } returns MutableLiveData()
        every { queueManager.canPlayPrevious } returns MutableLiveData()
        every { queueManager.durationMillis } returns MutableLiveData()
        every { queueManager.positionMillis } returns MutableLiveData()
        every { queueManager.queue } returns MutableLiveData()

        // Mock all methods in the queueManager that we'll be testing
        every { queueManager.play() } just Runs
        every { queueManager.play(any()) } just Runs
        every { queueManager.pause() } just Runs
        every { queueManager.togglePlayPause() } just Runs
        every { queueManager.nextTrack() } returns null
        every { queueManager.previousTrack() } returns null
        every { queueManager.seekTo(any<Int>()) } just Runs
        every { queueManager.seekTo(any<Float>()) } just Runs
        every { queueManager.addTrackToQueue(any(), any()) } just Runs
        every { queueManager.removeTrack(any()) } just Runs
        every { queueManager.cleanQueue() } just Runs
        every { queueManager.setQueue(any()) } just Runs
        every { queueManager.reorderQueue(any(), any()) } just Runs

        // Mock the audio waveform processor
        every { audioWaveformProcessor.processAudio(any(), any(), any()) } just Runs

        // Mock TrackEntity properties and methods
        every { testTrack.id } returns 0L
        every { testTrack.name } returns "Test Track"
        every { testTrack.artistName } returns "Test Artist"
        every { testTrack.audioUri } returns testUri1
        every { testTrack.imageUri } returns testUri2
        every { testTrack.duration } returns 30000L
        every { testTrack.getAudioByteStream(any()) } answers {
            val callback = firstArg<(java.io.InputStream?) -> Unit>()
            callback(mockk<java.io.InputStream>(relaxed = true))
        }
        every { testTrack.getInputStream } returns { uri, callback -> 
            callback(mockk<java.io.InputStream>(relaxed = true))
        }
        
        every { testTrack1.id } returns 1L
        every { testTrack1.name } returns "Test Track 1"
        every { testTrack1.artistName } returns "Test Artist 1"
        every { testTrack1.audioUri } returns testUri1
        every { testTrack1.imageUri } returns testUri2
        every { testTrack1.duration } returns 30000L
        every { testTrack1.getAudioByteStream(any()) } answers {
            val callback = firstArg<(java.io.InputStream?) -> Unit>()
            callback(mockk<java.io.InputStream>(relaxed = true))
        }
        every { testTrack1.getInputStream } returns { uri, callback -> 
            callback(mockk<java.io.InputStream>(relaxed = true))
        }

        // Create a spy of AndroidTrackPlayerServices but prevent the constructor from running
        trackPlayerServices = spyk(AndroidTrackPlayerServices(application, audioWaveformProcessor), recordPrivateCalls = true)

        // Force service connection manually to simulate a connected state
        trackPlayerServices.onServiceConnected(mockk(), binder)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkStatic(Uri::class)
        unmockkStatic(Log::class)
        unmockkStatic(Intent::class)
        unmockkStatic(ComponentName::class)
    }

    @Test
    fun `play should call queueManager play`() {
        // When
        trackPlayerServices.play()

        // Then
        verify { queueManager.play() }
    }

    @Test
    fun `play with track should call queueManager play with track`() {
        // When
        trackPlayerServices.play(testTrack)

        // Then
        verify { queueManager.play(testTrack) }
    }

    @Test
    fun `pause should call queueManager pause`() {
        // When
        trackPlayerServices.pause()

        // Then
        verify { queueManager.pause() }
    }

    @Test
    fun `togglePlayAndPause should call queueManager togglePlayPause`() {
        // When
        trackPlayerServices.togglePlayAndPause()

        // Then
        verify { queueManager.togglePlayPause() }
    }

    @Test
    fun `playNext should call queueManager nextTrack when canPlayNext is true`() {
        // Given
        trackPlayerServices.canPlayNext.value = true

        // When
        trackPlayerServices.playNext()

        // Then
        verify { queueManager.nextTrack() }
    }

    @Test
    fun `playNext should not call queueManager nextTrack when canPlayNext is false`() {
        // Given
        trackPlayerServices.canPlayNext.value = false

        // When
        trackPlayerServices.playNext()

        // Then
        verify(exactly = 0) { queueManager.nextTrack() }
    }

    @Test
    fun `playPrevious should call seekTo when trackProgress more than 0_1`() {
        // Given
        trackPlayerServices.trackProgress.value = 0.2f
        trackPlayerServices.canPlayPrevious.value = true

        // When
        trackPlayerServices.playPrevious()

        // Then
        verify { queueManager.seekTo(0) }
        verify(exactly = 0) { queueManager.previousTrack() }
    }

    @Test
    fun `playPrevious should call seekTo when canPlayPrevious is false`() {
        // Given
        trackPlayerServices.trackProgress.value = 0.05f
        trackPlayerServices.canPlayPrevious.value = false

        // When
        trackPlayerServices.playPrevious()

        // Then
        verify { queueManager.seekTo(0) }
        verify(exactly = 0) { queueManager.previousTrack() }
    }

    @Test
    fun `playPrevious should call previousTrack when trackProgress less or eq 0_1 and canPlayPrevious is true`() {
        // Given
        trackPlayerServices.trackProgress.value = 0.05f
        trackPlayerServices.canPlayPrevious.value = true

        // When
        trackPlayerServices.playPrevious()

        // Then
        verify { queueManager.previousTrack() }
        verify(exactly = 0) { queueManager.seekTo(0) }
    }

    @Test
    fun `addToQueue should call queueManager addTrackToQueue`() {
        // When
        trackPlayerServices.addToQueue(testTrack, true)

        // Then
        verify { queueManager.addTrackToQueue(testTrack, true) }
    }

    @Test
    fun `removeFromQueue should call queueManager removeTrack`() {
        // When
        trackPlayerServices.removeFromQueue(2)

        // Then
        verify { queueManager.removeTrack(2) }
    }

    @Test
    fun `cleanQueue should call queueManager cleanQueue`() {
        // When
        trackPlayerServices.cleanQueue()

        // Then
        verify { queueManager.cleanQueue() }
    }

    @Test
    fun `setQueue should call queueManager setQueue`() {
        // Given
        val tracks = listOf(testTrack, testTrack1)

        // When
        trackPlayerServices.setQueue(tracks)

        // Then
        verify { queueManager.setQueue(tracks) }
    }

    @Test
    fun `reorderQueue should call queueManager reorderQueue`() {
        // When
        trackPlayerServices.reorderQueue(1, 3)

        // Then
        verify { queueManager.reorderQueue(1, 3) }
    }

    @Test
    fun `seekTo with milliseconds should call queueManager seekTo with milliseconds`() {
        // When
        trackPlayerServices.seekTo(15000)

        // Then
        verify { queueManager.seekTo(15000) }
    }

    @Test
    fun `seekTo with progress should call queueManager seekTo with progress`() {
        // When
        trackPlayerServices.seekTo(0.5f)

        // Then
        verify { queueManager.seekTo(0.5f) }
    }

    @Test
    fun `onServiceConnected should initialize player service and start queue manager`() {
        // Given - Setup is already done in setUp()

        // Then - Verify that the service is initialized correctly
        verify { playerService.startQueueManager() }
        // Verify that currentTrack LiveData is accessible (not null reference)
        assertNotNull(trackPlayerServices.currentTrack)
    }

    @Test
    fun `onServiceDisconnected should set playerService to null`() {
        // When
        trackPlayerServices.onServiceDisconnected(mockk())

        // Then
        // We need to use reflection to check the private variable
        val field = AndroidTrackPlayerServices::class.java.getDeclaredField("playerService")
        field.isAccessible = true
        assertNull(field.get(trackPlayerServices))
    }

    @Test
    fun `startObserver should set up all live data observers`() {
        // Given - Already called in setup
        val currentTrackLiveData = MutableLiveData<TrackEntity?>()
        val isPlayingLiveData = MutableLiveData<Boolean>()
        val progressLiveData = MutableLiveData<Float>()
        val canPlayNextLiveData = MutableLiveData<Boolean>()
        val canPlayPreviousLiveData = MutableLiveData<Boolean>()
        val durationLiveData = MutableLiveData<Int>()
        val positionLiveData = MutableLiveData<Int>()
        val queueLiveData = MutableLiveData<MutableList<ListableTrackModel>>()

        every { queueManager.currentTrack } returns currentTrackLiveData
        every { queueManager.isPlaying } returns isPlayingLiveData
        every { queueManager.progress } returns progressLiveData
        every { queueManager.canPlayNext } returns canPlayNextLiveData
        every { queueManager.canPlayPrevious } returns canPlayPreviousLiveData
        every { queueManager.durationMillis } returns durationLiveData
        every { queueManager.positionMillis } returns positionLiveData
        every { queueManager.queue } returns queueLiveData

        // Create a single instance to use for comparison
        val expectedListableTrack = ListableTrackModel(testTrack)

        // Call method under test by re-initializing the service connection
        trackPlayerServices.onServiceConnected(mockk(), binder)

        // When - Update values to test observers
        currentTrackLiveData.value = testTrack
        isPlayingLiveData.value = true
        progressLiveData.value = 0.3f
        canPlayNextLiveData.value = true
        canPlayPreviousLiveData.value = true
        durationLiveData.value = 300000
        positionLiveData.value = 90000
        queueLiveData.value = mutableListOf(expectedListableTrack)

        // Then - Verify that values were propagated
        assertEquals(testTrack, trackPlayerServices.currentTrack.value)
        assertEquals(true, trackPlayerServices.isPlaying.value)
        assertEquals(0.3f, trackPlayerServices.trackProgress.value)
        assertEquals(true, trackPlayerServices.canPlayNext.value)
        assertEquals(true, trackPlayerServices.canPlayPrevious.value)
        assertEquals(300000, trackPlayerServices.duration.value)
        assertEquals(90000, trackPlayerServices.position.value)
        // For queue comparison, we check the content by comparing the track IDs instead of object equality
        val actualQueue = trackPlayerServices.queue.value
        assertEquals(1, actualQueue?.size)
        assertEquals(testTrack.id, actualQueue?.first()?.id)
    }

    @Test
    fun `init should bind to PlayerService`() {
        // The binding should have happened in the constructor
        // Verify it was called with the right parameters - simplified to avoid Intent.getComponent() call
        verify {
            application.bindService(
                any(), // Just verify any intent was passed
                any(),
                Context.BIND_AUTO_CREATE
            )
        }
    }

    @Test
    fun `initialize should bind to service`() {
        // Given
        every { application.bindService(any(), any(), any<Int>()) } returns true

        // When
        trackPlayerServices.initialize()

        // Then
        verify { application.bindService(any(), any(), any<Int>()) }
    }

    @Test
    fun `play should delegate to queue manager`() {
        // Given
        every { queueManager.play() } just Runs

        // When
        trackPlayerServices.play()

        // Then
        verify { queueManager.play() }
    }

    @Test
    fun `pause should delegate to queue manager`() {
        // Given
        every { queueManager.pause() } just Runs

        // When
        trackPlayerServices.pause()

        // Then
        verify { queueManager.pause() }
    }

    @Test
    fun `audioWaveformProcessor should be mockable and not cause native library issues`() {
        // Given - setup in setUp() method with mocked audioWaveformProcessor
        
        // When - This would previously fail with Amplituda native library issues
        // but now works because we use the interface
        trackPlayerServices.initialize()
        
        // Then - Verify that the mock is working properly
        verify(exactly = 0) { audioWaveformProcessor.processAudio(any(), any(), any()) }
    }

    @Test
    fun `audioWaveformProcessor processAudio should be called when loadWaveForm is triggered`() {
        // Given - We need to trigger loadWaveForm indirectly through queueManager's currentTrack change
        val mockTrack = mockk<TrackEntity>()
        every { mockTrack.getAudioByteStream(any()) } answers {
            val callback = firstArg<(java.io.InputStream?) -> Unit>()
            callback(mockk<java.io.InputStream>())
        }
        
        // Get the currentTrack LiveData from queueManager to trigger the observer
        val currentTrackLiveData = queueManager.currentTrack
        
        // When - Setting queueManager's current track should trigger loadWaveForm through the observer
        currentTrackLiveData.value = mockTrack
        
        // Then - The audioWaveformProcessor should be called
        verify(timeout = 1000) { audioWaveformProcessor.processAudio(any(), any(), any()) }
    }

    @Test
    fun `loadWaveForm should handle null audioStream gracefully`() {
        // Given
        val mockTrack = mockk<TrackEntity>()
        every { mockTrack.getAudioByteStream(any()) } answers {
            val callback = firstArg<(java.io.InputStream?) -> Unit>()
            callback(null) // Simulate null audioStream
        }
        
        val currentTrackLiveData = queueManager.currentTrack
        
        // When
        currentTrackLiveData.value = mockTrack
        
        // Then - audioWaveformProcessor should not be called when audioStream is null
        verify(timeout = 1000, exactly = 0) { audioWaveformProcessor.processAudio(any(), any(), any()) }
    }

    @Test
    fun `audioWaveformProcessor should handle error callback`() {
        // Given
        val mockTrack = mockk<TrackEntity>()
        val mockInputStream = mockk<InputStream>()
        val testError = mockk<RuntimeException>()
        
        every { mockTrack.getAudioByteStream(any()) } answers {
            val callback = firstArg<(java.io.InputStream?) -> Unit>()
            callback(mockInputStream)
        }
        
        every { audioWaveformProcessor.processAudio(any(), any(), any()) } answers {
            val onError = thirdArg<(Throwable) -> Unit>()
            onError(testError)
        }
        
        // Mock printStackTrace to avoid actual stack trace output
        every { testError.printStackTrace() } just Runs
        
        val currentTrackLiveData = queueManager.currentTrack
        
        // When
        currentTrackLiveData.value = mockTrack
        
        // Then
        verify(timeout = 1000) { audioWaveformProcessor.processAudio(any(), any(), any()) }
        verify(timeout = 1000) { testError.printStackTrace() }
    }

    @Test
    fun `playNext should handle null canPlayNext value`() {
        // Given
        trackPlayerServices.canPlayNext.value = null

        // When
        trackPlayerServices.playNext()

        // Then - Should not call nextTrack when canPlayNext is null
        verify(exactly = 0) { queueManager.nextTrack() }
    }

    @Test
    fun `playPrevious should handle null trackProgress value`() {
        // Given
        trackPlayerServices.trackProgress.value = null
        trackPlayerServices.canPlayPrevious.value = true

        // When
        trackPlayerServices.playPrevious()

        // Then - Should call seekTo(0) when trackProgress is null
        verify { queueManager.seekTo(0) }
        verify(exactly = 0) { queueManager.previousTrack() }
    }

    @Test
    fun `playPrevious should handle null canPlayPrevious value`() {
        // Given
        trackPlayerServices.trackProgress.value = 0.05f
        trackPlayerServices.canPlayPrevious.value = null

        // When
        trackPlayerServices.playPrevious()

        // Then - Should call seekTo(0) when canPlayPrevious is null
        verify { queueManager.seekTo(0) }
        verify(exactly = 0) { queueManager.previousTrack() }
    }

    @Test
    fun `playPrevious should handle both null values`() {
        // Given
        trackPlayerServices.trackProgress.value = null
        trackPlayerServices.canPlayPrevious.value = null

        // When
        trackPlayerServices.playPrevious()

        // Then - Should call seekTo(0) when both values are null
        verify { queueManager.seekTo(0) }
        verify(exactly = 0) { queueManager.previousTrack() }
    }

    @Test
    fun `initialize should not bind service when playerService is not null`() {
        // Given - playerService is already set (from setUp)
        clearMocks(application)

        // When
        trackPlayerServices.initialize()

        // Then - Should not call bindService again
        verify(exactly = 0) { application.bindService(any(), any(), any<Int>()) }
    }

    @Test
    fun `initialize should bind service when playerService is null`() {
        // Given - Set playerService to null
        trackPlayerServices.onServiceDisconnected(mockk())
        clearMocks(application)
        every { application.bindService(any(), any(), any<Int>()) } returns true

        // When
        trackPlayerServices.initialize()

        // Then - Should call bindService when playerService is null
        verify(exactly = 1) { application.bindService(any(), any(), any<Int>()) }
    }

    @Test
    fun `all queue manager methods should handle null queueManager gracefully`() {
        // Given - Disconnect service to make queueManager null
        trackPlayerServices.onServiceDisconnected(mockk())

        // When/Then - All methods should not crash when queueManager is null
        trackPlayerServices.play()
        trackPlayerServices.play(testTrack)
        trackPlayerServices.pause()
        trackPlayerServices.togglePlayAndPause()
        trackPlayerServices.seekTo(1000)
        trackPlayerServices.seekTo(0.5f)
        trackPlayerServices.addToQueue(testTrack, true)
        trackPlayerServices.removeFromQueue(0)
        trackPlayerServices.cleanQueue()
        trackPlayerServices.setQueue(listOf(testTrack))
        trackPlayerServices.reorderQueue(0, 1)
        
        // Set up conditions for playNext and playPrevious
        trackPlayerServices.canPlayNext.value = true
        trackPlayerServices.playNext()
        
        trackPlayerServices.trackProgress.value = 0.05f
        trackPlayerServices.canPlayPrevious.value = true
        trackPlayerServices.playPrevious()

        // No exceptions should be thrown and no queue manager methods should be called
        // since queueManager is null
    }

    @Test
    fun `currentTrack observer should trigger loadWaveForm when track changes to null`() {
        // Given
        val currentTrackLiveData = queueManager.currentTrack
        
        // When - Set current track to null
        currentTrackLiveData.value = null
        
        // Then - audioWaveformProcessor should not be called for null track
        verify(timeout = 1000, exactly = 0) { audioWaveformProcessor.processAudio(any(), any(), any()) }
    }

    @Test
    fun `audioWaveformProcessor should handle success callback`() {
        // Given
        val mockTrack = mockk<TrackEntity>()
        val mockInputStream = mockk<InputStream>()
        val testAmplitudes = listOf(1, 2, 3, 4, 5)
        
        every { mockTrack.getAudioByteStream(any()) } answers {
            val callback = firstArg<(java.io.InputStream?) -> Unit>()
            callback(mockInputStream)
        }
        
        every { audioWaveformProcessor.processAudio(any(), any(), any()) } answers {
            val onSuccess = secondArg<(List<Int>) -> Unit>()
            onSuccess(testAmplitudes)
        }
        
        val currentTrackLiveData = queueManager.currentTrack
        
        // When
        currentTrackLiveData.value = mockTrack
        
        // Then
        verify(timeout = 1000) { audioWaveformProcessor.processAudio(any(), any(), any()) }
        // Wait a bit for the postValue to be processed
        Thread.sleep(100)
        assertEquals(testAmplitudes, trackPlayerServices.amplitudes.value)
    }
}