package br.com.felnanuke.mymusicapp.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.felnanuke.mymusicapp.components.PlayerCollapsed
import br.com.felnanuke.mymusicapp.components.TrackListTile
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.view_models.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "My Music",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !homeViewModel.hasRequiredPermissions -> {
                    PermissionRequiredScreen(
                        onRequestPermission = { homeViewModel.requestPermissions() }
                    )
                }
                homeViewModel.loading -> {
                    LoadingScreen()
                }
                homeViewModel.tracks.isEmpty() -> {
                    EmptyTracksScreen()
                }
                else -> {
                    TracksList(
                        currentTrack = homeViewModel.currentTrack,
                        tracks = homeViewModel.tracks,
                        playTrack = { track ->
                            homeViewModel.playTrack(track)
                        },
                        insertTrack = { track ->
                            homeViewModel.insertTrackToPlayList(track)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),

                    )
                }
            }

            // Animated bottom player
            AnimatedVisibility(
                visible = homeViewModel.currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                homeViewModel.currentTrack?.let { track ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 4.dp
                    ) {
                        PlayerCollapsed(
                            currentTrack = track,
                            playing = homeViewModel.isPlaying,
                            progress = homeViewModel.trackProgress,
                            togglePlay = { homeViewModel.togglePlayPause() },
                            openExpandedPlayerActivity = homeViewModel::openExpandedPlayer,
                            getTrack = homeViewModel::getTrack,
                            getCurrentTrackIndex = { homeViewModel.getTrackIndex(track) },
                            queue = homeViewModel.queue,
                            setTrack = homeViewModel::setTrack,
                            duration = track.duration,
                            currentTime = homeViewModel.trackPositionMillis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Loading your music...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyTracksScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp)),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = br.com.felnanuke.mymusicapp.R.drawable.ic_baseline_audiotrack_24),
                    contentDescription = "No Music",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No music found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add some music to your device to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TracksList(
    tracks: List<TrackEntity>,
    playTrack: (TrackEntity) -> Unit = {},
    insertTrack: (TrackEntity) -> Unit = {},
    currentTrack: TrackEntity? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 96.dp // Space for bottom player
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Header section
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = "${tracks.size} tracks",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        items(
            items = tracks,
            key = { track -> track.id }
        ) { track ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = if (currentTrack?.id == track.id) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                tonalElevation = if (currentTrack?.id == track.id) 4.dp else 0.dp
            ) {
                TrackListTile(
                    modifier = Modifier.padding(4.dp),
                    trackEntity = track,
                    playTrack = playTrack,
                    insertTrack = insertTrack,
                    playing = currentTrack?.id == track.id,
                    activateAnimation = currentTrack?.id == track.id,
                    leadingIcon = if (currentTrack?.id == track.id) {
                        { 
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Currently playing",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun PermissionRequiredScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container with background
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp)),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Permission Required",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This app needs access to your audio files to play music. Please grant the required permissions to continue.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        FilledTonalButton(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Grant Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Preview(showBackground = true, name = "Permission Required")
@Composable
fun PermissionRequiredPreview() {
    MaterialTheme {
        PermissionRequiredScreen(
            onRequestPermission = { }
        )
    }
}

@Preview(showBackground = true, name = "Loading Screen")
@Composable
fun LoadingScreenPreview() {
    MaterialTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true, name = "Empty Tracks")
@Composable
fun EmptyTracksScreenPreview() {
    MaterialTheme {
        EmptyTracksScreen()
    }
}

@Preview(showBackground = true, name = "Tracks List")
@Composable
fun TracksListPreview() {
    MaterialTheme {
        TracksList(
            tracks = listOf(
                TrackEntity(
                    0, "Todo Mundo Vai Sofrer", "Marilia Mendonça", Uri.parse(""), Uri.parse(""), 180000
                ),
                TrackEntity(
                    1, "Bem Que Se Avisei", "Marilia Mendonça", Uri.parse(""), Uri.parse(""), 200000
                ),
                TrackEntity(
                    2, "Supera", "Marilia Mendonça", Uri.parse(""), Uri.parse(""), 160000
                )
            ),
            currentTrack = TrackEntity(
                0, "Todo Mundo Vai Sofrer", "Marilia Mendonça", Uri.parse(""), Uri.parse(""), 180000
            )
        )
    }
}