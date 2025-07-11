package com.linc.audiowaveform

import android.view.MotionEvent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment

private val MinSpikeWidthDp: Dp = 1.dp
private val MaxSpikeWidthDp: Dp = 24.dp
private val MinSpikePaddingDp: Dp = 0.dp
private val MaxSpikePaddingDp: Dp = 12.dp
private val MinSpikeRadiusDp: Dp = 0.dp
private val MaxSpikeRadiusDp: Dp = 12.dp

private const val MinProgress: Float = 0F
private const val MaxProgress: Float = 1F

private const val MinSpikeHeight: Float = 1F
private const val DefaultGraphicsLayerAlpha: Float = 0.99F
private const val AmplitudeMultiplier: Float = 2F

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    style: DrawStyle = Fill,
    waveformBrush: Brush = SolidColor(Color.White),
    progressBrush: Brush = SolidColor(Color.Blue),
    waveformAlignment: WaveformAlignment = WaveformAlignment.Center,
    amplitudeType: AmplitudeType = AmplitudeType.Avg,
    onProgressChangeFinished: (() -> Unit)? = null,
    spikeAnimationSpec: AnimationSpec<Float> = tween(500),
    spikeWidth: Dp = 4.dp,
    spikeRadius: Dp = 2.dp,
    spikePadding: Dp = 1.dp,
    height: Dp = 100.dp,
    progress: Float = 0F,
    amplitudes: List<Int>,
    onProgressChange: (Float) -> Unit
) {
    val _progress = remember(progress) { progress.coerceIn(MinProgress, MaxProgress) }
    val _spikeWidth = remember(spikeWidth) { spikeWidth.coerceIn(MinSpikeWidthDp, MaxSpikeWidthDp) }
    val _spikePadding = remember(spikePadding) { spikePadding.coerceIn(MinSpikePaddingDp, MaxSpikePaddingDp) }
    val _spikeRadius = remember(spikeRadius) { spikeRadius.coerceIn(MinSpikeRadiusDp, MaxSpikeRadiusDp) }
    val _spikeTotalWidth = remember(spikeWidth, spikePadding) { _spikeWidth + _spikePadding }
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }
    var spikes by remember { mutableStateOf(0F) }
    var maxAmplitude = amplitudes.maxOrNull() ?: 0
    val spikesAmplitudes = remember(amplitudes, spikes, amplitudeType) {
        getSpikesAmplitudes(
            amplitudeType = amplitudeType,
            amplitudes = amplitudes,
            spikes = spikes.toInt(),
            minHeight = MinSpikeHeight,
            maxHeight = canvasSize.height.coerceAtLeast(MinSpikeHeight)
        )
    }.map { animateFloatAsState(it, spikeAnimationSpec).value }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .requiredHeight(height)
            .graphicsLayer(alpha = DefaultGraphicsLayerAlpha)
            .pointerInteropFilter {
                return@pointerInteropFilter when (it.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (it.x in 0F..canvasSize.width) {
                            onProgressChange(it.x / canvasSize.width)
                            true
                        } else false
                    }
                    MotionEvent.ACTION_UP -> {
                        onProgressChangeFinished?.invoke()
                        true
                    }
                    else -> false
                }
            }
            .then(modifier)
    ) {
        canvasSize = size
        spikes = size.width / _spikeTotalWidth.toPx()
        spikesAmplitudes.forEachIndexed { index, amplitude ->
            val height = (canvasSize.height * 0.35f) * (  amplitude/ maxAmplitude)
            drawRoundRect(
                brush = waveformBrush,
                topLeft = Offset(
                    x = index * _spikeTotalWidth.toPx(),
                    y = when(waveformAlignment) {
                        WaveformAlignment.Top -> 0F
                        WaveformAlignment.Bottom -> size.height - height
                        WaveformAlignment.Center -> size.height / 2F - height / 2F
                    }
                ),
                size = Size(
                    width = _spikeWidth.toPx(),
                    height = height
                ),
                cornerRadius = CornerRadius(_spikeRadius.toPx(), _spikeRadius.toPx()),
                style = style
            )
            drawRect(
                brush = progressBrush,
                size = Size(
                    width = _progress * size.width,
                    height = size.height
                ),
                blendMode = BlendMode.SrcAtop
            )
        }
    }
}

private fun getSpikesAmplitudes(
    amplitudeType: AmplitudeType,
    amplitudes: List<Int>,
    spikes: Int,
    minHeight: Float,
    maxHeight: Float
): List<Float> {
    if(amplitudes.isEmpty() || spikes == 0) {
        return List(spikes) { minHeight }
    }
    if(amplitudes.count() < spikes) {
        return amplitudes.map(Int::toFloat)
    }
    return amplitudes.map(Int::toFloat)
        .chunkedToSize(spikes) {
            when(amplitudeType) {
                AmplitudeType.Avg -> it.average()
                AmplitudeType.Max -> it.maxOrNull() ?: minHeight
                AmplitudeType.Min -> it.minOrNull() ?: minHeight
            }.toFloat().times(AmplitudeMultiplier).coerceIn(minHeight, maxHeight)
        }
}


