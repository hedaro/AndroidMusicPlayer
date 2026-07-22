package com.hedaro.musicplayer.ui.browse

import android.net.Uri
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the folder detail screen: the tracks in one device folder. */
@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val folderPath: String =
        Uri.decode(savedStateHandle.get<String>(Screen.FolderDetail.ARG_FOLDER_PATH).orEmpty())

    /** Display name = last path segment. */
    val folderName: String = folderPath.substringAfterLast('/').ifBlank { folderPath }

    val tracks: StateFlow<List<Track>> =
        musicRepository.observeFolderTracks(folderPath)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun play(index: Int) = playbackConnection.playTracks(tracks.value, index)

    fun shufflePlay() = playbackConnection.shufflePlay(tracks.value)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepository.setFavorite(track.id, !track.isFavorite) }
    }
}
