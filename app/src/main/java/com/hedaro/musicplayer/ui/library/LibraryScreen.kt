package com.hedaro.musicplayer.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.data.model.Track
import com.hedaro.musicplayer.data.model.TrackSort
import com.hedaro.musicplayer.ui.components.AddToPlaylistDialog
import com.hedaro.musicplayer.ui.components.TrackRow
import com.hedaro.musicplayer.ui.components.TrackRowMenuItem

@Composable
fun LibraryScreen(
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
                query = query,
                onQueryChange = viewModel::setQuery,
                onOpenSearch = { searchActive = true },
                onCloseSearch = {
                    searchActive = false
                    viewModel.setQuery("")
                },
                currentSort = sort,
                onSortSelected = viewModel::setSort,
                onShufflePlay = viewModel::shufflePlay,
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
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    currentSort: TrackSort,
    onSortSelected: (TrackSort) -> Unit,
    onShufflePlay: () -> Unit,
) {
    if (searchActive) {
        SearchTopBar(query = query, onQueryChange = onQueryChange, onClose = onCloseSearch)
    } else {
        TopAppBar(
            title = { Text(stringResource(R.string.nav_library)) },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search))
                }
                IconButton(onClick = onShufflePlay) {
                    Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.cd_shuffle))
                }

                var menuOpen by remember { mutableStateOf(false) }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.cd_sort))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    TrackSort.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(sortLabel(option)) },
                            onClick = {
                                onSortSelected(option)
                                menuOpen = false
                            },
                            trailingIcon = {
                                if (option == currentSort) Icon(Icons.Filled.Check, contentDescription = null)
                            },
                        )
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                )
            }
        },
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_search))
                }
            }
        },
    )
}

@Composable
private fun sortLabel(sort: TrackSort): String = stringResource(
    when (sort) {
        TrackSort.TITLE -> R.string.sort_title
        TrackSort.ARTIST -> R.string.sort_artist
        TrackSort.RECENTLY_ADDED -> R.string.sort_recently_added
        TrackSort.MOST_PLAYED -> R.string.sort_most_played
    },
)
