package br.com.felnanuke.mymusicapp.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.components.MarqueeText
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.ui.theme.MyMusicAppTheme
import br.com.felnanuke.mymusicapp.view_models.MusicPlayerViewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.linc.audiowaveform.AudioWaveform
import com.linc.audiowaveform.model.AmplitudeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun PlayerScreen(viewModel: MusicPlayerViewModel, popRoute: () -> Unit = {}) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(text = "Playing") }, navigationIcon = {
            IconButton(onClick = popRoute) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }

        }, actions = {
            IconButton(onClick = viewModel::openPlaylist) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_playlist_play_24),
                    contentDescription = null
                )
            }
        })
    }) {
        BoxWithConstraints {
            val constrained = this
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pageState = rememberPagerState(viewModel.trackIndex(viewModel.currentTrack))

                LaunchedEffect(key1 = pageState) {
                    if (pageState.currentPage != viewModel.trackIndex) {
                        pageState.scrollToPage(viewModel.trackIndex(viewModel.currentTrack))
                    }
                    snapshotFlow {
                        pageState.currentPage
                    }.collect { page ->
                        viewModel.setCurrentTrack(page)
                    }
                }

                LaunchedEffect(key1 = viewModel.currentTrack) {
                    snapshotFlow { viewModel.trackIndex(viewModel.currentTrack) }.collect { index ->
                        pageState.animateScrollToPage(index)

                    }
                }

                HorizontalPager(count = viewModel.queueTracks.count(), state = pageState) {
                    Column {
                        Box(modifier = Modifier.padding(horizontal = 32.dp)) {
                            CdAnimation(
                                trackEntity = viewModel.queueTracks[it],
                                viewModel.playing && viewModel.currentTrack == viewModel.queueTracks[it],
                                padding = 0.dp,
                                size = constrained.maxWidth
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Box(modifier = Modifier.padding(horizontal = 32.dp)) {
                            MarqueeText(
                                text = viewModel.queueTracks[it].name,
                                style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                                gradientEdgeColor = if (viewModel.playing) MaterialTheme.colorScheme.background else Color.Transparent,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                activated = viewModel.playing
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            MarqueeText(
                                text = viewModel.queueTracks[it].artistName,
                                style = MaterialTheme.typography.titleSmall.copy(textAlign = TextAlign.Center),
                                gradientEdgeColor = if (viewModel.playing) MaterialTheme.colorScheme.background else Color.Transparent,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                activated = viewModel.playing
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.padding(horizontal = 32.dp)) {


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.trackPositionMillis.let(viewModel::formatMillis),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            WaveForm(
                                viewModel.amplitudes,
                                viewModel.trackProgress,
                                onProgressChanged = viewModel::onProgressChanged,
                                onProgressChangedFinished = viewModel::onProgressFinished
                            )
                        }
                        Text(
                            text = viewModel.trackDuration.let {
                                viewModel.formatMillis(it)
                            }, style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 32.dp)
                ) {

                    MediaControllers(
                        isPlaying = viewModel.playing,
                        toggle = viewModel::togglePlay,
                        canSkipNext = viewModel.canPlayNext,
                        skipNext = viewModel::playNext,
                        skipPrevious = viewModel::playPrevious
                    )
                }

                Spacer(modifier = Modifier.weight(1f))


            }

        }
    }

}

///receive a waveform as fft and draw a waveform
@Composable
fun WaveForm(
    amplitudes: List<Int>,
    progress: Float,
    onProgressChanged: (Float) -> Unit = {},
    onProgressChangedFinished: () -> Unit = {}
) {


    AudioWaveform(
        amplitudes = amplitudes,
        progress = progress,
        height = 68.dp,
        onProgressChange = onProgressChanged,
        onProgressChangeFinished = onProgressChangedFinished,
        waveformBrush = SolidColor(Color.Gray),
        progressBrush = SolidColor(MaterialTheme.colorScheme.primary),
        amplitudeType = AmplitudeType.Avg,

        )


}


@Composable
fun MediaControllers(
    isPlaying: Boolean,
    toggle: () -> Unit,
    skipNext: () -> Unit = {},
    skipPrevious: () -> Unit = {},
    canSkipNext: Boolean = false,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically


    ) {

        IconButton(onClick = skipPrevious) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_skip_previous_24),
                contentDescription = "Skip previous",
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        IconButton(onClick = toggle) {
            Icon(
                painter = painterResource(id = if (isPlaying) R.drawable.ic_baseline_pause_circle_filled_24 else R.drawable.ic_baseline_play_circle_filled_24),
                contentDescription = "Play",
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        IconButton(onClick = skipNext, enabled = canSkipNext) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_skip_next_24),
                contentDescription = "Skip next",
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
fun CdAnimation(
    trackEntity: TrackEntity, playing: Boolean, padding: Dp = 8.dp, size: Dp = 200.dp
) {
    var rotation by remember {
        mutableStateOf(0f)
    }

    val speed by animateFloatAsState(
        targetValue = if (playing) 0.4f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),

        )


    LaunchedEffect(playing, speed, rotation) {
        while (isActive) {
            rotation += speed
            if (rotation > 360f) {
                rotation = 0f
            }
            delay(10)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .size(size)
            .aspectRatio(1f)
            .clip(CircleShape)
            .padding(padding)
            .shadow(elevation = 16.dp)

    ) {
        val boxWithConstraints = this

        Box(
            Modifier
                .aspectRatio(1f)
                .clip(shape = CircleShape)
                .rotate(rotation)
        ) {
            Image(painter = painterResource(id = R.drawable.vinyl), contentDescription = null)

            AsyncImage(
                model = trackEntity.imageUri,

                contentDescription = null, contentScale = ContentScale.Crop,

                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(shape = CircleShape)
                    .rotate(rotation)

            )
        }

        /// draw a circle in the center of the image
        Box(
            modifier = Modifier
                .size(boxWithConstraints.maxWidth * 0.16f)
                .aspectRatio(1f)
                .clip(shape = CircleShape)
                .background(color = MaterialTheme.colorScheme.background)
                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .size(boxWithConstraints.maxWidth * 0.23f)
                .aspectRatio(1f)
                .clip(shape = CircleShape)
                .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.14f))
                .align(Alignment.Center)
        )

    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    MyMusicAppTheme {
        MediaControllers(false, {})
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    MyMusicAppTheme {
        CdAnimation(
            trackEntity = TrackEntity(
                0, "Todo Mundo Vai Sofrer", "Marilia Mendonça", Uri.parse(""), Uri.parse(""), 2000
            ), playing = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview5() {
    MyMusicAppTheme {
        WaveForm(mockAmplitudes.toMutableList(), 0.5f)
    }
}


val mockAmplitudes = arrayOf(
    0,
    0,
    5,
    24,
    23,
    19,
    17,
    15,
    13,
    11,
    9,
    8,
    6,
    3,
    0,
    0,
    0,
    0,
    0,
    0,
    22,
    30,
    24,
    20,
    18,
    14,
    12,
    10,
    8,
    7,
    4,
    1,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    17,
    7,
    7,
    7,
    7,
    6,
    6,
    5,
    4,
    4,
    2,
    1,
    0,
    0,
    0,
    0,
    0,
    18,
    27,
    24,
    20,
    18,
    15,
    13,
    11,
    9,
    7,
    5,
    2,
    0,
    0,
    0,
    0,
    0,
    2,
    18,
    19,
    21,
    21,
    19,
    22,
    22,
    25,
    25,
    21,
    14,
    22,
    20,
    17,
    20,
    18,
    16,
    22,
    13,
    20,
    18,
    22,
    22,
    16,
    17,
    17,
    20,
    22,
    16,
    16,
    16,
    19,
    24,
    17,
    21,
    25,
    16,
    19,
    16,
    20,
    17,
    19,
    20,
    12,
    18,
    14,
    15,
    16,
    21,
    14,
    16,
    22,
    24,
    19,
    25,
    20,
    22,
    22,
    18,
    22,
    19,
    15,
    22,
    19,
    17,
    15,
    20,
    20,
    14,
    21,
    16,
    14,
    19,
    23,
    19,
    17,
    20,
    23,
    18,
    21,
    16,
    12,
    22,
    17,
    11,
    15,
    17,
    18,
    18,
    20,
    17,
    19,
    22,
    17,
    18,
    19,
    22,
    20,
    13,
    14,
    17,
    18,
    18,
    16,
    25,
    33,
    27,
    21,
    13,
    14,
    13,
    19,
    19,
    16,
    13,
    16,
    16,
    16,
    17,
    16,
    18,
    24,
    21,
    16,
    19,
    20,
    21,
    19,
    16,
    16,
    18,
    15,
    15,
    14,
    18,
    18,
    16,
    17,
    15,
    20,
    13,
    15,
    16,
    15,
    15,
    16,
    15,
    18,
    57,
    38,
    28,
    22,
    41,
    34,
    25,
    24,
    40,
    55,
    40,
    31,
    25,
    23,
)

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview2() {
//    MyMusicAppTheme {
////        Body(MusicPlayerViewModel())
//    }
//}