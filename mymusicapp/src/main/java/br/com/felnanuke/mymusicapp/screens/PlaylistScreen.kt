package br.com.felnanuke.mymusicapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.felnanuke.mymusicapp.view_models.MusicPlayerViewModel

@Composable
fun PlaylistScreen(viewModel: MusicPlayerViewModel) {
    Box(Modifier.padding(0.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (viewModel.loading) {
                CircularProgressIndicator()
            } else if (viewModel.queueTracks.isEmpty()) {
                Text(text = "No tracks found")
            } else {
                TracksList(currentTrack = viewModel.currentTrack,
                    tracks = viewModel.queueTracks,
                    playTrack = { track ->
                    },
                    insertTrack = { track ->
                    })
            }
        }

    }
}


}