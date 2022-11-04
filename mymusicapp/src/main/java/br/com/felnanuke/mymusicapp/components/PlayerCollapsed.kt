package br.com.felnanuke.mymusicapp.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.felnanuke.mymusicapp.CdAnimation
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PlayerCollapsed(
    trackEntity: TrackEntity,
    getProgress: () -> Float,
    getIsPlaying: () -> Boolean,
    togglePlay: () -> Unit,
    openExpandedPlayerActivity: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var playing by remember { mutableStateOf(true) }

    Card(backgroundColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .padding(0.dp)
            .clickable {
                openExpandedPlayerActivity()
            }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row {
                CdAnimation(
                    trackEntity = trackEntity,
                    getIsPlaying = getIsPlaying,
                    size = 50.dp,
                    padding = 0.dp
                )
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = trackEntity.name, style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        ), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = trackEntity.artistName,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraLight,
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                    )
                }
                /// icon button to toggle play or pause
                IconButton(onClick = { togglePlay() }) {
                    Icon(
                        painter = painterResource(id = if (playing) R.drawable.ic_baseline_pause_circle_outline_24 else R.drawable.ic_baseline_play_circle_outline_24),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.onPrimary

                    )
                }


            }
            Box(modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp)) {
                LaunchedEffect(Unit) {
                    while (isActive) {
                        progress = getProgress()
                        playing = getIsPlaying()
                        delay(80)
                    }
                }
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = progress,
                    color = MaterialTheme.colorScheme.onPrimary,

                    )
            }
        }


    }


}

@Preview
@Composable
fun PlayerCollapsedPreview() {
    PlayerCollapsed(TrackEntity(
        "Track 2", "Artist 2", Uri.parse(""), Uri.parse(""), 23
    ),
        getIsPlaying = { false },
        getProgress = { 0.5f },
        togglePlay = {},
        openExpandedPlayerActivity = {})

}