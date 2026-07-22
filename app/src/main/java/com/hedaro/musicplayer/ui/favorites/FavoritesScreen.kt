package com.hedaro.musicplayer.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.hedaro.musicplayer.ui.components.AddToPlaylistDialog
import com.hedaro.musicplayer.ui.components.TrackRow
import com.hedaro.musicplayer.ui.components.TrackRowMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_favorites)) },
                actions = {
                    if (tracks.isNotEmpty()) {
                        IconButton(onClick = viewModel::shufflePlay) {
                            Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.cd_shuffle))
                        }
                    }
                },
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
                Text(stringResource(R.string.empty_favorites))
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
