package com.hedaro.musicplayer.ui.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.playback.PlaybackConnection
import com.hedaro.musicplayer.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the album detail screen: the album's tracks and its title. */
@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val albumId: Long =
        savedStateHandle.get<String>(Screen.AlbumDetail.ARG_ALBUM_ID)?.toLongOrNull() ?: 0L

    val tracks: StateFlow<List<Track>> =
        musicRepository.observeAlbumTracks(albumId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val title: StateFlow<String> =
        tracks.map { it.firstOrNull()?.album.orEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun play(index: Int) = playbackConnection.playTracks(tracks.value, index)

    fun shufflePlay() = playbackConnection.shufflePlay(tracks.value)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepository.setFavorite(track.id, !track.isFavorite) }
    }
}
