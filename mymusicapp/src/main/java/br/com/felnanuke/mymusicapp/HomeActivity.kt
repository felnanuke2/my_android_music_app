package br.com.felnanuke.mymusicapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.felnanuke.mymusicapp.ui.theme.MyMusicAppTheme
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.felnanuke.mymusicapp.components.PlayerCollapsed
import br.com.felnanuke.mymusicapp.components.TrackListTile
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import br.com.felnanuke.mymusicapp.view_models.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    private lateinit var homeViewModel: HomeViewModel
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            homeViewModel.onStart()
        } else {

        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMusicAppTheme {
                homeViewModel = hiltViewModel()
                requestPermissions()
                setupServicePlayerService()
                setupListeners()
                registerObserver()
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    Body(homeViewModel)
                }
            }
        }
    }


private fun registerObserver(){
    PlayerService.currentTrack.observe(this){
        homeViewModel.currentTrack = it
    }
}

    private fun setupListeners() {
        homeViewModel.activityEvents.observe(this) {
            when (it) {
                HomeViewModel.OPEN_PLAYER_ACTIVITY_ACTION -> {
                    val intent = Intent(this, MusicPlayerActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }


    private fun requestPermissions() {
        shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)

        homeViewModel.onStart()

    }

    private fun setupServicePlayerService() {
        val intent = Intent(this.application, PlayerService::class.java)
        startService(intent)
        bindService(intent, homeViewModel.serviceConnection, 0)
    }

}


@Composable
fun Body(homeViewModel: HomeViewModel) {
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
                    getIsPlaying = { homeViewModel.getIsPlaying() },
                    getProgress = { homeViewModel.getPlayerProgress() },
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