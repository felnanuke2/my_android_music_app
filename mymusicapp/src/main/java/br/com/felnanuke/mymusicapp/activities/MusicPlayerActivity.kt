package br.com.felnanuke.mymusicapp.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.felnanuke.mymusicapp.screens.HomeScreen
import br.com.felnanuke.mymusicapp.screens.PlayerScreen
import br.com.felnanuke.mymusicapp.screens.PlaylistScreen
import br.com.felnanuke.mymusicapp.ui.theme.MyMusicAppTheme
import br.com.felnanuke.mymusicapp.view_models.MusicPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicPlayerActivity : ComponentActivity() {
    private lateinit var musicPlayerViewModel: MusicPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            musicPlayerViewModel = hiltViewModel()
            musicPlayerViewModel.navController = rememberNavController()
            startObservers()
            MyMusicAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = musicPlayerViewModel.navController as NavHostController,
                        startDestination = "player"
                    ) {
                        composable("player") {
                            PlayerScreen(musicPlayerViewModel)
                        }
                        composable("playlist") {
                            PlaylistScreen(viewModel = musicPlayerViewModel)
                        }

                    }
                }
            }
        }
    }

    private fun startObservers() {
        musicPlayerViewModel.playerManager.trackProgress.observe(this) { progress ->
            musicPlayerViewModel.trackProgress = progress ?: 0f
        }
        musicPlayerViewModel.playerManager.isPlaying.observe(this) { isPlaying ->
            musicPlayerViewModel.playing = isPlaying ?: false
        }
        musicPlayerViewModel.playerManager.currentTrack.observe(this) { track ->
            musicPlayerViewModel.currentTrack = track
        }
        musicPlayerViewModel.playerManager.canPlayNext.observe(this) { canPlayNext ->
            musicPlayerViewModel.canPlayNext = canPlayNext ?: false
        }
        musicPlayerViewModel.playerManager.canPlayPrevious.observe(this) { canPlayPrevious ->
            musicPlayerViewModel.canPlayPrevious = canPlayPrevious ?: false
        }
        musicPlayerViewModel.playerManager.amplitudes.observe(this) { amplitudes ->
            musicPlayerViewModel.amplitudes = amplitudes
        }

        musicPlayerViewModel.playerManager.positionMillis.observe(this) { positionMillis ->
            musicPlayerViewModel.trackPositionMillis = positionMillis
        }

        musicPlayerViewModel.playerManager.durationMillis.observe(this) { durationMillis ->
            musicPlayerViewModel.trackDuration = durationMillis
        }

    }
}

