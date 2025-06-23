package br.com.felnanuke.mymusicapp.core.infrastructure.android.audio

import br.com.felnanuke.mymusicapp.core.domain.data_sources.IAudioWaveformProcessor
import java.io.InputStream

/**
 * Mock implementation of IAudioWaveformProcessor for testing purposes.
 * This mock returns predefined amplitude data without requiring native libraries.
 */
class MockAudioWaveformProcessor : IAudioWaveformProcessor {
    
    var shouldSucceed: Boolean = true
    var mockAmplitudes: List<Int> = listOf(10, 20, 30, 15, 25, 35, 40, 20, 10, 5)
    var mockError: Exception = Exception("Mock error for testing")
    
    override fun processAudio(
        inputStream: InputStream,
        onSuccess: (List<Int>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (shouldSucceed) {
            onSuccess(mockAmplitudes)
        } else {
            onError(mockError)
        }
    }
}
