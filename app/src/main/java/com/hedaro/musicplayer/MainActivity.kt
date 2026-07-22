package com.hedaro.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hedaro.musicplayer.ads.AdProvider
import com.hedaro.musicplayer.ui.navigation.MusicNavHost
import com.hedaro.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host. Compose renders the whole UI via [MusicNavHost]; the (invisible today)
 * ad banner sits at the scaffold bottom.
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
    Scaffold(
        // The app's single ad insertion point. No-op today: renders nothing, reserves no space.
        bottomBar = { adProvider.BannerSlot(Modifier.fillMaxWidth()) },
    ) { innerPadding ->
        MusicNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
