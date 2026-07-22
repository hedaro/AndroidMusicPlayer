package com.hedaro.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hedaro.musicplayer.ui.components.AudioPermissionGate
import com.hedaro.musicplayer.ui.favorites.FavoritesScreen
import com.hedaro.musicplayer.ui.library.LibraryScreen
import com.hedaro.musicplayer.ui.nowplaying.NowPlayingScreen
import com.hedaro.musicplayer.ui.playlists.PlaylistDetailScreen
import com.hedaro.musicplayer.ui.playlists.PlaylistsScreen
import com.hedaro.musicplayer.ui.settings.SettingsScreen

/** App navigation graph: Library / Favorites / Playlists tabs, playlist detail, and Now Playing. */
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
                LibraryScreen(
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                )
            }
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen()
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onOpenPlaylist = { id -> navController.navigate(Screen.PlaylistDetail.createRoute(id)) },
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument(Screen.PlaylistDetail.ARG_PLAYLIST_ID) { type = NavType.StringType },
            ),
        ) {
            PlaylistDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
