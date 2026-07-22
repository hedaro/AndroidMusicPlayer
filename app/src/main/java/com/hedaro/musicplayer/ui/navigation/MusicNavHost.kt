package com.hedaro.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hedaro.musicplayer.ui.components.AudioPermissionGate
import com.hedaro.musicplayer.ui.library.LibraryScreen

/**
 * App navigation graph. Currently hosts the Library destination (gated behind audio permission);
 * Now Playing and Playlists routes are added in Steps 6b / 6c.
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
    }
}
