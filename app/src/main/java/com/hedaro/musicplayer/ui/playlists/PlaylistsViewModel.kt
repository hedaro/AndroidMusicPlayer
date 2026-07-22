package com.hedaro.musicplayer.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.model.Playlist
import com.hedaro.musicplayer.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the Playlists list screen. */
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> =
        playlistRepository.observePlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { playlistRepository.createPlaylist(name) }
    }
}
