package br.com.felnanuke.mymusicapp.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.ui.theme.MyMusicAppTheme
import coil.compose.AsyncImage


// Helper function to format time in mm:ss format
fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
fun TrackListTile(
    modifier: Modifier = Modifier,
    trackEntity: TrackEntity,
    playTrack: (TrackEntity) -> Unit,
    insertTrack: (TrackEntity) -> Unit,
    playing: Boolean = false,
    currentPosition: Long = 0L, // Current playing time in milliseconds
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    activateAnimation: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = if (playing) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { playTrack(trackEntity) }
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = trackEntity.imageUri,
                            contentDescription = "Track Image",
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.Medium,
                            fallback = painterResource(R.drawable.ic_baseline_audiotrack_24),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f)
            ) {
                MarqueeText(
                    text = trackEntity.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (playing) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = FontWeight.Medium
                    ),
                    overflow = TextOverflow.Ellipsis,
                    activated = activateAnimation,
                    gradientEdgeColor = Color.Transparent
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Artist and duration row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trackEntity.artistName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Duration and current position
                    if (playing && currentPosition > 0) {
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(trackEntity.duration)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        Text(
                            text = formatTime(trackEntity.duration),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            
            if (trailingIcon != null) {
                trailingIcon()
            } else {
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to Playlist") },
                            onClick = {
                                expanded = false
                                insertTrack(trackEntity)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_playlist_play_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyMusicAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Regular track tile
                TrackListTile(
                    trackEntity = TrackEntity(
                        0,
                        "Todo Mundo Vai Sofrer",
                        "Marilia Mendonça",
                        Uri.parse(""),
                        Uri.parse(""),
                        duration = 180000 // 3 minutes
                    ),
                    playTrack = {},
                    insertTrack = {}
                )
                
                // Playing track tile with current position
                TrackListTile(
                    trackEntity = TrackEntity(
                        1,
                        "Bem Pior Que Eu - Uma música com nome muito longo para testar o overflow",
                        "Marilia Mendonça feat. Henrique & Juliano",
                        Uri.parse(""),
                        Uri.parse(""),
                        duration = 210000 // 3.5 minutes
                    ),
                    playTrack = {},
                    insertTrack = {},
                    playing = true,
                    currentPosition = 65000, // 1:05
                    activateAnimation = true
                )
                
                // Another regular track
                TrackListTile(
                    trackEntity = TrackEntity(
                        2,
                        "Supera",
                        "Marilia Mendonça",
                        Uri.parse(""),
                        Uri.parse(""),
                        duration = 195000 // 3:15
                    ),
                    playTrack = {},
                    insertTrack = {}
                )
            }
        }
    }
}