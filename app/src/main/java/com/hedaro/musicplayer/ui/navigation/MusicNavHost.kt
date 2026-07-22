package com.hedaro.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hedaro.musicplayer.ui.components.AudioPermissionGate
import com.hedaro.musicplayer.ui.library.LibraryScreen
import com.hedaro.musicplayer.ui.nowplaying.NowPlayingScreen

/**
 * App navigation graph: Library (permission-gated) and the full Now Playing screen.
 * Playlists routes are added in Step 6c.
 */
@Composable
fun MusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Library.route,
        modifier = modifier,
    ) {
        composable(Screen.Library.route) {
            AudioPermissionGate {
                LibraryScreen()
            }
        }
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(onBack = { navController.popBackStack() })
        }
    }
}
