package com.hedaro.musicplayer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedaro.musicplayer.R

/** Navigation destinations. */
sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object Favorites : Screen("favorites")
    data object Playlists : Screen("playlists")
    data object NowPlaying : Screen("now_playing")

    data object PlaylistDetail : Screen("playlist/{playlistId}") {
        const val ARG_PLAYLIST_ID = "playlistId"
        fun createRoute(playlistId: Long): String = "playlist/$playlistId"
    }
}

/** Top-level tabs shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val screen: Screen,
    val icon: ImageVector,
    val labelRes: Int,
) {
    LIBRARY(Screen.Library, Icons.Filled.LibraryMusic, R.string.nav_library),
    FAVORITES(Screen.Favorites, Icons.Filled.Favorite, R.string.nav_favorites),
    PLAYLISTS(Screen.Playlists, Icons.AutoMirrored.Filled.QueueMusic, R.string.nav_playlists),
}
