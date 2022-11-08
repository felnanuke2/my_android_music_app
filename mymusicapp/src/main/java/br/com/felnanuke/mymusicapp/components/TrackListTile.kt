package br.com.felnanuke.mymusicapp.components

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImagePainter


@Composable
fun TrackListTile(
    modifier: Modifier = Modifier,
    trackEntity: TrackEntity,
    playTrack: (TrackEntity) -> Unit,
    insertTrack: (TrackEntity) -> Unit,
    playing: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,

    ) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                playTrack(trackEntity)
            }
            .padding(vertical = 2.dp)) {
        if (leadingIcon != null) {
            leadingIcon()
        } else {
            Card(
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = trackEntity.imageUri,
                        contentDescription = "Track Image",
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.Low,
                        fallback = painterResource(R.drawable.ic_baseline_audiotrack_24),
                        modifier = Modifier.clip(
                            RoundedCornerShape(8f)

                        )
                    )
                }

            }
        }
        Column(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = trackEntity.name, style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                text = trackEntity.artistName,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.ExtraLight)
            )
        }
        if (trailingIcon != null) {
            trailingIcon()
        } else {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, null)

                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        insertTrack(trackEntity)
                    }) {

                        Icon(
                            painterResource(id = R.drawable.ic_baseline_playlist_play_24),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Add to Playlist")
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
        TrackListTile(Modifier,TrackEntity(
            0,
            "Todo Mundo Vai Sofrer",
            "Marilia Mendonça",
            Uri.parse(""),
            Uri.parse(""),
            duration = 20000
        ), {}, {})
    }
}