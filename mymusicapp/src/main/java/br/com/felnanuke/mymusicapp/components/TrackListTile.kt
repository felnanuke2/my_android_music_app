package br.com.felnanuke.mymusicapp.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
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


@Composable
fun TrackListTile(
    trackEntity: TrackEntity,
    playTrack: (TrackEntity) -> Unit,
    insertTrack: (TrackEntity) -> Unit,
    playing: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        contentColor = if (playing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.background,
        elevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,

            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    playTrack(trackEntity)
                }
                .padding(vertical = 2.dp)) {
            Card(
                backgroundColor = colorResource(id = R.color.purple_200),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(id = R.drawable.music_solid),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)

                    )
                }
                AsyncImage(
                    model = trackEntity.imageUri,
                    contentDescription = "Track Image",
                    modifier = Modifier.clip(
                        RoundedCornerShape(8f)

                    )
                )
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

            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, null)

                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        insertTrack(trackEntity)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.List,
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
        TrackListTile(TrackEntity(
            "Todo Mundo Vai Sofrer",
            "Marilia Mendonça",
            Uri.parse(""),
            Uri.parse(""),
            duration = 20000
        ), {}, {})
    }
}