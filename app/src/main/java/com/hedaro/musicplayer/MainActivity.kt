package com.hedaro.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedaro.musicplayer.ads.AdProvider
import com.hedaro.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host. Compose renders the entire UI; navigation between the
 * Library / Now Playing / Playlists destinations is wired in the UI layer step.
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
                AppRoot(adProvider = adProvider)
            }
        }
    }
}

/**
 * Temporary placeholder root. Replaced by the navigation graph + screens in the
 * UI layer step (see ROADMAP step 6).
 */
@Composable
private fun AppRoot(adProvider: AdProvider) {
    Scaffold(
        // The app's single ad insertion point. No-op today: renders nothing, reserves no space.
        bottomBar = { adProvider.BannerSlot(Modifier.fillMaxWidth()) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Music Player — scaffold ready")
        }
    }
}
