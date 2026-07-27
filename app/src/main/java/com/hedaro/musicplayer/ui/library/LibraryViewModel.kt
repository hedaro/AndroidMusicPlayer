package com.hedaro.musicplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.model.Album
import com.hedaro.musicplayer.data.model.Folder
import com.hedaro.musicplayer.data.model.Playlist
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.model.TrackSort
import com.hedaro.musicplayer.data.repository.MusicRepository
import com.hedaro.musicplayer.data.repository.PlaylistRepository
import com.hedaro.musicplayer.playback.PlaybackConnection
import com.hedaro.musicplayer.util.matchesQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Browse mode within the Library screen. */
enum class LibraryTab { SONGS, ALBUMS, FOLDERS }

/**
 * Backs the Library screen: exposes the (sorted) track list, album/folder groupings, and the
 * selected browse tab, forwarding playback / favorite actions to the lower layers. Everything is
 * Flow-based, so lists re-emit automatically when the library, stats, or sort change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _sort = MutableStateFlow(TrackSort.TITLE)
    val sort: StateFlow<TrackSort> = _sort.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Sorted library, filtered by the current search query (title/artist/album, case-insensitive). */
    val tracks: StateFlow<List<Track>> =
        combine(
            _sort.flatMapLatest { musicRepository.observeTracks(it) },
            _query,
        ) { sorted, query ->
            if (query.isBlank()) sorted else sorted.filter { it.matchesQuery(query) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Playlists offered in the "add to playlist" dialog. */
    val playlists: StateFlow<List<Playlist>> =
        playlistRepository.observePlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<Album>> =
        musicRepository.observeAlbums()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val folders: StateFlow<List<Folder>> =
        musicRepository.observeFolders()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun setSort(newSort: TrackSort) {
        _sort.value = newSort
    }

    fun setQuery(newQuery: String) {
        _query.value = newQuery
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

    /** Play the currently-visible songs starting at [index] (in the current sort/filter). */
    fun play(index: Int) = playbackConnection.playFrom(sourceKey(), tracks.value, index)

    /** Shuffle-play the currently-visible songs. */
    fun shufflePlay() = playbackConnection.shufflePlay(sourceKey(), tracks.value)

    // Sort and filter are part of the identity: "all songs" and "filtered songs" are distinct
    // sources, so switching filter/sort rebuilds the queue rather than jumping within the old one.
    private fun sourceKey(): String = "library|sort=${_sort.value}|q=${_query.value}"

    fun playNext(track: Track) = playbackConnection.playNext(track)

    fun addToQueue(track: Track) = playbackConnection.addToQueue(track)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepository.setFavorite(track.id, !track.isFavorite) }
    }
}
