package com.hedaro.musicplayer.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.playback.PlaybackConnection
import com.hedaro.musicplayer.playback.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Per-track extras that aren't part of [PlaybackState] (they live in Room). */
data class NowPlayingExtras(
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
)

/**
 * Backs the Now Playing screen and the MiniPlayer. Exposes the shared [PlaybackState] and the
 * current track's favorite/play-count, and forwards transport actions to [PlaybackConnection].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackConnection: PlaybackConnection,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackConnection.playbackState

    /** Favorite + play count for whatever track is currently playing. */
    val extras: StateFlow<NowPlayingExtras> =
        playbackState
            .map { it.currentTrackId }
            .distinctUntilChanged()
            .flatMapLatest { id ->
                if (id == null) flowOf(null) else musicRepository.observeStats(id)
            }
            .map { NowPlayingExtras(isFavorite = it?.isFavorite ?: false, playCount = it?.playCount ?: 0) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NowPlayingExtras())

    fun playPause() = playbackConnection.playPause()
    fun next() = playbackConnection.next()
    fun previous() = playbackConnection.previous()
    fun seekTo(positionMs: Long) = playbackConnection.seekTo(positionMs)
    fun stepBy(deltaMs: Long) = playbackConnection.stepBy(deltaMs)
    fun toggleShuffle() = playbackConnection.toggleShuffle()
    fun cycleRepeat() = playbackConnection.cycleRepeatMode()

    fun toggleFavorite() {
        val trackId = playbackState.value.currentTrackId ?: return
        val makeFavorite = !extras.value.isFavorite
        viewModelScope.launch { musicRepository.setFavorite(trackId, makeFavorite) }
    }
}
