package br.com.felnanuke.mymusicapp.core.infrastructure.android.audio

import br.com.felnanuke.mymusicapp.core.domain.data_sources.IAudioWaveformProcessor
import linc.com.amplituda.Amplituda
import java.io.InputStream

/**
 * Implementation of IAudioWaveformProcessor using the Amplituda library.
 * This class wraps the native Amplituda functionality to provide a testable interface.
 */
class AmplitudaAudioWaveformProcessor(private val amplituda: Amplituda) : IAudioWaveformProcessor {
    
    override fun processAudio(
        inputStream: InputStream,
        onSuccess: (List<Int>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            amplituda.processAudio(inputStream).get({ result ->
                onSuccess(result.amplitudesAsList())
            }, { error ->
                onError(Exception(error.message, error))
            })
        } catch (e: Exception) {
            onError(e)
        }
    }
}
