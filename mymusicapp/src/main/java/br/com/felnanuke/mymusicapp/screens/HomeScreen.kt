package br.com.felnanuke.mymusicapp.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.felnanuke.mymusicapp.components.PlayerCollapsed
import br.com.felnanuke.mymusicapp.components.TrackListTile
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.view_models.HomeViewModel


@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    Box(Modifier.padding(0.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (homeViewModel.loading!!) {
                CircularProgressIndicator()
            } else if (homeViewModel.tracks!!.isEmpty()) {
                Text(text = "No tracks found")
            } else {
                TracksList(currentTrack = homeViewModel.currentTrack,
                    tracks = homeViewModel.tracks!!,
                    playTrack = { track ->
                        homeViewModel.playTrack(track)
                    },
                    insertTrack = { track ->
                        homeViewModel.insertTrackToPlayList(track)
                    })

            }


        }
        if (homeViewModel.currentTrack != null) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                PlayerCollapsed(trackEntity = homeViewModel.currentTrack!!,
                    playing = homeViewModel.isPlaying,
                    progress = homeViewModel.trackProgress,
                    togglePlay = { homeViewModel.togglePlayPause() },
                    openExpandedPlayerActivity = { homeViewModel.openExpandedPlayer() })
            }
        }
    }


}

@Composable
fun TracksList(
    tracks: List<TrackEntity>,
    playTrack: (TrackEntity) -> Unit = {},
    insertTrack: (TrackEntity) -> Unit = {},
    currentTrack: TrackEntity? = null,

    ) {
    LazyColumn(contentPadding = PaddingValues(bottom = 64.dp)) {
        items(tracks.size) { index ->
            TrackListTile(
                tracks[index],
                playTrack = playTrack,
                insertTrack = insertTrack,
                currentTrack == tracks[index]
            )
        }
    }
}


@Preview(showBackground = true, name = "Body")
@Composable
fun DefaultPreview() {
    Text(text = "Hello World!")

}

@Preview(showBackground = true, name = "List")
@Composable
fun ListPreview() {
    TracksList(
        tracks = listOf(
            TrackEntity(
                "Todo Mundo Vai Sofrer", "Marilia Mendonça", Uri.parse(""), Uri.parse(""), 2000
            )
        ),
    )

}