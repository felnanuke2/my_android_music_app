package br.com.felnanuke.mymusicapp.core.domain.data_sources

import java.io.InputStream

/**
 * Interface for audio waveform processing functionality.
 * This abstraction allows for easy testing by avoiding direct dependency on native libraries.
 */
interface IAudioWaveformProcessor {
    /**
     * Process audio from an input stream and return amplitudes as a list of integers.
     * 
     * @param inputStream The audio input stream to process
     * @param onSuccess Callback function called with the list of amplitudes when processing succeeds
     * @param onError Callback function called with an exception when processing fails
     */
    fun processAudio(
        inputStream: InputStream,
        onSuccess: (List<Int>) -> Unit,
        onError: (Exception) -> Unit
    )
}
