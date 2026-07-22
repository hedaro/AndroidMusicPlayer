package com.hedaro.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hedaro.musicplayer.ads.AdProvider
import com.hedaro.musicplayer.ui.components.MiniPlayer
import com.hedaro.musicplayer.ui.navigation.MusicNavHost
import com.hedaro.musicplayer.ui.navigation.Screen
import com.hedaro.musicplayer.ui.nowplaying.NowPlayingViewModel
import com.hedaro.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host. Renders the nav graph, a persistent MiniPlayer, and the (invisible today)
 * ad banner in the scaffold's bottom area.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Ad-free NoOpAdProvider today (see the `ads` package); injected by Hilt. */
    @Inject lateinit var adProvider: AdProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicPlayerTheme {
                MusicApp(adProvider = adProvider)
            }
        }
    }
}

@Composable
private fun MusicApp(adProvider: AdProvider) {
    val navController = rememberNavController()
    val playerViewModel: NowPlayingViewModel = hiltViewModel()
    val playback by playerViewModel.playbackState.collectAsStateWithLifecycle()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showMiniPlayer = playback.currentTrackId != null && currentRoute != Screen.NowPlaying.route

    Scaffold(
        bottomBar = {
            Column {
                if (showMiniPlayer) {
                    MiniPlayer(
                        state = playback,
                        onPlayPause = playerViewModel::playPause,
                        onClick = {
                            navController.navigate(Screen.NowPlaying.route) { launchSingleTop = true }
                        },
                    )
                }
                // The app's single ad insertion point. No-op today: renders nothing, reserves no space.
                adProvider.BannerSlot(Modifier.fillMaxWidth())
            }
        },
    ) { innerPadding ->
        MusicNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
