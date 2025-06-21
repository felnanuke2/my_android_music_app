package br.com.felnanuke.mymusicapp.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.screens.CdAnimation
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PlayerCollapsed(
    currentTrack: TrackEntity,
    progress: Float,
    playing: Boolean,
    togglePlay: () -> Unit,
    openExpandedPlayerActivity: () -> Unit,
    getCurrentTrackIndex: () -> Int = { 0 },
    queue: List<TrackEntity> = listOf(),
    getTrack: ((Int) -> TrackEntity)? = null,
    setTrack: ((Int) -> Unit)? = null,
    currentTime: Long = 0, // Current playing time in milliseconds
    duration: Long = 0L, // Total duration in milliseconds
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openExpandedPlayerActivity() },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                CdAnimation(
                    trackEntity = currentTrack, 
                    playing = playing, 
                    size = 56.dp, 
                    padding = 0.dp,
                )

                val pageState = rememberPagerState(getCurrentTrackIndex.invoke())

                LaunchedEffect(key1 = pageState) {
                    if (pageState.currentPage != getCurrentTrackIndex.invoke()) {
                        pageState.scrollToPage(getCurrentTrackIndex.invoke())
                    }
                    snapshotFlow {
                        pageState.currentPage
                    }.collect { page ->
                        setTrack?.invoke(page)
                    }
                }

                LaunchedEffect(key1 = currentTrack) {
                    snapshotFlow { getCurrentTrackIndex.invoke() }.collect { index ->
                        pageState.animateScrollToPage(index)

                    }
                }

                HorizontalPager(
                    count = queue.count(),
                    state = pageState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                ) { page ->
                    getTrack?.invoke(page)?.let { track ->
                        val isPlaying = track == currentTrack && playing
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            MarqueeText(
                                text = track.name,
                                style = MaterialTheme.typography.bodyLarge,
                                activated = isPlaying,
                                gradientEdgeColor = if (isPlaying) 
                                    MaterialTheme.colorScheme.primaryContainer else
                                    Color.Transparent
                            )
                            Text(
                                text = track.artistName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Play/Pause button
                FilledIconButton(
                    onClick = { togglePlay() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (playing) 
                                R.drawable.ic_baseline_pause_circle_outline_24 
                            else 
                                R.drawable.ic_baseline_play_circle_outline_24
                        ),
                        contentDescription = if (playing) "Pause" else "Play",
                        modifier = Modifier.size(24.dp)
                    )
                }


            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar and time display
            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PlayerCollapsedPreview() {
    PlayerCollapsed(
        TrackEntity(
            0, "Track 2", "Artist 2", Uri.parse(""), Uri.parse(""), 23
        ),
        playing = true, 
        progress = 0.5f, 
        togglePlay = {}, 
        openExpandedPlayerActivity = {},
        currentTime = 90000L, // 1:30
        duration = 180000L, // 3:00
    )
}