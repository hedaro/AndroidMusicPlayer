package com.hedaro.musicplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.model.TrackSort
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.playback.PlaybackConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Library screen: exposes the (sorted) track list and forwards playback / favorite
 * actions to the lower layers. The list re-queries automatically when the sort changes and when
 * the underlying library or stats change (both are Flows).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val _sort = MutableStateFlow(TrackSort.TITLE)
    val sort: StateFlow<TrackSort> = _sort.asStateFlow()

    val tracks: StateFlow<List<Track>> =
        _sort
            .flatMapLatest { musicRepository.observeTracks(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSort(newSort: TrackSort) {
        _sort.value = newSort
    }

    /** Play the whole library starting at [index] (in the current sort order). */
    fun play(index: Int) = playbackConnection.playTracks(tracks.value, index)

    /** Shuffle-play the whole library. */
    fun shufflePlay() = playbackConnection.shufflePlay(tracks.value)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepository.setFavorite(track.id, !track.isFavorite) }
    }
}
