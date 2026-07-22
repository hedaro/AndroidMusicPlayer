package com.hedaro.musicplayer.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The ad-free implementation used in this build: renders nothing and reserves no space,
 * so the app is genuinely ad-free. No ad SDK is referenced anywhere.
 */
@Singleton
class NoOpAdProvider @Inject constructor() : AdProvider {
    @Composable
    override fun BannerSlot(modifier: Modifier) {
        // Intentionally empty — no ads in this build.
    }
}
