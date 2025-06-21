package br.com.felnanuke.mymusicapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel()
            musicPlayerViewModel.navController = rememberNavController()
            
            // Handle intent and start observers after ViewModel is initialized
            handleIntent(musicPlayerViewModel)
            startObservers(musicPlayerViewModel)
            
            MyMusicAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = musicPlayerViewModel.navController as NavHostController,
                        startDestination = "player"
                    ) {
                        composable("player") {
                            PlayerScreen(
                                musicPlayerViewModel,
                                this@MusicPlayerActivity.onBackPressedDispatcher::onBackPressed
                            )
                        }
                        composable("playlist") {
                            PlaylistScreen(viewModel = musicPlayerViewModel)
                        }

                    }
                }
            }
        }
    }



    private fun openHomeActivity() {
        val intent = Intent(this@MusicPlayerActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }


    private fun handleIntent(musicPlayerViewModel: MusicPlayerViewModel) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                if (intent.type?.startsWith("audio/") == true) {
                    if (intent.data != null) {
                        musicPlayerViewModel.startPlayer(intent.data!!)
                        return@handleIntent
                    }

                }
                openHomeActivity()

            }
        }

    }

    private fun startObservers(musicPlayerViewModel: MusicPlayerViewModel) {
        musicPlayerViewModel.playerManager.trackProgress.observe(this) { progress ->
        }
        musicPlayerViewModel.playerManager.isPlaying.observe(this) { isPlaying ->
           musicPlayerViewModel.playing = isPlaying ?: false
        }
        musicPlayerViewModel.playerManager.currentTrack.observe(this) { track ->
            musicPlayerViewModel.currentTrack = track
            musicPlayerViewModel.trackIndex = musicPlayerViewModel.trackIndex(track)
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
        musicPlayerViewModel.playerManager.queue.observe(this) { queue ->
            musicPlayerViewModel.queueTracks = queue
            musicPlayerViewModel.queue = queue
        }


    }
}

