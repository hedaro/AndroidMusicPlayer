package com.hedaro.musicplayer.ui.browse

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.model.Playlist
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.data.repository.PlaylistRepository
import com.hedaro.musicplayer.playback.PlaybackConnection
import com.hedaro.musicplayer.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the folder detail screen: the tracks in one device folder. */
@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val folderPath: String =
        Uri.decode(savedStateHandle.get<String>(Screen.FolderDetail.ARG_FOLDER_PATH).orEmpty())

    /** Display name = last path segment. */
    val folderName: String = folderPath.substringAfterLast('/').ifBlank { folderPath }

    val tracks: StateFlow<List<Track>> =
        musicRepository.observeFolderTracks(folderPath)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Playlists offered in the "add to playlist" dialog. */
    val playlists: StateFlow<List<Playlist>> =
        playlistRepository.observePlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun play(index: Int) = playbackConnection.playFrom(sourceKey(), tracks.value, index)

    fun shufflePlay() = playbackConnection.shufflePlay(sourceKey(), tracks.value)

    private fun sourceKey(): String = "folder=$folderPath"

    fun playNext(track: Track) = playbackConnection.playNext(track)

    fun addToQueue(track: Track) = playbackConnection.addToQueue(track)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepository.setFavorite(track.id, !track.isFavorite) }
    }

    fun addToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch { playlistRepository.addTrack(playlistId, trackId) }
    }

    fun createPlaylistWithTrack(name: String, trackId: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            playlistRepository.addTrack(playlistId, trackId)
        }
    }
}
