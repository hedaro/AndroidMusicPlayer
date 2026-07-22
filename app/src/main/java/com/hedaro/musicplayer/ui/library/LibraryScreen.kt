package com.hedaro.musicplayer.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Album
import com.hedaro.musicplayer.data.model.Folder
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.model.TrackSort
import com.hedaro.musicplayer.ui.components.AddToPlaylistDialog
import com.hedaro.musicplayer.ui.components.AlbumRow
import com.hedaro.musicplayer.ui.components.FolderRow
import com.hedaro.musicplayer.ui.components.SearchField
import com.hedaro.musicplayer.ui.components.TrackRow
import com.hedaro.musicplayer.ui.components.TrackRowMenuItem
import com.hedaro.musicplayer.ui.components.TrackSortMenu

@Composable
fun LibraryScreen(
    onOpenSettings: () -> Unit,
    onOpenAlbum: (Long) -> Unit = {},
    onOpenFolder: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            LibraryHeader(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    viewModel.setTab(tab)
                    if (tab != LibraryTab.SONGS) searchActive = false // search only applies to Songs
                },
                searchActive = searchActive,
                onToggleSearch = { searchActive = !searchActive },
                query = query,
                onQueryChange = viewModel::setQuery,
                currentSort = sort,
                onSortSelected = viewModel::setSort,
                onShufflePlay = viewModel::shufflePlay,
                onOpenSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        when (selectedTab) {
            LibraryTab.SONGS -> SongsList(
                tracks = tracks,
                query = query,
                innerPadding = innerPadding,
                onPlay = viewModel::play,
                onToggleFavorite = viewModel::toggleFavorite,
                onAddToPlaylist = { trackForPlaylist = it },
            )
            LibraryTab.ALBUMS -> AlbumsList(albums, innerPadding, onOpenAlbum)
            LibraryTab.FOLDERS -> FoldersList(folders, innerPadding, onOpenFolder)
        }
    }

    trackForPlaylist?.let { track ->
        AddToPlaylistDialog(
            playlists = playlists,
            onAddToExisting = { playlistId ->
                viewModel.addToPlaylist(playlistId, track.id)
                trackForPlaylist = null
            },
            onCreateAndAdd = { name ->
                viewModel.createPlaylistWithTrack(name, track.id)
                trackForPlaylist = null
            },
            onDismiss = { trackForPlaylist = null },
        )
    }
}

@Composable
private fun SongsList(
    tracks: List<Track>,
    query: String,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onPlay: (Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
) {
    if (tracks.isEmpty()) {
        EmptyMessage(innerPadding, if (query.isBlank()) R.string.empty_library else R.string.empty_search_results)
        return
    }
    val addLabel = stringResource(R.string.action_add_to_playlist)
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                track = track,
                onClick = { onPlay(index) },
                onToggleFavorite = { onToggleFavorite(track) },
                menuItems = listOf(TrackRowMenuItem(addLabel) { onAddToPlaylist(track) }),
            )
        }
    }
}

@Composable
private fun AlbumsList(
    albums: List<Album>,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onOpenAlbum: (Long) -> Unit,
) {
    if (albums.isEmpty()) {
        EmptyMessage(innerPadding, R.string.empty_library)
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        items(albums, key = { it.id }) { album ->
            AlbumRow(album = album, onClick = { onOpenAlbum(album.id) })
        }
    }
}

@Composable
private fun FoldersList(
    folders: List<Folder>,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onOpenFolder: (String) -> Unit,
) {
    if (folders.isEmpty()) {
        EmptyMessage(innerPadding, R.string.empty_library)
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        items(folders, key = { it.path }) { folder ->
            FolderRow(folder = folder, onClick = { onOpenFolder(folder.path) })
        }
    }
}

@Composable
private fun EmptyMessage(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    messageRes: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(messageRes))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryHeader(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    searchActive: Boolean,
    onToggleSearch: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    currentSort: TrackSort,
    onSortSelected: (TrackSort) -> Unit,
    onShufflePlay: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Search / shuffle / sort act on the Songs list, so only show them on that tab.
    val showSongControls = selectedTab == LibraryTab.SONGS

    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                if (showSongControls) {
                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (searchActive) Icons.Filled.SearchOff else Icons.Filled.Search,
                            contentDescription = stringResource(R.string.cd_search),
                            tint = if (!searchActive && query.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                    IconButton(onClick = onShufflePlay) {
                        Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.cd_shuffle))
                    }
                    TrackSortMenu(currentSort = currentSort, onSortSelected = onSortSelected)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.cd_settings))
                }
            },
        )

        if (showSongControls && searchActive) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(R.string.search_hint),
            )
        }

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(stringResource(tabLabel(tab)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                )
            }
        }
    }
}

private fun tabLabel(tab: LibraryTab): Int = when (tab) {
    LibraryTab.SONGS -> R.string.tab_songs
    LibraryTab.ALBUMS -> R.string.tab_albums
    LibraryTab.FOLDERS -> R.string.tab_folders
}
