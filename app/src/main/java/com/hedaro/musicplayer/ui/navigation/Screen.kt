package com.hedaro.musicplayer.ui.navigation

/** Navigation destinations. More routes (Now Playing, Playlists) are added in later sub-steps. */
sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object NowPlaying : Screen("now_playing")
    data object Playlists : Screen("playlists")
}
