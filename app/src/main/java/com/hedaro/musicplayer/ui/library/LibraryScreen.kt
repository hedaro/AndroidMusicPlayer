package com.hedaro.musicplayer.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.model.TrackSort
import com.hedaro.musicplayer.ui.components.AddToPlaylistDialog
import com.hedaro.musicplayer.ui.components.SearchField
import com.hedaro.musicplayer.ui.components.TrackRow
import com.hedaro.musicplayer.ui.components.TrackRowMenuItem
import com.hedaro.musicplayer.ui.components.TrackSortMenu

@Composable
fun LibraryScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            LibraryTopBar(
                searchActive = searchActive,
                onToggleSearch = { searchActive = !searchActive }, // toggling off keeps the query
                query = query,
                onQueryChange = viewModel::setQuery,
                currentSort = sort,
                onSortSelected = viewModel::setSort,
                onShufflePlay = viewModel::shufflePlay,
                onOpenSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                val message = if (query.isBlank()) R.string.empty_library else R.string.empty_search_results
                Text(stringResource(message))
            }
        } else {
            val addLabel = stringResource(R.string.action_add_to_playlist)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                    TrackRow(
                        track = track,
                        onClick = { viewModel.play(index) },
                        onToggleFavorite = { viewModel.toggleFavorite(track) },
                        menuItems = listOf(
                            TrackRowMenuItem(addLabel) { trackForPlaylist = track },
                        ),
                    )
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(
    searchActive: Boolean,
    onToggleSearch: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    currentSort: TrackSort,
    onSortSelected: (TrackSort) -> Unit,
    onShufflePlay: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                // Search toggle. Tinted when a filter is active but the field is collapsed.
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
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.cd_settings))
                }
            },
        )

        if (searchActive) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(R.string.search_hint),
            )
        }
    }
}
