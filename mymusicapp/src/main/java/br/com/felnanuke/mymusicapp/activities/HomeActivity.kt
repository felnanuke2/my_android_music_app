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
                setupListeners()
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    HomeScreen(homeViewModel)
                }
            }
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


}

