package br.com.felnanuke.mymusicapp.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Colors
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.felnanuke.mymusicapp.R
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.screens.CdAnimation

@Composable
fun PlayerCollapsed(
    trackEntity: TrackEntity,
    progress: Float,
    playing: Boolean,
    togglePlay: () -> Unit,
    openExpandedPlayerActivity: () -> Unit
) {
    Card(backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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
                    trackEntity = trackEntity, playing = playing, size = 50.dp, padding = 0.dp,
                )
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    MarqueeText(
                        text = trackEntity.name,
                        style = MaterialTheme.typography.titleSmall,
                        activated = playing,
                        gradientEdgeColor = if (playing) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

                    )
                    Text(
                        text = trackEntity.artistName,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraLight,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                    )
                }
                /// icon button to toggle play or pause
                IconButton(onClick = { togglePlay() }) {
                    Icon(
                        painter = painterResource(id = if (playing) R.drawable.ic_baseline_pause_circle_outline_24 else R.drawable.ic_baseline_play_circle_outline_24),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer

                    )
                }


            }
            Box(modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp)) {

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = progress,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,

                    )
            }
        }


    }


}

@Preview
@Composable
fun PlayerCollapsedPreview() {
    PlayerCollapsed(TrackEntity(
        0, "Track 2", "Artist 2", Uri.parse(""), Uri.parse(""), 23
    ), playing = true, progress = 0.5f, togglePlay = {}, openExpandedPlayerActivity = {})

}