package br.com.felnanuke.mymusicapp.core.infrastructure.android.models

import android.net.Uri
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.core.infrastructure.android.services.InstantTaskExecutorRuleExtension
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.InputStream
import java.util.*

@ExtendWith(InstantTaskExecutorRuleExtension::class)
class ListableTrackModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val instantTaskExecutorRule = InstantTaskExecutorRuleExtension()
    }

    private lateinit var testUri1: Uri
    private lateinit var testUri2: Uri
    private lateinit var testTrack: TrackEntity
    private lateinit var testTrack2: TrackEntity
    private lateinit var mockInputStream: InputStream

    @BeforeEach
    fun setUp() {
        // Mock Uri class
        mockkStatic(Uri::class)
        testUri1 = mockk<Uri>()
        testUri2 = mockk<Uri>()
        mockInputStream = mockk<InputStream>(relaxed = true)

        // Create mock TrackEntity instances
        testTrack = mockk<TrackEntity>()
        every { testTrack.id } returns 1L
        every { testTrack.name } returns "Test Track"
        every { testTrack.artistName } returns "Test Artist"
        every { testTrack.audioUri } returns testUri1
        every { testTrack.imageUri } returns testUri2
        every { testTrack.duration } returns 30000L
        every { testTrack.getInputStream } returns { uri, callback -> 
            callback(mockInputStream)
        }

        testTrack2 = mockk<TrackEntity>()
        every { testTrack2.id } returns 2L
        every { testTrack2.name } returns "Test Track 2"
        every { testTrack2.artistName } returns "Test Artist 2"
        every { testTrack2.audioUri } returns testUri1
        every { testTrack2.imageUri } returns testUri2
        every { testTrack2.duration } returns 45000L
        every { testTrack2.getInputStream } returns { uri, callback -> 
            callback(mockInputStream)
        }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkStatic(Uri::class)
    }

    @Test
    fun `constructor with TrackEntity should create ListableTrackModel with same properties`() {
        // When
        val listableTrack = ListableTrackModel(testTrack)

        // Then
        assertEquals(testTrack.id, listableTrack.id)
        assertEquals(testTrack.name, listableTrack.name)
        assertEquals(testTrack.artistName, listableTrack.artistName)
        assertEquals(testTrack.audioUri, listableTrack.audioUri)
        assertEquals(testTrack.imageUri, listableTrack.imageUri)
        assertEquals(testTrack.duration, listableTrack.duration)
    }

    @Test
    fun `constructor with TrackEntity should generate unique UUID`() {
        // When
        val listableTrack1 = ListableTrackModel(testTrack)
        val listableTrack2 = ListableTrackModel(testTrack)

        // Then - Even with same track, UUIDs should be different
        assertNotEquals(listableTrack1.hashCode(), listableTrack2.hashCode())
        assertNotEquals(listableTrack1, listableTrack2)
    }

    @Test
    fun `constructor with all parameters should create ListableTrackModel correctly`() {
        // Given
        val testUUID = UUID.randomUUID()
        val mockGetAudioByteStream: (Uri, (InputStream?) -> Unit) -> Unit = { _, callback ->
            callback(mockInputStream)
        }

        // When
        val listableTrack = ListableTrackModel(
            id = 5L,
            name = "Direct Constructor Track",
            artistName = "Direct Artist",
            audioUri = testUri1,
            imageUri = testUri2,
            duration = 60000L,
            getAudioByteStream = mockGetAudioByteStream,
            uuid = testUUID
        )

        // Then
        assertEquals(5L, listableTrack.id)
        assertEquals("Direct Constructor Track", listableTrack.name)
        assertEquals("Direct Artist", listableTrack.artistName)
        assertEquals(testUri1, listableTrack.audioUri)
        assertEquals(testUri2, listableTrack.imageUri)
        assertEquals(60000L, listableTrack.duration)
        assertEquals(testUUID.hashCode(), listableTrack.hashCode())
    }

    @Test
    fun `equals should return true for same ListableTrackModel instances with same UUID`() {
        // Given
        val testUUID = UUID.randomUUID()
        val listableTrack1 = ListableTrackModel(
            id = 1L,
            name = "Track",
            artistName = "Artist",
            audioUri = testUri1,
            imageUri = testUri2,
            duration = 30000L,
            uuid = testUUID
        )
        val listableTrack2 = ListableTrackModel(
            id = 2L, // Different ID
            name = "Different Track", // Different name
            artistName = "Different Artist", // Different artist
            audioUri = testUri1,
            imageUri = testUri2,
            duration = 45000L, // Different duration
            uuid = testUUID // Same UUID
        )

        // When/Then
        assertEquals(listableTrack1, listableTrack2)
        assertTrue(listableTrack1.equals(listableTrack2))
    }

    @Test
    fun `equals should return false for ListableTrackModel instances with different UUIDs`() {
        // Given
        val listableTrack1 = ListableTrackModel(testTrack)
        val listableTrack2 = ListableTrackModel(testTrack)

        // When/Then
        assertNotEquals(listableTrack1, listableTrack2)
        assertFalse(listableTrack1.equals(listableTrack2))
    }

    @Test
    fun `equals should return false for different object types`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)
        val regularTrack = testTrack

        // When/Then
        assertNotEquals(listableTrack, regularTrack)
        assertFalse(listableTrack.equals(regularTrack))
        assertFalse(listableTrack.equals("string"))
        assertFalse(listableTrack.equals(null))
        assertFalse(listableTrack.equals(42))
    }

    @Test
    fun `equals should return true for same instance`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)

        // When/Then
        assertEquals(listableTrack, listableTrack)
        assertTrue(listableTrack.equals(listableTrack))
    }

    @Test
    fun `hashCode should return same value for equal ListableTrackModel instances`() {
        // Given
        val testUUID = UUID.randomUUID()
        val listableTrack1 = ListableTrackModel(
            id = 1L,
            name = "Track",
            artistName = "Artist",
            audioUri = testUri1,
            imageUri = testUri2,
            duration = 30000L,
            uuid = testUUID
        )
        val listableTrack2 = ListableTrackModel(
            id = 2L,
            name = "Different Track",
            artistName = "Different Artist",
            audioUri = testUri1,
            imageUri = testUri2,
            duration = 45000L,
            uuid = testUUID
        )

        // When/Then
        assertEquals(listableTrack1.hashCode(), listableTrack2.hashCode())
        assertEquals(testUUID.hashCode(), listableTrack1.hashCode())
    }

    @Test
    fun `hashCode should return different values for different UUIDs`() {
        // Given
        val listableTrack1 = ListableTrackModel(testTrack)
        val listableTrack2 = ListableTrackModel(testTrack)

        // When/Then
        assertNotEquals(listableTrack1.hashCode(), listableTrack2.hashCode())
    }

    @Test
    fun `should inherit all TrackEntity properties correctly`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)

        // When/Then - Verify inheritance
        assertTrue(listableTrack is TrackEntity)
        
        // Verify all inherited properties are accessible
        assertNotNull(listableTrack.id)
        assertNotNull(listableTrack.name)
        assertNotNull(listableTrack.artistName)
        assertNotNull(listableTrack.audioUri)
        assertNotNull(listableTrack.duration)
        assertNotNull(listableTrack.getInputStream)
    }

    @Test
    fun `getAudioByteStream should delegate to inherited functionality`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)
        var callbackInvoked = false
        var receivedInputStream: InputStream? = null

        // When
        listableTrack.getAudioByteStream { inputStream ->
            callbackInvoked = true
            receivedInputStream = inputStream
        }

        // Then
        assertTrue(callbackInvoked)
        assertEquals(mockInputStream, receivedInputStream)
    }

    @Test
    fun `constructor with null imageUri should handle correctly`() {
        // Given
        val trackWithNullImage = mockk<TrackEntity>()
        every { trackWithNullImage.id } returns 3L
        every { trackWithNullImage.name } returns "Track No Image"
        every { trackWithNullImage.artistName } returns "Artist"
        every { trackWithNullImage.audioUri } returns testUri1
        every { trackWithNullImage.imageUri } returns null
        every { trackWithNullImage.duration } returns 40000L
        every { trackWithNullImage.getInputStream } returns { uri, callback -> 
            callback(mockInputStream)
        }

        // When
        val listableTrack = ListableTrackModel(trackWithNullImage)

        // Then
        assertEquals(3L, listableTrack.id)
        assertEquals("Track No Image", listableTrack.name)
        assertEquals("Artist", listableTrack.artistName)
        assertEquals(testUri1, listableTrack.audioUri)
        assertNull(listableTrack.imageUri)
        assertEquals(40000L, listableTrack.duration)
    }

    @Test
    fun `constructor with default getAudioByteStream should work`() {
        // Given
        val testUUID = UUID.randomUUID()

        // When - Using default getAudioByteStream parameter
        val listableTrack = ListableTrackModel(
            id = 10L,
            name = "Default Track",
            artistName = "Default Artist",
            audioUri = testUri1,
            imageUri = testUri2,
            duration = 50000L,
            uuid = testUUID
        )

        // Then
        assertEquals(10L, listableTrack.id)
        assertEquals("Default Track", listableTrack.name)
        assertEquals("Default Artist", listableTrack.artistName)
        assertEquals(testUri1, listableTrack.audioUri)
        assertEquals(testUri2, listableTrack.imageUri)
        assertEquals(50000L, listableTrack.duration)
    }

    @Test
    fun `equals contract should be reflexive`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)

        // When/Then - Reflexive: x.equals(x) should return true
        assertTrue(listableTrack.equals(listableTrack))
    }

    @Test
    fun `equals contract should be symmetric`() {
        // Given
        val testUUID = UUID.randomUUID()
        val listableTrack1 = ListableTrackModel(
            id = 1L, name = "Track", artistName = "Artist",
            audioUri = testUri1, imageUri = testUri2, duration = 30000L, uuid = testUUID
        )
        val listableTrack2 = ListableTrackModel(
            id = 1L, name = "Track", artistName = "Artist",
            audioUri = testUri1, imageUri = testUri2, duration = 30000L, uuid = testUUID
        )

        // When/Then - Symmetric: x.equals(y) should return the same as y.equals(x)
        assertTrue(listableTrack1.equals(listableTrack2))
        assertTrue(listableTrack2.equals(listableTrack1))
    }

    @Test
    fun `equals contract should be transitive`() {
        // Given
        val testUUID = UUID.randomUUID()
        val listableTrack1 = ListableTrackModel(
            id = 1L, name = "Track", artistName = "Artist",
            audioUri = testUri1, imageUri = testUri2, duration = 30000L, uuid = testUUID
        )
        val listableTrack2 = ListableTrackModel(
            id = 2L, name = "Different", artistName = "Different",
            audioUri = testUri1, imageUri = testUri2, duration = 40000L, uuid = testUUID
        )
        val listableTrack3 = ListableTrackModel(
            id = 3L, name = "Another", artistName = "Another",
            audioUri = testUri1, imageUri = testUri2, duration = 50000L, uuid = testUUID
        )

        // When/Then - Transitive: if x.equals(y) and y.equals(z), then x.equals(z)
        assertTrue(listableTrack1.equals(listableTrack2))
        assertTrue(listableTrack2.equals(listableTrack3))
        assertTrue(listableTrack1.equals(listableTrack3))
    }

    @Test
    fun `equals contract should be consistent`() {
        // Given
        val listableTrack1 = ListableTrackModel(testTrack)
        val listableTrack2 = ListableTrackModel(testTrack)

        // When/Then - Consistent: multiple invocations should return same result
        val firstCall = listableTrack1.equals(listableTrack2)
        val secondCall = listableTrack1.equals(listableTrack2)
        val thirdCall = listableTrack1.equals(listableTrack2)

        assertEquals(firstCall, secondCall)
        assertEquals(secondCall, thirdCall)
    }

    @Test
    fun `equals contract should handle null correctly`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)

        // When/Then - Null: x.equals(null) should return false
        assertFalse(listableTrack.equals(null))
    }

    @Test
    fun `hashCode contract should be consistent with equals`() {
        // Given
        val testUUID = UUID.randomUUID()
        val listableTrack1 = ListableTrackModel(
            id = 1L, name = "Track", artistName = "Artist",
            audioUri = testUri1, imageUri = testUri2, duration = 30000L, uuid = testUUID
        )
        val listableTrack2 = ListableTrackModel(
            id = 2L, name = "Different", artistName = "Different",
            audioUri = testUri1, imageUri = testUri2, duration = 40000L, uuid = testUUID
        )

        // When/Then - If objects are equal, their hash codes must be equal
        assertTrue(listableTrack1.equals(listableTrack2))
        assertEquals(listableTrack1.hashCode(), listableTrack2.hashCode())
    }

    @Test
    fun `hashCode should be consistent across multiple calls`() {
        // Given
        val listableTrack = ListableTrackModel(testTrack)

        // When
        val hash1 = listableTrack.hashCode()
        val hash2 = listableTrack.hashCode()
        val hash3 = listableTrack.hashCode()

        // Then - Hash code should be consistent
        assertEquals(hash1, hash2)
        assertEquals(hash2, hash3)
    }
}