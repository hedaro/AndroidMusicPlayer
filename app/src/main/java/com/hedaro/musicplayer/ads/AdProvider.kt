package com.hedaro.musicplayer.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Seam for (future, optional) advertising. The app is ad-free today: the only implementation is
 * [NoOpAdProvider], which renders nothing.
 *
 * If banner ads are ever enabled for a Play Store release, add an `AdMobAdProvider` implementing
 * this interface and switch the Hilt binding in `AdModule` — no screen code changes, because the UI
 * only ever calls [BannerSlot]. By contract this is a **bottom banner only**: no interstitials,
 * no full-screen or rewarded ads.
 */
interface AdProvider {
    /** A single, unintrusive banner slot rendered at one place in the app scaffold. */
    @Composable
    fun BannerSlot(modifier: Modifier)
}
