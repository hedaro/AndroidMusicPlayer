package com.hedaro.musicplayer.ui.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedaro.musicplayer.R
import com.hedaro.musicplayer.ui.components.PlaylistNameDialog
import com.hedaro.musicplayer.ui.components.SearchField
import com.hedaro.musicplayer.ui.components.TrackRow
import com.hedaro.musicplayer.ui.components.TrackRowMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchActive = !searchActive }) {
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
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_more))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_rename)) },
                                onClick = { showRename = true; menuOpen = false },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete)) },
                                onClick = { showDelete = true; menuOpen = false },
                            )
                        }
                    },
                )
                if (searchActive) {
                    SearchField(
                        query = query,
                        onQueryChange = viewModel::setQuery,
                        placeholder = stringResource(R.string.search_hint_playlist),
                    )
                }
            }
        },
    ) { innerPadding ->
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                val message = if (query.isBlank()) R.string.empty_playlist_detail else R.string.empty_search_results
                Text(stringResource(message))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = { viewModel.play(0) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text(stringResource(R.string.action_play))
                    }
                    OutlinedButton(onClick = viewModel::shufflePlay, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Shuffle, contentDescription = null)
                        Text(stringResource(R.string.action_shuffle))
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                        val menuItems = buildList {
                            // Reordering is only meaningful on the full list, so hide it while searching.
                            if (query.isBlank()) {
                                if (index > 0) {
                                    add(TrackRowMenuItem(stringResource(R.string.cd_move_up)) { viewModel.move(index, index - 1) })
                                }
                                if (index < tracks.lastIndex) {
                                    add(TrackRowMenuItem(stringResource(R.string.cd_move_down)) { viewModel.move(index, index + 1) })
                                }
                            }
                            add(TrackRowMenuItem(stringResource(R.string.action_remove_from_playlist)) { viewModel.removeTrack(track) })
                        }
                        TrackRow(
                            track = track,
                            onClick = { viewModel.play(index) },
                            onToggleFavorite = { viewModel.toggleFavorite(track) },
                            menuItems = menuItems,
                        )
                    }
                }
            }
        }
    }

    if (showRename) {
        PlaylistNameDialog(
            title = stringResource(R.string.action_rename),
            confirmLabel = stringResource(R.string.action_rename),
            initialName = name,
            onConfirm = { newName ->
                viewModel.rename(newName)
                showRename = false
            },
            onDismiss = { showRename = false },
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.dialog_delete_playlist_title)) },
            text = { Text(stringResource(R.string.dialog_delete_playlist_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    viewModel.delete()
                    onBack()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
