package com.hedaro.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hedaro.musicplayer.ads.AdProvider
import com.hedaro.musicplayer.data.preferences.ThemeMode
import com.hedaro.musicplayer.ui.components.MiniPlayer
import com.hedaro.musicplayer.ui.navigation.MusicNavHost
import com.hedaro.musicplayer.ui.navigation.Screen
import com.hedaro.musicplayer.ui.navigation.TopLevelDestination
import com.hedaro.musicplayer.ui.nowplaying.NowPlayingViewModel
import com.hedaro.musicplayer.ui.settings.SettingsViewModel
import com.hedaro.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host. Renders the nav graph, a bottom navigation bar (on top-level tabs),
 * a persistent MiniPlayer, and the (invisible today) ad banner.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Ad-free NoOpAdProvider today (see the `ads` package); injected by Hilt. */
    @Inject lateinit var adProvider: AdProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            MusicPlayerTheme(darkTheme = darkTheme) {
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
    val isTopLevel = TopLevelDestination.entries.any { it.screen.route == currentRoute }
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
                if (isTopLevel) {
                    NavigationBar {
                        TopLevelDestination.entries.forEach { destination ->
                            val selected = currentRoute == destination.screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(destination.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
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
