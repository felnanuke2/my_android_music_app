package br.com.felnanuke.mymusicapp.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import br.com.felnanuke.mymusicapp.ui.theme.MyMusicAppTheme
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.felnanuke.mymusicapp.screens.HomeScreen
import br.com.felnanuke.mymusicapp.view_models.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    private lateinit var homeViewModel: HomeViewModel
    private var permissionsGranted = 0
    private val totalPermissions = 3

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionsGranted++
        }
        checkAllPermissionsGranted()
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.count { it }
        checkAllPermissionsGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMusicAppTheme {
                homeViewModel = hiltViewModel()
                requestPermissions()
                setupListeners()
                setupObservers()
                // Check permissions status after ViewModel is initialized
                checkAllPermissionsGranted()
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    HomeScreen(homeViewModel)
                }
            }
        }
    }

    private fun setupObservers() {
        homeViewModel.playerManager.currentTrack.observe(this) { track ->
            homeViewModel.currentTrack = track
        }
        homeViewModel.playerManager.isPlaying.observe(this) { isPlaying ->
            homeViewModel.isPlaying = isPlaying
        }
        homeViewModel.playerManager.trackProgress.observe(this) { progress ->
            homeViewModel.trackProgress = progress
        }
        homeViewModel.playerManager.queue.observe(this) { queue ->
            homeViewModel.queue = queue.toMutableList()
        }
    }

    private fun setupListeners() {
        homeViewModel.activityEvents.observe(this) {
            when (it) {
                HomeViewModel.OPEN_PLAYER_ACTIVITY_ACTION -> {
                    val intent = Intent(this, MusicPlayerActivity::class.java)
                    startActivity(intent)
                }
                HomeViewModel.REQUEST_PERMISSIONS_ACTION -> {
                    requestPermissions()
                }
            }
        }
    }

    private fun checkAllPermissionsGranted() {
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        permissionsGranted = requiredPermissions.count { permission ->
            checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        //at least one permission is required
        val hasRequiredPermissions = permissionsGranted >= 1
        homeViewModel.updatePermissionStatus(hasRequiredPermissions)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        requestMultiplePermissionsLauncher.launch(permissions)
    }


}

