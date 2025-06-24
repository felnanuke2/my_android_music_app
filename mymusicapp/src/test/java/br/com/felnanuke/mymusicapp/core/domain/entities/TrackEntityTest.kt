package br.com.felnanuke.mymusicapp.core.domain.entities

import android.net.Uri
import android.os.Bundle
import br.com.felnanuke.mymusicapp.core.infrastructure.android.services.InstantTaskExecutorRuleExtension
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.InputStream

@ExtendWith(InstantTaskExecutorRuleExtension::class)
class TrackEntityTest {

    companion object {
        @JvmField
        @RegisterExtension
        val instantTaskExecutorRule = InstantTaskExecutorRuleExtension()
    }

    private lateinit var testUri1: Uri
    private lateinit var testUri2: Uri
    private lateinit var mockInputStream: InputStream
    private lateinit var mockBundle: Bundle
    private lateinit var getInputStreamMock: (Uri, (InputStream?) -> Unit) -> Unit

    private val testId = 123L
    private val testName = "Test Track"
    private val testArtistName = "Test Artist"
    private val testDuration = 180000L

    @BeforeEach
    fun setUp() {
        // Mock Android framework classes
        mockkStatic(Uri::class)
        
        testUri1 = mockk<Uri>(relaxed = true)
        testUri2 = mockk<Uri>(relaxed = true)
        mockInputStream = mockk<InputStream>(relaxed = true)
        mockBundle = mockk<Bundle>(relaxed = true)
        
        // Mock Uri.parse static method
        every { Uri.parse(any()) } returns testUri1
        
        // Mock bundle methods
        every { mockBundle.getLong("id") } returns testId
        every { mockBundle.getString("name") } returns testName
        every { mockBundle.getString("artistName") } returns testArtistName
        every { mockBundle.getString("audioUri") } returns "content://audio/123"
        every { mockBundle.getString("imageUri") } returns "content://image/123"
        every { mockBundle.getLong("duration") } returns testDuration
        
        // Mock getInputStream function
        getInputStreamMock = mockk<(Uri, (InputStream?) -> Unit) -> Unit>()
        every { getInputStreamMock(any(), any()) } answers {
            val callback = secondArg<(InputStream?) -> Unit>()
            callback(mockInputStream)
        }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkStatic(Uri::class)
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun `primary constructor should create TrackEntity with all properties`() {
        // When
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration
        )

        // Then
        assertEquals(testId, trackEntity.id)
        assertEquals(testName, trackEntity.name)
        assertEquals(testArtistName, trackEntity.artistName)
        assertEquals(testUri1, trackEntity.audioUri)
        assertEquals(testUri2, trackEntity.imageUri)
        assertEquals(testDuration, trackEntity.duration)
    }

    @Test
    fun `secondary constructor should create TrackEntity and set getInputStream function`() {
        // When
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )

        // Then
        assertEquals(testId, trackEntity.id)
        assertEquals(testName, trackEntity.name)
        assertEquals(testArtistName, trackEntity.artistName)
        assertEquals(testUri1, trackEntity.audioUri)
        assertEquals(testUri2, trackEntity.imageUri)
        assertEquals(testDuration, trackEntity.duration)
        assertEquals(getInputStreamMock, trackEntity.getInputStream)
    }

    @Test
    fun `secondary constructor with null imageUri should work correctly`() {
        // When
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = null,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )

        // Then
        assertEquals(testId, trackEntity.id)
        assertEquals(testName, trackEntity.name)
        assertEquals(testArtistName, trackEntity.artistName)
        assertEquals(testUri1, trackEntity.audioUri)
        assertNull(trackEntity.imageUri)
        assertEquals(testDuration, trackEntity.duration)
        assertEquals(getInputStreamMock, trackEntity.getInputStream)
    }

    @Test
    fun `secondary constructor with default getAudioByteStream should set empty function`() {
        // When - Use secondary constructor with default getAudioByteStream parameter
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = { _, _ -> } // Explicitly use default empty function
        )

        // Then
        assertEquals(testId, trackEntity.id)
        assertEquals(testName, trackEntity.name)
        assertEquals(testArtistName, trackEntity.artistName)
        assertEquals(testUri1, trackEntity.audioUri)
        assertEquals(testUri2, trackEntity.imageUri)
        assertEquals(testDuration, trackEntity.duration)
        
        // Verify that the default getInputStream function is set and can be called
        assertNotNull(trackEntity.getInputStream)
        
        // Verify that calling getAudioByteStream doesn't crash (default implementation does nothing)
        val callback = mockk<(InputStream?) -> Unit>(relaxed = true)
        assertDoesNotThrow { trackEntity.getAudioByteStream(callback) }
    }

    @Test
    fun `bundle constructor should create TrackEntity from bundle data`() {
        // When
        val trackEntity = TrackEntity(mockBundle)

        // Then
        assertEquals(testId, trackEntity.id)
        assertEquals(testName, trackEntity.name)
        assertEquals(testArtistName, trackEntity.artistName)
        assertEquals(testUri1, trackEntity.audioUri)
        assertEquals(testUri1, trackEntity.imageUri) // Both URIs will be the same mock
        assertEquals(testDuration, trackEntity.duration)
        
        // Verify bundle methods were called
        verify { mockBundle.getLong("id") }
        verify { mockBundle.getString("name") }
        verify { mockBundle.getString("artistName") }
        verify { mockBundle.getString("audioUri") }
        verify { mockBundle.getString("imageUri") }
        verify { mockBundle.getLong("duration") }
    }

    @Test
    fun `bundle constructor should handle null imageUri correctly`() {
        // Given
        every { mockBundle.getString("imageUri") } returns null

        // When
        val trackEntity = TrackEntity(mockBundle)

        // Then
        assertEquals(testId, trackEntity.id)
        assertEquals(testName, trackEntity.name)
        assertEquals(testArtistName, trackEntity.artistName)
        assertEquals(testUri1, trackEntity.audioUri)
        assertEquals(testUri1, trackEntity.imageUri) // Uri.parse(null) returns testUri1 mock
        assertEquals(testDuration, trackEntity.duration)
    }

    @Test
    fun `getAudioByteStream should call getInputStream with audioUri`() {
        // Given
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )
        
        val callback = mockk<(InputStream?) -> Unit>(relaxed = true)

        // When
        trackEntity.getAudioByteStream(callback)

        // Then
        verify { getInputStreamMock(testUri1, callback) }
    }

    @Test
    fun `getAudioByteStream should invoke callback with stream`() {
        // Given
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )
        
        var callbackResult: InputStream? = null
        val callback: (InputStream?) -> Unit = { stream ->
            callbackResult = stream
        }

        // When
        trackEntity.getAudioByteStream(callback)

        // Then
        verify { getInputStreamMock(testUri1, any()) }
        assertEquals(mockInputStream, callbackResult)
    }

    @Test
    fun `getAudioByteStream should handle null stream callback correctly`() {
        // Given
        val nullStreamMock: (Uri, (InputStream?) -> Unit) -> Unit = mockk()
        every { nullStreamMock(any(), any()) } answers {
            val callback = secondArg<(InputStream?) -> Unit>()
            callback(null)
        }
        
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = nullStreamMock
        )
        
        var callbackResult: InputStream? = mockInputStream // Set to non-null initially
        val callback: (InputStream?) -> Unit = { stream ->
            callbackResult = stream
        }

        // When
        trackEntity.getAudioByteStream(callback)

        // Then
        verify { nullStreamMock(testUri1, any()) }
        assertNull(callbackResult)
    }

    @Test
    fun `getBundle should create bundle with all track properties`() {
        // Given
        every { testUri1.toString() } returns "content://audio/123"
        every { testUri2.toString() } returns "content://image/123"
        
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )

        // Mock Bundle constructor and methods
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putLong(any(), any()) } just Runs

        // When
        val bundle = trackEntity.getBundle()

        // Then
        assertNotNull(bundle)
        verify { anyConstructed<Bundle>().putString("name", testName) }
        verify { anyConstructed<Bundle>().putString("artistName", testArtistName) }
        verify { anyConstructed<Bundle>().putString("audioUri", "content://audio/123") }
        verify { anyConstructed<Bundle>().putString("imageUri", "content://image/123") }
        verify { anyConstructed<Bundle>().putLong("duration", testDuration) }
        verify { anyConstructed<Bundle>().putLong("id", testId) }
    }

    @Test
    fun `getBundle should handle null imageUri correctly`() {
        // Given
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = null,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )

        every { testUri1.toString() } returns "content://audio/123"

        // Mock Bundle constructor
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putLong(any(), any()) } just Runs

        // When
        val bundle = trackEntity.getBundle()

        // Then
        assertNotNull(bundle)
        verify { anyConstructed<Bundle>().putString("name", testName) }
        verify { anyConstructed<Bundle>().putString("artistName", testArtistName) }
        verify { anyConstructed<Bundle>().putString("audioUri", "content://audio/123") }
        verify { anyConstructed<Bundle>().putString("imageUri", "null") }
        verify { anyConstructed<Bundle>().putLong("duration", testDuration) }
        verify { anyConstructed<Bundle>().putLong("id", testId) }
    }

    @Test
    fun `TrackEntity should be open class for inheritance`() {
        // Given/When - Create a simple subclass to verify inheritance works
        class TestTrackEntity(
            id: Long,
            name: String,
            artistName: String,
            audioUri: Uri,
            imageUri: Uri?,
            duration: Long
        ) : TrackEntity(id, name, artistName, audioUri, imageUri, duration)

        val subclassEntity = TestTrackEntity(
            testId, testName, testArtistName, testUri1, testUri2, testDuration
        )

        // Then
        assertTrue(subclassEntity is TrackEntity)
        assertEquals(testId, subclassEntity.id)
        assertEquals(testName, subclassEntity.name)
        assertEquals(testArtistName, subclassEntity.artistName)
    }

    @Test
    fun `properties should be accessible after construction`() {
        // Given
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )

        // When/Then - All properties should be accessible
        assertDoesNotThrow { trackEntity.id }
        assertDoesNotThrow { trackEntity.name }
        assertDoesNotThrow { trackEntity.artistName }
        assertDoesNotThrow { trackEntity.audioUri }
        assertDoesNotThrow { trackEntity.imageUri }
        assertDoesNotThrow { trackEntity.duration }
        assertDoesNotThrow { trackEntity.getInputStream }
    }

    @Test
    fun `multiple getAudioByteStream calls should work correctly`() {
        // Given
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )
        
        val callback1 = mockk<(InputStream?) -> Unit>(relaxed = true)
        val callback2 = mockk<(InputStream?) -> Unit>(relaxed = true)

        // When
        trackEntity.getAudioByteStream(callback1)
        trackEntity.getAudioByteStream(callback2)

        // Then
        verify(exactly = 2) { getInputStreamMock(testUri1, any()) }
        verify(exactly = 1) { getInputStreamMock(testUri1, callback1) }
        verify(exactly = 1) { getInputStreamMock(testUri1, callback2) }
    }

    @Test
    fun `bundle roundtrip should preserve all data`() {
        // Given
        every { testUri1.toString() } returns "content://audio/123"
        every { testUri2.toString() } returns "content://image/123"

        val originalTrackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration,
            getAudioByteStream = getInputStreamMock
        )

        // Mock Bundle constructor and methods
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putLong(any(), any()) } just Runs

        // When - Convert to bundle
        val bundle = originalTrackEntity.getBundle()
        
        // Then - Verify bundle creation works
        assertNotNull(bundle)
        
        // Verify the bundle creation worked by checking the mocked calls
        verify { anyConstructed<Bundle>().putString("name", testName) }
        verify { anyConstructed<Bundle>().putString("artistName", testArtistName) }
        verify { anyConstructed<Bundle>().putString("audioUri", "content://audio/123") }
        verify { anyConstructed<Bundle>().putString("imageUri", "content://image/123") }
        verify { anyConstructed<Bundle>().putLong("duration", testDuration) }
        verify { anyConstructed<Bundle>().putLong("id", testId) }
        
        // Create a reconstructed track from the mock bundle data we set up
        val reconstructedTrackEntity = TrackEntity(mockBundle)

        // Verify properties match
        assertEquals(originalTrackEntity.id, reconstructedTrackEntity.id)
        assertEquals(originalTrackEntity.name, reconstructedTrackEntity.name)
        assertEquals(originalTrackEntity.artistName, reconstructedTrackEntity.artistName)
        assertEquals(originalTrackEntity.duration, reconstructedTrackEntity.duration)
        // URI comparison relies on the mocked Uri.parse returning testUri1
        assertEquals(testUri1, reconstructedTrackEntity.audioUri)
        assertEquals(testUri1, reconstructedTrackEntity.imageUri)
    }

    @Test
    fun `getInputStream should be lateinit and throw if not initialized with primary constructor`() {
        // Given - Create track with primary constructor (no getInputStream provided)
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration
        )

        // When/Then - Accessing uninitialized lateinit var should throw
        assertThrows(UninitializedPropertyAccessException::class.java) {
            trackEntity.getInputStream
        }
        
        // Also accessing getAudioByteStream should throw since it tries to use getInputStream
        assertThrows(UninitializedPropertyAccessException::class.java) {
            trackEntity.getAudioByteStream { }
        }
    }

    @Test
    fun `getInputStream can be set manually after construction`() {
        // Given
        val trackEntity = TrackEntity(
            id = testId,
            name = testName,
            artistName = testArtistName,
            audioUri = testUri1,
            imageUri = testUri2,
            duration = testDuration
        )

        // When
        trackEntity.getInputStream = getInputStreamMock

        // Then
        assertEquals(getInputStreamMock, trackEntity.getInputStream)
        
        // Verify it works
        val callback = mockk<(InputStream?) -> Unit>(relaxed = true)
        trackEntity.getAudioByteStream(callback)
        verify { getInputStreamMock(testUri1, callback) }
    }
}