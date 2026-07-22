package com.hedaro.musicplayer.ui.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.data.repository.PlaylistRepository
import com.hedaro.musicplayer.playback.PlaybackConnection
import com.hedaro.musicplayer.ui.navigation.Screen
import com.hedaro.musicplayer.util.matchesQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs a single playlist's detail screen (tracks, ordering, and playlist edits). */
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val musicRepository: MusicRepository,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val playlistId: Long =
        savedStateHandle.get<String>(Screen.PlaylistDetail.ARG_PLAYLIST_ID)?.toLongOrNull() ?: 0L

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val tracks: StateFlow<List<Track>> =
        combine(playlistRepository.observePlaylistTracks(playlistId), _query) { list, query ->
            if (query.isBlank()) list else list.filter { it.matchesQuery(query) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    val name: StateFlow<String> =
        playlistRepository.observePlaylists()
            .map { list -> list.firstOrNull { it.id == playlistId }?.name.orEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun play(index: Int) = playbackConnection.playTracks(tracks.value, index)

    fun shufflePlay() = playbackConnection.shufflePlay(tracks.value)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepository.setFavorite(track.id, !track.isFavorite) }
    }

    fun removeTrack(track: Track) {
        viewModelScope.launch { playlistRepository.removeTrack(playlistId, track.id) }
    }

    /** Persist a new track order (from drag-to-reorder). */
    fun reorder(orderedTrackIds: List<Long>) {
        viewModelScope.launch { playlistRepository.reorder(playlistId, orderedTrackIds) }
    }

    fun rename(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { playlistRepository.renamePlaylist(playlistId, newName) }
    }

    fun delete() {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
    }
}
