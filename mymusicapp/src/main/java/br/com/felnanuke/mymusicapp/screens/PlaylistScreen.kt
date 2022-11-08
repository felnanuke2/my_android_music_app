package br.com.felnanuke.mymusicapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.felnanuke.mymusicapp.components.TrackListTile
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.view_models.MusicPlayerViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(viewModel: MusicPlayerViewModel) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(text = "Playlist")
            }, navigationIcon = {
                IconButton(onClick = viewModel::popBackStack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
            }


            )
        },


        ) {
        Box(Modifier.padding(16.dp)) {
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
                    TracksQueue(
                        tracks = viewModel.queue,
                        playTrack = {},
                        currentTrack = viewModel.currentTrack,
                        onReorderQueue = viewModel::reorderQueue
                    )
                }
            }

        }
    }


}

@Composable
fun TracksQueue(
    tracks: List<TrackEntity>,
    playTrack: (TrackEntity) -> Unit,
    currentTrack: TrackEntity?,
    onReorderQueue: ((from: Int, to: Int) -> Unit)?,
) {
    val data = remember { mutableStateOf(tracks) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        onReorderQueue?.invoke(from.index, to.index)
    })
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        items(data.value.size, key = { it }) { index ->
            ReorderableItem(reorderableState = state, index = index, key = data) { isDragging ->
                TrackListTile(Modifier.shadow(if (isDragging) 16.dp else 0.dp),
                    data.value[index],
                    playTrack = playTrack,
                    insertTrack = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = br.com.felnanuke.mymusicapp.R.drawable.ic_baseline_list_24),
                            contentDescription = null
                        )
                    },
                    playing = data.value[index] == currentTrack,
                    trailingIcon = {

                    }


                )

            }
        }
    }


}


